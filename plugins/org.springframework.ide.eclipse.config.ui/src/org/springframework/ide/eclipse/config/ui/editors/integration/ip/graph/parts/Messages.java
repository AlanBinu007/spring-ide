/*******************************************************************************
 *  Copyright (c) 2012, 2014 Pivotal Software Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.parts;

import org.eclipse.osgi.util.NLS;

/**
 * @author Leo Dos Santos
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.config.ui.editors.integration.ip.graph.parts.messages"; //$NON-NLS-1$

	public static String IntIpPaletteFactory_TCP_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION;

	public static String IntIpPaletteFactory_TCP_CONNECTION_EVENT_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION;

	public static String IntIpPaletteFactory_TCP_INBOUND_GATEWAY_COMPONENT_DESCRIPTION;

	public static String IntIpPaletteFactory_TCP_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION;

	public static String IntIpPaletteFactory_TCP_OUTBOUND_GATEWAY_COMPONENT_DESCRIPTION;

	public static String IntIpPaletteFactory_UDP_INBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION;

	public static String IntIpPaletteFactory_UDP_OUTBOUND_CHANNEL_ADAPTER_COMPONENT_DESCRIPTION;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
