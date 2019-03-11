/*******************************************************************************
 * Copyright (c) 2005, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;
import org.springframework.ide.eclipse.beans.core.tests.MarkerAssertion;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * Test case to test the {@link NamespaceElementsRule}.
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @since 2.2.7
 */
public class NamespaceElementsRuleTest extends BeansCoreTestCase {

	private IResource resource;

	@BeforeClass
	public static void setUpAll() {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			/*
			 * Set non-locking class-loader for windows testing
			 */
			InstanceScope.INSTANCE.getNode(SpringCore.PLUGIN_ID).putBoolean(
					SpringCore.USE_NON_LOCKING_CLASSLOADER, true);
		}
	}
	
	@Before
	public void setUp() throws Exception {
		// Enable XSD tool annotation rule
		Set<ValidationRuleDefinition> rules = ValidationRuleDefinitionFactory
				.getRuleDefinitions("org.springframework.ide.eclipse.beans.core.beansvalidator");
		for (ValidationRuleDefinition rule : rules) {
			if ("org.springframework.ide.eclipse.beans.core.toolAnnotation-org.springframework.ide.eclipse.beans.core.beansvalidator"
					.equals(rule.getId())) {
				rule.setEnabled(true, null);
			}
		}

		resource = createPredefinedProjectAndGetResource("validation", "src/sts-385.xml");
		StsTestUtil.waitForResource(resource);
	}
	
	@After
	public void tearDown() throws Exception {
		resource.getProject().delete(true, null);
	}

	@Test
	public void testNamespaceValidation() throws Exception {

		MarkerAssertion[] assertions = new MarkerAssertion[] {
				new MarkerAssertion("'java.lang.String' is not a sub type of 'java.util.List'", 13),
				new MarkerAssertion("Class 'java.lang.NoSuchClass' not found", 14),
				new MarkerAssertion("Method 'nosuchmethod' not found in class 'java.lang.String'", 20),
				new MarkerAssertion("Referenced bean 'target2' not found", 21, IMarker.SEVERITY_WARNING),
				new MarkerAssertion("Field 'NO_SUCH_FIELD' not found on class 'org.springframework.core.Ordered'", 25),
				new MarkerAssertion("Class 'org.springframework.core.NoSuchClass' not found", 26) };

		IMarker[] markers = resource.findMarkers(BeansCorePlugin.PLUGIN_ID + ".problemmarker", false,
				IResource.DEPTH_ZERO);
		assertTrue(markers.length >= 8);
		MarkerAssertion.assertMarker(markers, assertions);
	}
}
