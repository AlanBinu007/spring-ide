/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchManager;
import org.springframework.ide.eclipse.boot.dash.metadata.IPropertyStore;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.RunTargetType;

public interface BootDashModelContext {

	IWorkspace getWorkspace();

	ILaunchManager getLaunchManager();

	void log(Exception e);

	IPath getStateLocation();

	IPropertyStore<IProject> getProjectProperties();

	IPropertyStore<RunTargetType> getRunTargetProperties();
	
	SecuredCredentialsStore getSecuredCredentialsStore();

}
