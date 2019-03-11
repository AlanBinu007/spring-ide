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
package org.springframework.ide.eclipse.beans.ui.editor.contentassist;

import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;

/**
 * {@link IContentAssistCalculator} that can be used to calculate proposals for classes or
 * interfaces.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class ClassContentAssistCalculator implements IContentAssistCalculator {

	private boolean isInterfaceRequired;

	private boolean useBoth;

	/**
	 * Default constructor
	 */
	public ClassContentAssistCalculator() {
		this.useBoth = true;
	}

	/**
	 * Constructor
	 * @param isInterfaceRequired true if only looking for interfaces
	 */
	public ClassContentAssistCalculator(boolean isInterfaceRequired) {
		this.isInterfaceRequired = isInterfaceRequired;
		this.useBoth = false;
	}

	/**
	 * Compute proposals. This implementation simply delegates to
	 * {@link BeansJavaCompletionUtils#addClassValueProposals()}
	 */
	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		if (useBoth) {
			BeansJavaCompletionUtils.addClassValueProposals(context, recorder,
					BeansJavaCompletionUtils.FLAG_PACKAGE | BeansJavaCompletionUtils.FLAG_CLASS
							| BeansJavaCompletionUtils.FLAG_INTERFACE);
		}
		else {
			if (isInterfaceRequired) {
				BeansJavaCompletionUtils.addInterfaceValueProposals(context, recorder);
			}
			else {
				BeansJavaCompletionUtils.addClassValueProposals(context, recorder);
			}
		}
	}
}
