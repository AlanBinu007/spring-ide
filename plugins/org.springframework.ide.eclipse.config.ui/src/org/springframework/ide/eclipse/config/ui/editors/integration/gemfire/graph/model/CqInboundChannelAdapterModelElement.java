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
package org.springframework.ide.eclipse.config.ui.editors.integration.gemfire.graph.model;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntGemfireSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractInboundChannelAdapterModelElement;

/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class CqInboundChannelAdapterModelElement extends AbstractInboundChannelAdapterModelElement {

	public CqInboundChannelAdapterModelElement() {
		super();
	}

	public CqInboundChannelAdapterModelElement(IDOMElement input, AbstractConfigGraphDiagram diagram) {
		super(input, diagram);
	}

	@Override
	public String getInputName() {
		return IntGemfireSchemaConstants.ELEM_CQ_INBOUND_CHANNEL_ADAPTER;
	}

}
