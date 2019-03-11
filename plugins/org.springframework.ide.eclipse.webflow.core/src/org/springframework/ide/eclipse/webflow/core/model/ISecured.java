/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.model;

/**
 * @author Christian Dupuis
 * @since 2.1.0
 */
public interface ISecured extends IWebflowModelElement {
	
	void setRoleAttributes(String attributes);
	
	String getRoleAttributes();
	
	void setMatchType(String matchType);
	
	String getMatchType();
	
}
