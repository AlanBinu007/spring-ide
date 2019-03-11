/*******************************************************************************
 * Copyright (c) 2007, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.validation;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ISourceRange;
import org.springframework.util.ObjectUtils;

/**
 * This class holds the information regarding a validation error.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class ValidationProblem {

	private String ruleId;
	private String errorId;
	private int severity;
	private String message;
	private int line;
	private IResource resource;
	private ValidationProblemAttribute[] attributes;

	public ValidationProblem(int severity, String message, IResource resource) {
		this(null, null, severity, message, resource, -1);
	}

	public ValidationProblem(int severity, String message, IResource resource, int line,
			ValidationProblemAttribute... attributes) {
		this(null, null, severity, message, resource, line, attributes);
	}

	public ValidationProblem(String ruleId, String errorId, int severity,
			String message, IResource resource, int line,
			ValidationProblemAttribute... attributes) {
		this.ruleId = ruleId;
		this.severity = severity;
		this.message = message;
		this.line = line;
		this.resource = resource;
		this.attributes = attributes;
		this.errorId = errorId;
	}

	public ValidationProblem(String errorId, int severity, String message, IResource resource, ISourceRange location) {
		this(null, errorId, severity, message, resource, -1, new ValidationProblemAttribute[] {
				new ValidationProblemAttribute(IMarker.CHAR_START, location.getOffset()),
				new ValidationProblemAttribute(IMarker.CHAR_END, location.getOffset()+location.getLength())
		});
	}

	/**
	 * Returns the ID of the {@link IValidationRule} which raised this problem
	 * or <code>null</code> if no validation rule was involved.
	 */
	public String getRuleId() {
		return ruleId;
	}

	/**
	 * Returns the ID of the error�which raised this problem
	 * or <code>null</code> if no error was involved.
	 */
	public String getErrorId() {
		return errorId;
	}

	/**
	 * Returns the severity level of this problem.
	 */
	public int getSeverity() {
		return severity;
	}

	/**
	 * Returns message text of this problem.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Returns the underlying resource.
	 */
	public IResource getResource() {
		return resource;
	}

	/**
	 * Returns the line number of this problem or <code>-1</code> if not
	 * available.
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Returns the attributes or <code>null</code> if not available.
	 */
	public ValidationProblemAttribute[] getAttributes() {
		return attributes;
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(ruleId);
		hashCode = 2 * hashCode + ObjectUtils.nullSafeHashCode(errorId);
		hashCode = 3 * hashCode + severity;
		hashCode = 4 * hashCode + ObjectUtils.nullSafeHashCode(message);
		hashCode = 5 * hashCode + line;
		hashCode = 6 * hashCode + ObjectUtils.nullSafeHashCode(attributes);
		hashCode = 7 * hashCode + ObjectUtils.nullSafeHashCode(resource);
		return 8 * hashCode;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ValidationProblem)) {
			return false;
		}
		ValidationProblem that = (ValidationProblem) other;
		if (!ObjectUtils.nullSafeEquals(this.ruleId, that.ruleId)) return false;
		if (!ObjectUtils.nullSafeEquals(this.errorId, that.errorId)) return false;
		if (this.severity != that.severity) return false;
		if (!ObjectUtils.nullSafeEquals(this.message, that.message)) return false;
		if (this.line != that.line) return false;
		if (!ObjectUtils.nullSafeEquals(this.attributes, that.attributes)) return false;
		if (!ObjectUtils.nullSafeEquals(this.resource, that.resource)) return false;
		return super.equals(other);
	}

	@Override
	public String toString() {
		return "ValidationProblem("+message+")";
	}
}
