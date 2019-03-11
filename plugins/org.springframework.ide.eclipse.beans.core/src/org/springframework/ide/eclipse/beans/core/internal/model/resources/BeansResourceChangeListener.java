/*******************************************************************************
 * Copyright (c) 2004, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.locate.BeansConfigLocatorDefinition;
import org.springframework.ide.eclipse.beans.core.model.locate.BeansConfigLocatorFactory;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeListener;

/**
 * Implementation of {@link IResourceChangeListener} which detects modifications to Spring projects
 * (add/remove Spring beans nature, open/close and delete) and Spring beans configurations (change
 * and delete).
 * <p>
 * An implementation of {@link IBeansResourceChangeEvents} has to be provided. Here are callbacks
 * defined for the different events.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansResourceChangeListener extends SpringResourceChangeListener {

	private IBeansResourceChangeEvents events;

	public BeansResourceChangeListener(IBeansResourceChangeEvents events) {
		super(events);
		this.events = events;
	}

	@Override
	protected IResourceDeltaVisitor getVisitor(int eventType) {
		return new BeansResourceVisitor(eventType);
	}

	/**
	 * Checks if a given <code>file</code> requires a refresh of the auto detected
	 * {@link IBeansConfig}s.
	 * <p>
	 * This is checked by asking every contributed {@link BeansConfigLocatorDefinition}.
	 */
	public static boolean requiresRefresh(IFile file) {
		for (final BeansConfigLocatorDefinition locator : BeansConfigLocatorFactory
				.getBeansConfigLocatorDefinitions()) {
			try {
				if (locator.isEnabled(file.getProject())
						&& locator.getBeansConfigLocator().supports(file.getProject())
						&& locator.getBeansConfigLocator().requiresRefresh(file)) {
					return true;
				}
			}
			catch (Exception e) {
				// Make sure that a extension contribution can't crash the resource listener
			}
		}
		return false;
	}

	/**
	 * Internal resource delta visitor.
	 */
	protected class BeansResourceVisitor extends SpringResourceVisitor {

		public BeansResourceVisitor(int eventType) {
			super(eventType);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean resourceAdded(IResource resource) {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				if (isProjectDescriptionFile(file)) {
					if (SpringCoreUtils.isSpringProject(file)) {
						events.projectDescriptionChanged(file, eventType);
					}
				}
				// Test for auto detected config before testing for normal config as otherwise
				// the auto-detected config will morph into a manual configured and added to .springBeans
				else if (isAutoDetectedConfig(file)) {
					events.configAdded(file, eventType, IBeansConfig.Type.AUTO_DETECTED);
				}
				else if (BeansCoreUtils.isBeansConfig(file)) {
					events.configAdded(file, eventType);
				}
				return false;
			}
			return super.resourceAdded(resource);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean resourceChanged(IResource resource, int flags) {
			if (resource instanceof IFile) {
				if ((flags & IResourceDelta.CONTENT) != 0) {
					IFile file = (IFile) resource;
					if (isProjectDescriptionFile(file)) {
						if (SpringCoreUtils.isSpringProject(file)) {
							events.projectDescriptionChanged(file, eventType);
						}
					}
					// Test for auto detected config before testing for normal config as otherwise
					// the auto-detected config will morph into a manual configured and added to .springBeans
					else if (isAutoDetectedConfig(file)) {
						events.configAdded(file, eventType, IBeansConfig.Type.AUTO_DETECTED);
					}
					else if (BeansCoreUtils.isBeansConfig(file, true)) {
						events.configChanged(file, eventType);
					}
					else if (requiresRefresh(file)) {
						events.listenedFileChanged(file, eventType);
					}
				}
				return false;
			}
			return super.resourceChanged(resource, flags);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean resourceRemoved(IResource resource) {
			if (resource instanceof IFile) {
				if (BeansCoreUtils.isBeansConfig(resource)) {
					events.configRemoved((IFile) resource, eventType);
				}
				else if (requiresRefresh((IFile) resource)) {
					events.listenedFileChanged((IFile) resource, eventType);
				}
				return false;
			}
			return super.resourceRemoved(resource);
		}

		/**
		 * Checks if the given <code>resource</code> represents a .springBeans project description.
		 */
		private boolean isProjectDescriptionFile(IResource resource) {
			return resource != null
					&& resource.isAccessible()
					&& resource.getType() == IResource.FILE
					&& ((resource.getFullPath().segmentCount() == 2 && resource.getName().equals(
							IBeansProject.DESCRIPTION_FILE)) || SpringCoreUtils
							.isManifest(resource));
		}

		/**
		 * Checks if the given <code>file</code> is an auto detected {@link IBeansConfig}.
		 */
		private boolean isAutoDetectedConfig(IFile file) {
			for (final BeansConfigLocatorDefinition locator : BeansConfigLocatorFactory
					.getBeansConfigLocatorDefinitions()) {
				try {
					if (locator.isEnabled(file.getProject())
							&& locator.getBeansConfigLocator().supports(file.getProject())) {
						if (locator.getBeansConfigLocator().isBeansConfig(file)) {
							return true;
						}
					}
				}
				catch (Exception e) {
					// Make sure that a extension contribution can't crash the resource listener
				}
			}
			return false;
		}

	}

}
