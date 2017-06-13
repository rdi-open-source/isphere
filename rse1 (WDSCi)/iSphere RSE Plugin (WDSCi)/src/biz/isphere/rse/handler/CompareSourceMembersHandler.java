/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.ibm.etools.iseries.core.api.ISeriesConnection;
import com.ibm.etools.iseries.core.api.ISeriesMember;

import biz.isphere.core.compareeditor.CompareAction;
import biz.isphere.core.compareeditor.CompareEditorConfiguration;
import biz.isphere.rse.compareeditor.RSECompareDialog;
import biz.isphere.rse.internal.RSEMember;

public class CompareSourceMembersHandler extends AbstractHandler implements IHandler {

    public static final String ID = "biz.isphere.core.command.SourceMemberCompare.open";

    private Shell shell;

    public Object execute(ExecutionEvent arg0) throws ExecutionException {

        RSEMember[] selectedMembers = new RSEMember[0];

        handleSourceCompare(selectedMembers);

        return null;
    }

    public void handleSourceCompare(RSEMember[] selectedMembers) {

        shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

        RSECompareDialog dialog;
        if (selectedMembers.length > 2) {
            dialog = new RSECompareDialog(shell, true, selectedMembers);
        } else if (selectedMembers.length == 2) {
            dialog = new RSECompareDialog(shell, true, selectedMembers[0], selectedMembers[1]);
        } else if (selectedMembers.length == 1) {
            dialog = new RSECompareDialog(shell, true, selectedMembers[0]);
        } else {
            dialog = new RSECompareDialog(shell, true);
        }

        if (dialog.open() == Dialog.OK) {

            boolean editable = dialog.isEditable();
            boolean considerDate = dialog.isConsiderDate();
            boolean ignoreCase = dialog.isIgnoreCase();
            boolean threeWay = dialog.isThreeWay();

            RSEMember rseAncestorMember = null;
            if (threeWay) {
                rseAncestorMember = dialog.getAncestorRSEMember();
            }

            CompareEditorConfiguration cc = new CompareEditorConfiguration();
            cc.setLeftEditable(editable);
            cc.setRightEditable(false);
            cc.setConsiderDate(considerDate);
            cc.setIgnoreCase(ignoreCase);
            cc.setThreeWay(threeWay);

            if (selectedMembers.length > 2) {
                for (RSEMember rseSelectedMember : selectedMembers) {
                    RSEMember rseRightMember = getRightRSEMember(dialog.getRightConnection(), dialog.getRightLibrary(), dialog.getRightFile(),
                        rseSelectedMember.getMember());
                    if (!rseRightMember.exists()) {
                        String message = biz.isphere.core.Messages.bind(biz.isphere.core.Messages.Member_2_of_file_1_in_library_0_not_found,
                            new Object[] { dialog.getRightLibrary(), dialog.getRightFile(), rseSelectedMember.getMember() });
                        MessageDialog.openError(shell, biz.isphere.core.Messages.Error, message);

                    } else {
                        CompareAction action = new CompareAction(cc, rseAncestorMember, rseSelectedMember, rseRightMember, null);
                        action.run();
                    }
                }
            } else {
                RSEMember rseLeftMember = dialog.getLeftRSEMember();
                RSEMember rseRightMember = dialog.getRightRSEMember();
                CompareAction action = new CompareAction(cc, rseAncestorMember, rseLeftMember, rseRightMember, null);
                action.run();
            }
        }
    }

    private RSEMember getRightRSEMember(ISeriesConnection connection, String libraryName, String sourceFileName, String memberName) {
        try {
            return new RSEMember((ISeriesMember)connection.getISeriesMember(libraryName, sourceFileName, memberName));
        } catch (Exception e) {
            MessageDialog.openError(shell, biz.isphere.core.Messages.Error, e.getMessage());
            return null;
        }
    }
}
