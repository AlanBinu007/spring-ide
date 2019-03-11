/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.internal.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * {@link AutowireCandidateResolver} implementation that matches bean definition qualifiers
 * against {@link Qualifier qualifier annotations} on the field or parameter to be autowired.
 * Also supports suggested expression values through a {@link Value value} annotation.
 * <p>Also supports JSR-330's {@link javax.inject.Qualifier} annotation, if available.
 * @author Christian Dupuis
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see AutowireCandidateQualifier
 * @see Qualifier
 * @see Value
 */
public class QualifierAnnotationAutowireCandidateResolver implements AutowireCandidateResolver {

	private final Set<Class<? extends Annotation>> qualifierTypes = new LinkedHashSet<Class<? extends Annotation>>();

	private IInjectionMetadataProviderProblemReporter problemReporter = new PassThroughProblemReporter();

	private Class<? extends Annotation> valueAnnotationType;

	/**
	 * Create a new QualifierAnnotationAutowireCandidateResolver
	 * for Spring's standard {@link Qualifier} annotation.
	 * <p>Also supports JSR-330's {@link javax.inject.Qualifier} annotation, if available.
	 */
	public QualifierAnnotationAutowireCandidateResolver(ClassLoader cl) {
		try {
			this.qualifierTypes.add((Class<? extends Annotation>) cl.loadClass(Qualifier.class.getName()));
			this.qualifierTypes.add((Class<? extends Annotation>) cl.loadClass("javax.inject.Qualifier"));
			this.valueAnnotationType = (Class<? extends Annotation>) cl.loadClass(Value.class.getName());
		}
		catch (ClassNotFoundException ex) {
			// JSR-330 API not available - simply skip.
		}
	}
	
	public void setProblemReporter(IInjectionMetadataProviderProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	/**
	 * Register the given type to be used as a qualifier when autowiring.
	 * <p>This identifies qualifier annotations for direct use (on fields,
	 * method parameters and constructor parameters) as well as meta
	 * annotations that in turn identify actual qualifier annotations.
	 * <p>This implementation only supports annotations as qualifier types.
	 * The default is Spring's {@link Qualifier} annotation which serves
	 * as a qualifier for direct use and also as a meta annotation.
	 * @param qualifierType the annotation type to register
	 */
	public void addQualifierType(Class<? extends Annotation> qualifierType) {
		this.qualifierTypes.add(qualifierType);
	}

	/**
	 * Determine whether the provided bean definition is an autowire candidate.
	 * <p>To be considered a candidate the bean's <em>autowire-candidate</em>
	 * attribute must not have been set to 'false'. Also, if an annotation on
	 * the field or parameter to be autowired is recognized by this bean factory
	 * as a <em>qualifier</em>, the bean must 'match' against the annotation as
	 * well as any attributes it may contain. The bean definition must contain
	 * the same qualifier or match by meta attributes. A "value" attribute will
	 * fallback to match against the bean name or an alias if a qualifier or
	 * attribute does not match.
	 * @see Qualifier
	 */
	public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
		if (!bdHolder.getBeanDefinition().isAutowireCandidate()) {
			// if explicitly false, do not proceed with qualifier check
			return false;
		}
		if (descriptor == null) {
			// no qualification necessary
			return true;
		}
		boolean match = checkQualifiers(bdHolder, descriptor.getAnnotations());
		if (match) {
			MethodParameter methodParam = descriptor.getMethodParameter();
			if (methodParam != null) {
				Method method = methodParam.getMethod();
				if (method == null || void.class.equals(method.getReturnType())) {
					match = checkQualifiers(bdHolder, methodParam.getMethodAnnotations());
				}
			}
		}
		return match;
	}

	/**
	 * Match the given qualifier annotations against the candidate bean definition.
	 */
	protected boolean checkQualifiers(BeanDefinitionHolder bdHolder, Annotation[] annotationsToSearch) {
		if (ObjectUtils.isEmpty(annotationsToSearch)) {
			return true;
		}
		SimpleTypeConverter typeConverter = new SimpleTypeConverter();
		for (Annotation annotation : annotationsToSearch) {
			Class<? extends Annotation> type = annotation.annotationType();
			if (isQualifier(type)) {
				if (!checkQualifier(bdHolder, annotation, typeConverter)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks whether the given annotation type is a recognized qualifier type.
	 */
	protected boolean isQualifier(Class<? extends Annotation> annotationType) {
		for (Class<? extends Annotation> qualifierType : this.qualifierTypes) {
			if (annotationType.equals(qualifierType) || annotationType.isAnnotationPresent(qualifierType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Match the given qualifier annotation against the candidate bean definition.
	 */
	protected boolean checkQualifier(
			BeanDefinitionHolder bdHolder, Annotation annotation, TypeConverter typeConverter) {

		Class<? extends Annotation> type = annotation.annotationType();
		AbstractBeanDefinition bd = (AbstractBeanDefinition) bdHolder.getBeanDefinition();
		AutowireCandidateQualifier qualifier = bd.getQualifier(type.getName());
		if (qualifier == null) {
			qualifier = bd.getQualifier(ClassUtils.getShortName(type));
		}
		if (qualifier == null) {
			Annotation targetAnnotation = null;
//			if (bd.getResolvedFactoryMethod() != null) {
//				targetAnnotation = bd.getResolvedFactoryMethod().getAnnotation(type);
//			}
			if (targetAnnotation == null) {
				// look for matching annotation on the target class
				Class<?> beanType = null;
				if (bd.getBeanClassName() != null) {
					try {
						beanType = org.springframework.ide.eclipse.core.java.ClassUtils.loadClass(bd.getBeanClassName());
					}
					catch (ClassNotFoundException e) {
					}
				}
				if (beanType != null) {
					targetAnnotation = ClassUtils.getUserClass(beanType).getAnnotation(type);
				}
			}
			if (targetAnnotation != null && targetAnnotation.equals(annotation)) {
				return true;
			}
		}
		Map<String, Object> attributes = AnnotationUtils.getAnnotationAttributes(annotation);
		if (attributes.isEmpty() && qualifier == null) {
			// if no attributes, the qualifier must be present
			return false;
		}
		for (Map.Entry<String, Object> entry : attributes.entrySet()) {
			String attributeName = entry.getKey();
			Object expectedValue = entry.getValue();
			Object actualValue = null;
			// check qualifier first
			if (qualifier != null) {
				actualValue = qualifier.getAttribute(attributeName);
			}
			if (actualValue == null) {
				// fall back on bean definition attribute
				actualValue = bd.getAttribute(attributeName);
			}
			if (actualValue == null && attributeName.equals(AutowireCandidateQualifier.VALUE_KEY) &&
					expectedValue instanceof String && bdHolder.matchesName((String) expectedValue)) {
				// fall back on bean name (or alias) match
				continue;
			}
			if (actualValue == null && qualifier != null) {
				// fall back on default, but only if the qualifier is present
				actualValue = AnnotationUtils.getDefaultValue(annotation, attributeName);
			}
			if (actualValue != null) {
				actualValue = typeConverter.convertIfNecessary(actualValue, expectedValue.getClass());
			}
			if (!expectedValue.equals(actualValue)) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Determine whether the given dependency carries a value annotation.
	 * @see Value
	 */
	public Object getSuggestedValue(DependencyDescriptor descriptor) {
		Object value = findValue(descriptor.getAnnotations(), descriptor);
		if (value == null) {
			MethodParameter methodParam = descriptor.getMethodParameter();
			if (methodParam != null) {
				value = findValue(methodParam.getMethodAnnotations(), descriptor);
			}
		}
		return value;
	}

	/**
	 * Determine a suggested value from any of the given candidate annotations.
	 */
	protected Object findValue(Annotation[] annotationsToSearch, DependencyDescriptor descriptor) {
		for (Annotation annotation : annotationsToSearch) {
			if (this.valueAnnotationType.isInstance(annotation)) {
				Object value = AnnotationUtils.getValue(annotation);
				if (value == null) {
					problemReporter.error("Value annotation must have a value attribute", descriptor);
					return null;
				}
				return value;
			}
		}
		return null;
	}

	public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
		// TODO Auto-generated method stub
		return null;
	}

}
