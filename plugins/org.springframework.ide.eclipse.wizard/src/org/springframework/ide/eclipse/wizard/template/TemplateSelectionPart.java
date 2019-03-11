/*******************************************************************************
 *  Copyright (c) 2012, 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.springframework.ide.eclipse.wizard.WizardImages;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ITemplateElement;
import org.springframework.ide.eclipse.wizard.template.infrastructure.SimpleProjectContentManager;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springframework.ide.eclipse.wizard.template.infrastructure.TemplateCategory;
import org.springframework.ide.eclipse.wizard.template.util.TemplatesPreferencePage;
import org.springframework.ide.eclipse.wizard.template.util.TemplatesPreferencesModel;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;
import org.springsource.ide.eclipse.commons.content.core.util.ContentUtil;
import org.springsource.ide.eclipse.commons.content.core.util.Descriptor;
import org.springsource.ide.eclipse.commons.content.core.util.IContentConstants;
import org.springsource.ide.eclipse.commons.ui.StsUiImages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Part that displays templates available when creating a project. Templates are
 * displayed in the part viewer based on metadata, but the actual contents of
 * the template are not download unless explicitly requested through the
 * download API. Selecting templates in the part viewer does not automatically
 * download the contents. This template selection area only performs validations
 * based on whether a template is selected or not.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @author Kaitlin Duck Sherwood
 * @author Nieraj Singh
 */
public class TemplateSelectionPart extends WizardPageArea {

	public static final String SIMPLE_PROJECTS_CATEGORY = "Simple Projects";

	private final List<Template> templates;

	private final IWizard wizard;

	private Label descriptionLabel;

	private StyledText descriptionText;

	private TreeViewer treeViewer;

	private Label legendImage;

	private Label legendText;

	private PropertyChangeListener contentManagerListener;

	private Button refreshButton;

	private final NewSpringProjectWizardModel model;

	public TemplateSelectionPart(IWizard wizard, NewSpringProjectWizardModel model,
			IWizardPageStatusHandler statusHandler) {
		super(statusHandler);
		this.wizard = wizard;
		this.model = model;
		templates = new ArrayList<Template>();

	}

	@Override
	public Control createArea(Composite parent) {

		initialiseTemplatesFromContentManager();

		Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(0, 0).applyTo(container);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(container, SWT.NONE);
		label.setText("Templates:"); //$NON-NLS-1$
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Tree tree = new Tree(container, SWT.FULL_SELECTION | SWT.BORDER);
		tree.setLinesVisible(false);
		tree.setHeaderVisible(false);
		tree.setEnabled(true);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 170;
		tree.setLayoutData(data);

		treeViewer = new TreeViewer(tree);

		treeViewer.setLabelProvider(new ILabelProvider() {

			public void removeListener(ILabelProviderListener listener) {
			}

			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			public void dispose() {
			}

			public void addListener(ILabelProviderListener listener) {
			}

			public String getText(Object element) {
				if (element instanceof ITemplateElement) {
					return ((ITemplateElement) element).getName();
				}
				return null;
			}

			public Image getImage(Object element) {
				if (element instanceof Template) {
					Template template = (Template) element;
					Image templateImage = WizardImages.getImage(WizardImages.TEMPLATE_ICON);

					// Simple Project templates are bundled in the plugin,
					// therefore do not require download icon, since they are
					// not downloaded
					if (template instanceof SimpleProject
							|| ((template.getItem().isLocal() || TemplateUtils.hasBeenDownloaded(template)) && !template
									.getItem().isNewerVersionAvailable())) {
						return templateImage;
					}

					return WizardImages.getImage(new DecorationOverlayIcon(templateImage, new ImageDescriptor[] {
							StsUiImages.DOWNLOAD_OVERLAY, null, null, null, null }));
				}

				if (element instanceof TemplateCategory) {
					return WizardImages.getImage(WizardImages.TEMPLATE_CATEGORY_ICON);
				}
				return null;
			}
		});

		treeViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				if (e1 instanceof TemplateCategory && e2 instanceof Template) {
					return -1;
				}
				if (e1 instanceof Template && e2 instanceof TemplateCategory) {
					return 1;
				}

				if (e1 instanceof ITemplateElement && e2 instanceof ITemplateElement) {
					ITemplateElement t1 = (ITemplateElement) e1;
					ITemplateElement t2 = (ITemplateElement) e2;
					// Special Case, Simple Projects category is placed above
					// others
					if (t1.getName().equals(SIMPLE_PROJECTS_CATEGORY)) {
						return -1;
					}
					else if (t2.getName().equals(SIMPLE_PROJECTS_CATEGORY)) {
						return 1;
					}
					else {
						return t1.getName().compareTo(t2.getName());
					}
				}
				return super.compare(viewer, e1, e2);
			}
		});

		treeViewer.setContentProvider(new TemplateContentProvider());

		treeViewer.setInput(templates);

		Composite legendContainer = new Composite(container, SWT.NONE);
		legendContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		GridLayout headerLayout = new GridLayout(3, false);
		headerLayout.marginWidth = 0;
		headerLayout.marginHeight = 0;
		legendContainer.setLayout(headerLayout);

		Composite legendComposite = new Composite(legendContainer, SWT.NONE);
		GridLayout legendLayout = new GridLayout(2, false);
		legendLayout.verticalSpacing = 0;
		legendLayout.marginHeight = 0;
		legendLayout.marginBottom = 0;

		int legendControlVerticalIndent = 5;

		legendComposite.setLayout(legendLayout);
		legendComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		legendImage = new Label(legendComposite, SWT.NONE);

		GridDataFactory.fillDefaults().grab(false, false).indent(0, legendControlVerticalIndent).applyTo(legendImage);

		legendImage.setImage(WizardImages.getImage(StsUiImages.DOWNLOAD_OVERLAY));
		legendImage.setToolTipText("Templates with this icon will be downloaded after clicking the 'Next' button.");

		legendText = new Label(legendComposite, SWT.NONE);
		legendText.setText("requires downloading");

		GridDataFactory.fillDefaults().grab(false, false).indent(0, legendControlVerticalIndent).applyTo(legendText);

		Hyperlink hyperlink = new Hyperlink(legendContainer, SWT.WRAP);
		GridDataFactory.fillDefaults().grab(false, false).indent(0, legendControlVerticalIndent).applyTo(hyperlink);
		hyperlink.setText(NLS.bind("Configure templates...", null));
		Color blue = new Color(null, 0, 0, 255);
		hyperlink.setForeground(blue);
		hyperlink.setUnderlined(true);
		blue.dispose();
		hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent event) {
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null,
						TemplatesPreferencePage.EXAMPLE_PREFERENCES_PAGE_ID, null, null);
				refreshButton.setEnabled(false);

				dialog.open();
				downloadDescriptors();

				refreshButton.setEnabled(!isRefreshing());

			}
		});

		refreshButton = new Button(legendContainer, SWT.PUSH);
		refreshButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		refreshButton.setText("Refresh");
		refreshButton.setEnabled(!isRefreshing());

		// refreshButton.setImage(WizardImages.getImage(WizardImages.REFRESH_ICON));

		refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// under most circumstances, we don't want to download templates
				// if there has not been any change. However, when the user
				// presses Refresh, they really do want to see something happen.
				ContentPlugin.getDefault().getManager().setDirty();

				downloadDescriptors();
			}

		});

		refreshButton
				.setToolTipText("Refresh the list of templates. Note that the current template selection will be cleared.");

		Composite descriptionComposite = new Composite(container, SWT.NONE);
		descriptionComposite.setLayout(new GridLayout());
		descriptionComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		descriptionLabel = new Label(descriptionComposite, SWT.NONE);
		descriptionLabel.setText("Description:"); //$NON-NLS-1$
		descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		descriptionText = new StyledText(descriptionComposite, SWT.WRAP | SWT.READ_ONLY | SWT.V_SCROLL);
		// descriptionText.setAlwaysShowScrollBars(false);
		Display display = Display.getCurrent();
		if (display != null) {
			descriptionText.setBackground(display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		}

		GridData descriptionData = new GridData(SWT.FILL, SWT.FILL, true, true);
		descriptionData.widthHint = 200;
		descriptionData.heightHint = 80;
		descriptionText.setLayoutData(descriptionData);

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = treeViewer.getSelection();
				if (selection instanceof TreeSelection) {
					Object element = ((TreeSelection) selection).getFirstElement();
					if (element instanceof Template) {
						setSeletectedTemplate((Template) element);
					}
					else if (element instanceof TemplateCategory) {
						// Note: ONLY clear the selected template if the
						// selection is a template category.
						// Do not clear the template if there is an empty
						// selection in the tree, as there
						// are times when the viewer
						// will set an empty selection in the tree due to a
						// refresh operation, and if
						// a template was already selected prior to that refresh
						// request, it should not be
						// overwritten with null as other operations will
						// require that the selected template not be changed
						setSeletectedTemplate(null);
					}
				}
			}

		});

		this.contentManagerListener = new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent event) {

				ContentManager manager = event.getSource() instanceof ContentManager ? (ContentManager) event
						.getSource() : null;
				final boolean hasTemplateContentChanged = manager != null && manager.isDirty();
				if (hasTemplateContentChanged) {

					clearTemplateSelection();

					// Only reinitialise templates if the manager is marked as
					// dirty, as template contents may have changed, therefore
					// new templates
					// need to be created in the tree viewer
					initialiseTemplatesFromContentManager();

				}
				// switch to UI thread to refresh the UI controls
				PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
					public void run() {
						refreshPage(hasTemplateContentChanged);
					}
				});

			}

		};

		ContentManager manager = ContentPlugin.getDefault().getManager();
		manager.addListener(contentManagerListener);

		// This does not automatically add the templates to the tree viewer
		// input.
		// Rather it downloads templates asynchronously, and the content manager
		// will then notify the tree viewer when the content is available and at
		// that time
		// refresh the tree input
		downloadDescriptors();

		expandCategory(SIMPLE_PROJECTS_CATEGORY);

		return container;

	}

	protected void clearTemplateSelection() {
		// Run synch in UI thread as it is most likely called from a non-worker
		// thread
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				// clear any selection in the tree viewer as well.
				if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
					treeViewer.setSelection(null);
				}
				setSeletectedTemplate(null);
			}
		});

	}

	protected void expandCategory(final String categoryName) {
		// Run in UI

		if (treeViewer == null || treeViewer.getTree().isDisposed()) {
			return;
		}
		TreeItem[] items = treeViewer.getTree().getItems();
		if (items != null) {
			for (TreeItem item : items) {
				Object itemObj = item.getData();
				if (itemObj instanceof TemplateCategory && ((TemplateCategory) itemObj).getName().equals(categoryName)) {

					// Note that calling expand on the tree item itself would
					// not work, because at this stage
					// the tree viewer content provider may not have yet
					// contributed children to the category element
					// and therefore the category tree item may not have
					// corresponding tree item children to display.
					// Instead, expand through the treeViewer rather than
					// through the tree item, as the treeViewer expand request
					// will trigger the content provider to create children for
					// the category tree item.

					treeViewer.expandToLevel(itemObj, 1);
					break;
				}
			}
		}

	}

	/**
	 * Refreshes list of descriptors, and clears any current template selection.
	 * This is to avoid retaining template selections that may no longer be up
	 * to date.
	 */
	protected void downloadDescriptors() {

		// Must execute as an asynch in UI thread to avoid Invalid UI
		// thread
		// exceptions
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				final String[] error = new String[1];
				final Exception[] e = new Exception[1];
				try {
					wizard.getContainer().run(true, true, new DownloadDescriptorJob());
				}
				catch (InvocationTargetException ie) {
					e[0] = ie;
					error[0] = ErrorUtils.getErrorMessage("Failed to download template descriptors", e[0]);
				}
				catch (InterruptedException inte) {
					e[0] = inte;
					error[0] = "Download of descriptors interrupted";
				}

				if (error[0] != null && e[0] != null) {
					setError(error[0], e[0]);
				}
			}
		});

	}

	protected void setSeletectedTemplate(Template template) {
		model.selectedTemplate.setValue(template);
		if (template != null) {
			setDescription(template);
		}
		notifyStatusChange(validateArea());
	}

	@Override
	protected IStatus validateArea() {
		Template template = model.selectedTemplate.getValue();
		IStatus status = Status.OK_STATUS;
		if (template != null) {

			String warning = getWarning(template);

			// Warnings apply to both Simple Projects and templates
			if (warning != null) {
				status = createStatus(warning, IStatus.WARNING);
			}
			else if (!(template instanceof SimpleProject)) {
				String message = "Click 'Next' to load the template contents.";
				status = createStatus(message, IStatus.INFO);
			}

			// For the purposes of selecting a template, if it is not null, the
			// area is complete.
			// Other sections in the wizard may provider additional validation
			// on the template, but it is not the responsibility of the template
			// selection
			// area to provide validation beyond just selecting a template
			setAreaComplete(true);

		}
		else {
			setAreaComplete(false);
			status = createStatus("Please select a template.", IStatus.INFO);
		}
		return status;
	}

	protected void setError(String error, Throwable t) {
		notifyStatusChange(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, error, t), false);
	}

	protected void setError(IStatus error) {
		notifyStatusChange(error, false);
	}

	/**
	 * This initialises the templates directly from the content manager with
	 * updated information. Any existing selections in the model are also
	 * removed, as that template may no longer be valid.
	 * 
	 * Note that is is sometimes run in a worker thread, therefore any UI
	 * callbacks must be performed as UI jobs.
	 * 
	 */
	private void initialiseTemplatesFromContentManager() {

		templates.clear();

		TemplatesPreferencesModel model = TemplatesPreferencesModel.getInstance();
		Collection<ContentItem> items = ContentPlugin.getDefault().getManager()
				.getItemsByKind(ContentManager.KIND_TEMPLATE);

		List<ContentItem> sortedItems = new ArrayList<ContentItem>();
		sortedItems.addAll(items);
		Collections.sort(sortedItems, new Comparator<ContentItem>() {
			public int compare(ContentItem o1, ContentItem o2) {
				Descriptor descriptor1 = o1.getRemoteDescriptor();
				if (descriptor1 == null) {
					descriptor1 = o1.getLocalDescriptor();
				}

				Descriptor descriptor2 = o2.getRemoteDescriptor();
				if (descriptor2 == null) {
					descriptor2 = o2.getLocalDescriptor();
				}
				return descriptor1.getVersion().compareTo(descriptor2.getVersion()) * -1;
			}
		});

		Set<String> templateIds = new HashSet<String>();

		for (ContentItem item : sortedItems) {
			String templateId = item.getId();
			if (!templateIds.contains(templateId)) {

				Template template = new Template(item, null);

				templates.add(template);
				templateIds.add(templateId);
			}
		}

		// Add the Simple Projects
		List<SimpleProject> simpleProjects = getSimpleProjects();
		templates.addAll(simpleProjects);

		if (model.shouldShowSelfHostedProjects()) {

			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject project : projects) {
				IFile templateFile = project.getFile(IContentConstants.TEMPLATE_DATA_FILE_NAME);
				IFile wizardFile = project.getFile(IContentConstants.WIZARD_DATA_FILE_NAME);
				if (templateFile.exists() && wizardFile.exists()) {
					File file = templateFile.getLocation().toFile();
					try {
						DocumentBuilder documentBuilder = ContentUtil.createDocumentBuilder();
						Document document = documentBuilder.parse(file);
						Element rootNode = document.getDocumentElement();
						if (rootNode != null) {
							NodeList children = rootNode.getChildNodes();
							for (int i = 0; i < children.getLength(); i++) {
								Node childNode = children.item(i);
								if (childNode.getNodeType() == Node.ELEMENT_NODE) {
									if ("descriptor".equals(childNode.getNodeName())) {
										Descriptor descriptor = Descriptor.read(childNode);
										ContentItem item = new ContentItem(descriptor.getId(), project);
										item.setLocalDescriptor(descriptor);
										descriptor.setUrl(project.getName());
										ImageDescriptor icon = null;
										Template template = new Template(item, icon);
										templates.add(template);
									}
								}
							}
						}
					}
					catch (CoreException e) {
						String message = NLS.bind("Error getting and parsing descriptors file in background {0}",
								e.getMessage());
						MessageDialog.openWarning(wizard.getContainer().getShell(), "Warning", message);
					}
					catch (SAXException e) {
						String message = NLS.bind("Error parsing tmp descriptors file at {0} in background.\n{1}",
								file, e.getMessage());
						MessageDialog.openWarning(wizard.getContainer().getShell(), "Warning", message);
					}
					catch (IOException e) {
						String message = NLS.bind("IO error on file at {0} opened in background.\n{1}", file,
								e.getMessage());
						MessageDialog.openWarning(wizard.getContainer().getShell(), "Warning", message);
					}

				}
			}
		}
		Collections.sort(templates, new Comparator<Template>() {
			public int compare(Template t1, Template t2) {
				return t1.getName().compareTo(t2.getName());
			}
		});

	}

	/**
	 * Returns non-null list of Simple Projects. If errors occur while resolving
	 * simple projects, an empty list is returned, and error logged in the
	 * container of the part
	 */
	protected List<SimpleProject> getSimpleProjects() {

		final List<SimpleProject> projects = new ArrayList<SimpleProject>();
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {

				final String[] error = new String[1];
				final Exception[] exception = new Exception[1];

				try {
					IRunnableWithProgress runnable = new IRunnableWithProgress() {

						public void run(IProgressMonitor monitor) throws InvocationTargetException,
								InterruptedException {
							try {

								List<SimpleProject> prj = SimpleProjectContentManager.getManager().getSimpleProjects(
										monitor);
								projects.addAll(prj);
							}
							catch (CoreException e) {
								throw new InvocationTargetException(e);
							}
						}
					};
					wizard.getContainer().run(false, false, runnable);
				}
				catch (InvocationTargetException e) {
					error[0] = ErrorUtils.getErrorMessage("Failed to load Simple Project template content", e);
					exception[0] = e;
				}
				catch (InterruptedException e) {
					error[0] = "Failure while loading Simple Project templates due to interrupt exception. Template content may not have been loaded correctly.";
					exception[0] = e;
				}

				if (error[0] != null && exception[0] != null) {

					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
						public void run() {
							setError(error[0], exception[0]);
						}
					});
				}

			}
		});
		return projects;

	}

	private void refreshPage(boolean refreshAll) {

		// The content may have changed (e.g. if templates manually refreshed by
		// the user)
		// therefore invoke the content provider again to refresh the contents
		// of the tree viewer
		// with the new contents.
		if (refreshAll) {
			treeViewer.refresh(true);

			boolean needsDownload = false;
			for (Template template : templates) {
				if (!(template instanceof SimpleProject) && !template.getItem().isLocal()) {
					needsDownload = true;
					break;
				}
			}

			expandCategory(SIMPLE_PROJECTS_CATEGORY);

			legendImage.setVisible(needsDownload);
			legendText.setVisible(needsDownload);
			descriptionText.setText(""); //$NON-NLS-1$
			refreshButton.setEnabled(true);
		}
		else {
			refreshSelectedTemplateInViewer();
		}

	}

	public void refreshSelectedTemplateInViewer() {
		Template selectedTemplate = model.selectedTemplate.getValue();
		if (selectedTemplate != null && treeViewer != null && !treeViewer.getTree().isDisposed()) {
			treeViewer.refresh(selectedTemplate, true);
			treeViewer.setSelection(new StructuredSelection(selectedTemplate), true);
		}
	}

	private void setDescription(Template template) {
		String description = null;

		if (template != null) {
			description = template.getDescription();
			// Do not show URL for Simple Projects
			if (!(template instanceof SimpleProject) && template.getItem().getRemoteDescriptor() != null) {
				description += "\n\nURL:" + template.getItem().getRemoteDescriptor().getUrl();
			}
		}

		if (description != null) {
			descriptionText.setText(description);
		}
		else {
			descriptionText.setText(""); //$NON-NLS-1$
		}
		descriptionText.redraw();
	}

	private String getWarning(Template template) {
		String requiredBundleStr = null;
		ContentItem contentItem = template.getItem();
		if (contentItem.getLocalDescriptor() != null) {
			requiredBundleStr = contentItem.getLocalDescriptor().getRequiresBundle();
		}
		if (requiredBundleStr == null && contentItem.getRemoteDescriptor() != null) {
			requiredBundleStr = contentItem.getRemoteDescriptor().getRequiresBundle();
		}

		StringBuilder missingBundleStr = new StringBuilder();
		if (requiredBundleStr != null) {
			String[] requiredBundles = requiredBundleStr.split(",");
			for (String requiredBundle : requiredBundles) {
				if (Platform.getBundle(requiredBundle.trim()) == null) {
					if (missingBundleStr.length() > 0) {
						missingBundleStr.append(", ");
					}
					missingBundleStr.append(requiredBundle.trim());
				}
			}
		}

		if (missingBundleStr.length() > 0) {
			String message = NLS.bind("To ensure project compiles properly, please install bundle(s) {0}.",
					missingBundleStr);
			return message;
		}
		return null;
	}

	public void dispose() {
		Assert.isNotNull(contentManagerListener);
		ContentPlugin.getDefault().getManager().removeListener(contentManagerListener);

	}

	private boolean isRefreshing() {
		return ContentPlugin.getDefault().getManager().isRefreshing();
	}

}
