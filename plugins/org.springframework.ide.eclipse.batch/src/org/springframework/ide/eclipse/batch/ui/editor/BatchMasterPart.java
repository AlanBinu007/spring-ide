/*******************************************************************************
 *  Copyright (c) 2012 - 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.batch.ui.editor;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceMasterPart;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BatchMasterPart extends AbstractNamespaceMasterPart {

	public BatchMasterPart(AbstractConfigFormPage page, Composite parent) {
		super(page, parent);
	}

	@Override
	protected void createEmptyDocumentActions(IMenuManager manager, IDOMDocument doc) {
		// TODO Auto-generated method stub

	}

	@Override
	protected String getSectionTitle() {
		return Messages.getString("BatchMasterPart.MASTER_SECTION_TITLE"); //$NON-NLS-1$
	}

}
