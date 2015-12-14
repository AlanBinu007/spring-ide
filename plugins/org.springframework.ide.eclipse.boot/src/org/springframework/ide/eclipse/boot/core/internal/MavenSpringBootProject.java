/*******************************************************************************
 * Copyright (c) 2013, 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.internal;

import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.ARTIFACT_ID;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCIES;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.DEPENDENCY;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.GROUP_ID;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.findChild;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.findChilds;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.getChild;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.getTextValue;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.performOnDOMDocument;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.createElementWithText;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.format;
import static org.eclipse.m2e.core.ui.internal.editing.PomEdits.OPTIONAL;

import org.eclipse.m2e.core.ui.internal.UpdateMavenProjectJob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.IMavenProjectRegistry;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits.Operation;
import org.eclipse.m2e.core.ui.internal.editing.PomEdits.OperationTuple;
import org.eclipse.m2e.core.ui.internal.editing.PomHelper;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.IMavenCoordinates;
import org.springframework.ide.eclipse.boot.core.MavenCoordinates;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;
import org.springframework.ide.eclipse.boot.core.StarterId;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class MavenSpringBootProject extends SpringBootProject {

	//TODO: all of the starter manipulation code completely ignores the version infos in SpringBootStarter objects.
	// This is ok assuming that versions always follow the 'managed' version in parent pom.
	// If that is not the case then... ??

	//TODO: properly handle pom manipulation when pom file is open / dirty in an editor.
	// minimum requirement: detect and prohibit by throwing an error.

	private static final List<SpringBootStarter> NO_STARTERS = Arrays
			.asList(new SpringBootStarter[0]);

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private IProject project;

	public MavenSpringBootProject(IProject project) {
		Assert.isNotNull(project);
		this.project = project;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	/**
	 * @return List of maven coordinates for known boot starters. These are
	 *         discovered dynamically based on project contents. E.g. for maven
	 *         projects we examine the 'dependencyManagement' section of the
	 *         project's effective pom.
	 *
	 * @throws CoreException
	 */
	@Override
	public List<SpringBootStarter> getKnownStarters() throws CoreException {
		MavenProject mp = getMavenProject();
		if (mp!=null) {
			DependencyManagement depMan = mp.getDependencyManagement();
			if (depMan != null) {
				List<Dependency> deps = depMan.getDependencies();
				return getStarters(deps);
			}
		}
		return NO_STARTERS;
	}

	private MavenProject getMavenProject() throws CoreException {
		IMavenProjectRegistry pr = MavenPlugin.getMavenProjectRegistry();
		IMavenProjectFacade mpf = pr.getProject(project);
		if (mpf!=null) {
			return mpf.getMavenProject(new NullProgressMonitor());
		}
		return null;
	}

	private IFile getPomFile() {
		return project.getFile(new Path("pom.xml"));
	}

	@Override
	public List<SpringBootStarter> getBootStarters() throws CoreException {
		MavenProject mp = getMavenProject();
		if (mp!=null) {
			return getStarters(mp.getDependencies());
		}
		return Collections.emptyList();
	}


	@Override
	public List<IMavenCoordinates> getDependencies() throws CoreException {
		MavenProject mp = getMavenProject();
		if (mp!=null) {
			return toMavenCoordinates(mp.getDependencies());
		}
		return Collections.emptyList();
	}



	private List<IMavenCoordinates> toMavenCoordinates(List<Dependency> dependencies) {
		ArrayList<IMavenCoordinates> converted = new ArrayList<>(dependencies.size());
		for (Dependency d : dependencies) {
			converted.add(new MavenCoordinates(d.getGroupId(), d.getArtifactId(), d.getClassifier(), d.getVersion()));
		}
		return converted;
	}

	private List<SpringBootStarter> getStarters(List<Dependency> deps) {
		if (deps != null) {
			ArrayList<SpringBootStarter> starters = new ArrayList<SpringBootStarter>();
			for (Dependency _dep : deps) {
				IMavenCoordinates dep = new MavenCoordinates(_dep.getGroupId(),
						_dep.getArtifactId(), _dep.getVersion());
				if (SpringBootStarter.isStarter(dep)) {
					starters.add(new SpringBootStarter(dep));
				}
			}
			return starters;
		}
		return NO_STARTERS;
	}

	@Override
	public void removeStarter(final SpringBootStarter starter)
			throws CoreException {
		try {
			List<SpringBootStarter> starters = getBootStarters();
			boolean changed = starters.remove(starter);
			if (changed) {
				setStarters(starters);
			}
		} catch (Throwable e) {
			throw ExceptionUtil.coreException(e);
		}
	}

	@Override
	public void addStarter(final SpringBootStarter starter)
			throws CoreException {
		try {
			List<SpringBootStarter> starters = getBootStarters();
			boolean changed = starters.add(starter);
			if (changed) {
				setStarters(starters);
			}
		} catch (Throwable e) {
			throw ExceptionUtil.coreException(e);
		}
	}

	/**
	 * Determine the 'managed' version, if any, associate with a given dependency.
	 * @return Version string or null.
	 */
	private String getManagedVersion(IMavenCoordinates dep) {
		try {
			MavenProject mp = getMavenProject();
			if (mp!=null) {
				DependencyManagement managedDeps = mp.getDependencyManagement();
				if (managedDeps!=null) {
					List<Dependency> deps = managedDeps.getDependencies();
					if (deps!=null && !deps.isEmpty()) {
						for (Dependency d : deps) {
							if ("jar".equals(d.getType())) {
								if (dep.getArtifactId().equals(d.getArtifactId()) && dep.getGroupId().equals(d.getGroupId())) {
									return d.getVersion();
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return null;
	}

	private void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}


	@Override
	public void addMavenDependency(final IMavenCoordinates dep, final boolean preferManagedVersion) throws CoreException {
		addMavenDependency(dep, preferManagedVersion, false);
	}

	@Override
	public void addMavenDependency(
			final IMavenCoordinates dep,
			final boolean preferManagedVersion, final boolean optional
	) throws CoreException {
		try {
			IFile file = getPomFile();
			performOnDOMDocument(new OperationTuple(file, new Operation() {
				public void process(Document document) {
					Element depsEl = getChild(
							document.getDocumentElement(), DEPENDENCIES);
					if (depsEl==null) {
						//TODO: handle this case
					} else {
						String version = dep.getVersion();
						String managedVersion = getManagedVersion(dep);
						if (managedVersion!=null) {
							//Decide whether we can/should inherit the managed version or override it.
							if (preferManagedVersion || managedVersion.equals(version)) {
								version = null;
							}
						} else {
							//No managed version. We have to include a version in xml added to the pom.
						}
						Element xmlDep = PomHelper.createDependency(depsEl,
								dep.getGroupId(),
								dep.getArtifactId(),
								version
						);
						if (optional) {
							createElementWithText(xmlDep, OPTIONAL, "true");
							format(xmlDep);
						}
					}
				}
			}));
		} catch (Throwable e) {
			throw ExceptionUtil.coreException(e);
		}
	}

	@Override
	public void setStarters(Collection<SpringBootStarter> _starters) throws CoreException {
		try {
			final Set<StarterId> starters = new HashSet<StarterId>();
			for (SpringBootStarter s : _starters) {
				starters.add(s.getId());
			}

			IFile file = getPomFile();
			performOnDOMDocument(new OperationTuple(file, new Operation() {
				public void process(Document document) {
					Element depsEl = getChild(
							document.getDocumentElement(), DEPENDENCIES);
					List<Element> children = findChilds(depsEl, DEPENDENCY);
					for (Element c : children) {
						//We only care about 'starter' dependencies. Leave everything else alone.
						// Also... don't touch nodes that are already there, unless they are to
						// be removed. This way we don't mess up versions, comments or other stuff
						// that a user may have inserted via manual edits.
						String aid = getTextValue(findChild(c, ARTIFACT_ID));
						String gid = getTextValue(findChild(c, GROUP_ID));
						if (aid!=null && gid!=null) { //ignore invalid entries that don't have gid or aid
							if (SpringBootStarter.isStarterAId(aid)) {
								StarterId id = new StarterId(gid, aid);
								boolean keep = starters.remove(id);
								if (!keep) {
									depsEl.removeChild(c);
								}
							}
						}
					}

					//if 'starters' is not empty at this point, it contains remaining ids we have not seen
					// in the pom, so we need to add them.
					for (StarterId s : starters) {
						PomHelper.createDependency(depsEl,
								s.getGroupId(), s.getArtifactId(),
								null);
					}
				}
			}));
		} catch (Throwable e) {
			throw ExceptionUtil.coreException(e);
		}
	}

	@Override
	public void updateProjectConfiguration() {
		new UpdateMavenProjectJob(new IProject[] {
				getProject()
		}).schedule();
 	}

	@Override
	public String getBootVersion() {
		try {
			MavenProject mp = getMavenProject();
			if (mp!=null) {
				return getBootVersion(mp.getDependencies());
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return SpringBootCore.getDefaultBootVersion();
	}

	private String getBootVersion(List<Dependency> dependencies) {
		for (Dependency dep : dependencies) {
			if (dep.getArtifactId().startsWith("spring-boot") && dep.getGroupId().equals("org.springframework.boot")) {
				return dep.getVersion();
			}
		}
		return SpringBootCore.getDefaultBootVersion();
	}
}
