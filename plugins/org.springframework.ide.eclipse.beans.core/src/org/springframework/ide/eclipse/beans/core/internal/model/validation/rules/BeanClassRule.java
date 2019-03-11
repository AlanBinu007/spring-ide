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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.validation.AbstractBeanValidationRule;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

/**
 * Validates a given {@link IBean}'s bean class. Skips child beans and bean class names with placeholders.
 * <p>
 * Note: this implementation also skips class names from the Spring DM framework.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Terry Denney
 * @since 2.0
 */
public class BeanClassRule extends AbstractBeanValidationRule {

	/**
	 * Internal list of full-qualified class names that should be ignored by this validation rule.
	 */
	private List<String> ignorableClasses = new ArrayList<String>();

	public void setIgnorableClasses(String classNames) {
		if (StringUtils.hasText(classNames)) {
			this.ignorableClasses = Arrays.asList(StringUtils.delimitedListToStringArray(classNames, ",", "\r\n\f "));
		}
	}

	@Override
	public void validate(IBean bean, IBeansValidationContext context, IProgressMonitor monitor) {
		BeanDefinition beanDefinition = ((Bean) bean).getBeanDefinition();
		String className = beanDefinition.getBeanClassName();

		// Validate bean class and constructor arguments - skip child beans and
		// class names with placeholders
		if (className != null && !SpringCoreUtils.hasPlaceHolder(className) && !ignorableClasses.contains(className)) {
			IType type = JdtUtils.getJavaType(BeansModelUtils.getProject(bean).getProject(), className);
			try {
				IModelSourceLocation sourceLocation = bean.getElementSourceLocation();
				if (type != null) {
					if (!bean.isAbstract() && !bean.isFactory() && !(sourceLocation instanceof JavaModelSourceLocation)) {
						if (type.isInterface()) {
							context.warning(bean, "CLASS_NOT_CLASS", "Class '" + className + "' is an interface",
									new ValidationProblemAttribute("CLASS", className), 
									new ValidationProblemAttribute("BEAN_NAME", bean.getElementName()));
						} else if (type.isClass() && Flags.isAbstract(type.getFlags())) {
							context.warning(bean, "CLASS_NOT_CONCRETE", "Class '" + className + "' is abstract",
									new ValidationProblemAttribute("CLASS", className), 
									new ValidationProblemAttribute("BEAN_NAME", bean.getElementName()));
						}
					}
				} else {
					context.error(bean, "CLASS_NOT_FOUND", "Class '" + className + "' not found",
							new ValidationProblemAttribute("CLASS", className), 
							new ValidationProblemAttribute("BEAN_NAME", bean.getElementName()));
				}
			}
			catch (JavaModelException e) {
			}
		}
	}
}
