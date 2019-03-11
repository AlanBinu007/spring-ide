/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

/**
 * @author Martin Lippert
 * @since 3.4.0
 */
public interface TypeHierarchyElementCacheFactory {
	
	public TypeHierarchyElementCache createTypeHierarchyElementCache();

}
