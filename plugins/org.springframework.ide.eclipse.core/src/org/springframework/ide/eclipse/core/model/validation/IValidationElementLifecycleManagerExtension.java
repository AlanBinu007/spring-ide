/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;

/**
 * Extension to the {@link IValidationElementLifecycleManager} interface that adds additional
 * methods.
 * <p>
 * NOTE: Methods will be called in the following order.
 * <ul>
 * <li>
 * {@link IValidationElementLifecycleManagerExtension#setKind(int)}</li>
 * <li>
 * {@link IValidationElementLifecycleManager#init(IResource)}</li>
 * <li>
 * {@link IValidationElementLifecycleManager#getRootElement()}</li>
 * <li>
 * {@link IValidationElementLifecycleManager#getContextElements()}</li>
 * <li>
 * {@link IValidationElementLifecycleManager#destory()}</li>
 * </ul>
 * @author Christian Dupuis
 * @since 2.2.3
 */
public interface IValidationElementLifecycleManagerExtension extends
		IValidationElementLifecycleManager {

	/**
	 * Sets the kind of the build. See {@link IncrementalProjectBuilder}
	 * @param kind the kind of the build
	 */
	void setKind(int kind);

}
