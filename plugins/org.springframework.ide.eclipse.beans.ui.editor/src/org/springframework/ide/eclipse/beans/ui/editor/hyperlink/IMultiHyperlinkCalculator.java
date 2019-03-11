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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Implementations of this interface are able to calculate multiple hyperlinks {@link IHyperlink} instances for a given
 * attribute.
 * @author Christian Dupuis
 * @since 2.2.7
 */
public interface IMultiHyperlinkCalculator extends IHyperlinkCalculator {

	/**
	 * Calculate {@link IHyperlink} instance for the given context.
	 * <p>
	 * Note: this method will only be called if {@link #isLinkableAttr(Attr)} returns true.
	 */
	IHyperlink[] createHyperlinks(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor);

}
