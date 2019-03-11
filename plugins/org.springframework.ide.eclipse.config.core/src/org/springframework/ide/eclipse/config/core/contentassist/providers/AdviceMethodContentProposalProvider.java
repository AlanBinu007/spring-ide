/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.core.contentassist.providers;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.aop.AdviceMethodContentAssistCalculator;
import org.springframework.ide.eclipse.config.core.contentassist.XmlBackedContentProposalProvider;


/**
 * An {@link XmlBackedContentProposalProvider} that uses
 * {@link AdviceMethodContentAssistCalculator} as its content assist calculator.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0.0
 */
@SuppressWarnings("restriction")
public class AdviceMethodContentProposalProvider extends XmlBackedContentProposalProvider {

	/**
	 * Constructs a content proposal provider for an XML attribute.
	 * 
	 * @param input the XML element to serve as the model of this proposal
	 * provider
	 * @param attrName the name of the attribute to compute proposals for
	 */
	public AdviceMethodContentProposalProvider(IDOMElement input, String attrName) {
		super(input, attrName);
	}

	@Override
	protected IContentAssistCalculator createContentAssistCalculator() {
		return new AdviceMethodContentAssistCalculator();
	}

}
