/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model;

import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * Defines a holder of a value.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IBeansValueHolder extends IBeansModelElement,
		ISourceModelElement {

	Object getValue();
}
