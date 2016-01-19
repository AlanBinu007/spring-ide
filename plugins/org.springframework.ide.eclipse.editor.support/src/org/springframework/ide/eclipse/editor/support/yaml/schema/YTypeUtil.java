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
package org.springframework.ide.eclipse.editor.support.yaml.schema;

import java.util.List;
import java.util.Map;

/**
 * An implementation of YTypeUtil provides implementations of various
 * methods operating on YTypes and interpreting them in some context
 * (e.g. the meaning of YType objects may depend on types resolved
 * from the current project's classpath).
 *
 * @author Kris De Volder
 */
public interface YTypeUtil {
	boolean isAtomic(YType type);
	boolean isMap(YType type);
	boolean isSequencable(YType type);
	YType getDomainType(YType type);
	String[] getHintValues(YType yType);

	//TODO: only one of these two should be enough?
	List<YTypedProperty> getProperties(YType type);
	Map<String, YType> getPropertiesMap(YType yType);
	String niceTypeName(YType type);
}
