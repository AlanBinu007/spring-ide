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

import java.util.Map;

/**
 * Defines a holder of a {@link Map.Entry}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IBeansMapEntry extends IBeansValueHolder {

	Object getKey();
}
