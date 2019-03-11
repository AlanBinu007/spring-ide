/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class Action extends AbstractAction implements ICloneableModelElement<Action> {

	/**
	 * Clone model element.
	 * 
	 * @return the action
	 */
	public Action cloneModelElement() {
		Action state = new Action();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		state.setType(getType());
		return state;
	}

	/**
	 * Apply clone values.
	 * 
	 * @param element the element
	 */
	public void applyCloneValues(Action element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode().replaceChild(element.getNode(), this.node);
			}
			setType(element.getType());
			init(element.getNode(), parent);
			super.fireStructureChange(MOVE_CHILDREN, new Integer(0));
			super.firePropertyChange(PROPS);
		}
	}

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = null;
		if (WebflowModelXmlUtils.isVersion1Flow(this)) {
			node = (IDOMNode) parent.getNode().getOwnerDocument().createElement("action");
		}
		else {
			node = (IDOMNode) parent.getNode().getOwnerDocument().createElement("render");
		}
		init(node, parent);
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {
			for (IAttribute state : getAttributes()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
		}
	}

	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		children.addAll(getAttributes());
		return children.toArray(new IModelElement[children.size()]);
	}
	

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		if (WebflowModelXmlUtils.isVersion1Flow(this)) {
			return getAttribute("name");
		}
		else {
			return getAttribute("fragments");
		}
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		if (WebflowModelXmlUtils.isVersion1Flow(this)) {
			setAttribute("name", name);
		}
		else {
			setAttribute("fragments", name);
		}
	}
}
