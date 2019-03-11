/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.type.asm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.Type;
import org.springframework.ide.eclipse.core.type.AnnotationMetadata;

/**
 * ASM class visitor which looks for the class name and implemented types as
 * well as for the annotations defined on the class, exposing them through the
 * {@link org.springframework.ide.eclipse.core.type.AnnotationMetadata}interface.
 * 
 * @author Christian Dupuis
 * @author Juergen Hoeller
 * @author Martin Lippert
 * @since 2.0.2
 * @see ClassMetadataReadingVisitor
 */
public class AnnotationMetadataReadingVisitor extends ClassMetadataReadingVisitor implements AnnotationMetadata {

	private final Map<String, Map<String, Object>> attributesMap = new LinkedHashMap<String, Map<String, Object>>();

	public AnnotationVisitor visitAnnotation(final String desc, boolean visible) {
		final String className = Type.getType(desc).getClassName();
		final Map<String, Object> attributes = new LinkedHashMap<String, Object>();
		return new EmptyAnnotationVisitor() {
			public void visit(String name, Object value) {
				attributes.put(name, value);
			}

			public void visitEnd() {
				attributesMap.put(className, attributes);
			}
		};
	}

	public Set<String> getAnnotationTypes() {
		return this.attributesMap.keySet();
	}

	public boolean hasAnnotation(String annotationType) {
		return this.attributesMap.containsKey(annotationType);
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		return this.attributesMap.get(annotationType);
	}

	public String getEnclosingClassName() {
		return null;
	}

	public boolean hasEnclosingClass() {
		return false;
	}

	public boolean isIndependent() {
		return false;
	}

	public boolean isFinal() {
		return false;
	}

}
