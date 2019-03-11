/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.jdt.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.data.internal.validation.InvalidDerivedQueryRule;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
/**
 * @author Oliver Gierke
 * @deprecated replaced with {@link InvalidDerivedQueryRule}.
 */
public class SpringDataCompilationParticipant extends CompilationParticipant {

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.CompilationParticipant#isActive(org.eclipse.jdt.core.IJavaProject)
	 */
	@Override
	public boolean isActive(IJavaProject project) {
		return SpringCoreUtils.isSpringProject(project.getResource());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.compiler.CompilationParticipant#reconcile(org.eclipse.jdt.core.compiler.ReconcileContext)
	 */
	@Override
	public void reconcile(ReconcileContext context) {

		try {

			CompilationUnit compilationUnit = context.getAST3();
			ITypeRoot typeRoot = compilationUnit.getTypeRoot();
			IType type = typeRoot.findPrimaryType();

			// Skip non-interfaces
			if (type == null || !type.isInterface() || type.isAnnotation()) {
				super.reconcile(context);
				return;
			}
			
			// Skip non-spring-data repositories
			if (!RepositoryInformation.isSpringDataRepository(type)) {
				super.reconcile(context);
				return;
			}

			// resolve repository information and generate problem markers
			RepositoryInformation information = new RepositoryInformation(type);

			Class<?> domainClass = information.getManagedDomainClass();
			if (domainClass == null) {
				super.reconcile(context);
				return;
			}

			List<CategorizedProblem> problems = new ArrayList<CategorizedProblem>();

			for (IMethod method : information.getMethodsToValidate()) {

				String methodName = method.getElementName();

				try {
					new PartTree(methodName, domainClass);
				} catch (PropertyReferenceException e) {
					problems.add(new InvalidDerivedQueryProblem(method, e.getMessage()));
				}
			}

			context.putProblems("org.eclipse.jdt.core.problem", problems.toArray(new CategorizedProblem[problems.size()]));

		} catch (JavaModelException e) {
			SpringCore.log(e);
		} catch (Exception e) {
			SpringCore.log(e);
		} catch (Error e) {
			SpringCore.log(e);
		}

		super.reconcile(context);
	}
}
