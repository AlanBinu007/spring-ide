/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.yaml.reconcile;

import static org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesProblemType.YAML_SYNTAX_ERROR;

import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.boot.properties.editor.FuzzyMap;
import org.springframework.ide.eclipse.boot.properties.editor.PropertyInfo;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtilProvider;
import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblem;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlASTProvider;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.YamlASTReconciler;
import org.springframework.ide.eclipse.editor.support.yaml.reconcile.YamlReconcileEngine;

public class ApplicationYamlReconcileEngine extends YamlReconcileEngine {

	private SpringPropertyIndexProvider indexProvider;
	private TypeUtilProvider typeUtilProvider;

	public ApplicationYamlReconcileEngine(YamlASTProvider astProvider, SpringPropertyIndexProvider indexProvider, TypeUtilProvider typeUtilProvider) {
		super(astProvider);
		this.indexProvider = indexProvider;
		this.typeUtilProvider = typeUtilProvider;
	}

	protected YamlASTReconciler getASTReconciler(IDocument doc, IProblemCollector problemCollector) {
		FuzzyMap<PropertyInfo> index = indexProvider.getIndex(doc);
		if (index!=null && !index.isEmpty()) {
			IndexNavigator nav = IndexNavigator.with(index);
			return new ApplicationYamlASTReconciler(problemCollector, nav, typeUtilProvider.getTypeUtil(doc));
		}
		return null;
	}

	@Override
	protected ReconcileProblem syntaxError(String msg, int offset, int len) {
		return SpringPropertyProblem.problem(YAML_SYNTAX_ERROR, msg, offset, len);
	}

}
