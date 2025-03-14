/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.rse.Messages;
import biz.isphere.rse.connection.ConnectionManager;
import biz.isphere.rse.handler.DisplayModuleViewHandler;

import com.ibm.etools.iseries.services.qsys.api.IQSYSProgramBase;
import com.ibm.etools.iseries.subsystems.qsys.objects.QSYSRemoteObject;
import com.ibm.etools.iseries.subsystems.qsys.objects.QSYSRemoteProgramModule;

public class DisplayModuleViewAction implements IObjectActionDelegate {

    protected IStructuredSelection structuredSelection;
    protected Shell shell;

    public void run(IAction arg0) {

        if (structuredSelection != null && !structuredSelection.isEmpty()) {

            for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
                Object object = iterator.next();

                QSYSRemoteObject qsysRemoteObject = (QSYSRemoteObject)object;

                if (qsysRemoteObject instanceof QSYSRemoteProgramModule) {

                    QSYSRemoteProgramModule qsysRemoteProgramModule = (QSYSRemoteProgramModule)qsysRemoteObject;

                    try {

                        IHost host = qsysRemoteProgramModule.getRemoteObjectContext().getObjectSubsystem().getObjectSubSystem().getHost();
                        String connectionName = ConnectionManager.getConnectionName(host);

                        IQSYSProgramBase program = qsysRemoteProgramModule.getProgram();

                        String programName = program.getName();
                        String libraryName = program.getLibrary();
                        String objectType = program.getType();
                        String moduleName = qsysRemoteProgramModule.getName();

                        Map<String, String> parameters = new HashMap<String, String>();
                        parameters.put(DisplayModuleViewHandler.PARAMETER_CONNECTION_NAME, connectionName);
                        parameters.put(DisplayModuleViewHandler.PARAMETER_PROGRAM_NAME, programName);
                        parameters.put(DisplayModuleViewHandler.PARAMETER_LIBRARY_NAME, libraryName);
                        parameters.put(DisplayModuleViewHandler.PARAMETER_OBJECT_TYPE, objectType);
                        parameters.put(DisplayModuleViewHandler.PARAMETER_MODULE_NAME, moduleName);

                        ExecutionEvent event = new ExecutionEvent(null, parameters, null, null);

                        DisplayModuleViewHandler handler = new DisplayModuleViewHandler();
                        handler.execute(event);

                    } catch (ExecutionException e) {
                        ISpherePlugin.logError("*** Could not retrieve module source ***", e); //$NON-NLS-1$
                        MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), Messages.E_R_R_O_R,
                            e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {

        structuredSelection = null;

        if (isValidSelection(selection)) {
            structuredSelection = (IStructuredSelection)selection;
        }
    }

    private boolean isValidSelection(ISelection selection) {

        boolean isValid = true;

        IStructuredSelection structuredSelection = (IStructuredSelection)selection;
        for (Iterator<?> iterator = structuredSelection.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (!isValidObjectInstance(object)) {
                isValid = false;
                break;
            }
        }

        return isValid;
    }

    private boolean isValidObjectInstance(Object object) {
        return object instanceof QSYSRemoteProgramModule;
    }

    public void setActivePart(IAction action, IWorkbenchPart workbenchPart) {
        shell = workbenchPart.getSite().getShell();
    }

}
