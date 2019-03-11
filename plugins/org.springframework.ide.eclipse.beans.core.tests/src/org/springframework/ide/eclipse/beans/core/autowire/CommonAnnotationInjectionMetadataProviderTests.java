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
package org.springframework.ide.eclipse.beans.core.autowire;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.AutowireDependencyProvider;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Tomasz Zarna
 *
 */
public class CommonAnnotationInjectionMetadataProviderTests extends BeansCoreTestCase {

	private IProject project;
	private IBeansModel model;
	private IBeansProject beansProject;

	@BeforeClass
	public static void setUp() {
		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			/*
			 * Set non-locking class-loader for windows testing
			 */
			InstanceScope.INSTANCE.getNode(SpringCore.PLUGIN_ID).putBoolean(
					SpringCore.USE_NON_LOCKING_CLASSLOADER, true);
		}
	}

	@Before
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("autowire", "org.springframework.ide.eclipse.beans.core.tests");
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
	}
	
	@After
	public void deleteProject() throws Exception {
		project.delete(true, null);
	}

	@Test
	public void testResourceInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/context/annotation/testResourceInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 70 });
		allowedRefs.put("testBean2", new Integer[] { 110 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 2);
		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testExtendedResourceInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/context/annotation/testExtendedResourceInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 70 });
		allowedRefs.put("testBean2", new Integer[] { 143 });
		allowedRefs.put("testBean3", new Integer[] { 148 });
		allowedRefs.put("testBean4", new Integer[] { 130 });
		allowedRefs.put("xy", new Integer[] { 135, 153, 236 });
		allowedRefs.put("beanFactory", new Integer[] { 140 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 2);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 7);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}

		bean = BeansModelUtils.getBean("annotatedBean2", config);

		assertTrue(references.containsKey(bean));

		refs = references.get(bean);
		assertTrue(refs.size() == 1);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testExtendedResourceInjectionWithOverriding() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/context/annotation/testExtendedResourceInjectionWithOverriding-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 70 });
		allowedRefs.put("testBean3", new Integer[] { 148 });
		allowedRefs.put("testBean4", new Integer[] { 130 });
		allowedRefs.put("xy", new Integer[] { 135, 153 });
		allowedRefs.put("beanFactory", new Integer[] { 140 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		
		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 6);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

	@Test
	public void testExtendedEjbInjection() throws Exception {
		BeansConfig config = new BeansConfig(beansProject, "src/org/springframework/context/annotation/testExtendedEjbInjection-context.xml", IBeansConfig.Type.MANUAL);

		Map<String, Integer[]> allowedRefs = new HashMap<String, Integer[]>();
		allowedRefs.put("testBean", new Integer[] { 70 });
		allowedRefs.put("testBean2", new Integer[] { 196 });
		allowedRefs.put("testBean3", new Integer[] { 201 });
		allowedRefs.put("testBean4", new Integer[] { 183 });
		allowedRefs.put("xy", new Integer[] { 188, 206 });
		allowedRefs.put("beanFactory", new Integer[] { 193 });

		AutowireDependencyProvider provider = new AutowireDependencyProvider(config, config);
		Map<IBean, Set<IBeanReference>> references = provider.resolveAutowiredDependencies();
		IBean bean = BeansModelUtils.getBean("annotatedBean", config);

		assertTrue(references.size() == 1);
		assertTrue(references.containsKey(bean));

		Set<IBeanReference> refs = references.get(bean);
		assertTrue(refs.size() == 7);

		for (IBeanReference ref : refs) {
			assertTrue(allowedRefs.containsKey(ref.getBeanName()));
			assertTrue(Arrays.asList(allowedRefs.get(ref.getBeanName())).contains(
					ref.getElementSourceLocation().getStartLine()));
		}
	}

}