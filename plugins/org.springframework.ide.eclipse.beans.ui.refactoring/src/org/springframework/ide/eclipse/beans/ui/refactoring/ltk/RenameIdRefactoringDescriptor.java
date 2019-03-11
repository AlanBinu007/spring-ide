/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.ltk;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.springframework.ide.eclipse.beans.ui.refactoring.Activator;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @author Martin Lippert
 * @since 2.0
 */
public class RenameIdRefactoringDescriptor extends RefactoringDescriptor {

	public static final String REFACTORING_ID = Activator.PLUGIN_ID
			+ ".ltk.renameIdRefactoring";

	private final Map<String, String> arguments;

	public RenameIdRefactoringDescriptor(String project, String description,
			String comment, Map<String, String> arguments) {
		super(REFACTORING_ID, project, description, comment,
				RefactoringDescriptor.STRUCTURAL_CHANGE
						| RefactoringDescriptor.MULTI_CHANGE);
		this.arguments = arguments;
	}

	@Override
	public Refactoring createRefactoring(RefactoringStatus status)
			throws CoreException {
		RenameIdRefactoring refactoring = new RenameIdRefactoring();
		status.merge(refactoring.initialize(arguments));
		return refactoring;
	}

	public Map<String, String> getArguments() {
		return arguments;
	}
}
