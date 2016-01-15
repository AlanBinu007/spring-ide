/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeRef;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeRef.RootRef;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeRef.SeqRef;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeRef.TupleValueRef;
import org.springframework.ide.eclipse.editor.support.yaml.ast.NodeUtil;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPathSegment.YamlPathSegmentType;

/**
 * @author Kris De Volder
 */
public class YamlPath {

	public static final YamlPath EMPTY = new YamlPath();
	private final YamlPathSegment[] segments;

	public YamlPath(List<YamlPathSegment> segments) {
		this.segments = segments.toArray(new YamlPathSegment[segments.size()]);
	}

	public YamlPath() {
		this.segments = new YamlPathSegment[0];
	}

	public YamlPath(YamlPathSegment... segments) {
		this.segments = segments;
	}

	public String toPropString() {
		StringBuilder buf = new StringBuilder();
		boolean first = true;
		for (YamlPathSegment s : segments) {
			if (first) {
				buf.append(s.toPropString());
			} else {
				buf.append(s.toNavString());
			}
			first = false;
		}
		return buf.toString();
	}

	public String toNavString() {
		StringBuilder buf = new StringBuilder();
		for (YamlPathSegment s : segments) {
			buf.append(s.toNavString());
		}
		return buf.toString();
	}

	public YamlPathSegment[] getSegments() {
		return segments;
	}

	/**
	 * Parse a YamlPath from a dotted property name. The segments are obtained
	 * by spliting the name at each dot.
	 */
	public static YamlPath fromProperty(String propName) {
		ArrayList<YamlPathSegment> segments = new ArrayList<YamlPathSegment>();
		for (String s : propName.split("\\.")) {
			segments.add(YamlPathSegment.valueAt(s));
		}
		return new YamlPath(segments);
	}

	/**
	 * Create a YamlPath with a single segment (i.e. like 'fromProperty', but does
	 * not parse '.' as segment separators.
	 */
	public static YamlPath fromSimpleProperty(String name) {
		return new YamlPath(YamlPathSegment.valueAt(name));
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("YamlPath(");
		boolean first = true;
		for (YamlPathSegment s : segments) {
			if (!first) {
				buf.append(", ");
			}
			buf.append(s);
			first = false;
		}
		buf.append(")");
		return buf.toString();
	}

	public int size() {
		return segments.length;
	}

	public YamlPathSegment getSegment(int segment) {
		if (segment>=0 && segment<segments.length) {
			return segments[segment];
		}
		return null;
	}

	public YamlPath append(YamlPathSegment s) {
		YamlPathSegment[] newPath = Arrays.copyOf(segments, segments.length+1);
		newPath[segments.length] = s;
		return new YamlPath(newPath);
	}

	public <T extends YamlNavigable<T>> T traverse(T startNode) throws Exception {
		T node = startNode;
		for (YamlPathSegment s : segments) {
			if (node==null) {
				return null;
			}
			node = node.traverse(s);
		}
		return node;
	}

	public YamlPath dropFirst(int dropCount) {
		if (dropCount>=size()) {
			return EMPTY;
		}
		if (dropCount==0) {
			return this;
		}
		YamlPathSegment[] newPath = new YamlPathSegment[segments.length-dropCount];
		for (int i = 0; i < newPath.length; i++) {
			newPath[i] = segments[i+dropCount];
		}
		return new YamlPath(newPath);
	}

	public YamlPath dropLast() {
		return dropLast(1);
	}

	public YamlPath dropLast(int dropCount) {
		if (dropCount>=size()) {
			return EMPTY;
		}
		if (dropCount==0) {
			return this;
		}
		YamlPathSegment[] newPath = new YamlPathSegment[segments.length-dropCount];
		for (int i = 0; i < newPath.length; i++) {
			newPath[i] = segments[i];
		}
		return new YamlPath(newPath);
	}


	public boolean isEmpty() {
		return segments.length==0;
	}

	public YamlPath tail() {
		return dropFirst(1);
	}

	/**
	 * Attempt to convert a path represented as a list of {@link NodeRef} into YamlPath.
	 * <p>
	 * Note that not all AST path can be converted into a YamlPath. Some paths in AST
	 * do not have a corresponding YamlPath. For such cases this method may return null.
	 */
	public static YamlPath fromASTPath(List<NodeRef<?>> path) {
		List<YamlPathSegment> segments = new ArrayList<YamlPathSegment>(path.size());
		for (NodeRef<?> nodeRef : path) {
			switch (nodeRef.getKind()) {
			case ROOT:
				RootRef rref = (RootRef) nodeRef;
				segments.add(YamlPathSegment.valueAt(rref.getIndex()));
				break;
			case KEY: {
				String key = NodeUtil.asScalar(nodeRef.get());
				if (key==null) {
					return null;
				} else {
					segments.add(YamlPathSegment.keyAt(key));
				} }
				break;
			case VAL: {
				TupleValueRef vref = (TupleValueRef) nodeRef;
				String key = NodeUtil.asScalar(vref.getTuple().getKeyNode());
				if (key==null) {
					return null;
				} else {
					segments.add(YamlPathSegment.valueAt(key));
				} }
				break;
			case SEQ:
				SeqRef sref = ((SeqRef)nodeRef);
				segments.add(YamlPathSegment.valueAt(sref.getIndex()));
				break;
			default:
				return null;
			}
		}
		return new YamlPath(segments);
	}

	public YamlPathSegment getLastSegment() {
		if (!isEmpty()) {
			return segments[segments.length-1];
		}
		return null;
	}

	/**
	 * Attempt to interpret last segment of path as a bean property name.
	 * @return The name of the property or null if not applicable.
	 */
	public String getBeanPropertyName() {
		if (!isEmpty()) {
			YamlPathSegment lastSegment = getLastSegment();
			YamlPathSegmentType kind = lastSegment.getType();
			if (kind==YamlPathSegmentType.KEY_AT_KEY ||  kind==YamlPathSegmentType.VAL_AT_KEY) {
				return lastSegment.toPropString();
			}
		}
		return null;
	}

	public boolean pointsAtKey() {
		YamlPathSegment s = getLastSegment();
		return s!=null && s.getType()==YamlPathSegmentType.KEY_AT_KEY;
	}



}
