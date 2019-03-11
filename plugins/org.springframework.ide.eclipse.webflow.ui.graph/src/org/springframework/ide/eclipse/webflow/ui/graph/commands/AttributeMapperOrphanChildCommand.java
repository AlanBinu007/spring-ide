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
package org.springframework.ide.eclipse.webflow.ui.graph.commands;

import org.eclipse.gef.commands.Command;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;

/**
 * @author Christian Dupuis
 */
public class AttributeMapperOrphanChildCommand extends Command {

    /**
     * 
     */
    private IAttributeMapper child;

    /**
     * 
     */
    private ISubflowState parent;

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        parent.removeAttributeMapper();
    }

    /**
     * 
     * 
     * @param child 
     */
    public void setChild(IAttributeMapper child) {
        this.child = child;
    }

    /**
     * 
     * 
     * @param parent 
     */
    public void setParent(ISubflowState parent) {
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        parent.setAttributeMapper(child);
    }

}
