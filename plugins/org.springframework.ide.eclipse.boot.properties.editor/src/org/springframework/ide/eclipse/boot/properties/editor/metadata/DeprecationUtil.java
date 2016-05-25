/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.metadata;

import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.springframework.boot.configurationmetadata.Deprecation;
import org.springframework.ide.eclipse.boot.util.Log;

import com.google.common.collect.ImmutableSet;

public class DeprecationUtil {

	private static final ImmutableSet<String> DEPRECATED_ANOT_NAMES = ImmutableSet.of(
			"org.springframework.boot.context.properties.DeprecatedConfigurationProperty",
			"DeprecatedConfigurationProperty",
			"java.lang.Deprecated",
			"Deprecated"
	);

	/**
	 * Extract {@link Deprecation} info from annotations on a {@link IJavaElement}
	 */
	public static Deprecation extract(IJavaElement je) {
		if (je instanceof IAnnotatable) {
			return extract((IAnnotatable)je);
		}
		return null;
	}

	/**
	 * Extract {@link Deprecation} info from annotations on a {@link IJavaElement}
	 */
	private static Deprecation extract(IAnnotatable m) {
		try {
			for (IAnnotation a : m.getAnnotations()) {
				if (DEPRECATED_ANOT_NAMES.contains(a.getElementName())) {
					Deprecation d = new Deprecation();
					for (IMemberValuePair pair : a.getMemberValuePairs()) {
						String name = pair.getMemberName();
						if (name.equals("reason")) {
							d.setReason((String) pair.getValue());
						} else if (name.equals("replacement")) {
							d.setReplacement((String) pair.getValue());
						}
					}
					return d;
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}


}
