/*******************************************************************************
 * Copyright (c) 2012-2013 Task Force IT-Consulting GmbH, Waltrop and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Task Force IT-Consulting GmbH - initial API and implementation
 *******************************************************************************/

package biz.isphere.rse.spooledfiles;

import java.util.Vector;

import com.ibm.etools.systems.core.SystemPlugin;
import com.ibm.etools.systems.model.ISystemRemoteChangeEvents;
import com.ibm.etools.systems.model.SystemRegistry;

public class SpooledFileMessageAction extends AbstractSpooledFileAction {

	public String execute(SpooledFileResource spooledFileResource) {

		String message = spooledFileResource.getSpooledFile().replyMessage();
		
		if (message == null) {
			SystemRegistry sr = SystemPlugin.getDefault().getSystemRegistry();
			Vector<SpooledFileResource> spooledFileVector = new Vector<SpooledFileResource>();
			spooledFileVector.addElement(spooledFileResource);
			sr.fireRemoteResourceChangeEvent(
					ISystemRemoteChangeEvents.SYSTEM_REMOTE_RESOURCE_CREATED, 
					spooledFileVector, 
					null, 
					null, 
					null, 
					null);
		}
		
		return message;
		
	}

}