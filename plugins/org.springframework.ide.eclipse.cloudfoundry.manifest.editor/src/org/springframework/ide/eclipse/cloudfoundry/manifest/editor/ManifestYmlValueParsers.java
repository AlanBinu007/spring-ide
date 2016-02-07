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
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.editor.support.util.ValueParser;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Methods and constants to create/get parsers for some atomic types
 * used in manifest yml schema.
 *
 * @author Kris De Volder
 */
public class ManifestYmlValueParsers {

	public static final ValueParser POS_INTEGER = integerRange(0, null);

	public static final ValueParser MEMORY = new ValueParser() {

		private final ImmutableSet<String> GIGABYTE = ImmutableSet.of("G", "GB");
		private final ImmutableSet<String> MEGABYTE = ImmutableSet.of("M", "MB");
		private final Set<String> UNITS = Sets.union(GIGABYTE, MEGABYTE);

		@Override
		public Object parse(String str) {
			str = str.trim();
			String unit = getUnit(str.toUpperCase());
			if (unit==null) {
				throw new NumberFormatException(
						"'"+str+"' doesn't end with a valid unit of memory ('M', 'MB', 'G' or 'GB')"
				);
			}
			str = str.substring(0, str.length()-unit.length());
			int unitSize = GIGABYTE.contains(unit)?1024:1;
			int value = Integer.parseInt(str);
			if (value<0) {
				throw new NumberFormatException("Negative value is not allowed");
			}
			return value * unitSize;
		}

		private String getUnit(String str) {
			for (String u : UNITS) {
				if (str.endsWith(u)) {
					return u;
				}
			}
			return null;
		}
	};

	public static ValueParser integerAtLeast(final Integer lowerBound) {
		return integerRange(lowerBound, null);
	}

	public static ValueParser integerRange(final Integer lowerBound, final Integer upperBound) {
		Assert.isLegal(lowerBound==null || upperBound==null || lowerBound <= upperBound);
		return new ValueParser() {
			@Override
			public Object parse(String str) {
				int value = Integer.parseInt(str);
				if (lowerBound!=null && value<lowerBound) {
					if (lowerBound==0) {
						throw new NumberFormatException("Value must be positive");
					} else {
						throw new NumberFormatException("Value must be at least "+lowerBound);
					}
				}
				if (upperBound!=null && value>upperBound) {
					throw new NumberFormatException("Value must be at most "+upperBound);
				}
				return value;
			}
		};
	}

}
