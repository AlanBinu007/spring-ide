/*******************************************************************************
 * Copyright (c) 2006, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public final class SpringUIMessages extends NLS {

	private static final String BUNDLE_NAME =
			"org.springframework.ide.eclipse.ui.SpringUIMessages";

	private SpringUIMessages() {
		// Do not instantiate
	}

	public static String Plugin_internalError;

	public static String ProjectNature_errorMessage;
	public static String ProjectNature_addError;
	public static String ProjectNature_removeError;

	public static String OpenInEditor_errorMessage;

	public static String ImageDescriptorRegistry_wrongDisplay;

	public static String ProjectBuilderPropertyPage_title;
	public static String ProjectBuilderPropertyPage_description;
	public static String ProjectBuilderPropertyPage_noBuilderDescription;
	public static String ProjectBuilderPropertyPage_builderDescription;
	public static String ProjectBuilderPropertyPage_IncrementalCompileMessage;
	public static String ProjectBuilderPropertyPage_IncrementalCompileNote;
	public static String ProjectBuilderPropertyPage_NonLockingClassLoaderMessage;
	public static String ProjectBuilderPropertyPage_NonLockingClassLoaderNote;
	
	public static String ProjectValidatorPropertyPage_title;
	public static String ProjectValidatorPropertyPage_description;
	public static String ProjectValidatorPropertyPage_noBuilderDescription;
	public static String ProjectValidatorPropertyPage_builderDescription;

	public static String PropertiesPage_title;

	static {
		NLS.initializeMessages(BUNDLE_NAME, SpringUIMessages.class);
	}
}
