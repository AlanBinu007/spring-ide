/*******************************************************************************
 * Copyright (c) 2013, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.io.ExternalFile;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansConfigFactory {
	
	public static final String JAVA_CONFIG_TYPE = "java:";

	/**
	 * @since 3.3.0
	 */
	public static IBeansConfig create(IBeansProject project, String name, IBeansConfig.Type type) {
		return create(project, name, type, true);
	}
	
	/**
	 * @since 3.6.1
	 */
	public static IBeansConfig create(IBeansProject project, String name, IBeansConfig.Type type, boolean removeProjectPrefixFromName) {
		if (name != null && name.startsWith(JAVA_CONFIG_TYPE)) {
			String className = name.substring(JAVA_CONFIG_TYPE.length());
			IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());

			try {
				IType configClass = javaProject.findType(className);
				return new BeansJavaConfig(project, configClass, className, IBeansConfig.Type.MANUAL);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		else {
			if (removeProjectPrefixFromName && name != null && name.length() > 0 && name.charAt(0) == '/') {
				String projectPath = '/' + project.getElementName() + '/';
				if (name.startsWith(projectPath)) {
					name = name.substring(projectPath.length());
				}
			}
			return new BeansConfig(project, name, IBeansConfig.Type.MANUAL);
		}
		return null;
	}
	
	/**
	 * @since 3.3.0
	 */
	public static String getConfigName(IFile file, IProject project) {
		String configName;
	
		if (!"xml".equals(file.getFileExtension())) {
			IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());
			if (javaProject != null) {
				IJavaElement element = JavaCore.create(file, javaProject);
				if (element != null && element.getPrimaryElement() instanceof ICompilationUnit) {
					String typeName = element.getElementName();
					String fileExtension = file.getFileExtension();
					if (fileExtension != null && fileExtension.length() > 0) {
						typeName = typeName.substring(0, typeName.length() - (fileExtension.length() + 1));
					}
					
					IJavaElement parent = element.getParent();
					String packageName = "";
					if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
						IPackageFragment packageFragment = (IPackageFragment) parent;
						if (!packageFragment.isDefaultPackage()) {
							packageName = packageFragment.getElementName() + ".";
						}
						
						return JAVA_CONFIG_TYPE + packageName + typeName;
					}
				}
			}
		}

		if (file.getProject().equals(project.getProject()) && !(file instanceof ExternalFile)) {
			configName = file.getProjectRelativePath().toString();
		}
		else {
			configName = file.getFullPath().toString();
		}
		return configName;
	}

	/**
	 * @since 3.3.0
	 */
	public static boolean isJavaConfigFile(IFile file) {
		return file != null && (file.getFileExtension().equals("java") || file.getFileExtension().equals("class") 
				|| file.getFileExtension().equals("groovy"));
	}

}
