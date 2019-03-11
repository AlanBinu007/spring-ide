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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.core.type.asm.AnnotationMetadataReadingVisitor;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;
import org.springframework.ide.eclipse.core.type.asm.EmptyAnnotationVisitor;
import org.springframework.ide.eclipse.core.type.asm.EmptyMethodVisitor;

/**
 * Validates a given {@link IBean}'s if all {@link Required} annotated properties are configured.
 * 
 * @author Christian Dupuis
 * @author Terry Denney
 * @author Martin Lippert
 * @since 2.0.1
 */
public class RequiredPropertyRule extends AbstractBeanValidationRule {

	private static final String REQUIRED_ANNOTATION_TYPE_PROPERTY_NAME = "requiredAnnotationType";

	@Override
	protected boolean supportsBean(IBean bean, IBeansValidationContext context) {
		return !bean.isAbstract()
				&& context.isBeanRegistered(AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
						RequiredAnnotationBeanPostProcessor.class.getName());
	}

	/**
	 * Validates the given {@link IBean}.
	 * <p>
	 * First checks if the bean is not abstract and if the {@link RequiredAnnotationBeanPostProcessor} is registered in
	 * the application context. If so the bean class is scanned by using an ASM-based {@link ClassVisitor} for any
	 * {@link Required} annotated property setters.
	 */
	@Override
	public void validate(IBean bean, IBeansValidationContext context, IProgressMonitor monitor) {
		BeanDefinition mergedBd = BeansModelUtils.getMergedBeanDefinition(bean, context.getContextElement());
		String mergedClassName = mergedBd.getBeanClassName();
		IType type = ValidationRuleUtils.extractBeanClass(mergedBd, bean, mergedClassName, context);
		if (type != null) {
			validatePropertyNames(type, bean, mergedBd, context);
		}
	}

	/**
	 * Validates {@link PropertyValues} of given {link BeanDefinition} if all required are configured.
	 * @param type the type whose hierarchy to check for {@link Required} annotated properties
	 * @param bean the underlying {@link IBean} instance
	 * @param mergedBd the {@link BeanDefinition} behind the {@link IBean}
	 * @param context context to retrieve a {@link ClassReaderFactory} and report errors
	 */
	private void validatePropertyNames(IType type, IBean bean, BeanDefinition mergedBd, IBeansValidationContext context) {
		try {
			RequiredAnnotationMetadata annotationMetadata = getRequiredAnnotationMetadata(context
					.getClassReaderFactory(), bean, type, getRequiredAnnotationTypes(context));

			List<String> missingProperties = new ArrayList<String>();
			Set<IMethod> properties = Introspector.findAllWritableProperties(type);
			for (IMethod property : properties) {
				String propertyName = java.beans.Introspector.decapitalize(property.getElementName().substring(3));
				if (annotationMetadata.isRequiredProperty(propertyName)
						&& mergedBd.getPropertyValues().getPropertyValue(propertyName) == null) {
					missingProperties.add(propertyName);
				}
			}

			// add the error message
			if (missingProperties.size() > 0) {
				String msg = buildExceptionMessage(missingProperties, bean.getElementName());
				context.error(bean, "REQUIRED_PROPERTY_MISSING", msg, new ValidationProblemAttribute("CLASS", type
						.getFullyQualifiedName()), new ValidationProblemAttribute("BEAN_NAME", bean.getElementName()), 
						new ValidationProblemAttribute("MISSING_PROPERTIES",
						missingProperties));
			}
		}
		catch (JavaModelException e) {
			BeansCorePlugin.log(e);
		}
	}

	/**
	 * Retrieves a instance of {@link RequiredAnnotationMetadata} that contains information about used annotations in
	 * the class under question
	 */
	private RequiredAnnotationMetadata getRequiredAnnotationMetadata(final ClassReaderFactory classReaderFactory,
			final IBean bean, final IType type, Set<String> requiredAnnotationTypes) {
		String className = type.getFullyQualifiedName();
		RequiredAnnotationMetadata visitor = new RequiredAnnotationMetadata(requiredAnnotationTypes);
		try {
			while (className != null && !Object.class.getName().equals(className)) {
				ClassReader classReader = classReaderFactory.getClassReader(className);
				classReader.accept(visitor, 0);
				className = visitor.getSuperClassName();
			}
		}
		catch (IOException e) {
			// ignore any missing files here as this will be
			// reported as missing bean class
		}
		return visitor;
	}

	/**
	 * ASM based visitor that checks the precedence of an {@link Required} annotation on <b>any</b> property setter.
	 */
	private static class RequiredAnnotationMetadata extends AnnotationMetadataReadingVisitor {

		private Set<String> requiredAnnotationTypes = new HashSet<String>();

		private Set<String> requiredPropertyNames = new HashSet<String>();

		public RequiredAnnotationMetadata(Set<String> requiredAnnotationTypes) {
			for (String className : requiredAnnotationTypes) {
				this.requiredAnnotationTypes.add('L' + className.replace('.', '/') + ';');
			}
		}

		@Override
		public MethodVisitor visitMethod(int modifier, final String name, String params, String arg3, String[] arg4) {
			if (name.startsWith("set")) {
				return new EmptyMethodVisitor() {
					@Override
					public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
						if (requiredAnnotationTypes.contains(desc)) {
							requiredPropertyNames.add(java.beans.Introspector.decapitalize(name.substring(3)));
						}
						return new EmptyAnnotationVisitor();
					}
				};
			}
			return new EmptyMethodVisitor();
		}

		public boolean isRequiredProperty(String propertyName) {
			return requiredPropertyNames.contains(propertyName);
		}
	}

	/**
	 * Extracts the configured <code>requiredAnnotationType</code> values from all registered
	 * {@link RequiredAnnotationBeanPostProcessor}.
	 * @since 2.0.2
	 */
	private Set<String> getRequiredAnnotationTypes(IBeansValidationContext context) {
		Set<String> requiredAnnotationTypes = new HashSet<String>();
		Set<BeanDefinition> bds = context.getRegisteredBeanDefinition(
				AnnotationConfigUtils.REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME,
				RequiredAnnotationBeanPostProcessor.class.getName());
		for (BeanDefinition bd : bds) {
			PropertyValue property = bd.getPropertyValues().getPropertyValue(REQUIRED_ANNOTATION_TYPE_PROPERTY_NAME);
			if (property != null && property.getValue() instanceof TypedStringValue) {
				requiredAnnotationTypes.add(((TypedStringValue) property.getValue()).getValue());
			}
			else {
				requiredAnnotationTypes.add(Required.class.getName());
			}
		}
		return requiredAnnotationTypes;
	}

	/**
	 * Build an exception message for the given list of invalid properties.
	 * @param invalidProperties the list of names of invalid properties
	 * @param beanName the name of the bean
	 * @return the exception message
	 */
	private String buildExceptionMessage(List<String> invalidProperties, String beanName) {
		int size = invalidProperties.size();
		StringBuilder sb = new StringBuilder();
		sb.append(size == 1 ? "Property" : "Properties");
		for (int i = 0; i < size; i++) {
			String propertyName = invalidProperties.get(i);
			if (i > 0) {
				if (i == (size - 1)) {
					sb.append(" and");
				}
				else {
					sb.append(",");
				}
			}
			sb.append(" '").append(propertyName).append("'");
		}
		sb.append(size == 1 ? " is" : " are");
		sb.append(" required for bean '").append(beanName).append("'");
		return sb.toString();
	}
}
