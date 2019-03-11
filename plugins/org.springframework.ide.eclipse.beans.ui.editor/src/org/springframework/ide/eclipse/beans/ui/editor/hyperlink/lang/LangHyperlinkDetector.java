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
package org.springframework.ide.eclipse.beans.ui.editor.hyperlink.lang;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.BeanHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.ClassHyperlinkCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;

/**
 * {@link INamespaceHyperlinkDetector} responsible for handling hyperlink
 * detection on elements of the <code>lang:*</code> namespace.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class LangHyperlinkDetector extends NamespaceHyperlinkDetectorSupport implements
		IHyperlinkDetector {

	@Override
	public void init() {
		registerHyperlinkCalculator("script-interfaces", new ClassHyperlinkCalculator());
		registerHyperlinkCalculator("customizer-ref", new BeanHyperlinkCalculator());
	}
}
