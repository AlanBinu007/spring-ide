/*******************************************************************************
 * Copyright (c) 2008 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.bean;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.IHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.JavaElementHyperlink;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * {@link IHyperlinkCalculator} for the factory-method attribute.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 2.2.1
 */
public class FactoryMethodHyperlinkCalculator implements IHyperlinkCalculator {

	/**
	 * {@inheritDoc}
	 */
	public IHyperlink createHyperlink(String name, String target, Node node, Node parentNode, IDocument document,
			ITextViewer textViewer, IRegion hyperlinkRegion, IRegion cursor) {
		NamedNodeMap attributes = node.getAttributes();
		String className = null;
		if (attributes != null && getFactoryBeanReferenceNode(attributes) != null) {
			Node factoryBean = getFactoryBeanReferenceNode(attributes);
			if (factoryBean != null) {
				String factoryBeanId = factoryBean.getNodeValue();
				className = BeansEditorUtils.getClassNameForBean(BeansEditorUtils.getFile(document), node
						.getOwnerDocument(), factoryBeanId);
			}
		}
		else if (attributes != null && attributes.getNamedItem("class") != null) {
			className = attributes.getNamedItem("class").getNodeValue();
		}
		try {
			IFile file = BeansEditorUtils.getFile(document);
			if (file != null && file.exists()) {
				IType type = JdtUtils.getJavaType(file.getProject(), className);
				IMethod method = Introspector.findMethod(type, target, -1, Public.DONT_CARE, Static.DONT_CARE);
				if (method != null) {
					return new JavaElementHyperlink(hyperlinkRegion, method);
				}
			}
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	protected Node getFactoryBeanReferenceNode(NamedNodeMap attributes) {
		return attributes.getNamedItem("factory-bean");
	}

}
