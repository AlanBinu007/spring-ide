/*******************************************************************************
 * Copyright (c) 2006, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.model;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;

/**
 * @author Christian Dupuis
 */
public interface IAopProject {

	Set<IAopReference> getAllReferences();

	void addAopReference(IAopReference reference);

	IJavaProject getProject();

	void clearReferencesForResource(IResource resource);

	Set<IAopReference> getReferencesForResource(IResource resource);
}
