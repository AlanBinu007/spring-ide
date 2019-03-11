/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.SuperTypeHierarchyCache;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Helpers for validation rules.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.0
 */
public final class ValidationRuleUtils {

	public static final String FACTORY_BEAN_REFERENCE_PREFIX = "&";

	public static final String FACTORY_BEAN_REFERENCE_REGEXP = "[" + FACTORY_BEAN_REFERENCE_PREFIX + "]";

	public static final String ASPECT_OF_METHOD_NAME = "aspectOf";

	/**
	 * Returns <code>true</code> if the specified text is a reference to a
	 * factory bean, e.g. <code>&factoryBean</code>.
	 */
	public static boolean isFactoryBeanReference(String property) {
		return property.startsWith(FACTORY_BEAN_REFERENCE_PREFIX);
	}

	/**
	 * Returns the given {@link IBean bean}'s bean class name. For child beans
	 * the corresponding parents are resolved within the bean's
	 * {@link IBeansConfig} only.
	 */
	public static String getBeanClassName(IBean bean) {
		return getBeanClassName(bean, BeansModelUtils.getConfig(bean));
	}

	/**
	 * Returns the given {@link IBean bean}'s bean class name. For child beans
	 * the corresponding parents are resolved within the given context (
	 * {@link IBeansConfig} or {@link IBeansConfigSet}).
	 */
	public static String getBeanClassName(IBean bean, IModelElement context) {
		BeanDefinition bd = BeansModelUtils.getMergedBeanDefinition(bean, context);
		if (bd != null) {
			return bd.getBeanClassName();
		}
		return null;
	}

	/**
	 * Returns all registered {@link BeanDefinition} matching the given
	 * <code>beanName</code> and <code>beanClass</code>.
	 */
	public static Set<BeanDefinition> getBeanDefinitions(String beanName, String beanClass, IBeansValidationContext context) {
		Set<BeanDefinition> beanDefinition = new HashSet<BeanDefinition>();
		try {
			beanDefinition.add(context.getCompleteRegistry().getBeanDefinition(beanName));
		} catch (NoSuchBeanDefinitionException e) {
			// this is ok here
		}

		// fall back for manual installation of the post processor
		IType[] allSubtypes = null;
		try {
			IType beanClassType = JdtUtils.getJavaType(context.getRootElementProject(), beanClass);
			ITypeHierarchy hierarchy = SuperTypeHierarchyCache.getTypeHierarchy(beanClassType);
			allSubtypes = hierarchy.getAllSubtypes(beanClassType);
		}
		catch (JavaModelException e) {
			// ignore, falls back to JdtUtils.doesExtend
		}

		for (String name : context.getCompleteRegistry().getBeanDefinitionNames()) {
			try {
				BeanDefinition db = context.getCompleteRegistry().getBeanDefinition(name);
				if (db.getBeanClassName() != null) {
					if (db.getBeanClassName().equals(beanClass)) {
						beanDefinition.add(db);
					}
					else if (allSubtypes != null) {
						for (int i = 0; i < allSubtypes.length; i++) {
							if (allSubtypes[i].getFullyQualifiedName().equals(db.getBeanClassName()) &&
									allSubtypes[i].exists()) {
								beanDefinition.add(db);
							}
						}
					}
					else if (Introspector.doesExtend(JdtUtils.getJavaType(context.getRootElementProject(), db.getBeanClassName()), beanClass)) {
							beanDefinition.add(db);
					}
				}

			} catch (BeanDefinitionStoreException e1) {
				// ignore here
			}
		}
		return beanDefinition;
	}

	/**
	 * Extracts the {@link IType} of a bean definition.
	 * <p>
	 * Honors <code>factory-method</code>s and <code>factory-bean</code>.
	 */
	public static IType extractBeanClass(BeanDefinition bd, IBean bean, String mergedClassName, IBeansValidationContext context) {

		IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean).getProject(), mergedClassName);
		// 1. factory-method on bean
		if (bd.getFactoryMethodName() != null && bd.getFactoryBeanName() == null) {
			type = extractTypeFromFactoryMethod(bd, type);
		}
		// 2. factory-method on factory-bean
		else if (bd.getFactoryMethodName() != null && bd.getFactoryBeanName() != null) {
			try {
				AbstractBeanDefinition factoryBd = (AbstractBeanDefinition) context.getCompleteRegistry().getBeanDefinition(bd.getFactoryBeanName());
				IType factoryBeanType = extractBeanClass(factoryBd, bean, factoryBd.getBeanClassName(), context);
				if (factoryBeanType != null) {
					type = extractTypeFromFactoryMethod(bd, factoryBeanType);
				}
			} catch (NoSuchBeanDefinitionException e) {

			}
		}
		return type;
	}

	/**
	 * Extracts the {@link IType} of a {@link BeanDefinition} by only looking at
	 * the <code>factory-method</code>. The passed in {@link IType} <b>must</b>
	 * be the bean class or the resolved type of the factory bean in use.
	 */
	private static IType extractTypeFromFactoryMethod(BeanDefinition bd, IType type) {
		String factoryMethod = bd.getFactoryMethodName();
		try {
			int argCount = (!bd.isAbstract() ? bd.getConstructorArgumentValues().getArgumentCount() : -1);
			Set<IMethod> methods = Introspector.getAllMethods(type);
			for (IMethod method : methods) {
				if (factoryMethod.equals(method.getElementName()) && method.getParameterNames().length == argCount) {
					type = JdtUtils.getJavaTypeFromSignatureClassName(method.getReturnType(), type);
					break;
				}
			}
		} catch (JavaModelException e) {
		}
		return type;
	}

	public static String getBeanName(IModelElement element) {
		if (element instanceof IBean) {
			return element.getElementName();
		}
		if (element != null) {
			return getBeanName(element.getElementParent());
		}
		return null;
	}

}
