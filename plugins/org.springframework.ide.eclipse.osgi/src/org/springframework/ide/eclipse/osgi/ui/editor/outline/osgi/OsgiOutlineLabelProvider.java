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
package org.springframework.ide.eclipse.osgi.ui.editor.outline.osgi;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.osgi.OsgiUIImages;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.0.1
 */
@SuppressWarnings("restriction")
public class OsgiOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		return OsgiUIImages.getImage(OsgiUIImages.IMG_OBJS_OSGI);
	}

	@Override
	public String getText(Object element) {
		Node node = (Node) element;
		return node.getLocalName();
	}
}
