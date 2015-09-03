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
package org.springframework.ide.eclipse.boot.util;

import java.io.File;
import java.io.IOException;

import org.springframework.ide.eclipse.boot.core.BootActivator;

public class FileUtil {

	public static boolean isJarFile(File jarFile) {
		try {
			return jarFile!=null && jarFile.isFile() && jarFile.toString().toLowerCase().endsWith(".jar");
		} catch (Throwable e) {
			BootActivator.log(e);
			return false;
		}
	}

	public static File getTempFolder(String TEMP_FOLDER_NAME) throws IOException {
		File tempFolder = File.createTempFile(TEMP_FOLDER_NAME, null);
		tempFolder.delete();
		tempFolder.mkdirs();
		if (!tempFolder.exists()) {
			throw new IOException("Failed to create temporary jar file when packaging application for deployment: "
							+ tempFolder.getAbsolutePath());
		}
		return tempFolder;
	}


}
