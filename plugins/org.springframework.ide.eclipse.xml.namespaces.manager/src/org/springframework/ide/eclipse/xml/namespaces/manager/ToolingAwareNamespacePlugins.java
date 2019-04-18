/*******************************************************************************
 * Copyright (c) 2009, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.manager;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.xml.namespaces.SpringXmlNamespacesPlugin;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinition;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionListener;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionResolver;
import org.springframework.ide.eclipse.xml.namespaces.model.NamespaceDefinition;
import org.springframework.ide.eclipse.xml.namespaces.util.TargetNamespaceScanner;
import org.springsource.ide.eclipse.commons.frameworks.core.util.StringUtils;

/**
 * Extension to Spring DM's {@link NamespacePlugins} class that handles registering of XSDs in Eclipse' XML Catalog and
 * builds an internal representation of {@link INamespaceDefinition}s.
 * 
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.2.5
 */
public class ToolingAwareNamespacePlugins extends NamespacePlugins implements INamespaceDefinitionResolver {

	private static final String META_INF = "META-INF/";

	private static final String SPRING_SCHEMAS = "spring.schemas";

	private static final String SPRING_TOOLING = "spring.tooling";

	/** Namespace definitions keyed by the namespace uri */
	private volatile Map<String, NamespaceDefinition> namespaceDefinitionRegistry = new HashMap<String, NamespaceDefinition>();

	/** Namespace definition set keyed by the registering bundle */
	private volatile Map<Bundle, Set<NamespaceDefinition>> namespaceDefinitionsByBundle = new HashMap<Bundle, Set<NamespaceDefinition>>();

	/** Listeners to inform about namespace changes */
	private volatile Set<INamespaceDefinitionListener> namespaceDefinitionListeners = Collections
			.synchronizedSet(new HashSet<INamespaceDefinitionListener>());

	/**
	 * {@inheritDoc}
	 */
	@Override
	void addPlugin(Bundle bundle, boolean lazyBundle, boolean applyCondition) {
		super.addPlugin(bundle, lazyBundle, applyCondition);
		addNamespaceDefinition(bundle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean removePlugin(Bundle bundle) {
		removeNamespaceDefinition(bundle);
		return super.removePlugin(bundle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<INamespaceDefinition> getNamespaceDefinitions() {
		return new HashSet<INamespaceDefinition>(namespaceDefinitionRegistry.values());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public INamespaceDefinition resolveNamespaceDefinition(String namespaceUri) {
		return namespaceDefinitionRegistry.get(namespaceUri);
	}

	/**
	 * Register the XML namespace catalog and build the {@link INamespaceDefinition}s.
	 */
	private void addNamespaceDefinition(Bundle bundle) {

		Set<NamespaceDefinition> namespaceDefinitions = new HashSet<NamespaceDefinition>();
		this.namespaceDefinitionsByBundle.put(bundle, namespaceDefinitions);

		Enumeration<URL> schemas = bundle.findEntries(META_INF, SPRING_SCHEMAS, false);
		if (schemas != null) {
			while (schemas.hasMoreElements()) {
				Properties props = loadProperties(schemas.nextElement());

				for (Object xsd : props.keySet()) {

					String key = xsd.toString();
					String namespaceUri = TargetNamespaceScanner.getTargetNamespace(bundle.getEntry(props.getProperty(key)));
					String icon = null;
					String prefix = null;
					String name = null;

					if (StringUtils.hasText(namespaceUri)) {

						Enumeration<URL> tooling = bundle.findEntries(META_INF, SPRING_TOOLING, false);
						if (tooling != null) {
							while (tooling.hasMoreElements()) {
								Properties toolingProps = loadProperties(tooling.nextElement());

								icon = toolingProps.getProperty(namespaceUri + "@icon");
								prefix = toolingProps.getProperty(namespaceUri + "@prefix");
								name = toolingProps.getProperty(namespaceUri + "@name");
							}
						}

						if (namespaceDefinitionRegistry.containsKey(namespaceUri)) {
							namespaceDefinitionRegistry.get(namespaceUri).addSchemaLocation(key);
							namespaceDefinitionRegistry.get(namespaceUri).addUri(props.getProperty(key));
						}
						else {
							NamespaceDefinition namespaceDefinition = new NamespaceDefinition(props);
							namespaceDefinition.setName(name);
							namespaceDefinition.setPrefix(prefix);
							namespaceDefinition.setIconPath(icon);
							namespaceDefinition.addSchemaLocation(key);
							namespaceDefinition.setBundle(bundle);
							namespaceDefinition.setNamespaceUri(namespaceUri);
							namespaceDefinition.addUri(props.getProperty(key));
							registerNamespaceDefinition(namespaceUri, namespaceDefinition);
							namespaceDefinitions.add(namespaceDefinition);
						}
					}
				}
			}
		}
	}

	private void registerNamespaceDefinition(String uri, final NamespaceDefinition definition) {
		namespaceDefinitionRegistry.put(uri, definition);

		for (final INamespaceDefinitionListener listener : namespaceDefinitionListeners) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void run() throws Exception {
					listener.onNamespaceDefinitionRegistered(new INamespaceDefinitionListener.NamespaceDefinitionChangeEvent(
							definition, null));
				}

				@Override
				public void handleException(Throwable exception) {
					SpringXmlNamespacesPlugin.log(exception);
				}
			});
		}
	}

	/**
	 * Fail safe loading of a {@link Properties} from an {@link URL}. Returns an empty {@link Properties} if the an
	 * {@link IOException} occurs.
	 */
	private Properties loadProperties(URL url) {
		Properties toolingProps = new Properties();
		try {
			toolingProps.load(url.openStream());
		}
		catch (IOException e) {
			SpringXmlNamespacesPlugin.log(e);
		}
		return toolingProps;
	}

	/**
	 * Removes any registered {@link INamespaceDefinition}s and XML catalog entries.
	 */
	private void removeNamespaceDefinition(Bundle bundle) {
		if (namespaceDefinitionsByBundle.containsKey(bundle)) {
			for (NamespaceDefinition definition : namespaceDefinitionsByBundle.get(bundle)) {
				unregisterNamespaceDefinition(definition);
			}
			namespaceDefinitionsByBundle.remove(bundle);
		}
	}

	private void unregisterNamespaceDefinition(final NamespaceDefinition definition) {
		namespaceDefinitionRegistry.remove(definition.getNamespaceUri());

		for (final INamespaceDefinitionListener listener : namespaceDefinitionListeners) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void run() throws Exception {
					listener.onNamespaceDefinitionUnregistered(new INamespaceDefinitionListener.NamespaceDefinitionChangeEvent(
							definition, null));
				}

				@Override
				public void handleException(Throwable exception) {
					SpringXmlNamespacesPlugin.log(exception);
				}
			});
		}
	}

	public void unregisterNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		namespaceDefinitionListeners.remove(listener);
	}

	public void registerNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		if (!namespaceDefinitionListeners.contains(listener)) {
			namespaceDefinitionListeners.add(listener);
		}
	}
}