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
package org.springframework.ide.eclipse.config.ui.editors.namespaces;

import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterDetailsBlock;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class NamespacesFormPage extends AbstractConfigFormPage {

	public final static String ID = "com.springsource.sts.config.ui.editors.namespaces"; //$NON-NLS-1$

	public NamespacesFormPage(AbstractConfigEditor editor) {
		super(editor, ID, Messages.getString("NamespacesFormPage.FORM_TITLE")); //$NON-NLS-1$
	}

	@Override
	protected AbstractConfigMasterDetailsBlock createMasterDetailsBlock() {
		return new NamespacesMasterDetailsBlock(this);
	}

}
