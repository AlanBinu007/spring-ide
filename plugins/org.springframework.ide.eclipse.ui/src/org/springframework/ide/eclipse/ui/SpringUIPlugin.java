/*******************************************************************************
 * Copyright (c) 2005, 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springsource.ide.eclipse.commons.ui.ImageDescriptorRegistry;

/**
 * The main plugin class to be used in the desktop.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class SpringUIPlugin extends AbstractUIPlugin {

	/**
	 * Plugin identifier for Spring IDE UI (value
	 * <code>org.springframework.ide.eclipse.ui</code>).
	 */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.ui";
	
	public static final String SORTING_ENABLED_KEY = PLUGIN_ID + ".sortingEnabled";

	/**
	 * Viewer identifier of the Spring Explorer (value
	 * <code>org.springframework.ide.eclipse.ui.navigator.springExplorer</code>).
	 */
	public static final String SPRING_EXPLORER_ID = PLUGIN_ID
			+ ".navigator.springExplorer";

	public static final String PROJECT_EXPLORER_CONTENT_PROVIDER_ID = PLUGIN_ID
			+ ".navigator.projectExplorerContent";

	public static final String SPRING_EXPLORER_CONTENT_PROVIDER_ID = PLUGIN_ID
			+ ".navigator.springExplorerContent";
	
	public static final String ECLEMMA_COVERAGE_ACTION_SET_PRE_ECLIPSE = "com.mountainminds.eclemma.ui.CoverageActionSet";
	public static final String ECLEMMA_UI_BUNDLE_ID_PRE_ECLIPSE = "com.mountainminds.eclemma.ui";

	public static final String ECLEMMA_COVERAGE_ACTION_SET_POST_ECLIPSE = "org.eclipse.eclemma.ui.CoverageActionSet";
	public static final String ECLEMMA_UI_BUNDLE_ID_POST_ECLIPSE = "org.eclipse.eclemma.ui";

	private static final String SPRING_PERSPECTIVE_ID = "com.springsource.sts.ide.perspective";
	
	/** The shared instance. */
	private static SpringUIPlugin plugin;

	private ResourceBundle resourceBundle;
	private ImageDescriptorRegistry imageDescriptorRegistry;
	private ILabelProvider labelProvider;

	/**
	 * Creates the Spring UI plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform.
	 * Clients must not call.
	 */
	public SpringUIPlugin() {
		plugin = this;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		SpringUIImages.initializeImageRegistry(registry);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (labelProvider != null) {
			labelProvider.dispose();
			labelProvider = null;
		}
		if (imageDescriptorRegistry != null) {
			imageDescriptorRegistry.dispose();
			imageDescriptorRegistry = null;
		}
		super.stop(context);
	}
	
	

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		addEclemmaActionSetToSpringPerspective();
	}
	
	/**
	 * Add Eclemma action set to Spring perspective is Eclemma UI plugin is vailable 
	 */
	private void addEclemmaActionSetToSpringPerspective() {
		if (Platform.getBundle(ECLEMMA_UI_BUNDLE_ID_POST_ECLIPSE) != null) {
			addEclemmaActionSetToSpringPerspective(ECLEMMA_COVERAGE_ACTION_SET_POST_ECLIPSE);
		}
		else if (Platform.getBundle(ECLEMMA_UI_BUNDLE_ID_PRE_ECLIPSE) != null) {
			addEclemmaActionSetToSpringPerspective(ECLEMMA_COVERAGE_ACTION_SET_PRE_ECLIPSE);
		}
	}

	/**
	 * Add Eclemma action set to Spring perspective is Eclemma UI plugin is vailable 
	 */
	private void addEclemmaActionSetToSpringPerspective(final String actionSetID) {
		getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				final IWorkbenchWindow activeWorkbenchWindow = getWorkbench().getActiveWorkbenchWindow();
				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
				if (SPRING_PERSPECTIVE_ID.equals(activePage.getPerspective().getId())) {
					activePage.showActionSet(actionSetID);
				} else {
					activeWorkbenchWindow.addPerspectiveListener(new PerspectiveAdapter() {
						@Override
						public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspectiveDescriptor) {
							super.perspectiveActivated(page, perspectiveDescriptor);
							if (SPRING_PERSPECTIVE_ID.equals(perspectiveDescriptor.getId())) {
								page.showActionSet(actionSetID);
								/*
								 * Yes, this listener can be removed while listener body is being executed :-)
								 */
								activeWorkbenchWindow.removePerspectiveListener(this);
							}
						}
					});
				}
			}
		});
	}

	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return getDefault().internalGetImageDescriptorRegistry();
	}

	private synchronized ImageDescriptorRegistry
			internalGetImageDescriptorRegistry() {
		if (imageDescriptorRegistry == null) {
			imageDescriptorRegistry = new ImageDescriptorRegistry();
		}
		return imageDescriptorRegistry;
	}

	/**
	 * Returns then singleton instance of
	 * <code>SpringUILabelProvider(true)</code>.
	 * <p>
	 * <b>For this instance the dispose method must never be called!! This is
	 * done by <code>Plugin.stop()</code> instead.</b>
	 */
	public static ILabelProvider getLabelProvider() {
		return getDefault().internalGetLabelProvider();
	}

	private synchronized ILabelProvider internalGetLabelProvider() {
		if (labelProvider == null) {
			labelProvider = new SpringUILabelProvider(true);
		}
		return labelProvider;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Returns the shared instance.
	 */
	public static SpringUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	public static IWorkbenchPage getActiveWorkbenchPage() {
		return getActiveWorkbenchWindow().getActivePage();
	}

	public static boolean isDebug(String option) {
		String value = Platform.getDebugOption(option);
		return (value != null && value.equalsIgnoreCase("true") ? true : false);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message the text to write to the log
	 */
	public static void log(String message, Throwable exception) {
		IStatus status = createErrorStatus(message, exception);
		getDefault().getLog().log(status);
	}
	
	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus(
							SpringUIMessages.Plugin_internalError, exception));
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus createErrorStatus(String message,
											Throwable exception) {
		if (message == null) {
			message= ""; 
		}		
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}
}
