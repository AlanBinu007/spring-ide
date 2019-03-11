/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAnnotationAopDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Christian Dupuis
 */
public class AopReferenceModelMarkerUtils {

	private static final String SOURCE_RESOURCE = "source_resource";

	private static final String MARKER_COUNT = "marker_count";

	public static final String AOP_MARKER = Activator.PLUGIN_ID + ".marker";

	public static final String AOP_PROBLEM_MARKER = Activator.PLUGIN_ID + ".problemmarker";

	public static final String BEFORE_ADVICE_MARKER = Activator.PLUGIN_ID + ".beforeadvicemarker";

	public static final String AFTER_ADVICE_MARKER = Activator.PLUGIN_ID + ".afteradvicemarker";

	public static final String AROUND_ADVICE_MARKER = Activator.PLUGIN_ID + ".aroundadvicemarker";

	public static final String INTRODUCTION_MARKER = Activator.PLUGIN_ID + ".introductionmarker";

	public static final String SOURCE_BEFORE_ADVICE_MARKER = Activator.PLUGIN_ID + ".sourcebeforeadvicemarker";

	public static final String SOURCE_AFTER_ADVICE_MARKER = Activator.PLUGIN_ID + ".sourceafteradvicemarker";

	public static final String SOURCE_AROUND_ADVICE_MARKER = Activator.PLUGIN_ID + ".sourcearoundadvicemarker";

	public static final String SOURCE_INTRODUCTION_MARKER = Activator.PLUGIN_ID + ".sourceintroductionmarker";

	public static Map<ADVICE_TYPE, String> sourceMarkerMapping;

	public static Map<ADVICE_TYPE, String> targetMarkerMapping;
	
	private static int AOP_MARKER_SEVERITY = IMarker.SEVERITY_INFO;

	static {
		sourceMarkerMapping = new HashMap<ADVICE_TYPE, String>();
		sourceMarkerMapping.put(ADVICE_TYPE.BEFORE, SOURCE_BEFORE_ADVICE_MARKER);
		sourceMarkerMapping.put(ADVICE_TYPE.AFTER, SOURCE_AFTER_ADVICE_MARKER);
		sourceMarkerMapping.put(ADVICE_TYPE.AFTER_RETURNING, SOURCE_AFTER_ADVICE_MARKER);
		sourceMarkerMapping.put(ADVICE_TYPE.AFTER_THROWING, SOURCE_AFTER_ADVICE_MARKER);
		sourceMarkerMapping.put(ADVICE_TYPE.AROUND, SOURCE_AROUND_ADVICE_MARKER);
		sourceMarkerMapping.put(ADVICE_TYPE.DECLARE_PARENTS, SOURCE_INTRODUCTION_MARKER);

		targetMarkerMapping = new HashMap<ADVICE_TYPE, String>();
		targetMarkerMapping.put(ADVICE_TYPE.BEFORE, BEFORE_ADVICE_MARKER);
		targetMarkerMapping.put(ADVICE_TYPE.AFTER, AFTER_ADVICE_MARKER);
		targetMarkerMapping.put(ADVICE_TYPE.AFTER_RETURNING, AFTER_ADVICE_MARKER);
		targetMarkerMapping.put(ADVICE_TYPE.AFTER_THROWING, AFTER_ADVICE_MARKER);
		targetMarkerMapping.put(ADVICE_TYPE.AROUND, AROUND_ADVICE_MARKER);
		targetMarkerMapping.put(ADVICE_TYPE.DECLARE_PARENTS, INTRODUCTION_MARKER);
	}

	public static void createMarker(IAopReference reference, IResource sourceResource) {
		createSourceMarker(reference, sourceMarkerMapping.get(reference.getAdviceType()), sourceResource);
		createTargetMarker(reference, targetMarkerMapping.get(reference.getAdviceType()), sourceResource);
	}

	public static void createTargetMarker(IAopReference reference, String markerId, IResource sourceResource) {
		if (reference.getAdviceType() == ADVICE_TYPE.DECLARE_PARENTS) {
			createProblemMarker(reference.getTarget().getResource(), "aspect declarations <"
					+ reference.getDefinition().getAspectName() + ">", AOP_MARKER_SEVERITY, reference.getTargetStartLine(), markerId,
					sourceResource);
			if (reference.getTargetBeanResource() != null && reference.getTargetBeanStartline() > 0) {
				createProblemMarker(reference.getTargetBeanResource(), "aspect declarations <"
						+ reference.getDefinition().getAspectName() + ">", AOP_MARKER_SEVERITY, reference.getTargetBeanStartline(),
						markerId, sourceResource);
			}
		}
		else {
			createProblemMarker(reference.getTarget().getResource(), "advised by "
					+ AopReferenceModelUtils.getJavaElementLinkNameForMarker(reference.getSource()), AOP_MARKER_SEVERITY, reference
					.getTargetStartLine(), markerId, sourceResource);
			if (reference.getTargetBeanResource() != null && reference.getTargetBeanStartline() > 0) {
				createProblemMarker(reference.getTargetBeanResource(), "advised by "
						+ AopReferenceModelUtils.getJavaElementLinkNameForMarker(reference.getSource()), AOP_MARKER_SEVERITY, reference
						.getTargetBeanStartline(), markerId, sourceResource);
			}
		}
	}

	public static void createSourceMarker(IAopReference reference, String markerId, IResource sourceResource) {
		// if (reference.getDefinition().getAspectLineNumber() > 0
		// && !(reference.getDefinition() instanceof JavaAspectDefinition)) {
		// }
		if (reference.getAdviceType() == ADVICE_TYPE.DECLARE_PARENTS) {
			if (reference.getDefinition() instanceof IAnnotationAopDefinition) {
				createProblemMarker(reference.getSource().getResource(), "declared on "
						+ AopReferenceModelUtils.getJavaElementLinkNameForMarker(reference.getTarget()), AOP_MARKER_SEVERITY, reference
						.getSourceStartLine(), markerId, sourceResource);
			}
			else {
				createProblemMarker(sourceResource, "declared on "
						+ AopReferenceModelUtils.getJavaElementLinkNameForMarker(reference.getTarget()), AOP_MARKER_SEVERITY, reference
						.getDefinition().getAspectStartLineNumber(), markerId, sourceResource);
			}
		}
		else {
			if (reference.getSource() != null) {
				createProblemMarker(reference.getSource().getResource(), "advises "
						+ AopReferenceModelUtils.getJavaElementLinkNameForMarker(reference.getTarget()), AOP_MARKER_SEVERITY, reference
						.getSourceStartLine()	, markerId, sourceResource);
			}
			createProblemMarker(reference.getDefinition().getResource(), "advises "
					+ AopReferenceModelUtils.getJavaElementLinkNameForMarker(reference.getTarget()), AOP_MARKER_SEVERITY, reference
					.getDefinition().getAspectStartLineNumber(), markerId, sourceResource);
		}
	}

	public static void deleteProblemMarkers(IResource resource) {
		if (resource != null && resource.isAccessible()) {
			try {
				resource.getProject().deleteMarkers(AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

				IProject project = resource.getProject();
				String resourceName = resource.getFullPath().toString();
				IMarker[] markers = project.findMarkers(AopReferenceModelMarkerUtils.AOP_MARKER, true,
						IResource.DEPTH_INFINITE);
				for (IMarker marker : markers) {
					String sourceResourceName = marker.getAttribute(SOURCE_RESOURCE, "");
					if (sourceResourceName != null && sourceResourceName.startsWith(resourceName)) {
						marker.delete();
					}
				}
			}
			catch (CoreException e) {
				SpringCore.log(e);
			}
		}

		// delete markers on depending projects
		IJavaProject jp = JdtUtils.getJavaProject(resource);
		if (jp != null) {
			List<IJavaProject> jps = JdtUtils.getAllDependingJavaProjects(jp);
			for (IJavaProject p : jps) {
				try {
					IProject project = p.getProject();
					String resourceName = resource.getFullPath().toString();
					IMarker[] markers = project.findMarkers(AopReferenceModelMarkerUtils.AOP_MARKER, true,
							IResource.DEPTH_INFINITE);
					for (IMarker marker : markers) {
						String sourceResourceName = marker.getAttribute(SOURCE_RESOURCE, "");
						if (resourceName.equals(sourceResourceName)) {
							marker.delete();
						}
					}
				}
				catch (CoreException e) {
					SpringCore.log(e);
				}
			}
		}
	}

	public static void createProblemMarker(IResource resource, String message, int severity, int line, String markerId,
			IResource sourceResource) {
		createProblemMarker(resource, message, severity, line, markerId, 1, sourceResource);
	}

	public static void createProblemMarker(IResource resource, String message, int severity, int line, String markerId,
			int markerCount, IResource sourceResource) {
		if (resource != null && resource.isAccessible()) {
			try {
				// First check if specified marker already exists

				if (severity == IMarker.SEVERITY_ERROR) {
					IMarker[] markers = resource.findMarkers(AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, true,
							IResource.DEPTH_ZERO);
					for (IMarker marker : markers) {
						int l = marker.getAttribute(IMarker.LINE_NUMBER, -1);
						String msg = marker.getAttribute(IMarker.MESSAGE, "");
						if (l == line && message.equals(msg)) {
							return;
						}
					}
				}

				IMarker[] markers = resource.findMarkers(AopReferenceModelMarkerUtils.AOP_MARKER, true,
						IResource.DEPTH_ZERO);
				for (IMarker marker : markers) {
					int l = marker.getAttribute(IMarker.LINE_NUMBER, -1);
					int count = marker.getAttribute(MARKER_COUNT, 1);
					String msg = marker.getAttribute(IMarker.MESSAGE, "");
					count++;
					if (l == line && message != null && message.equals(msg) && marker.getType() == markerId) {
						return;
					}
					if (l == line && marker.getType() != markerId) {
						resource.findMarker(marker.getId()).delete();
						createProblemMarker(resource, count + " Spring AOP marker at this line", AOP_MARKER_SEVERITY, line,
								AopReferenceModelMarkerUtils.AOP_MARKER, count, sourceResource);
						return;
					}
					else if (l == line && marker.getType() == markerId) {
						marker.setAttribute(IMarker.MESSAGE, count + " Spring AOP marker at this line");
						marker.setAttribute(MARKER_COUNT, count);
						return;
					}
				}

				// Create new marker
				IMarker marker = resource.createMarker(markerId);
				Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put(IMarker.MESSAGE, message);
				attributes.put(IMarker.SEVERITY, new Integer(severity));
				attributes.put(MARKER_COUNT, markerCount);
				if (sourceResource != null) {
					attributes.put(SOURCE_RESOURCE, sourceResource.getFullPath().toString());
				}
				if (line > 0) {
					attributes.put(IMarker.LINE_NUMBER, new Integer(line));
				}
				marker.setAttributes(attributes);
			}
			catch (CoreException e) {
				SpringCore.log(e);
			}
		}
	}

}
