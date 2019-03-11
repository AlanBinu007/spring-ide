/*******************************************************************************
 * Copyright (c) 2004, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.test;

import junit.framework.TestCase;

/**
 * Base class for the tests. Defines generic setUp and TearDown methods.
 * @author Loren Rosen
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class AbstractSpringIdeTest extends TestCase {

	protected TestProject project;

	public AbstractSpringIdeTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		project = new TestProject();
	}

	protected void tearDown() throws Exception {
		project.dispose();
	}
}
