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
package org.springframework.ide.eclipse.config.ui.contenttype;

import org.eclipse.osgi.util.NLS;

/**
 * @author Leo Dos Santos
 */
public class ContentMessages extends NLS {
	private static final String BUNDLE_NAME = "org.springframework.ide.eclipse.config.ui.contenttype.messages"; //$NON-NLS-1$

	public static String SpringElementContentDescriber_ERROR_CREATING_CONTENT_DESCRIBER;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ContentMessages.class);
	}

	private ContentMessages() {
	}
}
