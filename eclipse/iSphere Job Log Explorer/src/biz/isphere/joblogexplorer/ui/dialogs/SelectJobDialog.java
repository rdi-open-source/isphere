/*******************************************************************************
 * Copyright (c) 2012-2022 iSphere Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.ibm.as400.access.AS400;

import biz.isphere.base.internal.ClipboardHelper;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.base.jface.dialogs.XDialog;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.internal.QualifiedJobName;
import biz.isphere.core.swt.widgets.HistoryCombo;
import biz.isphere.core.swt.widgets.WidgetFactory;
import biz.isphere.core.swt.widgets.connectioncombo.ConnectionCombo;
import biz.isphere.joblogexplorer.ISphereJobLogExplorerPlugin;
import biz.isphere.joblogexplorer.Messages;
import biz.isphere.joblogexplorer.api.retrievejobinformation.JOBI0400;
import biz.isphere.joblogexplorer.api.retrievejobinformation.QUSRJOBI;

public class SelectJobDialog extends XDialog {

    private static final String CONNECTION = "CONNECTION"; //$NON-NLS-1$
    private static final String JOB_NAME = "JOB_NAME"; //$NON-NLS-1$
    private static final String USER_NAME = "USER_NAME"; //$NON-NLS-1$
    private static final String JOB_NUMBER = "JOB_NUMBER"; //$NON-NLS-1$
    private static final String WATCH_CLIPBOARD = "WATCH_CLIPBOARD"; //$NON-NLS-1$

    private ConnectionCombo cmbConnections;
    private HistoryCombo txtJobName;
    private HistoryCombo txtUserName;
    private HistoryCombo txtJobNumber;

    private Composite container;
    private String connectionName;
    private String jobName;
    private String userName;
    private String jobNumber;
    private Button chkWatchClipboard;

    public SelectJobDialog(Shell shell) {
        super(shell);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.Explore_job_log);
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        container = (Composite)super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        createLabel(container, Messages.SelectJobDialog_Connection, Messages.SelectJobDialog_Connection_Tooltip);

        cmbConnections = WidgetFactory.createConnectionCombo(container);
        cmbConnections.setLayoutData(createLayoutData(100));
        cmbConnections.setToolTipText(Messages.SelectJobDialog_Connection_Tooltip);

        createLabel(container, Messages.SelectJobDialog_JobName, Messages.SelectJobDialog_JobName_Tooltip);
        txtJobName = WidgetFactory.createNameHistoryCombo(container);
        txtJobName.setLayoutData(createLayoutData());
        txtJobName.setToolTipText(Messages.SelectJobDialog_JobName_Tooltip);

        createLabel(container, Messages.SelectJobDialog_UserName, Messages.SelectJobDialog_UserName_Tooltip);
        txtUserName = WidgetFactory.createNameHistoryCombo(container);
        txtUserName.setLayoutData(createLayoutData());
        txtUserName.setToolTipText(Messages.SelectJobDialog_UserName_Tooltip);

        createLabel(container, Messages.SelectJobDialog_JobNumber, Messages.SelectJobDialog_JobNumber_Tooltip);
        txtJobNumber = WidgetFactory.createIntegerHistoryCombo(container, 6);
        txtJobNumber.setLayoutData(createLayoutData());
        txtJobNumber.setToolTipText(Messages.SelectJobDialog_JobNumber_Tooltip);

        WidgetFactory.createLineFiller(container);

        chkWatchClipboard = WidgetFactory.createCheckbox(container, Messages.SelectJobDialog_Retrieve_job_name_from_clipboard);
        chkWatchClipboard.setToolTipText(Messages.SelectJobDialog_Retrieve_job_name_from_clipboard_tooltip);
        chkWatchClipboard.setLayoutData(createSpanLayoutData(2));

        WidgetFactory.createLineFiller(container);

        configureControls();

        loadValues();

        return container;
    }

    public boolean haveConnections() {

        if (cmbConnections.getItemCount() > 0) {
            return true;
        }

        return false;
    }

    public String getConnectionName() {

        return connectionName;
    }

    public String getJobName() {
        return jobName;
    }

    public String getUserName() {
        return userName;
    }

    public String getJobNumber() {
        return jobNumber;
    }

    @Override
    protected void okPressed() {

        if (validated()) {
            storeValues();
            super.okPressed();
        }
    }

    private boolean validated() {

        if (!haveConnections()) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.Error_No_connections_available);
            cmbConnections.setFocus();
            return false;
        }

        if (StringHelper.isNullOrEmpty(connectionName)) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.Error_AllDataRequired);
            cmbConnections.setFocus();
            return false;
        }

        if (StringHelper.isNullOrEmpty(jobName)) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.Error_AllDataRequired);
            txtJobName.setFocus();
            return false;
        }

        if (StringHelper.isNullOrEmpty(userName)) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.Error_AllDataRequired);
            txtUserName.setFocus();
            return false;
        }

        if (StringHelper.isNullOrEmpty(jobNumber)) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R, Messages.Error_AllDataRequired);
            txtJobNumber.setFocus();
            return false;
        }

        AS400 system = IBMiHostContributionsHandler.getSystem(connectionName);
        if (system == null) {
            MessageDialog.openError(getShell(), Messages.E_R_R_O_R,
                Messages.bind(Messages.Error_Connection_A_not_found_or_not_available, connectionName));
            cmbConnections.setFocus();
            return false;
        }

        QUSRJOBI qusrjobi = new QUSRJOBI(system);
        qusrjobi.setJob(jobName, userName, jobNumber);

        JOBI0400 jobi0400 = new JOBI0400(system);
        if (!qusrjobi.execute(jobi0400)) {
            String errorID = qusrjobi.getErrorMessageID();
            if (JOBI0400.JOB_NOT_FOUND_MSGID.equals(errorID)) {
                MessageDialog.openError(getShell(), Messages.E_R_R_O_R,
                    Messages.bind(Messages.Error_Job_not_found, new Object[] { jobName, userName, jobNumber }));
                cmbConnections.setFocus();
                return false;
            }
        }

        return true;
    }

    private GridData createLayoutData() {
        GridData gridData = new GridData();
        gridData.widthHint = 160;
        return gridData;
    }

    private GridData createLayoutData(int minWidth) {

        GridData gridData = createLayoutData();
        gridData.minimumWidth = minWidth;
        gridData.grabExcessHorizontalSpace = true;

        return gridData;
    }

    private GridData createSpanLayoutData(int horizontalSpan) {
        GridData gridData = new GridData();
        gridData.horizontalSpan = horizontalSpan;
        return gridData;
    }

    @Override
    public void setFocus() {

        if (!cmbConnections.hasConnection()) {
            cmbConnections.setFocus();
            return;
        }

        if (StringHelper.isNullOrEmpty(txtJobName.getText())) {
            txtJobName.setFocus();
            return;
        }

        if (StringHelper.isNullOrEmpty(txtUserName.getText())) {
            txtUserName.setFocus();
            return;
        }

        txtJobNumber.setFocus();
    }

    private void loadValues() {

        cmbConnections.setQualifiedConnectionName(loadValue(CONNECTION, null));
        connectionName = cmbConnections.getQualifiedConnectionName();

        txtJobName.load(getDialogSettingsManager(), getKey("#jobName")); //$NON-NLS-1$
        txtUserName.load(getDialogSettingsManager(), getKey("#userName")); //$NON-NLS-1$
        txtJobNumber.load(getDialogSettingsManager(), getKey("#jobNumber")); //$NON-NLS-1$

        txtJobName.setText(loadValue(JOB_NAME, ""));
        txtUserName.setText(loadValue(USER_NAME, ""));
        txtJobNumber.setText(loadValue(JOB_NUMBER, ""));
        chkWatchClipboard.setSelection(loadBooleanValue(WATCH_CLIPBOARD, false));
    }

    private void storeValues() {

        storeValue(CONNECTION, connectionName);
        storeValue(JOB_NAME, jobName);
        storeValue(USER_NAME, userName);
        storeValue(JOB_NUMBER, jobNumber);
        storeValue(WATCH_CLIPBOARD, chkWatchClipboard.getSelection());

        updateAndStoreHistory(txtJobName, jobName);
        updateAndStoreHistory(txtUserName, userName);
        updateAndStoreHistory(txtJobNumber, jobNumber);
    }

    private void updateAndStoreHistory(HistoryCombo historyCombo, String value) {

        historyCombo.updateHistory(value);
        historyCombo.store();
    }

    private String getKey(String key) {
        return getClass().getName() + "." + key; //$NON-NLS-1$
    }

    private void configureControls() {

        container.addListener(SWT.Activate, new QualifiedJobNameListener());

        cmbConnections.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent event) {
                connectionName = cmbConnections.getQualifiedConnectionName();
            }

            public void widgetDefaultSelected(SelectionEvent event) {
                widgetSelected(event);
            }
        });

        txtJobName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                jobName = txtJobName.getText().trim();
                return;
            }
        });

        txtJobName.addVerifyListener(new QualifiedJobNameListener());

        txtUserName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                userName = txtUserName.getText().trim();
            }
        });

        txtUserName.addVerifyListener(new QualifiedJobNameListener());

        txtJobNumber.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                jobNumber = txtJobNumber.getText().trim();
            }
        });

        txtJobNumber.addVerifyListener(new QualifiedJobNameListener());

        chkWatchClipboard.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent paramSelectionEvent) {
                if (chkWatchClipboard.getSelection()) {
                    new QualifiedJobNameListener().handleEvent(null);
                }
            }
        });
    }

    private Label createLabel(Composite parent, String text, String tooltip) {

        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        label.setToolTipText(tooltip);

        return label;
    }

    /**
     * Overridden make this dialog resizable {@link XDialog}.
     */
    @Override
    protected boolean isResizable() {
        return true;
    }

    /**
     * Overridden to provide a default size to {@link XDialog}.
     */
    @Override
    protected Point getDefaultSize() {
        return super.getDefaultSize();
    }

    /**
     * Overridden to let {@link XDialog} store the state of this dialog in a
     * separate section of the dialog settings file.
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        return super.getDialogBoundsSettings(ISphereJobLogExplorerPlugin.getDefault().getDialogSettings());
    }

    @Override
    public Point getMinimalSize() {
        return new Point(430, 260);
    }

    private class ConnectionLabelProvider extends LabelProvider {

        @Override
        public String getText(Object element) {

            if (element instanceof String) {
                return (String)element;
            }

            return super.getText(element);
        }
    }

    private class QualifiedJobNameListener implements VerifyListener, Listener {

        public void verifyText(VerifyEvent event) {
            if (event.text.trim().length() > 10) {
                // Retrieve job, user and number from a qualified job name of
                // format '123456/USER/JOB' or 'JOB(123456/USER/JOB)'.
                if (setQualifiedJobName(event.text)) {
                    event.doit = false;
                }
            }
        }

        public void handleEvent(Event event) {
            if (chkWatchClipboard.getSelection()) {
                if (ClipboardHelper.hasTextContents()) {
                    String text = ClipboardHelper.getText().trim().toUpperCase();
                    if (text.length() > 10 && text.length() <= 33) {
                        setQualifiedJobName(text);
                    }
                }
            }
        }

        private boolean setQualifiedJobName(String qualifiedJobName) {

            qualifiedJobName = qualifiedJobName.trim().toUpperCase();

            if (qualifiedJobName.startsWith("JOB(")) {
                qualifiedJobName = qualifiedJobName.substring(4);
                if (qualifiedJobName.endsWith(")")) {
                    qualifiedJobName = qualifiedJobName.substring(0, qualifiedJobName.length() - 1);
                }
            }

            // Retrieve job, user and number from a qualified job name of
            // format '123456/USER/JOB'.
            if (QualifiedJobName.isValid(qualifiedJobName)) {
                QualifiedJobName qJobName = new QualifiedJobName(qualifiedJobName);
                txtJobName.setText(qJobName.getJob());
                txtUserName.setText(qJobName.getUser());
                txtJobNumber.setText(qJobName.getNumber());
                return true;
            }

            return false;
        }
    }
}