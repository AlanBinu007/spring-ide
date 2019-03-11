/*******************************************************************************
 * Copyright (c) 2010, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core;

import java.io.File;

import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.osgi.framework.Bundle;

/**
 * {@link IDynamicVariableResolver} that is capable to resolve <code>$
 * {bundle_state_loc}</code> to the bundle location.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.3.1
 */
@SuppressWarnings("restriction")
public class BundleStateLocationVariableResolver implements IDynamicVariableResolver {

	public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
		if (argument != null) {
			Bundle bundle = Platform.getBundle(argument);
			if (bundle != null) {
				IPath stateLocation = InternalPlatform.getDefault().getStateLocation(bundle, true);
				String path = stateLocation.toString();
				if (path != null && path.endsWith(File.separator)) {
					return path.substring(0, path.length() - 1);
				}
				return path;
			}
		}
		return null;
	}

}
