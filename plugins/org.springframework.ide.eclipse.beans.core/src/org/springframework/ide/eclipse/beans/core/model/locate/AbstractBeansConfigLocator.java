/*******************************************************************************
 * Copyright (c) 2008, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.locate;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;

/**
 * Base {@link IBeansConfigLocator} implementation that only implements
 * {@link #isBeansConfig(IFile)}.
 * <p>
 * @author Christian Dupuis
 * @since 2.0.5
 */
public abstract class AbstractBeansConfigLocator implements IBeansConfigLocator {

	/** Internal list of allowed file extensions */
	protected static final Set<String> FILE_EXTENSIONS = new ConcurrentSkipListSet<String>(Arrays.asList(new String[] { "xml" }));

	/**
	 * Checks if the given <code>file</code> is accessible and its file extension is in the list
	 * of allowed file extensions.
	 */
	@Override
	public final boolean isBeansConfig(IFile file) {
		if (file.isAccessible() && getAllowedFileExtensions().contains(file.getFileExtension())) {
			Set<IPath> rootPaths = getRootDirectories(file.getProject());
			for (IPath path : rootPaths) {
				if (path.isPrefixOf(file.getFullPath())) {				
					return locateBeansConfigs(file.getProject(), null).contains(file);
				}
			}
		}
		return false;
	}

	/**
	 * Returns a list of allowed file extensions. Subclasses may override this method to return
	 * other allowed file extensions.
	 * @return list of allowed file extensions.
	 */
	protected Set<String> getAllowedFileExtensions() {
		return FILE_EXTENSIONS;
	}

	/**
	 * Returns <code>null</code> to express that this locater does not want to organize located
	 * files in a config set.
	 */
	@Override
	public String getBeansConfigSetName(Set<IFile> files) {
		return null;
	}
	
	/**
	 * No configuration required as this locator does not create a {@link IBeansConfigSet}.
	 */
	@Override
	public void configureBeansConfigSet(IBeansConfigSet configSet) {
		// no op here
	}
	
	/**
	 * Return the root directories to search for {@link IFile} representing Spring configuration
	 * files.
	 * @param project the {@link IProject} to search.
	 * @return the {@link Set} of {@link IPath}s representing the roots to search
	 */
	protected abstract Set<IPath> getRootDirectories(IProject project);

}
