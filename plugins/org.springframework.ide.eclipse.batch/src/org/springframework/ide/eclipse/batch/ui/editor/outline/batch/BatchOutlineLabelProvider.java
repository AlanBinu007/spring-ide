/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.batch.ui.editor.outline.batch;

import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.xml.ui.internal.contentoutline.JFaceNodeLabelProvider;
import org.springframework.ide.eclipse.batch.BatchUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.outline.BeansContentOutlineConfiguration;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @since 2.2.2
 */
@SuppressWarnings("restriction")
public class BatchOutlineLabelProvider extends JFaceNodeLabelProvider {

	@Override
	public Image getImage(Object object) {
		return BatchUIImages.getImage(BatchUIImages.IMG_OBJS_BATCH);
	}

	@Override
	public String getText(Object element) {
		Node node = (Node) element;
		NamedNodeMap attrs = node.getAttributes();
		Node id;
		String name = node.getLocalName();
		String text = name;
		
		if (BeansContentOutlineConfiguration.isShowAttributes()) {
			id = attrs.getNamedItem("id");
			if (id != null) {
				text = text.concat(" " + id.getNodeValue());
			}
		}
		return text;
	}
}
