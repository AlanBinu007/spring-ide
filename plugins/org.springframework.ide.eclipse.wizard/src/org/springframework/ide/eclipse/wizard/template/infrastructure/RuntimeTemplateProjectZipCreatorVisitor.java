/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.springsource.ide.eclipse.commons.content.core.util.IContentConstants;
import org.springsource.ide.eclipse.commons.core.FileUtil;
import org.springsource.ide.eclipse.commons.core.Policy;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

/**
 * @author Kaitlin Sherwood
 * @author Martin Lippert
 */
public class RuntimeTemplateProjectZipCreatorVisitor implements IResourceVisitor {

	private final ZipOutputStream out;

	private final IProgressMonitor monitor;

	private final Set<String> excludes;

	public RuntimeTemplateProjectZipCreatorVisitor(ZipOutputStream out, IProgressMonitor monitor) {
		this.out = out;
		this.monitor = monitor;

		this.excludes = new HashSet<String>();
		this.excludes.add("build");
		this.excludes.add("target");
		this.excludes.add(".DS_Store");
		this.excludes.add(IContentConstants.TEMPLATE_DATA_FILE_NAME);
		this.excludes.add(IContentConstants.WIZARD_DATA_FILE_NAME);
	}

	private boolean select(IResource resource) {
		if (resource.isDerived()) {
			return false;
		}

		IPath fullPath = resource.getFullPath();
		String[] segments = fullPath.segments();
		if (segments != null) {
			for (String segment : segments) {
				if (this.excludes.contains(segment)) {
					return false;
				}
			}
		}

		return true;
	}

	public void addFile(IFile file) throws CoreException, IOException {
		InputStream in = new BufferedInputStream(file.getContents());
		try {
			int len;
			byte[] buffer = new byte[FileUtil.BUFFER_SIZE];
			while ((len = in.read(buffer)) > 0) {
				Policy.checkCancelled(monitor);
				out.write(buffer, 0, len);
			}
		}
		finally {
			in.close();
		}
	}

	public boolean visit(IResource resource) throws CoreException {
		if (resource instanceof IProject) {
			return true;
		}

		if (!select(resource)) {
			return false;
		}

		String path = "template/" + resource.getProjectRelativePath().toString();
		try {
			if (resource instanceof IFile) {
				ZipEntry entry = new ZipEntry(path);
				out.putNextEntry(entry);
				addFile((IFile) resource);
				out.closeEntry();
			}
			else if (resource instanceof IFolder) {
				ZipEntry entry = new ZipEntry(path + "/");
				out.putNextEntry(entry);
				out.closeEntry();
			}
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Could not add \"" + path
					+ "\" to archive.", e));
		}

		return true;
	}

}
