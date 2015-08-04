/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.List;

public interface ModifiableModel {

	abstract public boolean canBeAdded(List<Object> sources);

	abstract public void add(List<Object> sources, UserInteractions ui) throws Exception;

	abstract public void delete(List<BootDashElement> toRemove, UserInteractions ui);

}
