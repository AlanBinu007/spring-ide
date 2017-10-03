/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.actions;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public class OpenBeanClassAction extends AbstractOpenResourceAction {

	public OpenBeanClassAction() {
		super("Open Bean Class");
	}
	
	@Override
	public void run() {
		IStructuredSelection selection = getStructuredSelection();
		List<?> elements = selection.toList();
		for (Object obj : elements) {
			if (obj instanceof LiveBean) {
				LiveBean bean = (LiveBean) obj;
				TypeLookup appName = bean.getTypeLookup();
				String beanClass = bean.getBeanType();
				if (appName != null) {
					if (beanClass != null && beanClass.trim().length() > 0) {
						if (beanClass.startsWith("com.sun.proxy")) {
							// Special case for proxy beans, extract the type
							// from the resource field
							String resource = bean.getResource();
							if (resource != null && resource.trim().length() > 0 && !resource.equalsIgnoreCase(null)) {
								String resourcePath = extractResourcePath(resource);
								if (resourcePath.endsWith(".class")) {
									openInEditor(appName, extractClassName(resourcePath));
								}
							}
						}
						else {
							openInEditor(appName, beanClass);
						}
					}
					else {
						// No type field, so infer class from bean ID
						openInEditor(appName, bean.getId());
					}
				}
			}
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			List<?> elements = selection.toList();
			for (Object obj : elements) {
				if (obj instanceof LiveBean) {
					LiveBean bean = (LiveBean) obj;
					String beanClass = bean.getBeanType();
					if (beanClass != null && beanClass.trim().length() > 0) {
						return true;
					}
					else {
						return hasTypeInProject(bean.getTypeLookup(), bean.getId());
					}
				}
			}
		}
		return false;
	}

}
