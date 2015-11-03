/*******************************************************************************
 * Copyright (c) 2014-2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil.ValueParser;

/**
 * Parser that always fails, regardless of the input. Used for types who's value cannot be
 * expressed as a 'scalar' string value.
 *
 * @author Kris De Volder
 */
public class AlwaysFailingParser implements ValueParser {

	private String typeName;

	public AlwaysFailingParser(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public Object parse(String str) {
		throw new IllegalArgumentException("'"+str+"' is not valid for type '"+typeName+"'");
	}

}
