/*******************************************************************************
 * Copyright (c) 2004, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model;

import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This interface provides information for a Spring beans configuration.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public interface IBeansConfig extends IBeansModelElement, IResourceModelElement, IBeanClassAware {
	
	public static final QualifiedName CONFIG_FILE_TAG = new QualifiedName(BeansCorePlugin.PLUGIN_ID, "configFileTag"); //$NON-NLS-1$\
	public static final String CONFIG_FILE_TAG_VALUE  = "taggedAsPotentialConfigFile";

	enum Type {
		MANUAL, AUTO_DETECTED
	}

	String EXTERNAL_FILE_NAME_PREFIX = "external:/";

	String DEFAULT_LAZY_INIT = "false";

	String DEFAULT_AUTO_WIRE = "no";

	String DEFAULT_DEPENDENCY_CHECK = "none";

	String DEFAULT_INIT_METHOD = "";

	String DEFAULT_DESTROY_METHOD = "";

	String DEFAULT_MERGE = "false";

	String getDefaultLazyInit();

	String getDefaultAutowire();

	String getDefaultDependencyCheck();

	String getDefaultInitMethod();

	String getDefaultDestroyMethod();

	String getDefaultMerge();

	Set<IBeansImport> getImports();

	Set<IBeanAlias> getAliases();

	IBeanAlias getAlias(String name);

	Set<IBeansComponent> getComponents();

	Set<IBean> getBeans();

	IBean getBean(String name);

	boolean hasBean(String name);

	/**
	 * Returns <code>true</code> if the underlying resource has been changed
	 * @since 2.0.3
	 */
	boolean resourceChanged();

	/**
	 * Type of this configuration file. Could either be manual or automatic configured
	 * @since 2.0.5
	 */
	Type getType();

	/**
	 * Register an {@link IBeansConfigEventListener} with the {@link IBeansConfig}.
	 * @since 2.2.5
	 */
	void registerEventListener(IBeansConfigEventListener configEventListener);

	/**
	 * Un-register an {@link IBeansConfigEventListener} with the {@link IBeansConfig}.
	 * @since 2.2.5
	 */
	void unregisterEventListener(IBeansConfigEventListener configEventListener);

	/**
	 * checks whether this beans config scans for annotations or not
	 * @since 3.4.0
	 */
	boolean doesAnnotationScanning();

	/**
	 * Gets the 'raw' bean definitions that were recorded in a given context. This information
	 * is used to discover implicitly defined beans (i.e beans that were
	 * registered to the BeanDefinitionRegistry indirectly rather than directly defined by an
	 * xml configuration element. Implementors of this interface may
	 * choose not to implement raw bean definition recording, in which case they
	 * can simply return null from this method.
	 *
	 * @since 3.7.0
	 */
	BeanDefinitionRegistry getRawBeanDefinitions(CompositeComponentDefinition context);

}
