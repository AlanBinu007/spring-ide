/*******************************************************************************
 * Copyright (c) 2006, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.decorator;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAnnotationAopDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopModelChangedListener;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdviceAopTargetBeanNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdviceAopTargetMethodNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdviceDeclareParentAopSourceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdviceRootAopReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdvisedAopSourceMethodNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdvisedAopSourceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.AdvisedDeclareParentAopSourceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.BeanMethodReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.BeanReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IReferenceNode;
import org.springframework.ide.eclipse.aop.ui.navigator.util.AopReferenceModelImages;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * {@link ILightweightLabelDecorator} that decorates advised Java elements.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AopReferenceModelImageDecorator extends BeansModelLabelDecorator implements
		ILightweightLabelDecorator {

	public static final String DECORATOR_ID = org.springframework.ide.eclipse.aop.ui.Activator.PLUGIN_ID
			+ ".decorator.adviceimagedecorator";

	private void internalUpdate() {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {

				IBaseLabelProvider provider = PlatformUI.getWorkbench().getDecoratorManager()
						.getBaseLabelProvider(BeansModelLabelDecorator.DECORATOR_ID);
				if (provider != null) {
					fireLabelProviderChanged(new LabelProviderChangedEvent(provider));
				}

				fireLabelProviderChanged(new LabelProviderChangedEvent(
						AopReferenceModelImageDecorator.this));
			}
		});
	}

	private IAopModelChangedListener listener;

	public AopReferenceModelImageDecorator() {
		super();
		listener = new IAopModelChangedListener() {
			public void changed() {
				internalUpdate();
			}
		};
		Activator.getModel().registerAopModelChangedListener(listener);
	}

	public void decorate(Object element, IDecoration decoration) {
		// add the orange triangle to the icon if this method,
		// class or aspect is advised
		if ((element instanceof IMethod || element instanceof SourceType)) {
			IJavaElement je = (IJavaElement) element;
			IJavaProject jp = je.getJavaProject();
			// only query the model if the element is in an Spring project
			if ((jp != null) && SpringCoreUtils.isSpringProject(jp.getProject())) {
				if (je instanceof IMethod && Activator.getModel().isAdvised(je)) {
					decoration.addOverlay(AopReferenceModelImages.DESC_OVR_ADVICE,
							IDecoration.TOP_LEFT);
				}
			}
		}
		else if (element instanceof BeanMethodReferenceNode
				&& Activator.getModel().isAdvised(
						((BeanMethodReferenceNode) element).getJavaElement())) {
			decoration.addOverlay(AopReferenceModelImages.DESC_OVR_ADVICE, IDecoration.TOP_LEFT);
		}
		else if (element instanceof AdviceAopTargetMethodNode) {
			decoration.addOverlay(AopReferenceModelImages.DESC_OVR_ADVICE, IDecoration.TOP_LEFT);
		}
		else if (element instanceof AdviceAopTargetBeanNode) {
			decoration.addOverlay(AopReferenceModelImages.DESC_OVR_ADVICE, IDecoration.TOP_LEFT);
		}
		else if (element instanceof BeanReferenceNode
				&& Activator.getModel().isAdvised(((BeanReferenceNode) element).getBean())) {
			decoration.addOverlay(AopReferenceModelImages.DESC_OVR_ADVICE, IDecoration.TOP_LEFT);
		}
		else if (element instanceof AdvisedAopSourceMethodNode) {
			if (Activator.getModel().isAdvised(
					((AdvisedAopSourceMethodNode) element).getReference().getSource()))
				decoration
						.addOverlay(AopReferenceModelImages.DESC_OVR_ADVICE, IDecoration.TOP_LEFT);
		}
		else if (element instanceof AdviceRootAopReferenceNode) {
			List<IAopReference> references = ((AdviceRootAopReferenceNode) element).getReference();
			for (IAopReference reference : references) {
				if (reference.getDefinition() instanceof IAnnotationAopDefinition) {
					decoration.addOverlay(AopReferenceModelImages.DESC_OVR_ANNOTATION,
							IDecoration.BOTTOM_LEFT);
					break;
				}
			}
		}
		else if (element instanceof AdvisedAopSourceNode) {
			IAopReference reference = ((AdvisedAopSourceNode) element).getReference();
			if (reference.getDefinition() instanceof IAnnotationAopDefinition) {
				decoration.addOverlay(AopReferenceModelImages.DESC_OVR_ANNOTATION,
						IDecoration.BOTTOM_LEFT);
			}
		}
		else if (element instanceof AdviceDeclareParentAopSourceNode) {
			IAopReference reference = ((AdviceDeclareParentAopSourceNode) element).getReference();
			if (reference.getDefinition() instanceof IAnnotationAopDefinition) {
				decoration.addOverlay(AopReferenceModelImages.DESC_OVR_ANNOTATION,
						IDecoration.BOTTOM_LEFT);
			}
		}
		else if (element instanceof AdvisedDeclareParentAopSourceNode) {
			IAopReference reference = ((AdvisedDeclareParentAopSourceNode) element).getReference();
			if (reference.getDefinition() instanceof IAnnotationAopDefinition) {
				decoration.addOverlay(AopReferenceModelImages.DESC_OVR_ANNOTATION,
						IDecoration.BOTTOM_LEFT);
			}
		}
		// add overlay to IBeans
		else if (element instanceof IBean) {
			IBean bean = (IBean) element;
			if (Activator.getModel().isAdvised(bean)) {
				decoration
						.addOverlay(AopReferenceModelImages.DESC_OVR_ADVICE, IDecoration.TOP_LEFT);
			}
		}

		if (element instanceof IReferenceNode
				&& ((IReferenceNode) element).getReferenceParticipant() != null) {
			super.decorate(((IReferenceNode) element).getReferenceParticipant(), decoration);
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (listener != null) {
			Activator.getModel().unregisterAopModelChangedListener(listener);
		}
	}
}
