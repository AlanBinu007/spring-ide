/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
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
 * {@link IContentAssistCalculator} that can be used to calculate proposals for class or interfaces
 * that are sub types of a given type name.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class ClassHierachyContentAssistCalculator implements IContentAssistCalculator {

	private final String typeName;
	
	private final int flags;

	/**
	 * Constructor
	 * @param typeName the name of the root type
	 */
	public ClassHierachyContentAssistCalculator(String typeName) {
		this(typeName, BeansJavaCompletionUtils.FLAG_CLASS | BeansJavaCompletionUtils.FLAG_INTERFACE);
	}

	/**
	 * Constructor
	 * @param typeName the name of the root type
	 * @param flags
	 */
	public ClassHierachyContentAssistCalculator(String typeName, int flags) {
		this.typeName = typeName;
		this.flags = flags;
	}

	/**
	 * Compute proposals. This implementation simply delegates to
	 * {@link BeansJavaCompletionUtils#addTypeHierachyAttributeValueProposals()}.
	 */
	public void computeProposals(IContentAssistContext context,
			IContentAssistProposalRecorder recorder) {
		BeansJavaCompletionUtils
				.addTypeHierachyAttributeValueProposals(context, recorder, typeName, flags);
	}
}
