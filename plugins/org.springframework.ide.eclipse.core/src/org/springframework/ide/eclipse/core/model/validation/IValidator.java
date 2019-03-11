/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.validation;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.project.IProjectContributor;

/**
 * This interface defines the contract for the
 * <code>org.springframework.ide.eclipse.core.validators</code> extension point.
 * <p>
 * All builders must implement this interface according to the following guidelines:
 * <ul>
 * <li>must supply a public, no-argument constructor</li>
 * <li>may implement other methods</li>
 * </ul>
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IValidator extends IProjectContributor {

	/**
	 * Returns a list of {@link IResource}s which are represented by the given object.
	 * @param object the object for which the corresponding {@link IResource}s should be retrieved
	 * @since 2.0.1
	 */
	Set<IResource> deriveResources(Object object);

	/**
	 * Validates all the given affected resources.
	 * @param affectedResources the resource affected by this build
	 * @param monitor a progress monitor, or <code>null</code> if progress reporting and
	 * cancellation are not desired * @param kind the kind of build (<code>0</code>,
	 * {@link IncrementalProjectBuilder#FULL_BUILD}, {@link IncrementalProjectBuilder#CLEAN_BUILD}
	 */
	void validate(Set<IResource> affectedResources, int kind, IProgressMonitor monitor)
			throws CoreException;
}
