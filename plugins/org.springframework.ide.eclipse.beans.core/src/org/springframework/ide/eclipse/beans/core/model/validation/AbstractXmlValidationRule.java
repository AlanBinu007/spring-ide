/*******************************************************************************
 * Copyright (c) 2008, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.validation;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.core.type.asm.ClassReaderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * {@link IValidationRule} implementation that allows to validate on the raw XML content in the context of the bean
 * validation request.
 * @author Christian Dupuis
 * @since 2.0.4
 * @see #supports(Node)
 * @see #validate(Node,IBeansValidationContext)
 */
@SuppressWarnings("restriction")
public abstract class AbstractXmlValidationRule implements IValidationRule<IBeansModelElement, IBeansValidationContext> {

	/**
	 * This rule support <strong>only</strong> {@link IBeansConfig} elements as this is the model element representing a
	 * actual file.
	 */
	public final boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof IBeansConfig
				|| (element instanceof IBeansImport && ((IBeansImport) element).getImportedBeansConfigs().size() > 0);
	}

	/**
	 * Validates a {@link IBeansConfig} or imported {@link IImportedBeansConfig} from a {@link IBeansImport}.
	 * <p>
	 * Calls for every {@link IBeansConfig} the {@link #validateBeansConfig(IBeansConfig, IBeansValidationContext)}
	 * method.
	 */
	public final void validate(IBeansModelElement element, final IBeansValidationContext context,
			IProgressMonitor monitor) {
		if (element instanceof IBeansConfig) {
			validateBeansConfig((IBeansConfig) element, context);
		}
		else if (element instanceof IBeansImport) {
			for (IImportedBeansConfig beansConfig : ((IBeansImport) element).getImportedBeansConfigs()) {
				validateBeansConfig(beansConfig, context);
			}
		}
	}

	/**
	 * Validates the {@link IBeansConfig} by creating a {@link DomVisitor} and visiting the entire dom model.
	 * <p>
	 * Every element will be visited and depending on the return of {@link #supports(Node)} the
	 * {@link #validate(Node, IBeansValidationContext)} will be called for the node.
	 * @see #supports(Node)
	 * @see #validate(Node, IBeansValidationContext)
	 */
	private void validateBeansConfig(final IBeansConfig element, final IBeansValidationContext context) {

		// Do not validate external configuration files
		if (element.isExternal()) {
			return;
		}

		if (!(context instanceof BeansValidationContext)) {
			return;
		}
		
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead((IFile) element.getElementResource());
			if (model != null) {
				Document document = ((DOMModelImpl) model).getDocument();
				if (document != null && document.getDocumentElement() != null) {
					DomVisitor visitor = new DomVisitor() {

						private IXmlValidationContext xmlContext = new XmlValidationContext(
								(BeansValidationContext) context, element);

						@Override
						public void validateNode(Node n) {
							validate(n, xmlContext);
						}

						@Override
						public boolean supportsNode(Node n) {
							return supports(n);
						}

					};
					visitor.visit(document);
				}
			}
		}
		catch (IOException e) {
			BeansCorePlugin.log(e);
		}
		catch (CoreException e) {
			BeansCorePlugin.log(e);
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
	}

	/**
	 * Returns <code>true</code> if given {@link Node n} is supported to be validated.
	 * @param n the node to validate
	 * @return true if validation should be called
	 */
	protected abstract boolean supports(Node n);

	/**
	 * Validates the given {@link Node n}
	 * @param n the node to validate
	 * @param context the current validation context
	 */
	protected abstract void validate(Node n, IXmlValidationContext context);

	/**
	 * Internal visitor implementation that visits an entire {@link Document}.
	 */
	private abstract static class DomVisitor {

		public void visit(Document dom) {
			if (supportsNode(dom)) {
				validateNode(dom);
			}
			visit(dom.getDocumentElement());
		}

		public void visit(Element e) {
			NodeList lst = e.getChildNodes();
			int len = lst.getLength();
			for (int i = 0; i < len; i++) {
				Node n = lst.item(i);
				if (supportsNode(n)) {
					validateNode(n);
				}
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					visit((Element) n);
				}
			}
		}

		public abstract void validateNode(Node n);

		public abstract boolean supportsNode(Node n);

	}

	/**
	 * Internal validation context implementation that converts {@link Node} object back to
	 * {@link IResourceModelElement} s.
	 */
	private static class XmlValidationContext implements IXmlValidationContext {

		private final IBeansConfig beansConfig;

		private final BeansValidationContext delegateContext;

		public XmlValidationContext(BeansValidationContext delegateContext, IBeansConfig beansConfig) {
			this.delegateContext = delegateContext;
			this.beansConfig = beansConfig;
		}

		/**
		 * {@inheritDoc}
		 */
		public ClassReaderFactory getClassReaderFactory() {
			return delegateContext.getClassReaderFactory();
		}

		/**
		 * {@inheritDoc}
		 */
		public BeanDefinitionRegistry getCompleteRegistry() {
			return delegateContext.getCompleteRegistry();
		}

		/**
		 * {@inheritDoc}
		 */
		public BeanDefinitionRegistry getIncompleteRegistry() {
			return delegateContext.getIncompleteRegistry();
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<BeanDefinition> getRegisteredBeanDefinition(String beanName, String beanClass) {
			return delegateContext.getRegisteredBeanDefinition(beanName, beanClass);
		}

		/**
		 * {@inheritDoc}
		 */
		public IProject getRootElementProject() {
			return delegateContext.getRootElementProject();
		}

		/**
		 * {@inheritDoc}
		 */
		public IResource getRootElementResource() {
			return delegateContext.getRootElementResource();
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isBeanRegistered(String beanName, String beanClass) {
			return delegateContext.isBeanRegistered(beanName, beanClass);
		}

		/**
		 * {@inheritDoc}
		 */
		public void error(IResourceModelElement element, String problemId, String message,
				ValidationProblemAttribute... attributes) {
			delegateContext.error(element, problemId, message, attributes);
		}

		/**
		 * {@inheritDoc}
		 */
		public void error(Node node, String problemId, String message, ValidationProblemAttribute... attributes) {
			delegateContext.error(getResourceModelElementFromNode(node), problemId, message, attributes);
		}

		/**
		 * {@inheritDoc}
		 */
		public IResourceModelElement getContextElement() {
			return delegateContext.getContextElement();
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<ValidationProblem> getProblems() {
			return delegateContext.getProblems();
		}

		/**
		 * {@inheritDoc}
		 */
		public IResourceModelElement getRootElement() {
			return delegateContext.getRootElement();
		}

		/**
		 * {@inheritDoc}
		 */
		public void info(IResourceModelElement element, String problemId, String message,
				ValidationProblemAttribute... attributes) {
			delegateContext.info(element, problemId, message, attributes);
		}

		/**
		 * {@inheritDoc}
		 */
		public void info(Node n, String problemId, String message, ValidationProblemAttribute... attributes) {
			delegateContext.info(getResourceModelElementFromNode(n), problemId, message, attributes);
		}

		/**
		 * {@inheritDoc}
		 */
		public void setCurrentRuleDefinition(ValidationRuleDefinition ruleDefinition) {
			delegateContext.setCurrentRuleDefinition(ruleDefinition);
		}

		/**
		 * {@inheritDoc}
		 */
		public void warning(IResourceModelElement element, String problemId, String message,
				ValidationProblemAttribute... attributes) {
			delegateContext.warning(element, problemId, message, attributes);
		}

		/**
		 * {@inheritDoc}
		 */
		public void warning(Node n, String problemId, String message, ValidationProblemAttribute... attributes) {
			delegateContext.warning(getResourceModelElementFromNode(n), problemId, message, attributes);
		}

		/**
		 * {@inheritDoc}
		 */
		public List<ToolAnnotationData> getToolAnnotation(Node n, String attributeName) {
			return delegateContext.getToolAnnotation(n, attributeName);
		}

		private IResourceModelElement getResourceModelElementFromNode(Node n) {
			if (n instanceof IDOMNode) {
				IDOMNode domNode = ((IDOMNode) n);
				int startLine = domNode.getStructuredDocument().getLineOfOffset(domNode.getStartOffset()) + 1;
				int endLine = domNode.getStructuredDocument().getLineOfOffset(domNode.getStartOffset()) + 1;
				IModelElement modelElement = BeansModelUtils.getMostSpecificModelElement(startLine, endLine,
						(IFile) beansConfig.getElementResource(), null);
				if (modelElement instanceof IResourceModelElement) {
					return (IResourceModelElement) modelElement;
				}
			}
			return delegateContext.getRootElement();
		}
		
		/**
		 * {@inheritDoc}
		 */
		public IProjectClassLoaderSupport getProjectClassLoaderSupport() {
			return delegateContext.getProjectClassLoaderSupport();
		}

		/**
		 * {@inheritDoc}
		 */
		public void addProblems(ValidationProblem... problems) {
			delegateContext.addProblems(problems);
		}

	}

}
