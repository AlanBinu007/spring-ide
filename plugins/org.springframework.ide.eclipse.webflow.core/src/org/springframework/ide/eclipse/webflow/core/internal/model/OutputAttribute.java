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
import org.springframework.ide.eclipse.webflow.core.model.IOutputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class OutputAttribute extends InputAttribute implements IOutputAttribute {

	@Override
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = null;
		if (WebflowModelXmlUtils.isVersion1Flow(this)) {
			node = (IDOMNode) parent.getNode().getOwnerDocument().createElement("output-attribute");
		}
		else {
			node = (IDOMNode) parent.getNode().getOwnerDocument().createElement("output");
		}
		init(node, parent);
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {
		visitor.visit(this, monitor);
	}

	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		return children.toArray(new IModelElement[children.size()]);
	}
}
