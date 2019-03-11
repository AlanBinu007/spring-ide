/*******************************************************************************
 * Copyright (c) 2004, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.editor;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanConstructorArgument;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.ObjectUtils;

/**
 * This editor input is used to specify the list of {@link IBean}s which should be displayed in the beans graph editor.
 * Therefore a source model model element (
 * {@link IBean}>, {@link IBeanConfig} or {@link IBeanConfigSet}) has to be
 * specified. For a given bean it's parent bean (for child beans only), constructor argument values and property values
 * are checked. {@link IBean} look-up is done from the specified context (
 * {@link IBeanConfig} or {@link IBeanConfigSet}). This list of beans is accessible via {@link #getBeans()}. This context used for bean look-up is accessible via
 * {@link #getContext()}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class GraphEditorInput implements IEditorInput, IPersistableElement {

	private String elementId;

	private String contextId;

	private String name = "Spring Dependency Graph";

	private String toolTip = " ";

	private boolean hasError;

	/**
	 * Creates a list with all beans which are referenced from the model element defined by given ID.
	 * @param elementID the model element's ID
	 * @throws IllegalArgumentException if unsupported model element is specified
	 */
	public GraphEditorInput(String elementId) {
		this(elementId, getContext(elementId));
	}

	/**
	 * Creates a list with all beans which are referenced from the model element defined by given ID.
	 * @param elementId the model element's ID
	 * @param contextId the context's ID
	 * @throws IllegalArgumentException if unsupported model element or context is specified
	 */
	public GraphEditorInput(String elementId, String contextId) {
		this.elementId = elementId;
		this.contextId = contextId;
	}

	protected void init() {
		IModelElement element = BeansCorePlugin.getModel().getElement(elementId);
		IModelElement context = BeansCorePlugin.getModel().getElement(contextId);

		// Prepare name and tooltip for given element and context
		if (element instanceof IBeansConfig) {
			String toolTipPrefix = BeansGraphPlugin.getResourceString("ShowGraphAction.name.config");
			IBeansConfig config = (IBeansConfig) element;
			IResource resource = config.getElementResource();
			if (resource != null) {
				if (config.isElementArchived()) {
					ZipEntryStorage storage = new ZipEntryStorage(config);
					name = storage.getName();
					toolTip = toolTipPrefix + storage.getFile().getProjectRelativePath().toString() + " - "
							+ storage.getFullPath().toString();
				}
				else {
					name = resource.getName();
					toolTip = toolTipPrefix + resource.getFullPath().toString();
				}
			}
			else {
				name = BeansGraphPlugin.getResourceString("ShowGraphAction.name.undefined");
				toolTip = BeansGraphPlugin.getResourceString("ShowGraphAction.name.config") + name;
			}
		}
		else if (element instanceof IBeansConfigSet) {
			IModelElement parent = ((IBeansConfigSet) element).getElementParent();
			name = element.getElementName();
			toolTip = BeansGraphPlugin.getResourceString("ShowGraphAction.name.configSet") + parent.getElementName()
					+ '/' + element.getElementName();
		}
		else if (element instanceof IBeansComponent || element instanceof IBean) {
			name = element.getElementName();
			StringBuffer buffer = new StringBuffer();
			if (element instanceof IBeansComponent) {
				buffer.append(BeansGraphPlugin.getResourceString("ShowGraphAction.name.component"));
			}
			else {
				buffer.append(BeansGraphPlugin.getResourceString("ShowGraphAction.name.bean"));
			}
			if (context instanceof IBeansConfig) {
				buffer.append(BeansGraphPlugin.getResourceString("ShowGraphAction.name.config"));
				buffer.append(context.getElementName());
				buffer.append(": ");
			}
			else if (context instanceof IBeansConfigSet) {
				buffer.append(BeansGraphPlugin.getResourceString("ShowGraphAction.name.configSet"));
				buffer.append(context.getElementParent().getElementName());
				buffer.append('/');
				buffer.append(context.getElementName());
				buffer.append(": ");
			}
			buffer.append(element.getElementName());
			toolTip = buffer.toString();
		}
		// else {
		// throw new IllegalArgumentException("Unsupported model element " + element);
		// }
	}
	
	private IModelElement getElement(String elementId) {
		return BeansCorePlugin.getModel().getElement(elementId);
	}
	
	public String getName() {
		return name;
	}

	public String getElementId() {
		return elementId;
	}

	public String getContextId() {
		return contextId;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IModelElement.class) {
			return getElement(elementId);
		}
		return null;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return BeansUIImages.DESC_OBJS_SPRING;
	}

	public IPersistableElement getPersistable() {
		return (hasError ? null : this);
	}

	public String getToolTipText() {
		return toolTip;
	}

	public String getFactoryId() {
		return GraphEditorInputFactory.getFactoryId();
	}

	public void saveState(IMemento memento) {
		GraphEditorInputFactory.saveState(memento, this);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof GraphEditorInput)) {
			return false;
		}
		GraphEditorInput that = (GraphEditorInput) other;
		if (!ObjectUtils.nullSafeEquals(this.elementId, that.elementId))
			return false;
		return ObjectUtils.nullSafeEquals(this.contextId, that.contextId);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(elementId);
		return 29 * hashCode + ObjectUtils.nullSafeHashCode(contextId);
	}

	private static IModelElement getContext(IModelElement element) {
		if (element instanceof IBean) {
			return element.getElementParent();
		}
		else if (element instanceof IBeanConstructorArgument || element instanceof IBeanProperty) {
			return element.getElementParent().getElementParent();
		}
		return element;
	}

	private static String getContext(String elementId) {
		IModelElement element = BeansCorePlugin.getModel().getElement(elementId);
		return getContext(element).getElementID();
	}
}
