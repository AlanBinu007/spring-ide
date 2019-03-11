/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.internal.project;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidatorDefinitionFactory;
import org.springframework.ide.eclipse.core.model.validation.IValidator;
import org.springframework.ide.eclipse.core.project.DefaultProjectContributorState;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.core.project.IProjectContributionEventListener;
import org.springframework.ide.eclipse.core.project.IProjectContributor;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.project.IProjectContributorStateAware;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinition;
import org.springframework.ide.eclipse.core.project.ProjectBuilderDefinitionFactory;
import org.springframework.ide.eclipse.core.project.ProjectContributionEventListenerFactory;

/**
 * Incremental project builder which implements the Strategy GOF pattern. For every modified {@link IResource} within a
 * Spring project all implementations of the interface {@link IProjectBuilder} provided via the extension point
 * <code>org.springframework.ide.eclipse.core.builders</code> and the interface {@link IValidator} provided via the
 * extension point <code>org.springframework.ide.eclipse.core.validators</code> are called.
 * <p>
 * This {@link IncrementalProjectBuilder} makes state in form of an instance of {@link IProjectContributorState} for the
 * {@link IProjectContributor} accessible. This state should be used to store arbitrary state object in order to save
 * calculation time for subsequent {@link IValidator} or {@link IProjectBuilder}.
 * <p>
 * {@link IProjectBuilder} or {@link IValidator} implementations that want to access the state should implement the
 * {@link IProjectContributorStateAware} interface to a call back with the current state.
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 * 
 * @since 2.0
 * @see IProjectContributor
 * @see IProjectBuilder
 * @see IValidator
 * @see IProjectContributorState
 */
public class SpringProjectContributionManager extends IncrementalProjectBuilder {
	
	private static Object dummyMapObject = new Object();
	private static Map<String, Object> classpathChanged = new ConcurrentHashMap<String, Object>();
	
	/**
	 * indicate that the classpath changed for the given project since the last build
	 * This mirrors the same behavior as the JavaBuilder, which keeps a state between
	 * builds and checks for classpath changes at every build.
	 * 
	 * This is triggered by a element change listener in SpringModel that keeps listening
	 * for classpath changes.
	 * 
	 * @param projectName The name of the project
	 */
	public static void classpathChanged(String projectName) {
		classpathChanged.put(projectName, dummyMapObject);
	}

	/**
	 * {@inheritDoc}
	 */
	protected final IProject[] build(final int kind, Map args, final IProgressMonitor monitor) throws CoreException {
		final IProject project = getProject();
		final IResourceDelta delta = getDelta(project);

		final List<ProjectBuilderDefinition> builderDefinitions = ProjectBuilderDefinitionFactory
				.getProjectBuilderDefinitions();
		final List<ValidatorDefinition> validatorDefinitions = ValidatorDefinitionFactory.getValidatorDefinitions();
		final List<IProjectContributionEventListener> listeners = ProjectContributionEventListenerFactory
				.getProjectContributionEventListeners();

		// Set up the state object
		final IProjectContributorState state = prepareState(project, builderDefinitions, validatorDefinitions);
		
		// check for classpath changes (that require a full build)
		Object removed = classpathChanged.remove(project.getName());
		final int buildKind = removed != null ? IncrementalProjectBuilder.FULL_BUILD : kind;
		
		// Fire start event on listeners
		for (final IProjectContributionEventListener listener : listeners) {
			execute(new SafeExecutableWithMonitor() {
				public void execute(IProgressMonitor subMonitor) throws Exception {
					listener.start(buildKind, delta, builderDefinitions, validatorDefinitions, state, project, subMonitor);
				}
			}, monitor);

		}

		// At first run all builders
		for (ProjectBuilderDefinition builderDefinition : builderDefinitions) {
			if (builderDefinition.isEnabled(project)) {
				Set<IResource> affectedResources = getAffectedResources(builderDefinition.getProjectBuilder(), project,
						buildKind, delta);
				runBuilder(builderDefinition, affectedResources, buildKind, monitor, listeners);
			}
		}

		// Finally run all validators
		for (ValidatorDefinition validatorDefinition : validatorDefinitions) {
			if (validatorDefinition.isEnabled(project)) {
				Set<IResource> affectedResources = getAffectedResources(validatorDefinition.getValidator(), project, buildKind, delta);
				runValidator(validatorDefinition, affectedResources, buildKind, monitor, listeners);
			}
		}

		// Fire end event on listeners
		for (final IProjectContributionEventListener listener : listeners) {
			execute(new SafeExecutableWithMonitor() {
				public void execute(IProgressMonitor subMonitor) throws Exception {
					listener.finish(buildKind, delta, builderDefinitions, validatorDefinitions, state, project, subMonitor);
				}
			}, monitor);
		}

		return null;
	}

	/**
	 * Collects all affected resources from the given {@link IResourceDelta} and {@link IProjectContributor}.
	 */
	private Set<IResource> getAffectedResources(IProjectContributor contributor, IProject project, int kind,
			IResourceDelta delta) throws CoreException {
		Set<IResource> affectedResources;
		if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD) {
			ResourceTreeVisitor visitor = new ResourceTreeVisitor(contributor);
			project.accept(visitor);
			affectedResources = visitor.getResources();
		}
		else {
			ResourceDeltaVisitor visitor = new ResourceDeltaVisitor(contributor, kind);
			delta.accept(visitor);
			affectedResources = visitor.getResources();
		}
		return affectedResources;
	}

	/**
	 * Instantiate the {@link IProjectContributorState} object. The state object is then passed to any
	 * {@link IProjectBuilder} and {@link IValidator} that implements the {@link IProjectContributorStateAware}
	 * interface.
	 * <p>
	 * This implementation creates an instance of {@link DefaultProjectContributorState}.
	 */
	private IProjectContributorState prepareState(IProject project, List<ProjectBuilderDefinition> builderDefinitions,
			List<ValidatorDefinition> validatorDefinitions) {

		IProjectContributorState context = new DefaultProjectContributorState();
		context.hold(project);
		
		for (ProjectBuilderDefinition builderDefinition : builderDefinitions) {
			if (builderDefinition.isEnabled(project)
					&& builderDefinition.getProjectBuilder() instanceof IProjectContributorStateAware) {
				((IProjectContributorStateAware) builderDefinition.getProjectBuilder())
						.setProjectContributorState(context);
			}
		}

		for (ValidatorDefinition validatorDefinition : validatorDefinitions) {
			if (validatorDefinition.isEnabled(project)
					&& validatorDefinition.getValidator() instanceof IProjectContributorStateAware) {
				((IProjectContributorStateAware) validatorDefinition.getValidator())
						.setProjectContributorState(context);
			}
		}

		return context;
	}

	/**
	 * Runs all given {@link IProjectBuilder} in the order as they are given in the set.
	 */
	private void runBuilder(final ProjectBuilderDefinition builderDefinition, final Set<IResource> affectedResources,
			final int kind, IProgressMonitor monitor, final List<IProjectContributionEventListener> listeners) {
		
		for (final IProjectContributionEventListener listener : listeners) {

			execute(new SafeExecutableWithMonitor() {

				@SuppressWarnings("deprecation")
				public void execute(IProgressMonitor subMonitor) throws Exception {
					listener.startContributor(builderDefinition.getProjectBuilder(), affectedResources, subMonitor);
					listener.startProjectBuilder(builderDefinition, affectedResources, subMonitor);
				}
			}, monitor);

		}

		execute(new SafeExecutableWithMonitor() {

			public void execute(IProgressMonitor subMonitor) throws Exception {
				builderDefinition.getProjectBuilder().build(affectedResources, kind, subMonitor);
			}
		}, monitor);

		for (final IProjectContributionEventListener listener : listeners) {
			
			execute(new SafeExecutableWithMonitor() {
				
				@SuppressWarnings("deprecation")
				public void execute(IProgressMonitor subMonitor) throws Exception {
					listener.finishContributor(builderDefinition.getProjectBuilder(), affectedResources, subMonitor);
					listener.finishProjectBuilder(builderDefinition, affectedResources, subMonitor);
				}
			}, monitor);
			
		}
	}

	/**
	 * Runs all given {@link IValidator} in the order as they are given in the set.
	 */
	private void runValidator(final ValidatorDefinition validatorDefinition, final Set<IResource> affectedResources,
			final int kind, IProgressMonitor monitor, List<IProjectContributionEventListener> listeners) {

		for (final IProjectContributionEventListener listener : listeners) {

			execute(new SafeExecutableWithMonitor() {

				@SuppressWarnings("deprecation")
				public void execute(IProgressMonitor subMonitor) throws Exception {
					listener.startContributor(validatorDefinition.getValidator(), affectedResources, subMonitor);
					listener.startValidator(validatorDefinition, affectedResources, subMonitor);
				}
			}, monitor);

		}

		execute(new SafeExecutableWithMonitor() {

			public void execute(IProgressMonitor subMonitor) throws Exception {
				validatorDefinition.getValidator().validate(affectedResources, kind, subMonitor);
			}
		}, monitor);

		for (final IProjectContributionEventListener listener : listeners) {
			
			execute(new SafeExecutableWithMonitor() {
				
				@SuppressWarnings("deprecation")
				public void execute(IProgressMonitor subMonitor) throws Exception {
					listener.finishContributor(validatorDefinition.getValidator(), affectedResources, subMonitor);
					listener.finishValidator(validatorDefinition, affectedResources, subMonitor);
				}
			}, monitor);
			
		}
	}

	protected IProgressMonitor createProgressMonitor(IProgressMonitor monitor) {
		return new SubProgressMonitor(monitor, 1);
	}

	protected void execute(final SafeExecutableWithMonitor executable, IProgressMonitor monitor) {
		final IProgressMonitor subMonitor = createProgressMonitor(monitor);

		ISafeRunnable code = new ISafeRunnable() {
			public void handleException(Throwable e) {
				// nothing to do - exception is already logged
			}

			public void run() throws Exception {
				executable.execute(subMonitor);
			}
		};
		SafeRunner.run(code);

		subMonitor.done();
	}

	protected interface SafeExecutableWithMonitor {

		void execute(IProgressMonitor monitor) throws Exception;

	}

	/**
	 * Create a list of affected resources from a resource delta.
	 */
	public static class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		private IProjectContributor contributor;

		private int kind = -1;

		private Set<IResource> resources;

		public ResourceDeltaVisitor(IProjectContributor builder, int kind) {
			this.contributor = builder;
			this.resources = new LinkedHashSet<IResource>();
			this.kind = kind;
		}

		public Set<IResource> getResources() {
			return resources;
		}

		public boolean visit(IResourceDelta aDelta) throws CoreException {
			boolean visitChildren = false;

			IResource resource = aDelta.getResource();
			if (resource instanceof IProject) {

				// Only check projects with Spring beans nature
				visitChildren = SpringCoreUtils.isSpringProject(resource);
				if (visitChildren) {
					resources.addAll(contributor.getAffectedResources(resource, kind, aDelta.getKind()));
				}
			}
			else if (resource instanceof IFolder) {
				resources.addAll(contributor.getAffectedResources(resource, kind, aDelta.getKind()));
				visitChildren = true;
			}
			else if (resource instanceof IFile) {
				switch (aDelta.getKind()) {
				case IResourceDelta.ADDED:
				case IResourceDelta.CHANGED:
					resources.addAll(contributor.getAffectedResources(resource, kind, aDelta.getKind()));
					visitChildren = true;
					break;

				case IResourceDelta.REMOVED:
					resources.addAll(contributor.getAffectedResources(resource, kind, aDelta.getKind()));
					break;
				}
			}
			return visitChildren;
		}
	}

	/**
	 * Create a list of affected resources from a resource tree.
	 */
	public static class ResourceTreeVisitor implements IResourceVisitor {

		private IProjectContributor contributor;

		private Set<IResource> resources;

		public ResourceTreeVisitor(IProjectContributor builder) {
			this.contributor = builder;
			this.resources = new LinkedHashSet<IResource>();
		}

		public Set<IResource> getResources() {
			return resources;
		}

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				resources.addAll(contributor.getAffectedResources(resource, IncrementalProjectBuilder.FULL_BUILD,
						IResourceDelta.CHANGED));
			}
			else if (resource instanceof IProject) {
				resources.addAll(contributor.getAffectedResources(resource, IncrementalProjectBuilder.FULL_BUILD,
						IResourceDelta.CHANGED));
			}
			return true;
		}
	}
}
