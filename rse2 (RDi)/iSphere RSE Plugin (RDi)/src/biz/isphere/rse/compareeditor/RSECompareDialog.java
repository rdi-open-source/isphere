/*******************************************************************************
 * Copyright (c) 2012-2023 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.compareeditor;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.ibm.etools.iseries.rse.ui.widgets.QSYSMemberPrompt;
import com.ibm.etools.iseries.services.qsys.api.IQSYSFile;
import com.ibm.etools.iseries.services.qsys.api.IQSYSLibrary;
import com.ibm.etools.iseries.subsystems.qsys.api.IBMiConnection;

import biz.isphere.base.internal.ExceptionHelper;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.base.swt.widgets.UpperCaseOnlyVerifier;
import biz.isphere.core.annotations.CMOne;
import biz.isphere.core.compareeditor.CompareDialog;
import biz.isphere.core.compareeditor.LoadPreviousMemberValue;
import biz.isphere.core.internal.Member;
import biz.isphere.core.swt.widgets.WidgetFactory;
import biz.isphere.core.swt.widgets.connectioncombo.ConnectionCombo;
import biz.isphere.rse.Messages;
import biz.isphere.rse.connection.ConnectionManager;
import biz.isphere.rse.internal.RSEMember;

public class RSECompareDialog extends CompareDialog {

    private Group leftGroup;
    private Group ancestorGroup;

    private ConnectionCombo leftConnectionCombo;
    private QSYSMemberPrompt leftMemberPrompt;
    private IBMiConnection leftConnection;
    private String leftLibrary;
    private String leftFile;
    private String leftMember;

    private ConnectionCombo rightConnectionCombo;
    private QSYSMemberPrompt rightMemberPrompt;
    private IBMiConnection rightConnection;
    private String rightLibrary;
    private String rightFile;
    private String rightMember;

    private ConnectionCombo ancestorConnectionCombo;
    private QSYSMemberPrompt ancestorMemberPrompt;
    private IBMiConnection ancestorConnection;
    private String ancestorLibrary;
    private String ancestorFile;
    private String ancestorMember;

    /**
     * Creates the compare dialog, for 2 selected member.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option "Open for
     *        browse/edit" is displayed
     * @param leftMember - the left selected member
     * @param rightMember - the right selected member
     * @param switchMemberAllowed - Specifies, whether or not the switch button
     *        appears.
     */
    @CMOne(info = "Don`t change this constructor due to CMOne compatibility reasons")
    public RSECompareDialog(Shell parentShell, boolean selectEditable, RSEMember leftMember, RSEMember rightMember, boolean switchMemberAllowed) {
        super(parentShell, selectEditable, leftMember, rightMember);
        setHistoryValuesCategoryKey(null);
        initializeLeftMember(leftMember);
        initializeRightMember(rightMember);
        setSwitchMemberAllowed(switchMemberAllowed);
    }

    /**
     * Creates a three-way compare dialog.<br>
     * This constructor is used by CMOne.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option "Open for
     *        browse/edit" is displayed
     * @param leftMember - the left selected member
     * @param rightMember - the right selected member
     * @param ancestorMember - the ancestor member
     * @param switchMemberAllowed - Specifies, whether or not the switch button
     *        appears.
     */
    @CMOne(info = "Don`t change this constructor due to CMOne compatibility reasons")
    public RSECompareDialog(Shell parentShell, boolean selectEditable, RSEMember leftMember, RSEMember rightMember, RSEMember ancestorMember,
        boolean switchMemberAllowed) {
        super(parentShell, selectEditable, leftMember, rightMember, ancestorMember);
        setHistoryValuesCategoryKey(null);
        initializeLeftMember(leftMember);
        initializeRightMember(rightMember);
        initializeAncestorMember(ancestorMember);
        setSwitchMemberAllowed(switchMemberAllowed);
    }

    /**
     * Creates the compare dialog, for 3 and more selected member.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option "Open for
     *        browse/edit" is displayed
     * @param selectedMembers - the selected members that go to the left side of
     *        the compare dialog
     */
    public RSECompareDialog(Shell parentShell, boolean selectEditable, Member[] selectedMembers) {
        super(parentShell, selectEditable, selectedMembers);
        setHistoryValuesCategoryKey("multiple");
        initializeLeftMember(selectedMembers[0]);
        initializeRightMember(selectedMembers[0]);
    }

    /**
     * Creates the compare dialog, for 2 selected member.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option "Open for
     *        browse/edit" is displayed
     * @param leftMember - the left selected member
     * @param rightMember - the right selected member
     */
    public RSECompareDialog(Shell parentShell, boolean selectEditable, Member leftMember, Member rightMember) {
        super(parentShell, selectEditable, leftMember, rightMember);
        setHistoryValuesCategoryKey("2");
        initializeLeftMember(leftMember);
        initializeRightMember(rightMember);
    }

    /**
     * Creates the compare dialog, for 1 selected member.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option "Open for
     *        browse/edit" is displayed
     * @param leftMember - the left selected member
     */
    public RSECompareDialog(Shell parentShell, boolean selectEditable, Member leftMember) {
        super(parentShell, selectEditable, leftMember);
        setHistoryValuesCategoryKey("1");
        initializeLeftMember(leftMember);
    }

    /**
     * Creates the compare dialog, for 0 selected members.
     * 
     * @param parentShell - shell the dialog is associated to
     * @param selectEditable - specifies whether or not option "Open for
     *        browse/edit" is displayed
     */
    public RSECompareDialog(Shell parentShell, boolean selectEditable) {
        super(parentShell, selectEditable);
        setHistoryValuesCategoryKey("0");
    }

    private void initializeLeftMember(Member leftMember) {
        this.leftConnection = getConnection(leftMember);
        this.leftLibrary = leftMember.getLibrary();
        this.leftFile = leftMember.getSourceFile();
        this.leftMember = leftMember.getMember();
    }

    private void initializeRightMember(Member rightMember) {
        this.rightConnection = getConnection(rightMember);
        this.rightLibrary = rightMember.getLibrary();
        this.rightFile = rightMember.getSourceFile();
        this.rightMember = rightMember.getMember();
    }

    private void initializeAncestorMember(Member ancestorMember) {
        this.ancestorConnection = getConnection(ancestorMember);
        this.ancestorLibrary = ancestorMember.getLibrary();
        this.ancestorFile = ancestorMember.getSourceFile();
        this.ancestorMember = ancestorMember.getMember();
    }

    private IBMiConnection getConnection(Member member) {

        if (member instanceof RSEMember) {
            return ((RSEMember)member).getRSEConnection();
        } else {
            return ConnectionManager.getIBMiConnection(member.getConnection());
        }

    }

    @Override
    protected void createEditableLeftArea(Composite parent) {

        leftGroup = createMemberGroup(parent, getLeftGroupLabel());

        SelectionListener selectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setOKButtonEnablement();
                leftMemberPrompt.setSystemConnection(getHost(getCurrentLeftConnectionName()));
            }
        };

        leftConnectionCombo = createConnectionCombo(leftGroup, getLeftConnection(), selectionListener);

        ModifyListener modifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setOKButtonEnablement();
            }
        };

        leftMemberPrompt = createMemberPrompt(leftGroup, modifyListener, PREFIX_LEFT);
        leftMemberPrompt.getLibraryCombo().setFocus();
    }

    @Override
    protected void createEditableRightArea(Composite parent) {

        Group rightGroup = createMemberGroup(parent, Messages.Right);

        SelectionListener selectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setOKButtonEnablement();
                rightMemberPrompt.setSystemConnection(getHost(getCurrentRightConnectionName()));
            }
        };

        // Initialize right connection with left connection
        rightConnectionCombo = createConnectionCombo(rightGroup, getLeftConnection(), selectionListener);

        ModifyListener modifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setOKButtonEnablement();
            }
        };

        rightMemberPrompt = createMemberPrompt(rightGroup, modifyListener, PREFIX_RIGHT);
        rightMemberPrompt.getLibraryCombo().setFocus();

        rightMemberPrompt.getMemberCombo().setEnabled(!hasMultipleRightMembers());
        rightMemberPrompt.getMemberBrowseButton().setEnabled(!hasMultipleRightMembers());
    }

    @Override
    protected void createEditableAncestorArea(Composite parent) {

        ancestorGroup = createMemberGroup(parent, Messages.Ancestor);

        SelectionListener selectionListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setOKButtonEnablement();
                ancestorMemberPrompt.setSystemConnection(getHost(getCurrentAncestorConnectionName()));
            }
        };

        // Initialize ancestor connection with left connection
        ancestorConnectionCombo = createConnectionCombo(ancestorGroup, getLeftConnection(), selectionListener);

        ModifyListener modifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setOKButtonEnablement();
            }
        };

        ancestorMemberPrompt = createMemberPrompt(ancestorGroup, modifyListener, PREFIX_ANCESTOR);
    }

    protected void setLeftGroupLabel(String label) {
        if (leftGroup == null) {
            super.setLeftGroupLabel(label);
        } else {
            leftGroup.setText(label);
        }
    }

    private Group createMemberGroup(Composite parent, String label) {

        Group memberGroup = new Group(parent, SWT.NONE);
        memberGroup.setText(label);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        memberGroup.setLayout(layout);
        memberGroup.setLayoutData(getGridData());

        return memberGroup;
    }

    private ConnectionCombo createConnectionCombo(Group group, IBMiConnection connection, SelectionListener selectionListener) {

        ConnectionCombo connectionCombo = WidgetFactory.createConnectionCombo(group);
        if (connection != null) {
            connectionCombo.setQualifiedConnectionName(connection.getConnectionName());
        }
        connectionCombo.setLayoutData(getGridData());

        connectionCombo.addSelectionListener(selectionListener);

        return connectionCombo;
    }

    private QSYSMemberPrompt createMemberPrompt(Group leftGroup, ModifyListener modifyListener, String memberPromptType) {

        QSYSMemberPrompt memberPrompt = new QSYSMemberPrompt(leftGroup, SWT.NONE, false, false, QSYSMemberPrompt.FILETYPE_SRC);

        if (canStoreHistory()) {
            memberPrompt.getMemberCombo().setHistoryKey(getMemberPromptHistoryKey(memberPromptType, OBJECT_TYPE_MBR));
            memberPrompt.getFileCombo().setHistoryKey(getMemberPromptHistoryKey(memberPromptType, OBJECT_TYPE_SRC));
            memberPrompt.getLibraryCombo().setHistoryKey(getMemberPromptHistoryKey(memberPromptType, OBJECT_TYPE_LIB));
        }

        memberPrompt.getMemberCombo().setAutoUpperCase(true);
        memberPrompt.getFileCombo().setAutoUpperCase(true);
        memberPrompt.getLibraryCombo().setAutoUpperCase(true);

        memberPrompt.getMemberCombo().addModifyListener(modifyListener);
        memberPrompt.getFileCombo().addModifyListener(modifyListener);
        memberPrompt.getLibraryCombo().addModifyListener(modifyListener);

        memberPrompt.getMemberCombo().getCombo().addVerifyListener(new UpperCaseOnlyVerifier());
        memberPrompt.getFileCombo().getCombo().addVerifyListener(new UpperCaseOnlyVerifier());
        memberPrompt.getLibraryCombo().getCombo().addVerifyListener(new UpperCaseOnlyVerifier());

        return memberPrompt;
    }

    private void setOKButtonEnablement() {
        if (getOkButton() != null) {
            getOkButton().setEnabled(canFinish());
        }
    }

    @Override
    protected void setAncestorVisible(boolean visible) {
        ancestorGroup.setVisible(visible);
        if (visible) {
            ancestorMemberPrompt.getLibraryCombo().setFocus();
        } else {
            rightMemberPrompt.getLibraryCombo().setFocus();
        }
    }

    @Override
    public void setFocus() {

        if (leftMemberPrompt != null && StringHelper.isNullOrEmpty(getCurrentLeftLibraryName())) {
            leftMemberPrompt.getLibraryCombo().setFocus();
            return;
        }
        if (leftMemberPrompt != null && StringHelper.isNullOrEmpty(getCurrentLeftFileName())) {
            leftMemberPrompt.getFileCombo().setFocus();
            return;
        }
        if (leftMemberPrompt != null && StringHelper.isNullOrEmpty(getCurrentLeftMemberName())) {
            leftMemberPrompt.getMemberCombo().setFocus();
            return;
        }

        if (rightMemberPrompt != null && StringHelper.isNullOrEmpty(getCurrentRightLibraryName())) {
            rightMemberPrompt.getLibraryCombo().setFocus();
            return;
        }
        if (rightMemberPrompt != null && StringHelper.isNullOrEmpty(getCurrentRightFileName())) {
            rightMemberPrompt.getFileCombo().setFocus();
            return;
        }
        if (rightMemberPrompt != null && StringHelper.isNullOrEmpty(getCurrentRightMemberName())) {
            rightMemberPrompt.getMemberCombo().setFocus();
            return;
        }

        if (ancestorMemberPrompt != null && StringHelper.isNullOrEmpty(getCurrentAncestorLibraryName())) {
            ancestorMemberPrompt.getLibraryCombo().setFocus();
            return;
        }
        if (ancestorMemberPrompt != null && StringHelper.isNullOrEmpty(getCurrentAncestorFileName())) {
            ancestorMemberPrompt.getFileCombo().setFocus();
            return;
        }
        if (ancestorMemberPrompt != null && StringHelper.isNullOrEmpty(getCurrentAncestorMemberName())) {
            ancestorMemberPrompt.getMemberCombo().setFocus();
            return;
        }

    }

    @Override
    protected void okPressed() {

        if (hasEditableLeftMember()) {

            leftConnection = getCurrentLeftConnection();
            leftLibrary = getCurrentLeftLibraryName();
            leftFile = getCurrentLeftFileName();
            leftMember = getCurrentLeftMemberName();

            if (!validateMember(leftConnection, leftLibrary, leftFile, leftMember, leftMemberPrompt)) {
                return;
            }

        }

        if (hasEditableRightMember()) {

            rightConnection = getCurrentRightConnection();
            rightLibrary = getCurrentRightLibraryName();
            rightFile = getCurrentRightFileName();

            if (hasMultipleRightMembers()) {
                rightMember = null;
            } else {
                rightMember = getCurrentRightMemberName();
            }

            if (!validateMember(rightConnection, rightLibrary, rightFile, rightMember, rightMemberPrompt)) {
                return;
            }

        }

        if (isThreeWay() && hasEditableAncestorMember()) {

            ancestorConnection = getCurrentAncestorConnection();
            ancestorLibrary = getCurrentAncestorLibraryName();
            ancestorFile = getCurrentAncestorFileName();
            ancestorMember = getCurrentAncestorMemberName();

            if (!validateMember(ancestorConnection, ancestorLibrary, ancestorFile, ancestorMember, ancestorMemberPrompt)) {
                return;
            }

        }

        // Close dialog
        super.okPressed();
    }

    private boolean validateMember(IBMiConnection connection, String library, String file, String member, QSYSMemberPrompt memberPrompt) {

        if (!checkLibrary(connection, library)) {
            displayLibraryNotFoundMessage(library, memberPrompt);
            return false;
        }

        if (!checkFile(connection, library, file)) {
            displayFileNotFoundMessage(library, file, memberPrompt);
            return false;
        }

        if (member != null && !checkMember(connection, library, file, member)) {
            displayMemberNotFoundMessage(library, file, member, memberPrompt);
            return false;
        }

        return true;
    }

    private boolean checkLibrary(IBMiConnection connection, String libraryName) {

        IQSYSLibrary qsysLibrary = null;
        try {
            qsysLibrary = connection.getLibrary(libraryName, null);
        } catch (Exception e) {
        }

        if (qsysLibrary != null) {
            return true;
        }

        return false;
    }

    private void displayLibraryNotFoundMessage(String libraryName, QSYSMemberPrompt qsysMemberPrompt) {

        String message = biz.isphere.core.Messages.bind(Messages.Library_A_not_found, new Object[] { libraryName });
        MessageDialog.openError(getShell(), biz.isphere.core.Messages.Error, message);
        qsysMemberPrompt.getLibraryCombo().setFocus();
    }

    private boolean checkFile(IBMiConnection connection, String libraryName, String fileName) {

        IQSYSFile qsysFile = null;
        try {
            qsysFile = connection.getFile(libraryName, fileName, null);
        } catch (Exception e) {
        }

        if (qsysFile != null) {
            return true;
        }

        return false;
    }

    private void displayFileNotFoundMessage(String libraryName, String fileName, QSYSMemberPrompt qsysMemberPrompt) {

        String message = biz.isphere.core.Messages.bind(Messages.File_A_in_library_B_not_found, new Object[] { fileName, libraryName });
        MessageDialog.openError(getShell(), biz.isphere.core.Messages.Error, message);
        qsysMemberPrompt.getFileCombo().setFocus();
    }

    private boolean checkMember(IBMiConnection connection, String libraryName, String fileName, String memberName) {

        RSEMember rseMember = getRSEMember(connection, libraryName, fileName, memberName);
        if (rseMember == null) {
            return false;
        }

        if (rseMember.exists()) {
            return true;
        }

        return false;
    }

    private void displayMemberNotFoundMessage(String libraryName, String fileName, String memberName, QSYSMemberPrompt qsysMemberPrompt) {

        String message = biz.isphere.core.Messages.bind(biz.isphere.core.Messages.Member_2_of_file_1_in_library_0_not_found,
            new Object[] { libraryName, fileName, memberName });
        MessageDialog.openError(getShell(), biz.isphere.core.Messages.Error, message);
        qsysMemberPrompt.getMemberCombo().setFocus();

    }

    @Override
    protected boolean canFinish() {

        // Check left member is specified
        if (StringHelper.isNullOrEmpty(getCurrentLeftConnectionName()) || StringHelper.isNullOrEmpty(getCurrentLeftLibraryName())
            || StringHelper.isNullOrEmpty(getCurrentLeftFileName()) || StringHelper.isNullOrEmpty(getCurrentLeftMemberName())) {
            return false;
        }

        // Check right member is specified
        if (StringHelper.isNullOrEmpty(getCurrentRightConnectionName()) || StringHelper.isNullOrEmpty(getCurrentRightLibraryName())
            || StringHelper.isNullOrEmpty(getCurrentRightFileName()) || StringHelper.isNullOrEmpty(getCurrentRightMemberName())) {
            return false;
        }

        // Check ancestor member is specified
        if (isThreeWay()) {
            if (StringHelper.isNullOrEmpty(getCurrentAncestorConnectionName()) || StringHelper.isNullOrEmpty(getCurrentAncestorLibraryName())
                || StringHelper.isNullOrEmpty(getCurrentAncestorFileName()) || StringHelper.isNullOrEmpty(getCurrentAncestorMemberName())) {
                return false;
            }
        }

        // Ensure right and left members are different
        if (getCurrentRightConnectionName().equalsIgnoreCase(getCurrentLeftConnectionName())
            && getCurrentRightLibraryName().equalsIgnoreCase(getCurrentLeftLibraryName())
            && getCurrentRightFileName().equalsIgnoreCase(getCurrentLeftFileName())
            && (hasMultipleRightMembers() || getCurrentRightMemberName().equalsIgnoreCase(getCurrentLeftMemberName()))) {
            return false;
        }

        if (isThreeWay()) {
            // Ensure ancestor member is different from right member
            if (getCurrentAncestorConnectionName().equalsIgnoreCase(getCurrentRightConnectionName())
                && getCurrentAncestorLibraryName().equalsIgnoreCase(getCurrentRightLibraryName())
                && getCurrentAncestorFileName().equalsIgnoreCase(getCurrentRightFileName())
                && getCurrentAncestorMemberName().equalsIgnoreCase(getCurrentRightMemberName())) {
                return false;
            }
            // Ensure ancestor member is different from left member
            if (getCurrentAncestorConnectionName().equalsIgnoreCase(getCurrentLeftConnectionName())
                && getCurrentAncestorLibraryName().equalsIgnoreCase(getCurrentLeftLibraryName())
                && getCurrentAncestorFileName().equalsIgnoreCase(getCurrentLeftFileName())
                && getCurrentAncestorMemberName().equalsIgnoreCase(getCurrentLeftMemberName())) {
                return false;
            }
        }

        return true;
    }

    private IHost getHost(String qualifiedConnectionName) {

        if (StringHelper.isNullOrEmpty(qualifiedConnectionName)) {
            return null;
        }

        IBMiConnection ibMiConnection = ConnectionManager.getIBMiConnection(qualifiedConnectionName);
        if (ibMiConnection == null) {
            return null;
        }

        IHost host = ibMiConnection.getHost();
        return host;
    }

    private IBMiConnection getCurrentLeftConnection() {
        if (leftConnectionCombo == null) {
            // return value for read-only left member
            return leftConnection;
        }
        return ConnectionManager.getIBMiConnection(getCurrentLeftConnectionName());
    }

    private String getCurrentLeftConnectionName() {
        if (leftConnectionCombo == null) {
            // return value for read-only left member
            IBMiConnection connection = getLeftConnection();
            String qualifiedConnectionName = ConnectionManager.getConnectionName(connection);
            return qualifiedConnectionName;
        }
        return leftConnectionCombo.getQualifiedConnectionName();
    }

    private String getCurrentLeftLibraryName() {
        if (leftMemberPrompt == null) {
            // return value for read-only left member
            return getLeftLibrary();
        }
        return leftMemberPrompt.getLibraryName();
    }

    private String getCurrentLeftFileName() {
        if (leftMemberPrompt == null) {
            // return value for read-only left member
            return getLeftFile();
        }
        return leftMemberPrompt.getFileName();
    }

    private String getCurrentLeftMemberName() {
        if (leftMemberPrompt == null) {
            // return value for read-only left member
            return getLeftMember();
        }
        return leftMemberPrompt.getMemberName();
    }

    private IBMiConnection getCurrentRightConnection() {
        return ConnectionManager.getIBMiConnection(getCurrentRightConnectionName());
    }

    private String getCurrentRightConnectionName() {
        String qualifiedConnectionName = rightConnectionCombo.getQualifiedConnectionName();
        return qualifiedConnectionName;
    }

    private String getCurrentRightLibraryName() {
        return rightMemberPrompt.getLibraryName();
    }

    private String getCurrentRightFileName() {
        return rightMemberPrompt.getFileName();
    }

    private String getCurrentRightMemberName() {
        return rightMemberPrompt.getMemberName();
    }

    private IBMiConnection getCurrentAncestorConnection() {
        return ConnectionManager.getIBMiConnection(getCurrentAncestorConnectionName());
    }

    private String getCurrentAncestorConnectionName() {
        String qualifiedConnectionName = ancestorConnectionCombo.getQualifiedConnectionName();
        return qualifiedConnectionName;
    }

    private String getCurrentAncestorLibraryName() {
        return ancestorMemberPrompt.getLibraryName();
    }

    private String getCurrentAncestorFileName() {
        return ancestorMemberPrompt.getFileName();
    }

    private String getCurrentAncestorMemberName() {
        return ancestorMemberPrompt.getMemberName();
    }

    public RSEMember getRightRSEMember() {
        return getRSEMember(rightConnection, rightLibrary, rightFile, rightMember);
    }

    public RSEMember getLeftRSEMember() {
        return getRSEMember(leftConnection, leftLibrary, leftFile, leftMember);
    }

    public RSEMember getAncestorRSEMember() {
        return getRSEMember(ancestorConnection, ancestorLibrary, ancestorFile, ancestorMember);
    }

    private RSEMember getRSEMember(IBMiConnection connection, String library, String file, String member) {

        try {
            return new RSEMember(connection.getMember(library, file, member, null));
        } catch (Exception e) {
            MessageDialog.openError(getShell(), biz.isphere.core.Messages.Error, e.getMessage());
            return null;
        }
    }

    @Override
    protected void switchLeftAndRightMember(Member leftMember, Member rightMember) {
        super.switchLeftAndRightMember(leftMember, rightMember);
        initializeRightMember((RSEMember)leftMember);
        initializeLeftMember((RSEMember)rightMember);
    }

    private IBMiConnection getLeftConnection() {
        return leftConnection;
    }

    private String getLeftLibrary() {
        if (leftLibrary == null) {
            return ""; //$NON-NLS-1$
        }
        return leftLibrary;
    }

    private String getLeftFile() {
        if (leftFile == null) {
            return ""; //$NON-NLS-1$
        }
        return leftFile;
    }

    private String getLeftMember() {
        if (leftMember == null) {
            return ""; //$NON-NLS-1$
        }
        return leftMember;
    }

    @Override
    protected void loadScreenValues() {
        super.loadScreenValues();

        if (hasEditableLeftMember()) {
            // Load left member, when no members has been selected (iSphere
            // search selected from the main menu)
            if (isLoadingPreviousValuesOfLeftMemberEnabled()) {
                LoadPreviousMemberValue loadPreviousValue = LoadPreviousMemberValue.CONNECTION_LIBRARY_FILE_MEMBER;
                loadMemberValues(PREFIX_LEFT, loadPreviousValue, leftConnectionCombo, leftMemberPrompt);
            }
        }

        if (hasEditableRightMember()) {

            boolean hasLoaded = false;

            if (isLoadingPreviousValuesOfRightMemberEnabled()) {
                // Load previous member values
                LoadPreviousMemberValue loadPreviousValue = getLoadPreviousValuesOfRightMember();
                hasLoaded = loadMemberValues(PREFIX_RIGHT, loadPreviousValue, rightConnectionCombo, rightMemberPrompt);
            }

            if (!hasLoaded) {
                // Initialize right member with left member prompt
                setMemberValues(rightConnectionCombo, rightMemberPrompt, getCurrentLeftConnectionName(), getCurrentLeftLibraryName(),
                    getCurrentLeftFileName(), getCurrentLeftMemberName());
            }

            if (hasMultipleRightMembers()) {
                // Overwrite right member name to: *LEFT
                rightMemberPrompt.getMemberCombo().setText(SPECIAL_MEMBER_NAME_LEFT);
            }
        }

        if (hasEditableAncestorMember()) {

            boolean hasLoaded = false;

            if (isLoadingPreviousValuesOfAncestorMemberEnabled()) {
                LoadPreviousMemberValue loadPreviousValue = getLoadPreviousValuesOfAncestorMember();
                hasLoaded = loadMemberValues(PREFIX_ANCESTOR, loadPreviousValue, ancestorConnectionCombo, ancestorMemberPrompt);
            }

            if (!hasLoaded) {
                // Initialize ancestor member with left member prompt
                setMemberValues(ancestorConnectionCombo, ancestorMemberPrompt, getCurrentLeftConnectionName(), getCurrentLeftLibraryName(),
                    getCurrentLeftFileName(), getCurrentLeftMemberName());
            }
        }
    }

    private boolean loadMemberValues(String prefix, LoadPreviousMemberValue loadPreviousValue, ConnectionCombo connectionCombo,
        QSYSMemberPrompt memberPrompt) {

        String connection;
        if (loadPreviousValue.isConnection()) {
            connection = loadValue(getMemberPromptDialogSettingsKey(prefix, CONNECTION), getCurrentLeftConnectionName());
        } else {
            connection = getCurrentLeftConnectionName();
        }

        String library;
        if (loadPreviousValue.isLibrary()) {
            library = loadValue(getMemberPromptDialogSettingsKey(prefix, LIBRARY), getCurrentLeftLibraryName());
        } else {
            library = getCurrentLeftLibraryName();
        }

        String file;
        if (loadPreviousValue.isFile()) {
            file = loadValue(getMemberPromptDialogSettingsKey(prefix, FILE), getCurrentLeftMemberName());
        } else {
            file = getCurrentLeftFileName();
        }

        String member;
        if (loadPreviousValue.isMember()) {
            member = loadValue(getMemberPromptDialogSettingsKey(prefix, MEMBER), getCurrentLeftMemberName());
        } else {
            member = getCurrentLeftMemberName();
        }

        return setMemberValues(connectionCombo, memberPrompt, connection, library, file, member);
    }

    private boolean setMemberValues(ConnectionCombo connectionCombo, QSYSMemberPrompt memberPrompt, String qualifiedConnectionName, String library,
        String file, String member) {

        memberPrompt.setSystemConnection(null);
        memberPrompt.setLibraryName(""); //$NON-NLS-1$
        memberPrompt.setFileName(""); //$NON-NLS-1$
        memberPrompt.setMemberName(""); //$NON-NLS-1$

        if (haveMemberValues(qualifiedConnectionName, library, file, member)) {
            connectionCombo.setQualifiedConnectionName(qualifiedConnectionName);
            memberPrompt.getLibraryCombo().setText(library);
            memberPrompt.getFileCombo().setText(file);
            memberPrompt.getMemberCombo().setText(member);
            memberPrompt.setSystemConnection(getHost(qualifiedConnectionName));
            return true;
        }

        return false;
    }

    @Override
    protected void storeScreenValues() {
        super.storeScreenValues();

        if (hasEditableLeftMember()) {
            if (isLoadingPreviousValuesOfLeftMemberEnabled()) {
                storeMemberValues(PREFIX_LEFT, leftConnectionCombo, leftMemberPrompt);
            }
            storeHistory(leftMemberPrompt);
        }

        if (hasEditableRightMember()) {
            if (hasMultipleRightMembers()) {
                storeMemberValues(PREFIX_RIGHT, rightConnectionCombo, rightMemberPrompt);
            } else if (isLoadingPreviousValuesOfRightMemberEnabled()) {
                storeMemberValues(PREFIX_RIGHT, rightConnectionCombo, rightMemberPrompt);
            }
            storeHistory(rightMemberPrompt);
        }

        if (hasEditableAncestorMember() && isThreeWay()) {
            if (isLoadingPreviousValuesOfAncestorMemberEnabled()) {
                storeMemberValues(PREFIX_ANCESTOR, ancestorConnectionCombo, ancestorMemberPrompt);
            }
            storeHistory(ancestorMemberPrompt);
        }
    }

    private void storeMemberValues(String prefix, ConnectionCombo connectionCombo, QSYSMemberPrompt memberPrompt) {

        String connection = connectionCombo.getQualifiedConnectionName();
        String library = memberPrompt.getLibraryName();
        String file = memberPrompt.getFileName();
        String member = memberPrompt.getMemberName();

        if (haveMemberValues(connection, library, file, member)) {
            storeValue(getMemberPromptDialogSettingsKey(prefix, CONNECTION), connection);
            storeValue(getMemberPromptDialogSettingsKey(prefix, LIBRARY), library);
            storeValue(getMemberPromptDialogSettingsKey(prefix, FILE), file);
            storeValue(getMemberPromptDialogSettingsKey(prefix, MEMBER), member);
        }
    }

    private void storeHistory(QSYSMemberPrompt memberPrompt) {

        if (!canStoreHistory()) {
            return;
        }

        if (isSpecialMemberName(memberPrompt.getMemberName())) {
            return;
        }

        memberPrompt.updateHistory();
    }

    private boolean haveMemberValues(String qualifiedConnectionName, String library, String file, String member) {

        if (!StringHelper.isNullOrEmpty(qualifiedConnectionName) && !StringHelper.isNullOrEmpty(library) && !StringHelper.isNullOrEmpty(file)
            && !StringHelper.isNullOrEmpty(member)) {
            try {
                if (ConnectionManager.getIBMiConnection(qualifiedConnectionName) != null) {
                    return true;
                }
            } catch (Exception e) {
                MessageDialog.openError(getShell(), Messages.E_R_R_O_R, ExceptionHelper.getLocalizedMessage(e));
            }
        }

        return false;
    }

    private boolean hasEditableLeftMember() {
        return leftMemberPrompt != null;
    }

    private boolean hasEditableRightMember() {
        return rightMemberPrompt != null;
    }

    private boolean hasEditableAncestorMember() {
        return ancestorMemberPrompt != null;
    }
}
