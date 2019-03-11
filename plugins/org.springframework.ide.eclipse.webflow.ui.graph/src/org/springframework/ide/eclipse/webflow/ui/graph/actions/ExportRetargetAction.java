/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.actions;

import org.eclipse.ui.actions.RetargetAction;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowImages;

/**
 * @author Christian Dupuis
 */
public class ExportRetargetAction extends RetargetAction {

    public static final String ID = "Export_action";

    public ExportRetargetAction() {
        super(ExportAction.ID, "Export");
        setToolTipText("Exports the web flow to an image");
        setImageDescriptor(WebflowImages.DESC_OBJS_EXPORT_ENABLED);
        setDisabledImageDescriptor(WebflowImages.DESC_OBJS_EXPORT_DISABLED);
    }
}
