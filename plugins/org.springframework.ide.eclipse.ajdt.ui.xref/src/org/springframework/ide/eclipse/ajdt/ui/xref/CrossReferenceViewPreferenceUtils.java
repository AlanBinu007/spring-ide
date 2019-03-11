/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ajdt.ui.xref;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class CrossReferenceViewPreferenceUtils {

	public static final String XREF_CHECKED_FILTERS = Activator.PLUGIN_ID
			+ ".checked.filters"; //$NON-NLS-1$

	public static final String XREF_CHECKED_FILTERS_INPLACE = Activator.PLUGIN_ID
			+ ".checked.filters.inplace";

	@SuppressWarnings("unchecked")
	public static void setCheckedInplaceFilters(List l) {
		StringBuffer sb = new StringBuffer();
		sb.append("set: "); //$NON-NLS-1$
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			sb.append(name);
			if (iter.hasNext()) {
				sb.append(","); //$NON-NLS-1$
			}
		}
		IPreferenceStore pstoreInplace = Activator.getDefault()
				.getPreferenceStore();
		pstoreInplace.setValue(XREF_CHECKED_FILTERS_INPLACE, sb.toString());
	}

	public static List<Object> getFilterCheckedInplaceList() {
		IPreferenceStore pstoreInplace = Activator.getDefault()
				.getPreferenceStore();
		String xRefCheckedFilters = pstoreInplace
				.getString(XREF_CHECKED_FILTERS_INPLACE);
		if (!xRefCheckedFilters.startsWith("set: ")) { //$NON-NLS-1$
			return null;
		}
		xRefCheckedFilters = xRefCheckedFilters.substring("set: ".length()); //$NON-NLS-1$
		List<Object> checkedList = new ArrayList<Object>();
		StringTokenizer tokenizer = new StringTokenizer(xRefCheckedFilters, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			checkedList.add(tokenizer.nextElement());
		}
		return checkedList;
	}

	@SuppressWarnings("unchecked")
	public static void setCheckedFilters(List l) {

		StringBuffer sb = new StringBuffer();
		sb.append("set: "); //$NON-NLS-1$
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			sb.append(name);
			if (iter.hasNext()) {
				sb.append(","); //$NON-NLS-1$
			}
		}
		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
		pstore.setValue(XREF_CHECKED_FILTERS, sb.toString());
	}

	public static List<Object> getFilterCheckedList() {
		IPreferenceStore pstore = Activator.getDefault().getPreferenceStore();
		String xRefCheckedFilters = pstore.getString(XREF_CHECKED_FILTERS);
		if (!xRefCheckedFilters.startsWith("set: ")) { //$NON-NLS-1$
			return null;
		}
		xRefCheckedFilters = xRefCheckedFilters.substring("set: ".length()); //$NON-NLS-1$
		List<Object> checkedList = new ArrayList<Object>();
		StringTokenizer tokenizer = new StringTokenizer(xRefCheckedFilters, ","); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			checkedList.add(tokenizer.nextElement());
		}
		return checkedList;
	}

}
