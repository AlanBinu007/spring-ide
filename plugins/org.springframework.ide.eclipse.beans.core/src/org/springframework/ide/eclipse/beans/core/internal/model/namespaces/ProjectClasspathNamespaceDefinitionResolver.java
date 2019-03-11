/*******************************************************************************
 * Copyright (c) 2010, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.namespaces;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionListener;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionResolver;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * {@link INamespaceDefinitionResolver} that resolves {@link INamespaceDefinition}s from the project's classpath as
 * well.
 * <p>
 * Note: this implementation is currently not in use
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.3.1
 * @see BeansCorePlugin#getNamespaceDefinitionResolver(IProject)
 */
@SuppressWarnings("deprecation")
public class ProjectClasspathNamespaceDefinitionResolver implements INamespaceDefinitionResolver {

	public static final String DEFAULT_HANDLER_MAPPINGS_LOCATION = "META-INF/spring.handlers";

	public static final String DEFAULT_SCHEMA_MAPPINGS_LOCATION = "META-INF/spring.schemas";

	public static final String DEFAULT_TOOLING_MAPPINGS_LOCATION = "META-INF/spring.tooling";

	private Map<String, NamespaceDefinition> namespaceDefinitionRegistry = null;

	private final IProject project;

	private EnablementPropertyChangeListener propertyChangeListener;

	private final INamespaceDefinitionResolver resolver;

	public ProjectClasspathNamespaceDefinitionResolver(IProject project) {
		this.project = project;
		this.resolver = BeansCorePlugin.getNamespaceDefinitionResolver();

		init();

		registerListeners();
	}

	/**
	 * Dispose this resolver instance.
	 */
	public void dispose() {
		unRegisterListeners();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<INamespaceDefinition> getNamespaceDefinitions() {
		return new HashSet<INamespaceDefinition>(namespaceDefinitionRegistry.values());
	}

	/**
	 * {@inheritDoc}
	 */
	public INamespaceDefinition resolveNamespaceDefinition(String namespaceUri) {
		return namespaceDefinitionRegistry.get(namespaceUri);
	}

	/**
	 * Load all {@link NamespaceDefinition}s from the project classpath. Also handle extraction of icon file.
	 */
	private void init() {

		// long start = System.currentTimeMillis();

		this.namespaceDefinitionRegistry = new ConcurrentHashMap<String, NamespaceDefinition>();

		// Add in namespace definitions from the classpath
		if (NamespaceUtils.useNamespacesFromClasspath(project)) {

			ClassLoader cls = JdtUtils.getClassLoader(project, null);

			Map<String, String> handlerMappings = new HashMap<String, String>();
			Map<String, String> toolingMappings = new HashMap<String, String>();
			Properties schemaMappings = new Properties();

			try {
				Properties mappings = PropertiesLoaderUtils.loadAllProperties(DEFAULT_HANDLER_MAPPINGS_LOCATION, cls);
				CollectionUtils.mergePropertiesIntoMap(mappings, handlerMappings);
				schemaMappings = PropertiesLoaderUtils.loadAllProperties(DEFAULT_SCHEMA_MAPPINGS_LOCATION, cls);
				mappings = PropertiesLoaderUtils.loadAllProperties(DEFAULT_TOOLING_MAPPINGS_LOCATION, cls);
				CollectionUtils.mergePropertiesIntoMap(mappings, toolingMappings);
			}
			catch (IOException e) {
			}

			for (Object xsd : schemaMappings.keySet()) {
				String key = xsd.toString();

				String schemaUri = schemaMappings.getProperty(key);
				URL url = cls.getResource(schemaUri);

				// fallback, if schema location starts with / and therefore fails to be found by classloader
				if (url == null && schemaUri.startsWith("/")) {
					schemaUri = schemaUri.substring(1);
					url = cls.getResource(schemaUri);
				}
				
				if (url == null) {
					continue;
				}

				String namespaceUri = TargetNamespaceScanner.getTargetNamespace(url);
				
				if (StringUtils.hasText(namespaceUri)) {
				
					String icon = toolingMappings.get(namespaceUri + "@icon");
					String prefix = toolingMappings.get(namespaceUri + "@prefix");
					String name = toolingMappings.get(namespaceUri + "@name");

					if (namespaceDefinitionRegistry.containsKey(namespaceUri)) {
						namespaceDefinitionRegistry.get(namespaceUri).addSchemaLocation(key);
						namespaceDefinitionRegistry.get(namespaceUri).addUri(schemaUri);
					}
					else {
						File iconFile = extractIcon(namespaceUri, icon, cls);

						NamespaceDefinition namespaceDefinition = new ExternalImageNamespaceDefinition(schemaMappings,
								iconFile);
						namespaceDefinition.setName(name);
						namespaceDefinition.setPrefix(prefix);
						namespaceDefinition.setIconPath(icon);
						namespaceDefinition.addSchemaLocation(key);
						namespaceDefinition.setNamespaceUri(namespaceUri);
						namespaceDefinition.addUri(schemaUri);

						namespaceDefinitionRegistry.put(namespaceUri, namespaceDefinition);
					}
				}
			}
		}
		else {
			for (INamespaceDefinition namespaceDefinition : resolver.getNamespaceDefinitions()) {
				namespaceDefinitionRegistry.put(namespaceDefinition.getNamespaceUri(),
						(NamespaceDefinition) namespaceDefinition);
			}
		}
		// System.out.println(String.format("-- loading of namespace definitions took '%s'ms",
		// (System.currentTimeMillis() - start)));
	}

	/**
	 * Extract icon files from the given classloader and store it on the filesystem for later use.
	 */
	private File extractIcon(String namespaceUri, String icon, ClassLoader cls) {
		if (StringUtils.hasLength(icon)) {
			try {
				File iconDir = BeansCorePlugin.getDefault().getStateLocation().append("images").toFile();
				if (!iconDir.exists()) {
					iconDir.mkdirs();
					iconDir.deleteOnExit();
				}

				int ix = icon.lastIndexOf('.');
				File iconFile = null;
				if (ix > 0) {
					iconFile = new File(iconDir, Integer.toString(namespaceUri.hashCode()) + icon.substring(ix));
				}
				else {
					iconFile = new File(iconDir, Integer.toString(namespaceUri.hashCode()));
				}

				if (iconFile.exists()) {
					return iconFile;
				}

				FileCopyUtils.copy(cls.getResourceAsStream(icon), new FileOutputStream(iconFile));
				return iconFile;
			}
			catch (Exception e) {
				BeansCorePlugin.log(
						String.format("Error extracting icon file '%s' for namespace '%s'", icon, namespaceUri), e);
				return null;
			}
		}
		return null;
	}

	/**
	 * Register the {@link EnablementPropertyChangeListener} listener to get notifications on changes to the project
	 * preferences.
	 */
	private void registerListeners() {
		propertyChangeListener = new EnablementPropertyChangeListener();
		BeansCorePlugin.getDefault().getPluginPreferences().addPropertyChangeListener(propertyChangeListener);
		SpringCorePreferences.getProjectPreferences(project, BeansCorePlugin.PLUGIN_ID).getProjectPreferences()
				.addPreferenceChangeListener(propertyChangeListener);
		BeansCorePlugin.registerNamespaceDefinitionListener(propertyChangeListener);
	}

	/**
	 * Unregister the {@link EnablementPropertyChangeListener}.
	 */
	private void unRegisterListeners() {
		BeansCorePlugin.getDefault().getPluginPreferences().removePropertyChangeListener(propertyChangeListener);
		SpringCorePreferences.getProjectPreferences(project, BeansCorePlugin.PLUGIN_ID).getProjectPreferences()
				.removePreferenceChangeListener(propertyChangeListener);
		BeansCorePlugin.unregisterNamespaceDefinitionListener(propertyChangeListener);
	}

	/**
	 * {@link IPropertyChangeListener} and {@link IPreferenceChangeListener} implementation that detects changes to the
	 * classpath property.
	 * @since 2.5.0
	 */
	private class EnablementPropertyChangeListener implements IPropertyChangeListener, IPreferenceChangeListener,
			INamespaceDefinitionListener {

		private static final String KEY = BeansCorePlugin.PLUGIN_ID + "."
				+ BeansCorePlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID;

		private final String nodePath = "/project/" + project.getName() + "/" + BeansCorePlugin.PLUGIN_ID;

		/**
		 * {@inheritDoc}
		 */
		public void preferenceChange(PreferenceChangeEvent event) {
			if (KEY.equals(event.getKey()) && nodePath.equals(event.getNode().absolutePath())) {
				init();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void propertyChange(org.eclipse.core.runtime.Preferences.PropertyChangeEvent event) {
			if (BeansCorePlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID.equals(event.getProperty())) {
				init();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void onNamespaceDefinitionRegistered(NamespaceDefinitionChangeEvent event) {
			if (!NamespaceUtils.useNamespacesFromClasspath(project)) {
				init();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void onNamespaceDefinitionUnregistered(NamespaceDefinitionChangeEvent event) {
			if (!NamespaceUtils.useNamespacesFromClasspath(project)) {
				init();
			}
		}
	}

	/**
	 * Extension to {@link NamespaceDefinition} that handles loading of referenced namespace icons from extracted files.
	 * @since 2.5.0
	 */
	private class ExternalImageNamespaceDefinition extends NamespaceDefinition {

		private final File iconFile;

		public ExternalImageNamespaceDefinition(Properties uriMapping, File iconFile) {
			super(uriMapping);
			this.iconFile = iconFile;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public InputStream getIconStream() {
			if (iconFile != null && iconFile.exists() && iconFile.canRead()) {
				try {
					return new FileInputStream(iconFile);
				}
				catch (FileNotFoundException e) {
					return null;
				}
			}
			return null;
		}
	}

}
