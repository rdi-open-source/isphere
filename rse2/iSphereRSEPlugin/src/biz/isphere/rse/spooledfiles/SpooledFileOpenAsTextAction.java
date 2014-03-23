/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.spooledfiles;

import biz.isphere.core.preferencepages.IPreferences;

public class SpooledFileOpenAsTextAction extends AbstractSpooledFileAction {

	public String execute(SpooledFileResource spooledFileResource) {
		return spooledFileResource.getSpooledFile().open(IPreferences.OUTPUT_FORMAT_TEXT);
	}

}