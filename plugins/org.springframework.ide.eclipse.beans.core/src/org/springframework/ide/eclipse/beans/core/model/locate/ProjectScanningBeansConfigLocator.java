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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.DelegatingNamespaceHandlerResolver;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

/**
 * Basic {@link IBeansConfigLocator} that is capable for scanning an
 * {@link IProject} or {@link IJavaProject} for Spring XML configuration files.
 * <p>
 * Only those XML files that have any known namespace uri at the root element
 * level are being considered to be a suitable candidate.
 * 
 * @author Christian Dupuis
 * @since 2.0.5
 */
@SuppressWarnings("restriction")
public class ProjectScanningBeansConfigLocator extends
		AbstractJavaProjectPathMatchingBeansConfigLocator {

	/** Ant-style that matches on every XML file */
	private String ALLOWED_FILE_PATTERN = "**/*";

	/**
	 * Internal cache for {@link NamespaceHandlerResolver}s keyed by their
	 * {@link IProject}
	 */
	private Map<IProject, NamespaceHandlerResolver> namespaceResoverCache = new HashMap<IProject, NamespaceHandlerResolver>();

	/** Configured file patters derived from the configured file patterns */
	private Set<String> configuredFilePatterns = null;

	/** Configured file extensions from the dialog */
	private Set<String> configuredFileExtensions = null;

	/** The project this locator operates on */
	private IProject project = null;

	/**
	 * Constructor taking a string of CSV file extensions
	 * 
	 * @param configuredFileSuffixes
	 */
	public ProjectScanningBeansConfigLocator(String configuredFileSuffixes) {
		configuredFilePatterns = new ConcurrentSkipListSet<String>();
		configuredFileExtensions = new ConcurrentSkipListSet<String>();

		for (String filePattern : StringUtils
				.commaDelimitedListToStringArray(configuredFileSuffixes)) {
			filePattern = filePattern.trim();
			int ix = filePattern.lastIndexOf('.');
			if (ix != -1) {
				configuredFileExtensions.add(filePattern.substring(ix + 1));
			} else {
				configuredFileExtensions.add(filePattern);
			}
			configuredFilePatterns.add(ALLOWED_FILE_PATTERN + filePattern);
		}
	}

	/**
	 * As this locator is not intended to be used at runtime, we don't need to
	 * listen to any resource changes.
	 */
	@Override
	public boolean requiresRefresh(IFile file) {
		return false;
	}

	/**
	 * Supports both an normal {@link IProject} and a {@link IJavaProject} but
	 * it needs to have the Spring nature.
	 */
	@Override
	public boolean supports(IProject project) {
		return SpringCoreUtils.isSpringProject(project);
	}

	/**
	 * Returns a {@link NamespaceHandlerResolver} for the given {@link IProject}
	 * . First looks in the {@link #namespaceResoverCache cache} before creating
	 * a new instance.
	 */
	protected NamespaceHandlerResolver getNamespaceHandlerResolver(
			IProject project) {
		if (!namespaceResoverCache.containsKey(project)) {
			namespaceResoverCache.put(project,
					new DelegatingNamespaceHandlerResolver(
							NamespaceHandlerResolver.class.getClassLoader(),
							null));
		}
		return namespaceResoverCache.get(project);
	}

	/**
	 * Filters out every {@link IFile} which is has unknown root elements in its
	 * XML content.
	 */
	@Override
	protected Set<IFile> filterMatchingFiles(Set<IFile> files) {
		// if project is a java project remove bin dirs from the list
		Set<String> outputDirectories = new HashSet<String>();
		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (javaProject != null) {
			try {
				// add default output directory
				outputDirectories.add(javaProject.getOutputLocation()
						.toString());

				// add source folder specific output directories
				for (IClasspathEntry entry : javaProject.getRawClasspath()) {
					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE
							&& entry.getOutputLocation() != null) {
						outputDirectories.add(entry.getOutputLocation()
								.toString());
					}
				}
			} catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}

		Set<IFile> detectedFiles = new LinkedHashSet<IFile>();
		for (IFile file : files) {
			boolean skip = false;
			// first check if the file sits in an output directory
			String path = file.getFullPath().toString();
			for (String outputDirectory : outputDirectories) {
				if (path.startsWith(outputDirectory)) {
					skip = true;
				}
			}
			if (skip) {
				continue;
			}

			// check if the file is known Spring xml file
			IStructuredModel model = null;
			try {
				try {
					model = StructuredModelManager.getModelManager()
							.getExistingModelForRead(file);
				} catch (RuntimeException e) {
					// sometimes WTP throws a NPE in concurrency situations
				}
				if (model == null) {
					model = StructuredModelManager.getModelManager()
							.getModelForRead(file);
				}
				if (model != null) {
					IDOMDocument document = ((DOMModelImpl) model)
							.getDocument();
					if (document != null
							&& document.getDocumentElement() != null) {
						String namespaceUri = document.getDocumentElement()
								.getNamespaceURI();
						if (applyNamespaceFilter(file, namespaceUri)) {
							detectedFiles.add(file);
						}
					}
				}
			} catch (IOException e) {
				BeansCorePlugin.log(e);
			} catch (CoreException e) {
				BeansCorePlugin.log(e);
			} finally {
				if (model != null) {
					model.releaseFromRead();
				}
			}
		}
		return detectedFiles;
	}

	protected boolean applyNamespaceFilter(IFile file, String namespaceUri) {
		return (namespaceUri != null && (NamespaceUtils.DEFAULT_NAMESPACE_URI
				.equals(namespaceUri) || getNamespaceHandlerResolver(
				file.getProject()).resolve(namespaceUri) != null));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<String> getAllowedFilePatterns() {
		return configuredFilePatterns;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<String> getAllowedFileExtensions() {
		return configuredFileExtensions;
	}

	/**
	 * Returns the root directories to scan.
	 */
	@Override
	protected Set<IPath> getRootDirectories(IProject project) {
		this.project = project;

		Set<IPath> rootDirectories = new LinkedHashSet<IPath>();
		rootDirectories.add(project.getFullPath());
		return rootDirectories;
	}

}
