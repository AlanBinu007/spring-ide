/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model;

import org.eclipse.ui.IMemento;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.aspectj.TypePatternClassFilter;
import org.springframework.aop.support.ClassFilters;
import org.springframework.ide.eclipse.aop.core.model.IIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeanIntroductionDefinition extends BeanAspectDefinition implements
		IIntroductionDefinition {

	private String defaultImplName;

	private String introducedInterfaceName;

	private String typePattern;

	private ClassFilter typePatternClassFilter;

	public BeanIntroductionDefinition() {
		setType(ADVICE_TYPE.DECLARE_PARENTS);
	}

	@Override
	public String getAdviceMethodName() {
		throw new IllegalArgumentException();
	}

	public String getDefaultImplName() {
		return this.defaultImplName;
	}

	public String getFactoryId() {
		return BeanIntroductionDefinitionElementFactory.FACTORY_ID;
	}

	public String getImplInterfaceName() {
		return this.introducedInterfaceName;
	}

	@Override
	public ADVICE_TYPE getType() {
		return ADVICE_TYPE.DECLARE_PARENTS;
	}

	public ClassFilter getTypeMatcher() {
		if (this.typePatternClassFilter == null) {
			ClassFilter typePatternFilter = new TypePatternClassFilter(
					typePattern);

			// Excludes methods implemented.
			ClassFilter exclusion = new ClassFilter() {
				public boolean matches(Class clazz) {
					try {
						Class<?> implInterfaceClass = Thread.currentThread()
								.getContextClassLoader().loadClass(
										introducedInterfaceName);
						return !(implInterfaceClass.isAssignableFrom(clazz));
					}
					catch (ClassNotFoundException e) {
						return false;
					}
				}
			};
			this.typePatternClassFilter = ClassFilters.intersection(
					typePatternFilter, exclusion);
		}
		return this.typePatternClassFilter;
	}

	public String getTypePattern() {
		return this.typePattern;
	}

	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putString(
			BeanIntroductionDefinitionElementFactory.INTRODUCED_INTERFACE_NAME_ATTRIBUTE,
			this.introducedInterfaceName);
		memento.putString(
			BeanIntroductionDefinitionElementFactory.DEFAULT_IMPL_NAME_ATTRIBUTE,
			this.defaultImplName);
		memento.putString(
			BeanIntroductionDefinitionElementFactory.TYPE_PATTERN_ATTRIBUTE,
			this.typePattern);
	}

	public void setDefaultImplName(String defaultImplName) {
		this.defaultImplName = defaultImplName;
	}

	public void setIntroducedInterfaceName(String introducedInterfaceName) {
		this.introducedInterfaceName = introducedInterfaceName;
	}

	public void setTypePattern(String typePattern) {
		this.typePattern = typePattern;
	}
}
