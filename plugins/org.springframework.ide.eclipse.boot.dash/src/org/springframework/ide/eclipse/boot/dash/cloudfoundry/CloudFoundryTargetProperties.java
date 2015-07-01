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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.net.MalformedURLException;
import java.net.URL;

import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.springsource.ide.eclipse.commons.livexp.core.CompositeValidator;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

/**
 * Cloud Foundry Target properties that uses {@link LiveExpression} and
 * {@link Validator}.
 *
 */
public class CloudFoundryTargetProperties {

	private LiveVariable<String> url = new LiveVariable<String>();
	private LiveVariable<CloudSpace> space = new LiveVariable<CloudSpace>();
	private LiveVariable<Boolean> selfsigned = new LiveVariable<Boolean>(false);
	private LiveVariable<String> userName = new LiveVariable<String>();
	private LiveVariable<String> password = new LiveVariable<String>();

	private Validator credentialsValidator = new CredentialsValidator();
	private Validator spacesValidator = new SpacesValidator();
	private CompositeValidator allPropertiesValidator = new CompositeValidator();

	public CloudFoundryTargetProperties() {
		// The credentials validator should be notified any time there are
		// changes
		// to url, username, password and selfsigned setting.
		credentialsValidator.dependsOn(url);
		credentialsValidator.dependsOn(selfsigned);
		credentialsValidator.dependsOn(userName);
		credentialsValidator.dependsOn(password);

		// Spaces validator is notified when there are changes to the space
		// variable. This is a separate validator as space validation and spave
		// value setting may only occur AFTER ALL credentials/URL are entered or
		// validated, and different listeners may need to be registered for
		// credential validation
		// vs space validation
		spacesValidator.dependsOn(space);

		// Aggregate of the credentials and space validator.
		allPropertiesValidator.addChild(credentialsValidator);
		allPropertiesValidator.addChild(spacesValidator);
	}

	/**
	 * 
	 * @param allPropertiesValidationListener
	 *            listener that is notified when any property is validated
	 * @param credentialsValidationListener
	 *            listener that is notified when only credential properties are
	 *            validated (but not org/space)
	 * @param cloudSpaceChangeListener
	 *            listener that is notified when Cloud space is changed
	 */
	public void addListeners(ValueListener<ValidationResult> allPropertiesValidationListener,
			ValueListener<ValidationResult> credentialsValidationListener,
			ValueListener<CloudSpace> cloudSpaceChangeListener) {
		allPropertiesValidator.addListener(allPropertiesValidationListener);
		credentialsValidator.addListener(credentialsValidationListener);
		space.addListener(cloudSpaceChangeListener);
	}

	public String getUrl() {
		return this.url.getValue();
	}

	public void setUrl(String url) {
		this.url.setValue(url);
	}

	public CloudSpace getSpace() {
		return this.space.getValue();
	}

	public void setSpace(CloudSpace space) {
		this.space.setValue(space);
	}

	public boolean isSelfsigned() {
		return this.selfsigned.getValue();
	}

	public void setSelfsigned(boolean selfsigned) {
		this.selfsigned.setValue(selfsigned);
	}

	public String getUserName() {
		return this.userName.getValue();
	}

	public void setUserName(String userName) {
		this.userName.setValue(userName);
	}

	public String getPassword() {
		return this.password.getValue();
	}

	public void setPassword(String password) {
		this.password.setValue(password);
	}

	class CredentialsValidator extends Validator {
		@Override
		protected ValidationResult compute() {
			String infoMessage = null;

			if (isEmpty(getUserName())) {
				infoMessage = "Enter a username";
			} else if (isEmpty(getPassword())) {
				infoMessage = "Enter a password";
			} else if (isEmpty(getUrl())) {
				infoMessage = "Enter a target URL";
			} else {
				try {
					new URL(getUrl());
				} catch (MalformedURLException e) {
					return ValidationResult.error(e.getMessage());
				}
			}
			if (infoMessage != null) {
				return ValidationResult.info(infoMessage);
			}

			return ValidationResult.OK;
		}

		protected boolean isEmpty(String value) {
			return value == null || value.trim().length() == 0;
		}
	}

	class SpacesValidator extends Validator {

		@Override
		protected ValidationResult compute() {
			if (getSpace() == null) {
				return ValidationResult.info("Select a Cloud space");
			}
			return ValidationResult.OK;
		}
	}
}
