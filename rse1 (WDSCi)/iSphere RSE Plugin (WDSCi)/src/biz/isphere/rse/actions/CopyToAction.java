/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.sourcemembercopy.rse.CopyMemberDialog;
import biz.isphere.core.sourcemembercopy.rse.CopyMemberService;
import biz.isphere.rse.Messages;

import com.ibm.etools.iseries.core.api.ISeriesConnection;
import com.ibm.etools.iseries.core.api.ISeriesObject;
import com.ibm.etools.iseries.core.descriptors.ISeriesDataElementDescriptorType;
import com.ibm.etools.iseries.core.ui.actions.ISeriesSystemBaseAction;
import com.ibm.etools.systems.core.ui.SystemMenuManager;
import com.ibm.etools.systems.core.ui.actions.ISystemDynamicPopupMenuExtension;
import com.ibm.etools.systems.dstore.core.model.DataElement;

public class CopyToAction extends ISeriesSystemBaseAction implements ISystemDynamicPopupMenuExtension {

    protected ArrayList arrayListSelection;

    public CopyToAction() {
        super(Messages.iSphere_CopyMembersTo, "", null);
        arrayListSelection = new ArrayList();
        setContextMenuGroup("additions");
        allowOnMultipleSelection(true);
        setHelp("");
        setImageDescriptor(ISpherePlugin.getImageDescriptor(ISpherePlugin.IMAGE_COPY_MEMBERS_TO));
    }

    public void populateMenu(Shell shell, SystemMenuManager menu, IStructuredSelection selection, String menuGroup) {
        setShell(shell);
        menu.add("additions", this);
    }

    public boolean supportsSelection(IStructuredSelection selection) {

        this.arrayListSelection.clear();

        ArrayList<DataElement> arrayListSelection = new ArrayList<DataElement>();

        for (Iterator iterSelection = selection.iterator(); iterSelection.hasNext();) {
            Object objSelection = iterSelection.next();
            if (objSelection instanceof DataElement) {
                DataElement dataElement = (DataElement)objSelection;
                ISeriesDataElementDescriptorType type = ISeriesDataElementDescriptorType.getDescriptorTypeObject(dataElement);
                if (type.isSourceMember()) {
                    arrayListSelection.add(dataElement);
                }
            }
        }

        if (arrayListSelection.isEmpty()) {
            return false;
        }

        this.arrayListSelection = arrayListSelection;
        return true;

    }

    @Override
    public void run() {

        if (arrayListSelection.size() > 0) {

            CopyMemberService jobDescription = null;

            for (Iterator iterObjects = arrayListSelection.iterator(); iterObjects.hasNext();) {

                DataElement dataElement = (DataElement)iterObjects.next();

                ISeriesObject object = new ISeriesObject(dataElement);

                if (object != null) {

                    ISeriesConnection iseriesConnection = object.getISeriesConnection();

                    if (iseriesConnection != null) {

                        String connectionName = iseriesConnection.getSystemConnection().getAliasName();
                        if (jobDescription == null) {
                            jobDescription = new CopyMemberService(connectionName);
                        } else {
                            if (!jobDescription.getFromConnectionName().equals(connectionName)) {
                                MessageDialog.openError(getShell(), Messages.E_R_R_O_R,
                                    Messages.Cannot_copy_source_members_from_different_connections);
                                return;
                            }
                        }
                        jobDescription.addItem(object.getFile(), object.getLibrary(), object.getName());

                    }
                }
            }

            if (jobDescription.getItems().length > 0) {
                CopyMemberDialog dialog = new CopyMemberDialog(getShell());
                dialog.setContent(jobDescription);
                dialog.open();
            }
        }
    }
}
