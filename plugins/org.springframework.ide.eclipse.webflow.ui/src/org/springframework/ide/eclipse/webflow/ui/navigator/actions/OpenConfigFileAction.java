/*******************************************************************************
 * Copyright (c) 2007 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.part.FileEditorInput;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.Activator;
import org.springframework.ide.eclipse.webflow.ui.editor.SpringWebFlowEditor;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * Opens the {@link IWebflowConfig} in the standard Eclipse editor.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Leo Dos Santos
 * @since 2.0
 */
public class OpenConfigFileAction extends AbstractNavigatorAction {

	private IFile file;

	public OpenConfigFileAction(ICommonActionExtensionSite site) {
		super(site);
		setText("Op&en"); // TODO externalize text
	}

	public boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object sElement = selection.getFirstElement();
			if (sElement instanceof IWebflowConfig) {
				file = ((IWebflowConfig) sElement).getResource();
				return true;
			}
			else if (sElement instanceof IFile) {
				if (WebflowModelUtils.isWebflowConfig((IFile) sElement)
						&& Activator.SPRING_EXPLORER_CONTENT_PROVIDER_ID
								.equals(getActionSite().getExtensionId())) {
					file = (IFile) sElement;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		IEditorInput input = new FileEditorInput(file);
		IEditorPart part = SpringUIUtils.openInEditor(input, SpringWebFlowEditor.ID_EDITOR);
		if (part instanceof AbstractConfigEditor) {
			AbstractConfigEditor cEditor = (AbstractConfigEditor) part;
			IEditorPart source = cEditor.getSourcePage();
			if (source != null) {
				cEditor.setActiveEditor(source);
			}
		}
	}
}
