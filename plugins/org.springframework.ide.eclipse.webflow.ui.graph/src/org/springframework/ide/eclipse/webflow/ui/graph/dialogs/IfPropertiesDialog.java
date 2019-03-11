/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import ognl.Ognl;
import ognl.OgnlException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelDecorator;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelProvider;

/**
 * @author Christian Dupuis
 */
public class IfPropertiesDialog extends TitleAreaDialog {

	/**
	 * 
	 */
	private static final String EXPRESSION_PREFIX = "${";

	/**
	 * 
	 */
	private static final String EXPRESSION_SUFFIX = "}";

	/**
	 * 
	 */
	private Button browseElseButton;

	/**
	 * 
	 */
	private Button browseThenButton;

	/**
	 * 
	 */
	private SelectionListener buttonListener = new SelectionAdapter() {

		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	/**
	 * 
	 */
	private IIf cloneIf;

	/**
	 * 
	 */
	private Text elseText;

	/**
	 * 
	 */
	private boolean isNew = false;

	/**
	 * 
	 */
	private int LABEL_WIDTH = 70;

	/**
	 * 
	 */
	private Button ognlButton;

	/**
	 * 
	 */
	private Button okButton;

	/**
	 * 
	 */
	private Text onText;

	/**
	 * 
	 */
	private IDecisionState parent;

	/**
	 * 
	 */
	private IIf theIf;

	/**
	 * 
	 */
	private Text thenText;

	/**
	 * 
	 * 
	 * @param parentShell 
	 * @param state 
	 * @param newMode 
	 * @param parent 
	 */
	public IfPropertiesDialog(Shell parentShell, IDecisionState parent,
			IIf state, boolean newMode) {
		super(parentShell);
		this.theIf = state;
		this.parent = parent;
		this.cloneIf = (IIf) ((ICloneableModelElement<IIf>) this.theIf)
				.cloneModelElement();
		this.isNew = newMode;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.cloneIf.setTest(trimString(getTest()));
			if (isNew) {

				if (this.thenText != null && this.thenText.getText() != null
						&& !"".equals(this.thenText.getText())) {
					// this.theIf.setThenTransition(new IfTransition(
					// (ITransitionableTo) WebFlowCoreUtils.getStateById(
					// parent, this.thenText.getText()),
					// this.theIf, true));
				}

				if (this.elseText != null && this.elseText.getText() != null
						&& !"".equals(this.elseText.getText())) {
					// this.theIf.setElseTransition(new IfTransition(
					// (ITransitionableTo) WebFlowCoreUtils.getStateById(
					// parent, this.elseText.getText()),
					// this.theIf, false));
				}

			}
			((ICloneableModelElement<IIf>) this.theIf)
					.applyCloneValues(this.cloneIf);
		}
		super.buttonPressed(buttonId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getShellTitle());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		// do this here because setting the text will set enablement on the
		// ok button
		onText.setFocus();
		if (this.theIf != null && this.theIf.getTest() != null) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(getTitle());
		setMessage(getMessage());
		return contents;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(parentComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite nameGroup = new Composite(composite, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 2;
		layout1.marginWidth = 5;
		nameGroup.setLayout(layout1);

		Label onLabel = new Label(nameGroup, SWT.NONE);
		onLabel.setText("Test");
		onText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.theIf != null && this.theIf.getTest() != null) {
			this.onText.setText(this.theIf.getTest());
		}
		onText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		onText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		new Label(nameGroup, SWT.NONE);
		ognlButton = new Button(nameGroup, SWT.CHECK);
		ognlButton.setText("Parse OGNL transition criteria");
		ognlButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				validateInput();
			}
		});

		if (this.isNew) {

			// Group for attribute mapper settings.
			Group groupActionType = new Group(composite, SWT.NULL);
			GridLayout layoutAttMap = new GridLayout();
			layoutAttMap.marginWidth = 3;
			layoutAttMap.marginHeight = 3;
			groupActionType.setLayout(layoutAttMap);
			groupActionType.setText(" Transition Tragets ");
			groupActionType
					.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Composite methodComposite = new Composite(groupActionType, SWT.NONE);
			methodComposite
					.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			GridLayout layout3 = new GridLayout();
			layout3.marginHeight = 3;
			layout3.marginWidth = 5;
			layout3.numColumns = 3;
			methodComposite.setLayout(layout3);

			Label thenLabel = new Label(methodComposite, SWT.NONE);
			GridData gridData = new GridData(
					GridData.HORIZONTAL_ALIGN_BEGINNING);
			gridData.widthHint = LABEL_WIDTH;
			thenLabel.setLayoutData(gridData);
			thenLabel.setText("Then");

			thenText = new Text(methodComposite, SWT.SINGLE | SWT.BORDER);
			thenText.setEditable(false);

			if (this.theIf != null && this.theIf.getThenTransition() != null
					&& this.theIf.getThenTransition().getToState() != null) {
				thenText.setText(this.theIf.getThenTransition().getToState()
						.getId());
			}
			thenText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			thenText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});

			browseThenButton = new Button(methodComposite, SWT.PUSH);
			browseThenButton.setText("...");
			browseThenButton.setLayoutData(new GridData(
					GridData.HORIZONTAL_ALIGN_END));
			browseThenButton.addSelectionListener(buttonListener);

			// Label field.
			Label elseLabel = new Label(methodComposite, SWT.NONE);
			gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
			gridData.widthHint = LABEL_WIDTH;
			elseLabel.setLayoutData(gridData);

			// Add the text box for action classname.
			elseText = new Text(methodComposite, SWT.SINGLE | SWT.BORDER);
			if (this.theIf != null && this.theIf.getElseTransition() != null
					&& this.theIf.getElseTransition().getToState() != null) {
				elseText.setText(this.theIf.getElseTransition().getToState()
						.getId());
			}
			elseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			elseText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});
			browseElseButton = new Button(methodComposite, SWT.PUSH);
			browseElseButton.setText("...");
			browseElseButton.setLayoutData(new GridData(
					GridData.HORIZONTAL_ALIGN_END));
			browseElseButton.addSelectionListener(buttonListener);
		}

		applyDialogFont(parentComposite);
		return parentComposite;
	}

	/**
	 * Cut the expression from given criteria string and return it.
	 * 
	 * @param encodedCriteria 
	 * 
	 * @return 
	 */
	private String cutExpression(String encodedCriteria) {
		return encodedCriteria.substring(EXPRESSION_PREFIX.length(),
				encodedCriteria.length() - EXPRESSION_SUFFIX.length());
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public String getMessage() {
		return "Enter the details for the if criteria";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public IWebflowModelElement getModelElementParent() {
		return this.parent;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getShellTitle() {
		return "If";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public String getTest() {
		return this.onText.getText();
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getTitle() {
		return "If properties";
	}

	/**
	 * 
	 * 
	 * @param button 
	 */
	private void handleButtonPressed(Button button) {

		if (button.equals(browseThenButton)) {
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(
					getShell(), new DecoratingLabelProvider(
							new WebflowModelLabelProvider(),
							new WebflowModelLabelDecorator()));
			dialog.setBlockOnOpen(true);
			dialog.setElements(WebflowModelXmlUtils.getStates(parent, false)
					.toArray());
			dialog.setEmptySelectionMessage("Enter a valid state id");
			dialog.setTitle("State reference");
			dialog.setMessage("Please select a state reference");
			dialog.setMultipleSelection(false);
			if (Dialog.OK == dialog.open()) {
				this.thenText.setText(((IState) dialog.getFirstResult())
						.getId());
			}

		}
		else {
			ElementListSelectionDialog dialog = new ElementListSelectionDialog(
					getShell(), new DecoratingLabelProvider(
							new WebflowModelLabelProvider(),
							new WebflowModelLabelDecorator()));
			dialog.setBlockOnOpen(true);
			dialog.setElements(WebflowModelXmlUtils.getStates(parent, false)
					.toArray());
			dialog.setEmptySelectionMessage("Enter a valid state id");
			dialog.setTitle("State reference");
			dialog.setMessage("Please select a state reference");
			dialog.setMultipleSelection(false);
			if (Dialog.OK == dialog.open()) {
				this.elseText.setText(((IState) dialog.getFirstResult())
						.getId());
			}
		}
		this.validateInput();

	}

	/**
	 * 
	 */
	protected void handleTableSelectionChanged() {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * 
	 * @param error 
	 */
	protected void showError(String error) {
		super.setErrorMessage(error);
	}

	/**
	 * 
	 * 
	 * @param string 
	 * 
	 * @return 
	 */
	public String trimString(String string) {
		if (string != null && string == "") {
			string = null;
		}
		return string;
	}

	/**
	 * 
	 */
	protected void validateInput() {
		String id = this.onText.getText();
		boolean error = false;
		String errorMessage = null;
		if (id == null || "".equals(id)) {
			errorMessage = "A valid test criteria is required. ";
			error = true;
		}
		if (this.ognlButton.getSelection()) {
			if (!id.startsWith(EXPRESSION_PREFIX)
					|| !id.endsWith(EXPRESSION_SUFFIX)) {
				errorMessage = "A valid OGNL expression needs to start with '${' and ends with '}'. ";
				error = true;
			}
			else {
				try {

					Ognl.parseExpression(this.cutExpression(id));
				}
				catch (OgnlException e) {
					errorMessage = "Malformed OGNL expression. ";
					error = true;
				}
			}
		}
		if (isNew) {
			String thenTxt = this.thenText.getText();
			//String elseTxt = this.elseText.getText();
			if (thenTxt == null || "".equals(thenTxt)) {
				errorMessage = "A valid then transition target is required. ";
				error = true;
			}
		}
		if (error) {
			getButton(OK).setEnabled(false);
			setErrorMessage(errorMessage);
		}
		else {
			getButton(OK).setEnabled(true);
			setErrorMessage(null);
		}
	}

}
