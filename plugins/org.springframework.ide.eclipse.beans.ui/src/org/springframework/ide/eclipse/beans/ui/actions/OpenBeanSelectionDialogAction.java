/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.dialogs.BeanListSelectionDialog;

/**
 * Action that opens the Bean SelectionDialog
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 */
public class OpenBeanSelectionDialogAction implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;

	public void run(IAction action) {
		BeanListSelectionDialog dialog = new BeanListSelectionDialog(
				workbenchWindow.getShell());
		if (Window.OK == dialog.open()) {
			IBean bean = (IBean) dialog.getFirstResult();
			BeansUIUtils.openInEditor(bean);
		}
	}

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.workbenchWindow = window;
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
}
