/*******************************************************************************
 * Copyright (c) 2013, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.refactoring.RenameTypeArguments;
import org.eclipse.ltk.internal.core.refactoring.resource.RenameResourceProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.core.groovy.tests.Activator;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.refactoring.model.BeansJavaConfigRenameTypeRefactoringParticipant;
import org.springframework.ide.eclipse.beans.ui.refactoring.model.BeansJavaConfigTypeChange;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
@SuppressWarnings("restriction")
public class BeansJavaGroovyConfigRenameTypeRefactoringParticipantTest {
	
	private IProject project;
	private BeansModel model;
	private BeansProject beansProject;
	private IJavaProject javaProject;

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
	public void createProject() throws Exception {
		project = StsTestUtil.createPredefinedProject("beans-config-tests", Activator.PLUGIN_ID);
		javaProject = JdtUtils.getJavaProject(project);
		
		model = new BeansModel();
		beansProject = new BeansProject(model, project);
		model.addProject(beansProject);
	}
	
	@After
	public void deleteProject() throws Exception {
		new ACondition("Wait for Jobs") {
			@Override
			public boolean test() throws Exception {
				assertJobManagerIdle();
				return true;
			}
		}.waitFor(3 * 60 * 1000);
		project.delete(true, null);
	}
	
	@Test
	public void testBasicTypeRename() throws Exception {
		IType configClass = javaProject.findType("org.test.spring.SimpleConfigurationClass");
		beansProject.addConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.SimpleConfigurationClass", IBeansConfig.Type.MANUAL);
		
		BeansJavaConfigRenameTypeRefactoringParticipant participant = new BeansJavaConfigRenameTypeRefactoringParticipant();
		RenameTypeArguments arguments = new RenameTypeArguments("NewClassName", true, false, null);
		RenameResourceProcessor processor = new RenameResourceProcessor(configClass.getResource());
		participant.initialize(processor, configClass, arguments);
		BeansJavaConfigTypeChange change = (BeansJavaConfigTypeChange) participant.createChange(new NullProgressMonitor());
		change.setBeansModel(model);
		change.perform(new NullProgressMonitor());
		
		assertNull(beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.SimpleConfigurationClass"));
		IBeansConfig newConfig = beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.NewClassName");
		assertNotNull(newConfig);
	}

	@Test
	public void testTypeRenameInnerConfigurationClass() throws Exception {
		IType configClass = javaProject.findType("org.test.spring.OuterConfigurationClass$InnerConfigurationClass");
		beansProject.addConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.OuterConfigurationClass$InnerConfigurationClass", IBeansConfig.Type.MANUAL);
		
		BeansJavaConfigRenameTypeRefactoringParticipant participant = new BeansJavaConfigRenameTypeRefactoringParticipant();
		RenameTypeArguments arguments = new RenameTypeArguments("NewClassName", true, false, null);
		RenameResourceProcessor processor = new RenameResourceProcessor(configClass.getResource());
		participant.initialize(processor, configClass, arguments);
		BeansJavaConfigTypeChange change = (BeansJavaConfigTypeChange) participant.createChange(new NullProgressMonitor());
		change.setBeansModel(model);
		change.perform(new NullProgressMonitor());
		
		assertNull(beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.OuterConfigurationClass$InnerConfigurationClass"));
		IBeansConfig newConfig = beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.OuterConfigurationClass$NewClassName");
		assertNotNull(newConfig);
	}

	@Test
	public void testTypeRenameOuterConfigurationClass() throws Exception {
		IType outerClass = javaProject.findType("org.test.spring.OuterConfigurationClass");
		beansProject.addConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.OuterConfigurationClass$InnerConfigurationClass", IBeansConfig.Type.MANUAL);
		
		BeansJavaConfigRenameTypeRefactoringParticipant participant = new BeansJavaConfigRenameTypeRefactoringParticipant();
		RenameTypeArguments arguments = new RenameTypeArguments("NewClassName", true, false, null);
		RenameResourceProcessor processor = new RenameResourceProcessor(outerClass.getResource());
		participant.initialize(processor, outerClass, arguments);
		BeansJavaConfigTypeChange change = (BeansJavaConfigTypeChange) participant.createChange(new NullProgressMonitor());
		change.setBeansModel(model);
		change.perform(new NullProgressMonitor());
		
		assertNull(beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.OuterConfigurationClass$InnerConfigurationClass"));
		IBeansConfig newConfig = beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.NewClassName$InnerConfigurationClass");
		assertNotNull(newConfig);
	}

	@Test
	public void testTypeRenameDoubleOuterOuterConfigurationClass() throws Exception {
		IType outerClass = javaProject.findType("org.test.spring.DoubleOuterConfigurationClass");
		beansProject.addConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass", IBeansConfig.Type.MANUAL);
		
		BeansJavaConfigRenameTypeRefactoringParticipant participant = new BeansJavaConfigRenameTypeRefactoringParticipant();
		RenameTypeArguments arguments = new RenameTypeArguments("NewClassName", true, false, null);
		RenameResourceProcessor processor = new RenameResourceProcessor(outerClass.getResource());
		participant.initialize(processor, outerClass, arguments);
		BeansJavaConfigTypeChange change = (BeansJavaConfigTypeChange) participant.createChange(new NullProgressMonitor());
		change.setBeansModel(model);
		change.perform(new NullProgressMonitor());
		
		assertNull(beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass"));
		IBeansConfig newConfig = beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.NewClassName$OuterConfigurationClass$InnerConfigurationClass");
		assertNotNull(newConfig);
	}

	@Test
	public void testTypeRenameDoubleOuterConfigurationClass() throws Exception {
		IType outerClass = javaProject.findType("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass");
		beansProject.addConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass", IBeansConfig.Type.MANUAL);
		
		BeansJavaConfigRenameTypeRefactoringParticipant participant = new BeansJavaConfigRenameTypeRefactoringParticipant();
		RenameTypeArguments arguments = new RenameTypeArguments("NewClassName", true, false, null);
		RenameResourceProcessor processor = new RenameResourceProcessor(outerClass.getResource());
		participant.initialize(processor, outerClass, arguments);
		BeansJavaConfigTypeChange change = (BeansJavaConfigTypeChange) participant.createChange(new NullProgressMonitor());
		change.setBeansModel(model);
		change.perform(new NullProgressMonitor());
		
		assertNull(beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass"));
		IBeansConfig newConfig = beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.DoubleOuterConfigurationClass$NewClassName$InnerConfigurationClass");
		assertNotNull(newConfig);
	}

	@Test
	public void testTypeRenameDoubleOuterInnerConfigurationClass() throws Exception {
		IType outerClass = javaProject.findType("org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass");
		beansProject.addConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass", IBeansConfig.Type.MANUAL);
		
		BeansJavaConfigRenameTypeRefactoringParticipant participant = new BeansJavaConfigRenameTypeRefactoringParticipant();
		RenameTypeArguments arguments = new RenameTypeArguments("NewClassName", true, false, null);
		RenameResourceProcessor processor = new RenameResourceProcessor(outerClass.getResource());
		participant.initialize(processor, outerClass, arguments);
		BeansJavaConfigTypeChange change = (BeansJavaConfigTypeChange) participant.createChange(new NullProgressMonitor());
		change.setBeansModel(model);
		change.perform(new NullProgressMonitor());
		
		assertNull(beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$InnerConfigurationClass"));
		IBeansConfig newConfig = beansProject.getConfig(BeansConfigFactory.JAVA_CONFIG_TYPE + "org.test.spring.DoubleOuterConfigurationClass$OuterConfigurationClass$NewClassName");
		assertNotNull(newConfig);
	}

}
