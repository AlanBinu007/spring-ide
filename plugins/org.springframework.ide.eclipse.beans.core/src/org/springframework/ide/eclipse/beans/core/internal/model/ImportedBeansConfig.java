/*******************************************************************************
 * Copyright (c) 2008, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.DocumentDefaultsDefinition;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.core.io.ExternalFile;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;

/**
 * Imported Spring configuration file.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class ImportedBeansConfig extends AbstractBeansConfig implements IImportedBeansConfig {

	private IModelElement[] children = null;

	public ImportedBeansConfig(IBeansImport beansImport, Resource resource, Type type) {
		super(beansImport, resource.getFilename(), type);
		init(resource);
	}

	@Override
	public IModelElement[] getElementChildren() {
		return children;
	}

	@Override
	protected void readConfig() {
		if (!isModelPopulated) {
			try {
				w.lock();
				if (this.isModelPopulated) {
					return;
				}
				imports = new CopyOnWriteArraySet<IBeansImport>();
				aliases = new LinkedHashMap<String, IBeanAlias>();
				components = new LinkedHashSet<IBeansComponent>();
				beans = new LinkedHashMap<String, IBean>();
				problems = new CopyOnWriteArraySet<ValidationProblem>();
			}
			finally {
				w.unlock();
			}
		}
	}

	protected void readFinish() {
		isModelPopulated = true;

		List<ISourceModelElement> allChildren = new ArrayList<ISourceModelElement>(imports);
		allChildren.addAll(aliases.values());
		allChildren.addAll(components);
		allChildren.addAll(beans.values());
		Collections.sort(allChildren, new Comparator<ISourceModelElement>() {
			public int compare(ISourceModelElement element1, ISourceModelElement element2) {
				return element1.getElementStartLine() - element2.getElementStartLine();
			}
		});
		children = allChildren.toArray(new IModelElement[allChildren.size()]);
	}

	protected void addBean(IBean bean) {
		beans.put(bean.getElementName(), bean);
	}

	protected void addComponent(IBeansComponent beansComponent) {
		components.add(beansComponent);
	}

	protected void addAlias(IBeanAlias beanAlias) {
		aliases.put(beanAlias.getElementName(), beanAlias);
	}

	protected void setDefaults(DocumentDefaultsDefinition defaultsDefinition) {
		defaults = defaultsDefinition;
	}

	protected void addImport(IBeansImport beansImport) {
		imports.add(beansImport);
	}

	private void init(Resource resource) {
		if (resource instanceof IAdaptable) {
			if (((IAdaptable) resource).getAdapter(ZipEntryStorage.class) != null) {
				ZipEntryStorage storage = (ZipEntryStorage) ((IAdaptable) resource)
						.getAdapter(ZipEntryStorage.class);
				file = storage.getFile();
				setElementName(storage.getFullName());
				isArchived = true;
			}
			else if (((IAdaptable) resource).getAdapter(IFile.class) != null) {
				file = (IFile) ((IAdaptable) resource).getAdapter(IFile.class);
				isArchived = file instanceof ExternalFile;
			}
		}

		if (file == null || !file.isAccessible()) {
			modificationTimestamp = IResource.NULL_STAMP;
			String msg = "Imported Beans config file '" + resource + "' not accessible";
			problems = new CopyOnWriteArraySet<ValidationProblem>();
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, msg, file, -1));
		}
		else {
			modificationTimestamp = file.getModificationStamp();
			try {
				file.setSessionProperty(IBeansConfig.CONFIG_FILE_TAG, IBeansConfig.CONFIG_FILE_TAG_VALUE);
			} catch (CoreException e) {
				BeansCorePlugin.log(new Status(IStatus.WARNING, BeansCorePlugin.PLUGIN_ID, String.format(
						"Error occured while tagging config file '%s'", file.getFullPath()), e));
			}
		}
	}

	public BeanDefinitionRegistry getRawBeanDefinitions(CompositeComponentDefinition context) {
		return null;
	}
}
