/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.model;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0 
 */
public interface IWebflowModel extends IWebflowModelElement {

	IWebflowProject getProject(IProject project);
	
	boolean hasProject(IProject project);

	void removeProject(IProject project);
	
	void registerModelChangeListener(IWebflowModelListener listener);

	void removeModelChangeListener(IWebflowModelListener listener);

	void fireModelChangedEvent(IWebflowProject project);
	
	IModelElement getElement(String id);
}
