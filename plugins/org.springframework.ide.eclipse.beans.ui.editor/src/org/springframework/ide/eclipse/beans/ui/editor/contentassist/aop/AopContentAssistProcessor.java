/*******************************************************************************
 * Copyright (c) 2006, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.aop;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * {@link INamespaceContentAssistProcessor} responsible for the <code>aop:*</code> namespace.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class AopContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	public void init() {
		IContentAssistCalculator beanRef = new BeanReferenceContentAssistCalculator();
		registerContentAssistCalculator("aspect", "ref", beanRef);
		registerContentAssistCalculator("advisor", "advice-ref", beanRef);
		registerContentAssistCalculator("delegate-ref", beanRef);
		registerContentAssistCalculator("include", "name", beanRef);
		registerContentAssistCalculator("implement-interface", new ClassContentAssistCalculator(true));
		registerContentAssistCalculator("pointcut-ref", new PointcutReferenceContentAssistCalculator());
		registerContentAssistCalculator("default-impl", new DefaultImplContentAssistCalculator());
		registerContentAssistCalculator("method", new AdviceMethodContentAssistCalculator());
	}
}
