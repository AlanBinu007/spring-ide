/*******************************************************************************
 * Copyright (c) 2004, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.NamespaceManager;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.ProjectClasspathNamespaceDefinitionResolverCache;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionListener;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionResolver;
import org.springframework.ide.eclipse.core.MessageUtils;

/**
 * Central access point for the Spring Framework Core plug-in (id
 * <code>"org.springframework.ide.eclipse.beans.core"</code>).
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @author Martin Lippert
 */
public class BeansCorePlugin extends AbstractUIPlugin {

	/**
	 * Plugin identifier for Spring Beans Core (value <code>org.springframework.ide.eclipse.beans.core</code>).
	 */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.beans.core";

	private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	/** preference key to suppress missing namespace handler warnings */
	public static final String IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY = "ignoreMissingNamespaceHandler";

	public static final boolean IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY_DEFAULT = false;

	/** preference key to load namespace handler from classpath */
	public static final String LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID = "loadNamespaceHandlerFromClasspath";
	
	/** preference key to load namespace handler by searching source folders */
	public static final String DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID = "disableCachingForNamespaceLoadingFromClasspath";

	/** preference key for defining the parsing timeout */
	public static final String TIMEOUT_CONFIG_LOADING_PREFERENCE_ID = PLUGIN_ID + ".timeoutConfigLoading";

	/** preference key to enable namespace versions per namespace */
	public static final String PROJECT_PROPERTY_ID = "enable.project.preferences";

	/** preference key to specify the default namespace version */
	public static final String NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID = "default.version.";

	/** preference key to specify the default namespace version */
	public static final String NAMESPACE_PREFIX_PREFERENCE_ID = "prefix.";

	/** preference key to specify if versions should be taken from the classpath */
	public static final String NAMESPACE_DEFAULT_FROM_CLASSPATH_ID = "default.version.check.classpath";

	/** The shared instance */
	private static BeansCorePlugin plugin;

	/** The singleton beans model */
	private BeansModel model;

	/** Spring namespace/resolver manager */
	private NamespaceManager nsManager;

	private NamespaceBundleLister nsListener;
	
	private ServiceRegistration<?> projectAwareUrlService = null;

	/** Internal executor service */
	private ExecutorService executorService;
	private AtomicInteger threadCount = new AtomicInteger(0);
	private static final String THREAD_NAME_TEMPLATE = "Background Thread-%s (%s/%s.%s.%s)";

	/**
	 * Preference ID to globally disable any beans auto detection scanning.
	 */
	public static final String DISABLE_AUTO_DETECTION = BeansCorePlugin.class.getName()+".DISABLE_AUTO_DETECTION";

	/** Resource bundle */
	private ResourceBundle resourceBundle;

	/** Listeners to inform about namespace changes */
	private volatile Set<INamespaceDefinitionListener> namespaceDefinitionListeners = Collections
			.synchronizedSet(new HashSet<INamespaceDefinitionListener>());

	/**
	 * flag indicating whether the context is down or not - useful during shutdown
	 */
	private volatile boolean isClosed = false;

	/**
	 * Monitor used for dealing with the bundle activator and synchronous bundle threads
	 */
	private transient final Object monitor = new Object();

	/**
	 * Creates the Spring Beans Core plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform. Clients must not call.
	 */
	public BeansCorePlugin() {
		plugin = this;
		model = new BeansModel();
		
		try {
			resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
		}
		catch (MissingResourceException e) {
			resourceBundle = null;
		}
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		
		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put(URLConstants.URL_HANDLER_PROTOCOL,
				ProjectAwareUrlStreamHandlerService.PROJECT_AWARE_PROTOCOL);
		projectAwareUrlService = context.registerService(
				URLStreamHandlerService.class.getName(),
				new ProjectAwareUrlStreamHandlerService(), properties);
		
		executorService = Executors.newCachedThreadPool(new ThreadFactory() {
			
			public Thread newThread(Runnable runnable) {
				Version version = Version.parseVersion(getPluginVersion());
				String productId = "Spring IDE";
				IProduct product = Platform.getProduct();
				if (product != null && "com.springsource.sts".equals(product.getId()))
						productId = "STS";
				Thread reportingThread = new Thread(runnable, String.format(THREAD_NAME_TEMPLATE, threadCount.incrementAndGet(), 
						productId, version.getMajor(), version.getMinor(), version.getMicro()));
				reportingThread.setDaemon(true);
				return reportingThread;
			}
		});

		
		nsManager = new NamespaceManager(context);
		getPreferenceStore().setDefault(TIMEOUT_CONFIG_LOADING_PREFERENCE_ID, 60);
		getPreferenceStore().setDefault(NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, true);
		getPreferenceStore().setDefault(LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, true);

		Job modelJob = new Job("Initializing Spring Tooling") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				initNamespaceHandlers(context);
				model.start();
				return Status.OK_STATUS;
			}
		};
		modelJob.setRule(MultiRule.combine(ResourcesPlugin.getWorkspace().getRoot(), BeansCoreUtils.BEANS_MODEL_INIT_RULE));
		// modelJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		// modelJob.setSystem(true);
		modelJob.setPriority(Job.DECORATE);
		modelJob.schedule();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		synchronized (monitor) {
			// if already closed, bail out
			if (isClosed) {
				return;
			}
			isClosed = true;
		}
		model.stop();
		if (projectAwareUrlService != null) {
			projectAwareUrlService.unregister();
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static BeansCorePlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns the singleton {@link IBeansModel}.
	 */
	public static IBeansModel getModel() {
		return getDefault().model;
	}

	/**
	 * only for internal testing purposes
	 */
	public static void setModel(BeansModel model) {
		getDefault().model = model;
	}

	public static NamespaceHandlerResolver getNamespaceHandlerResolver() {
		return getDefault().nsManager.getNamespacePlugins();
	}

	public static INamespaceDefinitionResolver getNamespaceDefinitionResolver() {
		return getDefault().nsManager.getNamespacePlugins();
	}

	public static INamespaceDefinitionResolver getNamespaceDefinitionResolver(IProject project) {
		if (project != null) {
			return ProjectClasspathNamespaceDefinitionResolverCache.getResolver(project);
		}
		return getDefault().nsManager.getNamespacePlugins();
	}

	public static ExecutorService getExecutorService() {
		return getDefault().executorService;
	}

	public static void notifyNamespaceDefinitionListeners(IProject project) {
		for (INamespaceDefinitionListener listener : getDefault().namespaceDefinitionListeners) {
			listener.onNamespaceDefinitionRegistered(new INamespaceDefinitionListener.NamespaceDefinitionChangeEvent(
					null, project));
		}
	}

	public static void registerNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		getDefault().nsManager.getNamespacePlugins().registerNamespaceDefinitionListener(listener);
		getDefault().namespaceDefinitionListeners.add(listener);
	}

	public static void unregisterNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		getDefault().nsManager.getNamespacePlugins().unregisterNamespaceDefinitionListener(listener);
		getDefault().namespaceDefinitionListeners.remove(listener);
	}

	/**
	 * Returns the {@link IWorkspace} instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		String bundleString;
		ResourceBundle bundle = getDefault().getResourceBundle();
		if (bundle != null) {
			try {
				bundleString = bundle.getString(key);
			}
			catch (MissingResourceException e) {
				log(e);
				bundleString = "!" + key + "!";
			}
		}
		else {
			bundleString = "!" + key + "!";
		}
		return bundleString;
	}

	public static ClassLoader getClassLoader() {
		return BeansCorePlugin.class.getClassLoader();
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
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
		getDefault().getLog().log(createErrorStatus(getResourceString("Plugin.internal_error"), exception));
	}

	/**
	 * Returns a new {@link IStatus} with status "ERROR" for this plug-in.
	 */
	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static String getFormattedMessage(String key, Object... args) {
		return MessageUtils.format(getResourceString(key), args);
	}

	public static String getPluginVersion() {
		Bundle bundle = getDefault().getBundle();
		return bundle.getHeaders().get(Constants.BUNDLE_VERSION);
	}

	protected void maybeAddNamespaceHandlerFor(Bundle bundle, boolean isLazy) {
		nsManager.maybeAddNamespaceHandlerFor(bundle, isLazy);
	}

	protected void maybeRemoveNameSpaceHandlerFor(Bundle bundle) {
		nsManager.maybeRemoveNameSpaceHandlerFor(bundle);
	}

	protected void initNamespaceHandlers(BundleContext context) {

		// register listener first to make sure any bundles in INSTALLED state
		// are not lost
		nsListener = new NamespaceBundleLister();
		context.addBundleListener(nsListener);

		Bundle[] previousBundles = context.getBundles();

		for (int i = 0; i < previousBundles.length; i++) {
			Bundle bundle = previousBundles[i];
			if (isBundleResolved(bundle)) {
				nsManager.maybeAddNamespaceHandlerFor(bundle, false);
			}
			else if (isBundleLazyActivated(bundle)) {
				nsManager.maybeAddNamespaceHandlerFor(bundle, true);
			}
		}

		// discovery finished, publish the resolvers/parsers in the OSGi space
		nsManager.afterPropertiesSet();
	}

	public boolean isBundleResolved(Bundle bundle) {
		return (bundle.getState() >= Bundle.RESOLVED);
	}

	public boolean isBundleLazyActivated(Bundle bundle) {
		if (bundle.getState() == Bundle.STARTING) {
			Dictionary<String, String> headers = bundle.getHeaders();
			if (headers != null && headers.get(Constants.BUNDLE_ACTIVATIONPOLICY) != null) {
				String value = headers.get(Constants.BUNDLE_ACTIVATIONPOLICY).trim();
				return (value.startsWith(Constants.ACTIVATION_LAZY));
			}
		}
		return false;
	}

	/**
	 * Common base class for {@link ContextLoaderListener} listeners.
	 */
	private abstract class BaseListener implements BundleListener {

		/**
		 * common cache used for tracking down bundles started lazily so they don't get processed twice (once when
		 * started lazy, once when started fully)
		 */
		protected Map<Bundle, Object> lazyBundleCache = new WeakHashMap<Bundle, Object>();

		/** dummy value for the bundle cache */
		private final Object VALUE = new Object();

		// caches the bundle
		protected void push(Bundle bundle) {
			synchronized (lazyBundleCache) {
				lazyBundleCache.put(bundle, VALUE);
			}
		}

		// checks the presence of the bundle as well as removing it
		protected boolean pop(Bundle bundle) {
			synchronized (lazyBundleCache) {
				return (lazyBundleCache.remove(bundle) != null);
			}
		}

		/**
		 * A bundle has been started, stopped, resolved, or unresolved. This method is a synchronous callback, do not do
		 * any long-running work in this thread.
		 * 
		 * @see org.osgi.framework.SynchronousBundleListener#bundleChanged
		 */
		public void bundleChanged(BundleEvent event) {

			// check if the listener is still alive
			if (isClosed) {
				return;
			}
			try {
				handleEvent(event);
			}
			catch (Exception ex) {
				log(ex);
			}
		}

		protected abstract void handleEvent(BundleEvent event);
	}

	/**
	 * Bundle listener used for detecting namespace handler/resolvers. Exists as a separate listener so that it can be
	 * registered early to avoid race conditions with bundles in INSTALLING state but still to avoid premature context
	 * creation before the Spring {@link ContextLoaderListener} is not fully initialized.
	 */
	private class NamespaceBundleLister extends BaseListener {

		@Override
		protected void handleEvent(final BundleEvent event) {
			Bundle bundle = event.getBundle();

			switch (event.getType()) {
			case BundleEvent.LAZY_ACTIVATION: {
				push(bundle);
				maybeAddNamespaceHandlerFor(bundle, true);
				break;
			}
			case BundleEvent.RESOLVED: {
				if (!pop(bundle)) {
					maybeAddNamespaceHandlerFor(bundle, false);
				}
				break;
			}
			case BundleEvent.STOPPED: {
				pop(bundle);
				maybeRemoveNameSpaceHandlerFor(bundle);
				break;
			}
			default:
				break;
			}
		}
	}

	public boolean isAutoDetectionEnabled() {
		return !getPreferenceStore().getBoolean(DISABLE_AUTO_DETECTION);
	}
}
