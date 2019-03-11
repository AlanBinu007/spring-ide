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
package org.springframework.ide.eclipse.config.ui.editors.webflow.graph;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigEditPartFactory;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigPaletteFactory;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.model.WebFlowDiagram;
import org.springframework.ide.eclipse.config.ui.editors.webflow.graph.parts.WebFlowEditPartFactory;


/**
 * @author Leo Dos Santos
 */
public class WebFlowGraphicalEditor extends AbstractConfigGraphicalEditor {

	@Override
	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action = new ToggleActionStateAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new ToggleDecisionStateAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new ToggleEndStateAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new ToggleSubflowStateAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new ToggleViewStateAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
	}

	@Override
	protected ContextMenuProvider createContextMenuProvider() {
		return new WebFlowContextMenuProvider(getGraphicalViewer(), getActionRegistry());
	}

	@Override
	protected AbstractConfigEditPartFactory createEditPartFactory() {
		return new WebFlowEditPartFactory(this);
	}

	@Override
	protected AbstractConfigGraphDiagram createFlowDiagram() {
		return new WebFlowDiagram(this);
	}

	@Override
	protected AbstractConfigPaletteFactory createPaletteFactory() {
		return new WebFlowEditorPaletteFactory(this);
	}

}
