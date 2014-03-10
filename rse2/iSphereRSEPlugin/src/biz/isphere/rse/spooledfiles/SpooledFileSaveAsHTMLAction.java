/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Task Force IT-Consulting GmbH - initial API and implementation
 *******************************************************************************/

package biz.isphere.rse.spooledfiles;

public class SpooledFileSaveAsHTMLAction extends AbstractSpooledFileAction {

	public String execute(SpooledFileResource spooledFileResource) {
		return spooledFileResource.getSpooledFile().save(getShell(), "*HTML");
	}

}