/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;

/**
 * @author Christian Dupuis
 */
public class ClassBeanReferenceNode implements IReferenceNode {

	private Set<String> references;

	public ClassBeanReferenceNode(Set<String> reference) {
		this.references = reference;
	}

	public IReferenceNode[] getChildren() {
		List<IReferenceNode> nodes = new ArrayList<IReferenceNode>();
		for (String r : references) {
			nodes.add(new BeanReferenceNode(r, false));
		}
		return nodes.toArray(new IReferenceNode[nodes.size()]);
	}

	public Image getImage() {
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
	}

	public String getText() {
		return "class of";
	}

	public boolean hasChildren() {
		return true;
	}

	public Object getReferenceParticipant() {
		return null;
	}
}
