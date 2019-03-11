/*******************************************************************************
 * Copyright (c) 2006, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.navigator;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IRevealableReferenceNode;
import org.springframework.ide.eclipse.beans.ui.navigator.BeansNavigatorLabelProvider;

/**
 * {@link ICommonLabelProvider} that just delegates to
 * {@link IReferenceNode#getText()} and {@link IReferenceNode#getImage()} of
 * instances of {@link IReferenceNode}. Otherwise calls
 * {@link BeansNavigatorLabelProvider}.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class AopReferenceModelNavigatorLabelProvider extends
		BeansNavigatorLabelProvider {
	
	public AopReferenceModelNavigatorLabelProvider() {
		super(true);
	}
	
	@Override
	public String getDescription(Object element) {
		if (element instanceof IRevealableReferenceNode) {
			IRevealableReferenceNode node = (IRevealableReferenceNode) element;
			if (node.getResource() != null) {
				return node.getResource().getName()
						+ " - "
						+ node.getResource().getFullPath().toString()
								.substring(1);
			}
		}
		return super.getDescription(element);
	}

	@Override
	public Image getImage(Object element, Object parentElement) {
		if (element instanceof IReferenceNode) {
			return ((IReferenceNode) element).getImage();
		}
		return super.getImage(element, parentElement);
	}

	@Override
	public String getText(Object element, Object parentElement) {
		if (element instanceof IReferenceNode) {
			return ((IReferenceNode) element).getText();
		}
		else if (element instanceof IWorkspaceRoot) {
			return "Beans Cross References";
		}
		return super.getText(element, parentElement);
	}
}
