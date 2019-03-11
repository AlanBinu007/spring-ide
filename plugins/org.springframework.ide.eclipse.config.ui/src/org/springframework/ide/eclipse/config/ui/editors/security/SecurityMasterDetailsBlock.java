/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.security;

import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigDetailsPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigFormPage;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigMasterPart;
import org.springframework.ide.eclipse.config.ui.editors.AbstractNamespaceMasterDetailsBlock;

/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class SecurityMasterDetailsBlock extends AbstractNamespaceMasterDetailsBlock {

	public static final String DOCS_SPRINGSECURITY_20 = "https://static.springsource.org/spring-security/site/docs/2.0.x/reference/springsecurity.html"; //$NON-NLS-1$

	public static final String DOCS_SPRINGSECURITY_30 = "https://static.springsource.org/spring-security/site/docs/3.0.x/reference/springsecurity.html"; //$NON-NLS-1$

	public SecurityMasterDetailsBlock(AbstractConfigFormPage page) {
		super(page);
	}

	@Override
	protected AbstractConfigMasterPart createMasterSectionPart(AbstractConfigFormPage page, Composite parent) {
		return new SecurityMasterPart(page, parent);
	}

	@Override
	public AbstractConfigDetailsPart getDetailsPage(Object key) {
		if (getFormPage().getSchemaVersion().compareTo(new Version("3.0")) >= 0) { //$NON-NLS-1$
			return new SecurityDetailsPart(getMasterPart(), DOCS_SPRINGSECURITY_30);
		}
		else {
			return new SecurityDetailsPart(getMasterPart(), DOCS_SPRINGSECURITY_20);
		}
	}

}
