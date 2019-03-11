/*******************************************************************************
 * Copyright (c) 2007, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

/**
 * Base class for valdating a given {@link IBean}'s methods.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Terry Denney
 * @since 2.0
 */
public abstract class AbstractBeanMethodValidationRule extends AbstractBeanValidationRule {

	protected enum MethodType {
		FACTORY, INIT, DESTROY
	}

	protected void validateMethod(IBean bean, IType type, MethodType methodType, String methodName, int argCount,
			Static statics, IBeansValidationContext context) {
		if (methodName != null && !SpringCoreUtils.hasPlaceHolder(methodName)) {
			try {
				IMethod method = Introspector.findMethod(type, methodName, argCount, Public.DONT_CARE, statics, getTypeHierarchyEngine(context));
				// First check if we can find any matching method regardless of
				// visibility, if not create error marker
				if (method == null) {
					String className = type.getFullyQualifiedName();
					if (methodType == MethodType.FACTORY) {
						context.error(bean, "UNDEFINED_FACTORY_BEAN_METHOD", (statics == Static.YES ? "Static"
								: "Non-static")
								+ " factory method '"
								+ methodName
								+ "' "
								+ (argCount != -1 ? "with " + argCount + " arguments " : "")
								+ "not found in factory bean class '" + className + "'",
								new ValidationProblemAttribute("CLASS", className), new ValidationProblemAttribute(
										"METHOD", methodName),
										new ValidationProblemAttribute("BEAN_NAME", bean.getElementName()));
					}
					else if (methodType == MethodType.INIT) {
						context.error(bean, "UNDEFINED_INIT_METHOD", "Init-method '" + methodName
								+ "' not found in bean class '" + className + "'", new ValidationProblemAttribute(
								"CLASS", className), new ValidationProblemAttribute("METHOD", methodName),
								new ValidationProblemAttribute("BEAN_NAME", bean.getElementName()));
					}
					else if (methodType == MethodType.DESTROY) {
						context.error(bean, "UNDEFINED_DESTROY_METHOD", "Destroy-method '" + methodName
								+ "' not found in bean class '" + className + "'", new ValidationProblemAttribute(
								"CLASS", className), new ValidationProblemAttribute("METHOD", methodName),
								new ValidationProblemAttribute("BEAN_NAME", bean.getElementName()));
					}
				}

				// TODO CD check this back in after adding configurable rule
				// properties
				// If we find a matching method, but the visibility is not
				// public, then just create a warning
				/*
				 * else if (!Flags.isPublic(method.getFlags())) { if (methodType == MethodType.FACTORY) {
				 * context.warning(bean, "UNDEFINED_FACTORY_BEAN_METHOD", (statics == Static.NO ? "Non-static" :
				 * "Static") + " factory method '" + methodName + "' " + (argCount != -1 ? "with " + argCount +
				 * " arguments " : "") + "is not public in factory bean class '" + type.getFullyQualifiedName() + "'");
				 * } else if (methodType == MethodType.INIT) { context.warning(bean, "UNDEFINED_INIT_METHOD",
				 * "Init-method '" + methodName + "' is not public in bean class '" + type.getFullyQualifiedName() +
				 * "'"); } else if (methodType == MethodType.DESTROY) { context.warning(bean,
				 * "UNDEFINED_DESTROY_METHOD", "Destroy-method '" + methodName + "' is not public in bean class '" +
				 * type.getFullyQualifiedName() + "'"); } }
				 */
			}
			catch (JavaModelException e) {
				// Suppress this expection here
				// BeansCorePlugin.log(e);
			}
		}
	}
}
