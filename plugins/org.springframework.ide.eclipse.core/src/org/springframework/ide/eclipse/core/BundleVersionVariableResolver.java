/*******************************************************************************
 * Copyright (c) 2010, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.osgi.framework.Bundle;

/**
 * {@link IDynamicVariableResolver} that is capable to resolve <code>$
 * {bundle_version}</code> to the bundle location.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.3.1
 */
public class BundleVersionVariableResolver implements IDynamicVariableResolver {

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (argument != null) {
			Bundle bundle = Platform.getBundle(argument);
			if (bundle != null) {
				return bundle.getHeaders().get("Bundle-Version").toString();
			}
		}
		return null;
	}

}
