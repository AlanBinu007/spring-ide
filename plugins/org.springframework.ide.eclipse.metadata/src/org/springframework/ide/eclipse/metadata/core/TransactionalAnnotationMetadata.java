/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.metadata.core;

import java.util.Set;

import org.springframework.ide.eclipse.beans.core.metadata.model.AbstractAnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link IBeanMetadata} for the {@link Transactional} annotation.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class TransactionalAnnotationMetadata extends AbstractAnnotationMetadata {

	private static final long serialVersionUID = 6978657032146327628L;

	public TransactionalAnnotationMetadata(IBean bean, String handle, Object value,
			IModelSourceLocation location, Set<IMethodMetadata> methodMetaData) {
		super(bean, handle, value, location, methodMetaData);
	}

}
