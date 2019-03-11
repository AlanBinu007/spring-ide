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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractIdContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistProposalRecorder;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * {@link IContentAssistCalculator} that calculates bean id proposals based on the bean class.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class BeanIdContentAssistCalculator extends AbstractIdContentAssistCalculator {

	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		addBeanIdProposal(context, recorder);
	}

	private void addBeanIdProposal(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		String className = BeansEditorUtils.getClassNameForBean(context.getNode());
		if (className != null) {
			createBeanIdProposals(context, recorder, className);

			// add interface proposals
			IFile file = context.getFile();
			if (file != null && file.exists()) {
				IType type = JdtUtils.getJavaType(file.getProject(), className);
				Set<IType> allInterfaces = Introspector.getAllImplementedInterfaces(type);
				for (IType interf : allInterfaces) {
					createBeanIdProposals(context, recorder, interf.getFullyQualifiedName());
				}
			}
		}
	}

}
