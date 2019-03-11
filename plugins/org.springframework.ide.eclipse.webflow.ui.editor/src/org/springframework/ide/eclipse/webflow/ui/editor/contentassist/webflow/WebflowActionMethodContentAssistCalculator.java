/*******************************************************************************
 * Copyright (c) 2007 - 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.MethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.IMethodFilter;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * {@link MethodContentAssistCalculator} extension that is used to propose action methods.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public abstract class WebflowActionMethodContentAssistCalculator extends
		MethodContentAssistCalculator {

	public WebflowActionMethodContentAssistCalculator(IMethodFilter filter) {
		super(filter);
	}

	@Override
	protected final IType calculateType(IContentAssistContext context) {
		if (BeansEditorUtils.hasAttribute(context.getNode(), "bean")) {
			String className = null;
			IFile file = context.getFile();
			if (file != null && file.exists()) {
				IWebflowConfig config = Activator.getModel().getProject(file.getProject())
						.getConfig(file);
	
				if (config != null) {
					Set<IBean> beans = WebflowModelUtils.getBeans(config);
					for (IBean bean : beans) {
						if (bean.getElementName().equals(
								BeansEditorUtils.getAttribute(context.getNode(), "bean"))) {
							className = BeansModelUtils.getBeanClass(bean, null);
						}
					}
					return JdtUtils.getJavaType(file.getProject(), className);
				}
			}
		}
		return null;
	}
}
