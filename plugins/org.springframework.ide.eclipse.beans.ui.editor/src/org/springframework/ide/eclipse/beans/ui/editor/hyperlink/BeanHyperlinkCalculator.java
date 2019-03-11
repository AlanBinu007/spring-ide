/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMAttr;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.StringUtils;
import org.w3c.dom.Node;

/**
 * {@link IHyperlinkCalculator} implementation can calculates {@link IHyperlink}objects for navigating to referenced beans.
 * @author Christian Dupuis
 * @author Terry Denney
 * @since 2.0.2
 */
@SuppressWarnings("restriction")
public class BeanHyperlinkCalculator implements IHyperlinkCalculator, IMultiHyperlinkCalculator {

	/**
	 * Calculates a {@link IHyperlink} for a bean reference expressed by the
	 * given <code>target</code>.
	 * <p>
	 * First the bean is located within the same file. If no matching bean or
	 * other referenceable node can be found, the search is extended to existing
	 * {@link IBeansConfigSet}.
	 */
	public IHyperlink createHyperlink(String name, String target, Node node,
			Node parentNode, IDocument document, ITextViewer textViewer,
			IRegion hyperlinkRegion, IRegion cursor) {
		IFile file = BeansEditorUtils.getFile(document);
		Node bean = BeansEditorUtils.getFirstReferenceableNodeById(node
				.getOwnerDocument(), target, file);
		return createHyperlinkHelper(bean, target, hyperlinkRegion, textViewer, file);
	}
	
	private IHyperlink createHyperlinkHelper(Node bean, String target, IRegion hyperlinkRegion, ITextViewer textViewer, IFile file) {
		if (bean != null) {
			IRegion region = getHyperlinkRegion(bean);
			return new NodeElementHyperlink(bean, file, hyperlinkRegion, region, textViewer);
		}
		else {
			// assume this is an external reference
			Iterator<?> beans = BeansEditorUtils.getBeansFromConfigSets(file)
					.iterator();
			while (beans.hasNext()) {
				IBean modelBean = (IBean) beans.next();
				if (modelBean.getElementName().equals(target)) {
					return new ExternalBeanHyperlink(modelBean, hyperlinkRegion);
				}
			}
		}
		return null;
	}

	public IHyperlink[] createHyperlinks(String name, String target, Node node,
			Node parentNode, IDocument document, ITextViewer textViewer,
			IRegion hyperlinkRegion, IRegion cursor) {
		IFile file = BeansEditorUtils.getFile(document);
		List<Node> beans = BeansEditorUtils.getReferenceableNodesById(node.getOwnerDocument(), target, file);
		List<IHyperlink> result = new ArrayList<IHyperlink>();
		
		for(Node bean: beans) {
			IHyperlink link = createHyperlinkHelper(bean, target, hyperlinkRegion, textViewer, file);
			if (link != null) {
				result.add(link);
			}
		}
		
		// get beans from outside current file
		Set<IBean> beansFromConfigSets = BeansEditorUtils.getBeansFromConfigSets(file);
		for(IBean bean: beansFromConfigSets) {
			if (bean.getElementName().equals(target)) {
				result.add(new ExternalBeanHyperlink(bean, hyperlinkRegion));
			}
		}
		
		if (result.isEmpty()) {
			return null;
		}
		return result.toArray(new IHyperlink[result.size()]);
	}

	/**
	 * Returns the text region of given node.
	 */
	protected final IRegion getHyperlinkRegion(Node node) {
		if (node != null) {
			switch (node.getNodeType()) {
			case Node.DOCUMENT_TYPE_NODE:
			case Node.TEXT_NODE:
				IDOMNode docNode = (IDOMNode) node;
				return new Region(docNode.getStartOffset(), docNode
						.getEndOffset()
						- docNode.getStartOffset());

			case Node.ELEMENT_NODE:
				IDOMElement element = (IDOMElement) node;
				int endOffset;
				if (element.hasEndTag() && element.isClosed()) {
					endOffset = element.getStartEndOffset();
				}
				else {
					endOffset = element.getEndOffset();
				}
				return new Region(element.getStartOffset(), endOffset
						- element.getStartOffset());

			case Node.ATTRIBUTE_NODE:
				IDOMAttr att = (IDOMAttr) node;
				// do not include quotes in attribute value region
				int regOffset = att.getValueRegionStartOffset();
				int regLength = att.getValueRegionText().length();
				String attValue = att.getValueRegionText();
				if (StringUtils.isQuoted(attValue)) {
					regOffset += 1;
					regLength = regLength - 2;
				}
				return new Region(regOffset, regLength);
			}
		}
		return null;
	}

}
