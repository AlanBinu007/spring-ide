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
package org.springframework.ide.eclipse.beans.core.metadata.internal.model;

import java.io.Serializable;
import java.util.Set;

import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * Simple holder for {@link IBeanMetadata} and {@link IMethodMetadata} of one {@link IBean}.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public class BeanMetadataHolder implements Serializable {

	private static final long serialVersionUID = 5275241662588816808L;

	private long lastModified = -1;
	
	private String elemenetId;

	private Set<IBeanMetadata> beanMetaData;

	private Set<IMethodMetadata> methodMetaData;

	public String getElemenetId() {
		return elemenetId;
	}

	public void setElemenetId(String elemenetId) {
		this.elemenetId = elemenetId;
	}

	public Set<IBeanMetadata> getBeanMetaData() {
		return beanMetaData;
	}

	public void setBeanMetaData(Set<IBeanMetadata> beanMetaData) {
		this.beanMetaData = beanMetaData;
	}

	public Set<IMethodMetadata> getMethodMetaData() {
		return methodMetaData;
	}

	public void setMethodMetaData(Set<IMethodMetadata> methodMetaData) {
		this.methodMetaData = methodMetaData;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
}