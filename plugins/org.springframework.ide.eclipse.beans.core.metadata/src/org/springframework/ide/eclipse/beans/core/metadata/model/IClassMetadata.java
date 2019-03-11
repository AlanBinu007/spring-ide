/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.metadata.model;

import java.util.Set;

/**
 * Marker interface for class-level meta data.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IClassMetadata extends IBeanMetadata {
	
	/**
	 * Returns the {@link IMethodMetadata}. 
	 */
	Set<IMethodMetadata> getMethodMetaData();
}
