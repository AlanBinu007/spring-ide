/*******************************************************************************
 * Copyright (c) 2004, 2013 Spring IDE Developers
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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.core.model.ILazyInitializedModelElement;
import org.springframework.ide.eclipse.core.model.IModel;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * The {@link IBeansModel} manages instances of {@link IModelElement}s (e.g.
 * {@link IBeansProject}). {@link IBeansModelChangedListener}s register with the
 * {@link IBeansModel} and receive {@link BeansModelChangedEvent}s for all
 * changes.
 * <p>
 * The single instance of {@link IBeansModel} is available from the static
 * method {@link BeansCorePlugin#getModel()}.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public interface IBeansModel extends IBeansModelElement, IModel, ILazyInitializedModelElement {

	String ELEMENT_NAME = "BeansModel";

	/**
	 * Returns the beans project for given project.
	 * 
	 * @param project
	 *            the project a beans project is requested for
	 */
	IBeansProject getProject(IProject project);

	/**
	 * Returns the beans project for given project name or full-qualified config
	 * file name (including project name). <br>
	 * External config files (with a leading '/') are handled too.
	 * 
	 * @param name
	 *            the name of a project or a full-qualified config file name
	 */
	IBeansProject getProject(String name);

	/**
	 * Returns a collection of all projects defined in this model.
	 */
	Set<IBeansProject> getProjects();

	/**
	 * Returns the beans config for given config file.
	 * 
	 * @param configFile
	 *            the config file a beans config is requested for
	 */
	IBeansConfig getConfig(IFile configFile);

	/**
	 * Returns the beans config for given config file. Includes imported
	 * {@link IImportedBeansConfig} in the search if
	 * <code>includeImported</code> is true.
	 * 
	 * @param configFile
	 *            the config file a beans config is requested for
	 * @param includeImported
	 *            true if imported configs should be queried as well.
	 * @since 2.0.3
	 */
	IBeansConfig getConfig(IFile configFile, boolean includeImported);

	/**
	 * Returns all beans configs for given config file. Includes imported
	 * {@link IImportedBeansConfig} in the search if
	 * <code>includeImported</code> is true.
	 * 
	 * @param configFile
	 *            the config file a beans config is requested for
	 * @param includeImported
	 *            true if imported configs should be queried as well.
	 * @since 2.0.3
	 */
	Set<IBeansConfig> getConfigs(IFile configFile, boolean includeImported);

	/**
	 * Returns the beans config for given full-qualified config file name
	 * (including project name). <br>
	 * External config files (with a leading '/') are handled too.
	 * 
	 * @param name
	 *            the name of a config file a beans config is requested for
	 */
	IBeansConfig getConfig(String configName);

	/**
	 * Returns a list of all configs which contain a bean with given bean class.
	 */
	Set<IBeansConfig> getConfigs(String className);

	/**
	 * Returns true if the given resource is a config file. Includes imported
	 * {@link IImportedBeansConfig} in the search if
	 * <code>includeImported</code> is true.
	 * 
	 * @param configFile
	 *            the config file a beans config is requested for
	 * @param includeImported
	 *            true if imported configs should be queried as well.
	 * @since 3.2.0
	 */
	boolean isConfig(IFile resource, boolean includeImported);

}
