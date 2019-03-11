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
package org.springframework.ide.eclipse.webflow.core.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Christian Dupuis
 */
public interface IWebflowProject extends IModelElement {

	String DESCRIPTION_FILE_OLD = ".springWebflow";

	String DESCRIPTION_FILE =  DESCRIPTION_FILE_OLD; //".settings/" + Activator.PLUGIN_ID;

	List<IWebflowConfig> getConfigs();

	void setConfigs(List<IWebflowConfig> configs);

	IProject getProject();

	IWebflowConfig getConfig(IFile file);
	
	IWebflowConfig getConfig(String flowId);
	
	/**
	 * Returns true if this project's settings can be changed 
	 * @return true if this project can be changed.
	 * @since 2.0.3
	 * @deprecated this is now correctly handled internally using the team provider API
	 */
	@Deprecated
	boolean isUpdatable();

}
