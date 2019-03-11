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

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;

/**
 * @author Christian Dupuis
 */
public class InnerBeansReferenceNode implements IReferenceNode {

	private List<IReferenceNode> childs;

	public InnerBeansReferenceNode(List<IReferenceNode> childs) {
		this.childs = childs;
	}

	public IReferenceNode[] getChildren() {
		return childs.toArray(new IReferenceNode[this.childs.size()]);
	}

	public Image getImage() {
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_REFERENCE);
	}

	public String getText() {
		return "inner beans";
	}

	public boolean hasChildren() {
		return true;
	}

	public Object getReferenceParticipant() {
		return null;
	}
}
