/*******************************************************************************
 *  Copyright (c) 2012 - 2017 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *      DISID Corporation, S.L - Spring Roo maintainer
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal.maven;

import java.lang.reflect.Constructor;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.internal.lifecyclemapping.LifecycleMappingFactory;
import org.eclipse.m2e.core.internal.project.LifecycleMappingConfiguration;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.MavenProjectChangedEvent;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.maven.AbstractSpringProjectConfigurator;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.ui.RooUiActivator;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

/**
 * M2Eclipse project configuration extension that configures a project to get
 * the Roo project nature.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.5.0
 */
public class RooProjectConfigurator extends AbstractSpringProjectConfigurator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doConfigure(MavenProject mavenProject, IProject project, ProjectConfigurationRequest request,
			IProgressMonitor monitor) throws CoreException {
		// first apply Roo project nature
		boolean hasNature = false;
		// ROO-3881: Only parent modules should be define Spring Roo nature. To identify
		// if some module is parent or not we're going to check if the parent project is
		// Spring IO Platform. For multimodule projects, also check if has pom packaging. 
		MavenProject parent = mavenProject.getParent();
		boolean isSingleParent = parent != null && parent.getGroupId().equals("io.spring.platform") 
				&& parent.getArtifactId().equals("platform-bom");
		boolean isMultimoduleParent = isSingleParent && parent != null && parent.getPackaging().equals("pom");
		if(isMultimoduleParent){
			SpringCoreUtils.addProjectNature(project, SpringCore.NATURE_ID, monitor);
			SpringCoreUtils.addProjectNature(project, RooCoreActivator.NATURE_ID, monitor);
			hasNature = true;
		}else {
			for (Artifact art : mavenProject.getArtifacts()) {
				if (art.getArtifactId().startsWith("org.springframework.roo")
						&& art.getGroupId().equals("org.springframework.roo")) {
					SpringCoreUtils.addProjectNature(project, SpringCore.NATURE_ID, monitor);
					if(isSingleParent){
						SpringCoreUtils.addProjectNature(project, RooCoreActivator.NATURE_ID, monitor);
						hasNature = true;
					}
				}
			}
		}
		
		if (!hasNature && parent != null) {
			// traverse the parent chain
			IMavenProjectFacade facade = projectManager.getMavenProject(parent.getGroupId(),
					parent.getArtifactId(), parent.getVersion());
			if (facade != null && facade.getMavenProject() != null && facade.getProject() != null) {
				doConfigure(facade.getMavenProject(), facade.getProject(), request, monitor);
			}
		}
	}

	@Override
	public void mavenProjectChanged(MavenProjectChangedEvent event, IProgressMonitor monitor) throws CoreException {
		IMavenProjectFacade facade = event.getMavenProject();
		if (facade != null) {
			LifecycleMappingConfiguration oldConfiguration = LifecycleMappingConfiguration.restore(facade, monitor);
			if (oldConfiguration != null
					&& LifecycleMappingFactory.isLifecycleMappingChanged(facade, oldConfiguration, monitor)) {
				updateProjectConfiguration(facade.getProject());
			}
		}
	}
	
	private void updateProjectConfiguration(IProject project) {
		// TODO: remove this reflection if we ever set a minimum requirement of M2E 1.1
		try {
			Class<?> jobClass;
			Constructor<?> constr;
			WorkspaceJob job = null;
			try {
				// M2E 1.1 and greater
				jobClass = Class.forName("org.eclipse.m2e.core.ui.internal.UpdateMavenProjectJob");
				constr = jobClass.getConstructor(IProject[].class);
				job = (WorkspaceJob) constr.newInstance((Object) new IProject[] { project });
			} catch (ClassNotFoundException e) {
				// M2E 1.0
				jobClass = Class.forName("org.eclipse.m2e.core.ui.internal.UpdateConfigurationJob");
				constr = jobClass.getConstructor(IProject[].class, boolean.class, boolean.class);
				job = (WorkspaceJob) constr.newInstance(
						new IProject[] { project }, MavenPlugin.getMavenConfiguration().isOffline(), false);
			}
			if (job != null) {
				job.schedule();
			}
		}
		catch (Throwable e) {
			RooCoreActivator.log(new Status(IStatus.ERROR, RooUiActivator.PLUGIN_ID,
					"Unable to update Maven project configuration", e));
		}
	}

}
