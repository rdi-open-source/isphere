/*******************************************************************************
 * Copyright (c) 2005 SoftLanding Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     SoftLanding - initial API and implementation
 *     iSphere Project Owners - Maintenance and enhancements
 *******************************************************************************/
package biz.isphere.messagesubsystem.internal;

import java.util.Vector;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;

import biz.isphere.messagesubsystem.ISphereMessageSubsystemPlugin;
import biz.isphere.messagesubsystem.Messages;

import com.ibm.etools.systems.dftsubsystem.impl.DefaultSubSystemFactoryImpl;
import com.ibm.etools.systems.filters.SystemFilter;
import com.ibm.etools.systems.filters.SystemFilterPool;
import com.ibm.etools.systems.filters.SystemFilterPoolManager;
import com.ibm.etools.systems.filters.ui.actions.SystemChangeFilterAction;
import com.ibm.etools.systems.filters.ui.actions.SystemNewFilterAction;
import com.ibm.etools.systems.model.SystemConnection;
import com.ibm.etools.systems.subsystems.SubSystem;

public class QueuedMessageSubSystemFactory extends DefaultSubSystemFactoryImpl {

    public static final String ID = "biz.isphere.messagesubsystem.internal.QueuedMessageSubSystemFactory";

    public QueuedMessageSubSystemFactory() {
        super();
    }

    @Override
    protected SubSystem createSubSystemInternal(SystemConnection conn) {
        QueuedMessageSubSystem subSystem = new QueuedMessageSubSystem();
        return subSystem;
    }

    @Override
    protected void removeSubSystem(SubSystem subSystem) {
        getSubSystems(false);
        super.removeSubSystem(subSystem);
    }

    @Override
    public String getTranslatedFilterTypeProperty(SystemFilter selectedFilter) {
        return Messages.Message_Filter;
    }

    @Override
    protected SystemFilterPool createDefaultFilterPool(SystemFilterPoolManager mgr) {
        SystemFilterPool defaultPool = super.createDefaultFilterPool(mgr);
        Vector<String> strings = new Vector<String>();
        QueuedMessageFilter messageFilter = new QueuedMessageFilter();
        messageFilter.setMessageQueue("*CURRENT"); //$NON-NLS-1$
        strings.add(messageFilter.getFilterString());
        try {
            SystemFilter filter = mgr.createSystemFilter(defaultPool, Messages.My_Messages, strings);
            filter.setType(Messages.Message_Filter);
        } catch (Exception exc) {
        }
        return defaultPool;
    }

    @Override
    public boolean supportsNestedFilters() {
        return false;
    }
    
    /*
     * Start of RDi/WDSCi specific methods.
     */

    @Override
    protected IAction[] getNewFilterPoolFilterActions(SystemFilterPool selectedPool, Shell shell) {
        SystemNewFilterAction filterAction = (SystemNewFilterAction)super.getNewFilterPoolFilterAction(selectedPool, shell);
        filterAction.setWizardPageTitle(Messages.Message_Filter);
        filterAction.setPage1Description(Messages.Create_a_new_filter_to_list_messages);
        filterAction.setType(Messages.Message_Filter);
        filterAction.setText(Messages.Message_Filter_Dots);
        filterAction.setFilterStringEditPane(new QueuedMessageFilterStringEditPane(shell));
        IAction[] actions = new IAction[1];
        actions[0] = filterAction;
        return actions;
    }

    @Override
    protected IAction getChangeFilterAction(SystemFilter selectedFilter, Shell shell) {
        SystemChangeFilterAction action = (SystemChangeFilterAction)super.getChangeFilterAction(selectedFilter, shell);
        // String type = selectedFilter.getType();
        action.setDialogTitle(Messages.Change_Message_Filter);
        action.setFilterStringEditPane(new QueuedMessageFilterStringEditPane(shell));
        return action;
    }

    @Override
    protected Vector getAdditionalFilterActions(SystemFilter selectedFilter, Shell shell) {
        Vector actions = new Vector();
        return actions;
    }

    @Override
    public ImageDescriptor getSystemFilterImage(SystemFilter filter) {
        return ISphereMessageSubsystemPlugin.getImageDescriptor(ISphereMessageSubsystemPlugin.IMAGE_MESSAGE_FILTER);
    }

}