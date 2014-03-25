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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;

import com.ibm.etools.systems.dftsubsystem.impl.DefaultSubSystemFactoryImpl;
import com.ibm.etools.systems.filters.SystemFilter;
import com.ibm.etools.systems.filters.SystemFilterPool;
import com.ibm.etools.systems.filters.SystemFilterPoolManager;
import com.ibm.etools.systems.filters.ui.actions.SystemChangeFilterAction;
import com.ibm.etools.systems.filters.ui.actions.SystemNewFilterAction;
import com.ibm.etools.systems.model.SystemConnection;
import com.ibm.etools.systems.subsystems.SubSystem;

public class SpooledFileSubSystemFactory extends DefaultSubSystemFactoryImpl {

	public SpooledFileSubSystemFactory() {
		super();
	}
	
	protected SubSystem createSubSystemInternal(SystemConnection conn) {
	   	return new SpooledFileSubSystem();
	}
	
	public String getTranslatedFilterTypeProperty(SystemFilter selectedFilter) {
	   	return Messages.getString("Spooled_File_Filter");
	}
	
	protected SystemFilterPool createDefaultFilterPool(SystemFilterPoolManager mgr) {
		SystemFilterPool defaultPool = super.createDefaultFilterPool(mgr);
		Vector<String> strings = new Vector<String>();
		strings.add("*CURRENT/*/*/*/*/");
		try {
		  SystemFilter filter = mgr.createSystemFilter(defaultPool, Messages.getString("My_spooled_files"), strings);
		  filter.setType("spooled file");
		} catch (Exception exc) {}
		return defaultPool;
	}
	
	protected IAction[] getNewFilterPoolFilterActions(SystemFilterPool selectedPool, Shell shell)
	{
	  	SystemNewFilterAction filterAction = (SystemNewFilterAction)super.getNewFilterPoolFilterAction(selectedPool, shell);
	  	filterAction.setWizardPageTitle(Messages.getString("Spooled_File_Filter"));
	  	filterAction.setPage1Description(Messages.getString("Create_a_new_spooled_file_filter"));
	  	filterAction.setType(Messages.getString("Spooled_File_Filter"));
	  	filterAction.setText(Messages.getString("Spooled_file_filter") + "...");
	  	filterAction.setFilterStringEditPane(new SpooledFileFilterStringEditPane(shell));       		  	
	  	IAction[] actions = new IAction[1];
	  	actions[0] = filterAction;
	  	return actions;
	 }
	 
	protected IAction getChangeFilterAction(SystemFilter selectedFilter, Shell shell)
	{
	  	SystemChangeFilterAction action = (SystemChangeFilterAction)super.getChangeFilterAction(selectedFilter, shell);
	  	action.setDialogTitle(Messages.getString("Change_Spooled_File_Filter"));
	  	action.setFilterStringEditPane(new SpooledFileFilterStringEditPane(shell));
	  	return action;
	} 
	
	protected Vector getAdditionalFilterActions(SystemFilter selectedFilter, Shell shell) {
		Vector actions = new Vector();
		return actions;
	}
	
	public ImageDescriptor getSystemFilterImage(SystemFilter filter)
	{
	   	 return ISpherePlugin.getImageDescriptor(ISpherePlugin.IMAGE_SPOOLED_FILE_FILTER);
	}

	public boolean showGenericShowInTableOnFilter() {
		return false;
	}   	

}
