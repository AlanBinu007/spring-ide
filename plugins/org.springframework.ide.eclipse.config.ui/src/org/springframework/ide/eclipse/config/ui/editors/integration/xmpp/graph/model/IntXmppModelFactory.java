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
package org.springframework.ide.eclipse.config.ui.editors.integration.xmpp.graph.model;

import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.config.core.schemas.IntXmppSchemaConstants;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.AbstractIntegrationModelFactory;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class IntXmppModelFactory extends AbstractIntegrationModelFactory {

	public void getChildrenFromXml(List<Activity> list, IDOMElement input, Activity parent) {
		if (input.getLocalName().equals(IntXmppSchemaConstants.ELEM_HEADER_ENRICHER)) {
			HeaderEnricherModelElement enricher = new HeaderEnricherModelElement(input, parent.getDiagram());
			list.add(enricher);
		}
		else if (input.getLocalName().equals(IntXmppSchemaConstants.ELEM_INBOUND_CHANNEL_ADAPTER)) {
			InboundChannelAdapterModelElement adapter = new InboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntXmppSchemaConstants.ELEM_OUTBOUND_CHANNEL_ADAPTER)) {
			OutboundChannelAdapterModelElement adapter = new OutboundChannelAdapterModelElement(input,
					parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntXmppSchemaConstants.ELEM_PRESENCE_INBOUND_CHANNEL_ADAPTER)) {
			PresenceInboundChannelAdapterModelElement adapter = new PresenceInboundChannelAdapterModelElement(
					input, parent.getDiagram());
			list.add(adapter);
		}
		else if (input.getLocalName().equals(IntXmppSchemaConstants.ELEM_PRESENCE_OUTBOUND_CHANNEL_ADAPTER)) {
			PresenceOutboundChannelAdapterModelElement adapter = new PresenceOutboundChannelAdapterModelElement(
					input, parent.getDiagram());
			list.add(adapter);
		}
	}

}
