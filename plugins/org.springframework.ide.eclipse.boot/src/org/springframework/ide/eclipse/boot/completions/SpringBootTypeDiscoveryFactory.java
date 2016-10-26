/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.completions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeDiscovery;
import org.springsource.ide.eclipse.commons.completions.externaltype.ExternalTypeDiscoveryFactory;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

public class SpringBootTypeDiscoveryFactory implements ExternalTypeDiscoveryFactory {

	public SpringBootTypeDiscoveryFactory() {
	}

	private Map<String, SpringBootTypeDiscovery> instances;

	@Override
	public ExternalTypeDiscovery discoveryFor(IJavaProject jproject) {
		if (isApplicable(jproject)) {
			try {
				ISpringBootProject project = SpringBootCore.create(jproject);
				if (project!=null) {
					return discoveryFor(project.getBootVersion());
				}
			} catch (Exception e) {
				BootActivator.log(e);
			}
			return discoveryFor(SpringBootCore.getDefaultBootVersion());
		}
		return null;
	}

	private synchronized ExternalTypeDiscovery discoveryFor(String bootVersion) {
		if (instances==null) {
			instances = new HashMap<>();
		}
		SpringBootTypeDiscovery existing = instances.get(bootVersion);
		if (existing==null) {
			existing = new SpringBootTypeDiscovery(bootVersion);
			instances.put(bootVersion, existing);
		}
		return existing;
	}

	private boolean isApplicable(IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		return SpringCoreUtils.hasNature(project, SpringCoreUtils.NATURE_ID) &&
				BootPropertyTester.isBootProject(project);
	}

}
