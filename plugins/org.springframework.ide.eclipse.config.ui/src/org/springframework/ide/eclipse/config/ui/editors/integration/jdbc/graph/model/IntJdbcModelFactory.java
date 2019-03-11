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
package org.springframework.ide.eclipse.config.ui.editors.integration.jdbc.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntJdbcSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractIntegrationModelFactory;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class IntJdbcModelFactory extends AbstractIntegrationModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(IntJdbcSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER)) {
			InboundChannelAdapterModelElement adapter = new InboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJdbcSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER)) {
			OutboundChannelAdapterModelElement adapter = new OutboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJdbcSchemaConstants.ELEM_OUTBOUND_GATEWAY)) {
			OutboundGatewayModelElement gateway = new OutboundGatewayModelElement(input, parent.getDiagram());
			list.add(gateway);
		}
		else if (input.getLocalName().equals(IntJdbcSchemaConstants.ELEM_STORED_PROC_INBOUND_CHANNEL_ADAPTER)) {
			StoredProcInboundChannelAdapterModelElement adapter = new StoredProcInboundChannelAdapterModelElement(
					input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJdbcSchemaConstants.ELEM_STORED_PROC_OUTBOUND_CHANNEL_ADAPTER)) {
			StoredProcOutboundChannelAdapterModelElement adapter = new StoredProcOutboundChannelAdapterModelElement(
					input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntJdbcSchemaConstants.ELEM_STORED_PROC_OUTBOUND_GATEWAY)) {
			StoredProcOutboundGatewayModelElement gateway = new StoredProcOutboundGatewayModelElement(input,
					parent.getDiagram());
			list.add(gateway);
		}
	}

}
