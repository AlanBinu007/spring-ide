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
package org.springframework.ide.eclipse.webflow.ui.graph.parts;

import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.graph.CompoundDirectedGraph;
import org.eclipse.draw2d.graph.Node;
import org.eclipse.draw2d.graph.Subgraph;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Color;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.IEndState;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.ui.graph.figures.StateLabel;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelDecorator;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelProvider;

/**
 * @author Christian Dupuis
 */
public class StatePart extends AbstractStatePart {

	/**
	 * 
	 */
	public static final Color COLOR = new Color(null, 255, 255, 206);

	/**
	 * 
	 */
	protected static ILabelProvider labelProvider = new DecoratingLabelProvider(
			new WebflowModelLabelProvider(), new WebflowModelLabelDecorator());

	protected WebflowModelLabelProvider eLabelProvider = new WebflowModelLabelProvider();
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart#contributeNodesToGraph(org.eclipse.draw2d.graph.CompoundDirectedGraph,
	 * org.eclipse.draw2d.graph.Subgraph, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public void contributeNodesToGraph(CompoundDirectedGraph graph, Subgraph s,
			Map map) {
		Node n = new Node(this, s);
		n.outgoingOffset = 9;
		n.incomingOffset = 9;
		n.width = getFigure().getPreferredSize().width + 5;
		n.height = getFigure().getPreferredSize().height;
		if (getModel() instanceof IEndState)
			n.setPadding(new Insets(0, 40, 10, 40));
		else if (getModel() instanceof IActionElement
				|| getModel() instanceof IAttributeMapper
				|| getModel() instanceof IIf
				|| getModel() instanceof IExceptionHandler)
			n.setPadding(new Insets(0, 5, 5, 0));
		else
			n.setPadding(new Insets(0, 50, 50, 50));
		map.put(this, n);
		graph.nodes.add(n);

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		Label l = new StateLabel();
		// l.setBorder(new MarginBorder(3, 5, 3, 0));
		l.setBackgroundColor(COLOR);
		l.setLabelAlignment(PositionConstants.LEFT);
		l.setIcon(labelProvider.getImage(getModel()));
		l.setIconTextGap(5);
		l.setIconAlignment(PositionConstants.TOP);
		l.setBorder(new LineBorder());
		return l;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	protected void refreshVisuals() {
		((Label) getFigure()).setText(labelProvider.getText(getModel()) + " ");
		((Label) getFigure()).setIcon(labelProvider.getImage(getModel()));
		((Label) getFigure()).setToolTip(new Label(eLabelProvider.getText(getModel(), true, true, true)));

		// refresh parent icon to indicate if error exists
		if (getParent() instanceof ChildrenStatePart) {
			((ChildrenStatePart) getParent()).refreshVisuals();
		}
	}

}
