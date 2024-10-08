/*******************************************************************************
 * Copyright (c) 2012-2024 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.compareeditor.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;

import biz.isphere.core.compareeditor.CompareAction;
import biz.isphere.core.compareeditor.CompareEditorConfiguration;
import biz.isphere.core.compareeditor.SourceMemberCompareEditorConfiguration;
import biz.isphere.core.internal.IProjectMember;
import biz.isphere.core.internal.Member;
import biz.isphere.core.internal.handler.AbstractCommandHandler;
import biz.isphere.rse.compareeditor.RSECompareDialog;
import biz.isphere.rse.connection.ConnectionManager;
import biz.isphere.rse.internal.RSEMember;

public class CompareSourceMembersHandler extends AbstractCommandHandler {

    public static final String ID = "biz.isphere.core.command.SourceMemberCompare.open";

    public Object execute(ExecutionEvent event) throws ExecutionException {

        RSEMember[] selectedMembers = new RSEMember[0];

        handleSourceCompareInternally(getShell(), selectedMembers, true);

        return null;
    }

    // TODO: consider replacing the other handle* methods with this method
    public void handleSourceCompare(Member[] members, CompareEditorConfiguration cc) {

        if (cc.isShowDialog()) {
            if (cc.isEditMode()) {
                handleSourceCompare(members);
            } else {
                handleReadOnlySourceCompare(members);
            }
        } else {
            handleSourceCompareWithoutDialog(members, cc);
        }
    }

    private void handleSourceCompareWithoutDialog(Member[] members, CompareEditorConfiguration cc) {

        cc.setDropSequenceNumbersAndDateFields(!hasSequenceNumbersAndDateFields(members));

        Member leftMember = members[0];
        Member rightMember = members[1];
        CompareAction action = new CompareAction(cc, null, leftMember, rightMember, null);
        action.run();
    }

    public void handleReadOnlySourceCompare(Member[] selectedMembers) {
        handleSourceCompareInternally(getShell(), selectedMembers, false);
    }

    public void handleSourceCompare(Member[] selectedMembers) {
        handleSourceCompareInternally(getShell(), selectedMembers, true);
    }

    private void handleSourceCompareInternally(Shell shell, Member[] selectedMembers, boolean selectEditable) {

        RSECompareDialog dialog;
        if (selectedMembers.length > 2) {
            dialog = new RSECompareDialog(shell, selectEditable, selectedMembers);
        } else if (selectedMembers.length == 2) {
            dialog = new RSECompareDialog(shell, selectEditable, selectedMembers[0], selectedMembers[1]);
        } else if (selectedMembers.length == 1) {
            dialog = new RSECompareDialog(shell, selectEditable, selectedMembers[0]);
        } else {
            dialog = new RSECompareDialog(shell, selectEditable);
        }

        dialog.setDateOptionsEnabled(hasSequenceNumbersAndDateFields(selectedMembers));

        if (dialog.open() == Dialog.OK) {

            boolean editable = dialog.isEditable();
            boolean considerDate = dialog.isConsiderDate();
            boolean ignoreCase = dialog.isIgnoreCase();
            boolean ignoreChangesLeft = dialog.isIgnoreChangesLeft();
            boolean ignoreChangesRight = dialog.isIgnoreChangesRight();
            boolean threeWay = dialog.isThreeWay();

            RSEMember rseAncestorMember = null;
            if (threeWay) {
                rseAncestorMember = dialog.getAncestorRSEMember();
            }

            CompareEditorConfiguration cc = new SourceMemberCompareEditorConfiguration();
            cc.setLeftEditable(editable);
            cc.setRightEditable(false);
            cc.setConsiderDate(considerDate);
            cc.setIgnoreCase(ignoreCase);
            cc.setIgnoreChangesLeft(ignoreChangesLeft);
            cc.setIgnoreChangesRight(ignoreChangesRight);
            cc.setThreeWay(threeWay);
            cc.setDropSequenceNumbersAndDateFields(!hasSequenceNumbersAndDateFields(selectedMembers));

            if (selectedMembers.length > 2) {
                Member rseRightMember = dialog.getRightRSEMember();
                String rightConnection = rseRightMember.getConnection();
                String rightLibrary = rseRightMember.getLibrary();
                String rightSourceFile = rseRightMember.getSourceFile();
                for (Member rseCurrentLeftMember : selectedMembers) {
                    String currentMemberName = rseCurrentLeftMember.getMember();
                    rseRightMember = getMember(shell, rightConnection, rightLibrary, rightSourceFile, currentMemberName);
                    if (!rseRightMember.exists()) {
                        String message = biz.isphere.core.Messages.bind(biz.isphere.core.Messages.Member_2_of_file_1_in_library_0_not_found,
                            new Object[] { rightLibrary, rightSourceFile, currentMemberName });
                        MessageDialog.openError(shell, biz.isphere.core.Messages.Error, message);

                    } else {
                        CompareAction action = new CompareAction(cc, rseAncestorMember, rseCurrentLeftMember, rseRightMember, null);
                        action.run();
                    }
                }
            } else {
                Member rseLeftMember;
                if (selectedMembers.length == 1 && selectedMembers[0] instanceof IProjectMember) {
                    // Do not update the i Project member. The member is
                    // read-only in the dialog and there is no "switch" button
                    // in the dialog for switching the left and right members.
                    // We need to keep the i Project member because of the path
                    // to the local resource in the i Project.
                    rseLeftMember = selectedMembers[0];
                } else {
                    rseLeftMember = dialog.getLeftRSEMember();
                }
                Member rseRightMember = dialog.getRightRSEMember();
                CompareAction action = new CompareAction(cc, rseAncestorMember, rseLeftMember, rseRightMember, null);
                action.run();
            }
        }
    }

    private boolean hasSequenceNumbersAndDateFields(Member[] selectedMembers) {

        for (Member member : selectedMembers) {
            if (!member.hasSequenceNumbersAndDateFields()) {
                return false;
            }
        }

        return true;
    }

    private RSEMember getMember(Shell shell, String connectionName, String libraryName, String sourceFileName, String memberName) {
        try {
            IBMiConnection connection = ConnectionManager.getIBMiConnection(connectionName);
            return new RSEMember(connection.getMember(libraryName, sourceFileName, memberName, null));
        } catch (Exception e) {
            MessageDialog.openError(shell, biz.isphere.core.Messages.Error, e.getMessage());
            return null;
        }
    }
}
