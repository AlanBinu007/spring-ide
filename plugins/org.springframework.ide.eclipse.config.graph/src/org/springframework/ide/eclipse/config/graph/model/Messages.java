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
package org.springframework.ide.eclipse.config.graph.model;

import org.eclipse.osgi.util.NLS;

/**
 * @author Leo Dos Santos
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.config.graph.model.messages"; //$NON-NLS-1$

	public static String AbstractConfigFlowDiagram_ERROR_CREATING_GRAPH;

	public static String AbstractConfigFlowDiagram_ERROR_READING_COORDINATES;

	public static String AbstractConfigFlowDiagram_ERROR_SAVING_COORDINATES;

	public static String ModelElementCreationFactory_ERROR_CREATING_ELEMENT_MODEL;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
