/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.sse.ui.internal.contentassist.CustomCompletionProposal;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Utility class that creates content assist proposals for attribute names used
 * in the c namespace.
 * 
 * @author Leo Dos Santos
 * @since 2.8.0
 */
@SuppressWarnings("restriction")
public class ConstructorArgNameSearchRequestor {

	public static final int CONSTRUCTOR_RELEVANCE = 10;

	protected JavaElementImageProvider imageProvider;

	protected Set<String> argNames;

	protected ContentAssistRequest request;

	private String prefix;

	private boolean attrAtLocationHasValue;

	private String namespacePrefix = "";

	public ConstructorArgNameSearchRequestor(ContentAssistRequest request,
			String prefix, boolean attrAtLocationHasValue,
			String nameSpacePrefix) {
		this.request = request;
		this.argNames = new HashSet<String>();
		this.imageProvider = new JavaElementImageProvider();
		this.prefix = prefix;
		this.attrAtLocationHasValue = attrAtLocationHasValue;
		if (namespacePrefix != null) {
			this.namespacePrefix = nameSpacePrefix + ":";
		}
	}

	public void acceptSearchMatch(IMethod constructor) throws CoreException {
		if (Flags.isPublic(constructor.getFlags())
				&& ((IType) constructor.getParent()).isClass()
				&& constructor.isConstructor() && constructor.exists()) {
			createConstructorArgProposal(constructor);
		}
	}

	protected void createConstructorArgProposal(IMethod constructor) {
		try {
			String[] parameterNames = constructor.getParameterNames();
			String[] parameterTypes = JdtUtils
					.getParameterTypesString(constructor);
			for (String name : parameterNames) {
				if (!argNames.contains(name)) {
					String replaceText = namespacePrefix + prefix + name;
					StringBuffer buf = new StringBuffer();
					buf.append(" - ");
					buf.append(constructor.getElementName());
					buf.append('(');
					if (parameterTypes != null && parameterNames != null
							&& parameterTypes.length > 0
							&& parameterNames.length > 0) {
						buf.append(parameterTypes[0]);
						buf.append(' ');
						buf.append(parameterNames[0]);
						if (parameterTypes.length > 1 && parameterTypes.length > 1) {
							for (int i = 1; i < parameterTypes.length; i++) {
								buf.append(", ");
								buf.append(parameterTypes[i]);
								buf.append(' ');
								buf.append(parameterNames[i]);
							}
						}
					}
					buf.append(')');
					String displayText = buf.toString();

					Image image = imageProvider.getImageLabel(constructor,
							constructor.getFlags()
									| JavaElementImageProvider.SMALL_ICONS);

					int cursor = replaceText.length();
					String refReplaceText = replaceText + "-ref";
					if (!attrAtLocationHasValue) {
						replaceText += "=\"\"";
						cursor = replaceText.length() - 1;
					} else {
						cursor = replaceText.length() + 2;
					}

					CustomCompletionProposal proposal1 = new CustomCompletionProposal(
							replaceText, request.getReplacementBeginPosition(),
							request.getReplacementLength(), cursor, image,
							namespacePrefix + name + displayText, null, null,
							CONSTRUCTOR_RELEVANCE);

					if (!attrAtLocationHasValue) {
						refReplaceText += "=\"\"";
						cursor = refReplaceText.length() - 1;
					} else {
						cursor = refReplaceText.length() + 2;
					}
					image = BeansModelImages.getDecoratedImage(image,
							BeansModelImages.FLAG_EXTERNAL);
					CustomCompletionProposal proposal2 = new CustomCompletionProposal(
							refReplaceText,
							request.getReplacementBeginPosition(),
							request.getReplacementLength(), cursor, image,
							namespacePrefix + name + "-ref" + displayText,
							null, null, CONSTRUCTOR_RELEVANCE);

					request.addProposal(proposal1);
					request.addProposal(proposal2);
					argNames.add(name);
				}
			}
		} catch (JavaModelException e) {
			// do nothing
		}
	}

}
