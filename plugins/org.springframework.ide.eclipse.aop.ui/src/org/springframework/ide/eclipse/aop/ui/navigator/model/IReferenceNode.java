/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.navigator.model;

import org.eclipse.swt.graphics.Image;

/**
 * @author Christian Dupuis
 */
public interface IReferenceNode {

	IReferenceNode[] getChildren();

	boolean hasChildren();

	String getText();

	Image getImage();
	
	Object getReferenceParticipant();

}
