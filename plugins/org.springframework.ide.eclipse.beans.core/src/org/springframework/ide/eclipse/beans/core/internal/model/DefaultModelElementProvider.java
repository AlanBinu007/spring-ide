/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.namespaces.IModelElementProvider;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This class is an {@link IModelElementProvider} which converts a given {@link ComponentDefinition} into a single
 * {@link IBean} or an {@link IBeansComponent} containing {@link IBean}(s) or {@link IBeansComponent}(s).
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class DefaultModelElementProvider implements IModelElementProvider {

	public ISourceModelElement getElement(IBeansConfig config, ComponentDefinition definition) {
		if (definition instanceof CompositeComponentDefinition || definition.getBeanDefinitions().length > 1) {
			return createComponent(config, config, definition);
		}
		return createBean(config, definition);
	}

	private IBeansComponent createComponent(IBeansModelElement parent, IBeansConfig config,
			ComponentDefinition definition) {
		BeansComponent component = null;
		if (definition instanceof ProfileAwareCompositeComponentDefinition) {
			component = new ProfileAwareBeansComponent(parent, definition);
		}
		else {
			component = new BeansComponent(parent, definition);
		}

		// Create beans from wrapped bean definitions
		for (BeanDefinition beanDef : definition.getBeanDefinitions()) {
			if (beanDef instanceof AbstractBeanDefinition) {
				String beanName = UniqueBeanNameGenerator.generateBeanName(beanDef, config);
				IBean bean = new Bean(component, beanName, null, beanDef);
				component.addBean(bean);
			}
		}

		// Create components or beans from nested component definitions
		if (definition instanceof CompositeComponentDefinition) {
			for (ComponentDefinition compDef : ((CompositeComponentDefinition) definition).getNestedComponents()) {
				if (compDef instanceof CompositeComponentDefinition || compDef.getBeanDefinitions().length > 1) {
					component.addComponent(createComponent(component, config, compDef));
				}
				else {
					IBean bean = createBean(component, compDef);
					if (bean != null) {
						component.addBean(bean);
					}
				}
			}
			addImplicitBeans(component, (CompositeComponentDefinition) definition, config);
		}
		return component;
	}


	private void addImplicitBeans(BeansComponent component, CompositeComponentDefinition context, IBeansConfig config) {
		BeanDefinitionRegistry rawBeans = config.getRawBeanDefinitions(context);
		if (rawBeans!=null) {
			for (String beanName : rawBeans.getBeanDefinitionNames()) {
				if (!component.hasBean(beanName)) {
					IBean bean = createBean(component, beanName, rawBeans.getBeanDefinition(beanName));
					component.addBean(bean);
				}
			}
		}
	}

	private IBean createBean(BeansComponent parent, String beanName, BeanDefinition beanDefinition) {
		return new Bean(parent, new BeanDefinitionHolder(beanDefinition, beanName));
	}

	private IBean createBean(IBeansModelElement parent, ComponentDefinition definition) {
		BeanDefinition[] beanDefs = definition.getBeanDefinitions();
		if (beanDefs.length > 0) {
			BeanDefinitionHolder holder;
			if (definition instanceof BeanComponentDefinition) {
				holder = (BeanComponentDefinition) definition;
			}
			else {
				holder = new BeanDefinitionHolder(definition.getBeanDefinitions()[0], definition.getName());
			}
			return new Bean(parent, holder);
		}
		return null;
	}


}
