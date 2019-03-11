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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.gef.EditPolicy;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.ImplicitChannelEditPolicy;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.model.ImplicitChannelModelElement;


/**
 * @author Leo Dos Santos
 */
public class ImplicitChannelGraphicalEditPart extends BorderedIntegrationPart {

	public ImplicitChannelGraphicalEditPart(ImplicitChannelModelElement channel) {
		super(channel);
	}

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ImplicitChannelEditPolicy());
		installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, null);
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
	}

	@Override
	protected IFigure createFigure() {
		Label l = (Label) super.createFigure();
		l.setIcon(CommonImages.getImage(IntegrationImages.CHANNEL_GRAY));
		l.setEnabled(false);
		return l;
	}

	@Override
	public ImplicitChannelModelElement getModelElement() {
		return (ImplicitChannelModelElement) getModel();
	}

}
