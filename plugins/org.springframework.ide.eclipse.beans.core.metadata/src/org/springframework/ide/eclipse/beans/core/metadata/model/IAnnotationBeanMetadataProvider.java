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
package org.springframework.ide.eclipse.beans.core.metadata.model;

import java.util.Set;

import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.java.annotation.IAnnotationMetadata;

/**
 * A specialized meta data provider that creates {@link IBeanMetadata} from annotation processing.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IAnnotationBeanMetadataProvider {
	 
	/**
	 * Contribute {@link IBeanMetadata} for a given {@link IBean}.
	 * <p>
	 * Note: implementations of this method are not allowed to return
	 * <code>null</code>.
	 * @param bean the current {@link IBean} to search for bean meta data
	 * @param type the {@link IType}
	 * @param visitor a pre-populated {@link IAnnotationMetadata} 
	 * @return a {@link Set} of {@link IBeanMetadata}
	 */ 
	Set<IBeanMetadata> provideBeanMetadata(IBean bean, IType type,	
			IAnnotationMetadata metadata);

}
