/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.jms;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;

/**
 * {@link INamespaceHyperlinkDetector} responsible for handling hyperlink
 * detection on elements of the <code>jms:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class JmsHyperlinkDetector extends NamespaceHyperlinkDetectorSupport
		implements IHyperlinkDetector {

	@Override
	public void init() {
		BeanHyperlinkCalculator beanRef = new BeanHyperlinkCalculator();
		registerHyperlinkCalculator("listener-container", "connection-factory", beanRef);
		registerHyperlinkCalculator("listener-container", "task-executor", beanRef);
		registerHyperlinkCalculator("listener-container", "destination-resolver", beanRef);
		registerHyperlinkCalculator("listener-container", "message-converter", beanRef);
		registerHyperlinkCalculator("listener-container", "transaction-manager", beanRef);
		registerHyperlinkCalculator("listener", "ref", beanRef);
		registerHyperlinkCalculator("jca-listener-container", "resource-adapter", beanRef);
		registerHyperlinkCalculator("jca-listener-container", "activation-spec-factory", beanRef);
		registerHyperlinkCalculator("jca-listener-container", "message-converter", beanRef);
		
		registerHyperlinkCalculator("listener", "method", new ListenerMethodHyperlinkCalculator());
	}
}
