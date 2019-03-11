/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.context;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassHierachyContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.PackageContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * {@link INamespaceContentAssistProcessor} responsible for handling content
 * assist request on elements of the <code>context:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class ContextContentAssistProcessor extends
		NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		IContentAssistCalculator beanRef = new BeanReferenceContentAssistCalculator(true);
		registerContentAssistCalculator("component-scan", "name-generator",	beanRef);
		registerContentAssistCalculator("component-scan", "scope-resolver", beanRef);

		registerContentAssistCalculator("load-time-weaver", "weaver-class",
				new ClassHierachyContentAssistCalculator("org.springframework.instrument.classloading.LoadTimeWeaver"));

		registerContentAssistCalculator("component-scan", "base-package",
				new PackageContentAssistCalculator());
		
		ClassContentAssistCalculator clazz = new ClassContentAssistCalculator(false);
		registerContentAssistCalculator("include-filter", "expression", clazz);
		registerContentAssistCalculator("exclude-filter", "expression", clazz);
	}
}
