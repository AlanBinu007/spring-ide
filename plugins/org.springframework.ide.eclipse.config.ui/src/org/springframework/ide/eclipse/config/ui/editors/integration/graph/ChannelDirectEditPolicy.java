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
package org.springframework.ide.eclipse.config.ui.editors.integration.graph;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.DirectEditRequest;
import org.springframework.ide.eclipse.config.graph.model.Activity;
import org.springframework.ide.eclipse.config.graph.policies.ActivityDirectEditPolicy;


/**
 * @author Leo Dos Santos
 */
public class ChannelDirectEditPolicy extends ActivityDirectEditPolicy {

	@Override
	protected Command getDirectEditCommand(DirectEditRequest request) {
		Activity source = (Activity) getHost().getModel();
		RenameChannelCommand cmd = new RenameChannelCommand(source.getDiagram().getTextEditor());
		cmd.setSource(source);
		cmd.setOldName(((Activity) getHost().getModel()).getName());
		cmd.setName((String) request.getCellEditor().getValue());
		return cmd;
	}

}
