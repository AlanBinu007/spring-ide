/*******************************************************************************
 * Copyright (c) 2006, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model;

import java.lang.reflect.Method;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.springframework.beans.BeanUtils;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeanAspectDefinition implements IAspectDefinition, IAdaptable, IPersistableElement {

	protected String adviceMethodName;

	protected String[] adviceMethodParameterTypes = new String[0];

	protected String[] argNames;

	protected String aspectClassName;

	protected int aspectStartLineNumber = -1;

	protected int aspectEndLineNumber = -1;

	protected String aspectName = "";

	protected IResource file;

	protected boolean isProxyTargetClass = false;

	protected String pointcutExpressionString = null;

	protected String returning;

	protected String throwing;

	protected IAopReference.ADVICE_TYPE type;

	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(adviceMethodName);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(adviceMethodParameterTypes);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(argNames);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(aspectClassName);
		hashCode = hashCode + aspectEndLineNumber;
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(aspectName);
		hashCode = hashCode + aspectStartLineNumber;
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(returning);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(file);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(type);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(pointcutExpressionString);
		hashCode = hashCode + ObjectUtils.nullSafeHashCode(throwing);
		return 12 * hashCode;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeanAspectDefinition)) {
			return false;
		}
		BeanAspectDefinition that = (BeanAspectDefinition) other;
		if (!ObjectUtils.nullSafeEquals(this.adviceMethodName, that.adviceMethodName))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.adviceMethodParameterTypes, that.adviceMethodParameterTypes))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.argNames, that.argNames))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.aspectClassName, that.aspectClassName))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.aspectName, that.aspectName))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.aspectEndLineNumber, that.aspectEndLineNumber))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.aspectStartLineNumber, that.aspectStartLineNumber))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.returning, that.returning))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.file, that.file))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.type, that.type))
			return false;
		if (!ObjectUtils.nullSafeEquals(this.pointcutExpressionString, that.pointcutExpressionString))
			return false;
		return ObjectUtils.nullSafeEquals(this.throwing, that.throwing);
	}

	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPersistableElement.class)) {
			return this;
		}
		return null;
	}

	public Method getAdviceMethod() {
		try {
			Class<?> aspectClass = ClassUtils.loadClass(this.aspectClassName);
			Method method = BeanUtils.resolveSignature(this.adviceMethodName, aspectClass);
			return method;
		}
		catch (ClassNotFoundException e) {
			return null;
		}
	}

	public String getAdviceMethodName() {
		return adviceMethodName;
	}

	public String[] getAdviceMethodParameterTypes() {
		return this.adviceMethodParameterTypes;
	}

	public String[] getArgNames() {
		return argNames;
	}

	public String getAspectClassName() {
		return aspectClassName;
	}

	public int getAspectStartLineNumber() {
		return aspectStartLineNumber;
	}

	public String getAspectName() {
		return aspectName;
	}

	public String getFactoryId() {
		return BeanAspectDefinitionElementFactory.FACTORY_ID;
	}

	public String getPointcutExpression() {
		return this.pointcutExpressionString;
	}

	public IResource getResource() {
		return file;
	}

	public String getReturning() {
		return returning;
	}

	public String getThrowing() {
		return throwing;
	}

	public IAopReference.ADVICE_TYPE getType() {
		return type;
	}

	public boolean isProxyTargetClass() {
		return this.isProxyTargetClass;
	}
	
	public void saveState(IMemento memento) {
		memento.putString(BeanAspectDefinitionElementFactory.ADVICE_METHOD_NAME_ATTRIBUTE, this.adviceMethodName);
		memento.putString(BeanAspectDefinitionElementFactory.ADVICE_CLASS_NAME_ATTRIBUTE, this.aspectClassName);
		if (this.adviceMethodParameterTypes != null && this.adviceMethodParameterTypes.length > 0) {
			memento.putString(BeanAspectDefinitionElementFactory.ADVICE_METHOD_PARAMETER_TYPES_ATTRIBUTE, StringUtils
					.arrayToCommaDelimitedString(this.adviceMethodParameterTypes));
		}
		memento.putString(BeanAspectDefinitionElementFactory.ASPECT_NAME_ATTRIBUTE, this.aspectName);
		memento.putString(BeanAspectDefinitionElementFactory.POINTCUT_EXPRESSION_ATTRIBUTE,
				this.pointcutExpressionString);
		memento.putString(BeanAspectDefinitionElementFactory.RETURNING_ATTRIBUTE, this.returning);
		memento.putString(BeanAspectDefinitionElementFactory.THROWING_ATTRIBUTE, this.throwing);
		if (this.argNames != null && this.argNames.length > 0) {
			memento.putString(BeanAspectDefinitionElementFactory.ARG_NAMES_ATTRIBUTE, StringUtils
					.arrayToCommaDelimitedString(this.argNames));
		}
		memento.putInteger(BeanAspectDefinitionElementFactory.ASPECT_START_LINE_NUMBER_ATTRIBUTE,
				this.aspectStartLineNumber);
		memento.putInteger(BeanAspectDefinitionElementFactory.ASPECT_END_LINE_NUMBER_ATTRIBUTE,
				this.aspectEndLineNumber);
		if (this.file != null) {
			memento.putString(BeanAspectDefinitionElementFactory.FILE_ATTRIBUTE, this.file.getFullPath().toString());
		}
		memento.putString(BeanAspectDefinitionElementFactory.PROXY_TARGET_CLASS_ATTRIBUTE, Boolean
				.toString(this.isProxyTargetClass));
		memento.putString(BeanAspectDefinitionElementFactory.ADVICE_TYPE_ATTRIBUTE, this.type.toString());
	}

	public void setAdviceMethodName(String adviceMethodName) {
		this.adviceMethodName = adviceMethodName;
	}

	public void setAdviceMethodParameterTypes(String[] params) {
		this.adviceMethodParameterTypes = params;
	}

	public void setArgNames(String[] argNames) {
		this.argNames = argNames;
	}

	public void setAspectClassName(String aspectClassName) {
		this.aspectClassName = aspectClassName;
	}

	public void setAspectStartLineNumber(int aspectLineNumber) {
		this.aspectStartLineNumber = aspectLineNumber;
	}

	public void setAspectEndLineNumber(int aspectLineNumber) {
		this.aspectEndLineNumber = aspectLineNumber;
	}

	public void setAspectName(String aspectName) {
		if (!StringUtils.hasText(aspectName)) {
			this.aspectName = "anonymous aspect";
		}
		else {
			this.aspectName = aspectName;
		}
	}

	public void setPointcutExpression(String expression) {
		this.pointcutExpressionString = expression;
	}

	public void setProxyTargetClass(boolean proxyTargetClass) {
		this.isProxyTargetClass = proxyTargetClass;
	}

	public void setResource(IResource file) {
		this.file = file;
	}

	public void setReturning(String returning) {
		this.returning = returning;
	}

	public void setThrowing(String throwing) {
		this.throwing = throwing;
	}

	public void setType(IAopReference.ADVICE_TYPE type) {
		this.type = type;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Aspect definition");
		if (this.file != null) {
			buf.append(" [");
			buf.append(this.file.getFullPath().toFile());
			buf.append(":");
			buf.append(getAspectStartLineNumber());
			buf.append("]");
		}
		buf.append(" advise type [");
		ADVICE_TYPE type = getType();
		if (type == ADVICE_TYPE.AFTER) {
			buf.append("after");
		}
		else if (type == ADVICE_TYPE.AFTER_RETURNING) {
			buf.append("after-returning");
		}
		else if (type == ADVICE_TYPE.AFTER_THROWING) {
			buf.append("after-throwing");
		}
		else if (type == ADVICE_TYPE.BEFORE) {
			buf.append("before");
		}
		else if (type == ADVICE_TYPE.AROUND) {
			buf.append("after");
		}
		else if (type == ADVICE_TYPE.DECLARE_PARENTS) {
			buf.append("delcare parents");
		}
		buf.append("] advise [");
		buf.append(getAspectClassName());
		if (type != ADVICE_TYPE.DECLARE_PARENTS) {
			buf.append(".");
			buf.append(getAdviceMethodName());
		}
		buf.append("]");
		return buf.toString();
	}

	public int getAspectEndLineNumber() {
		return this.aspectEndLineNumber;
	}
}
