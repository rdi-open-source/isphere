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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import biz.isphere.core.swt.widgets.WidgetFactory;
import biz.isphere.messagesubsystem.Messages;

import com.ibm.as400.access.QueuedMessage;
import com.ibm.etools.systems.core.messages.SystemMessage;
import com.ibm.etools.systems.core.ui.SystemWidgetHelpers;
import com.ibm.etools.systems.core.ui.messages.SystemMessageDialog;
import com.ibm.etools.systems.filters.ui.SystemFilterStringEditPane;

public class QueuedMessageFilterStringEditPane extends SystemFilterStringEditPane {

    private static final String QUSRSYS = "QUSRSYS";
    private static final String ASTERISK = "*";
    private Text messageQueueText;
    private Text libraryText;
    private Text userText;
    private Text idText;
    private Text severityText;
    private Text fromJobText;
    private Text fromJobNumberText;
    private Text fromProgramText;
    private Text textText;
    private Combo messageTypeCombo;

    public QueuedMessageFilterStringEditPane(Shell shell) {
        super(shell);
    }

    @Override
    public Control createContents(Composite parent) {

        int nbrColumns = 2;
        Composite composite_prompts = SystemWidgetHelpers.createComposite(parent, nbrColumns);
        ((GridLayout)composite_prompts.getLayout()).marginWidth = 0;

        Label messageQueueLabel = new Label(composite_prompts, SWT.NONE);
        messageQueueLabel.setText(Messages.Message_queue_colon);

        messageQueueText = WidgetFactory.createText(composite_prompts);
        GridData gd = new GridData();
        gd.widthHint = 75;
        messageQueueText.setLayoutData(gd);
        messageQueueText.setTextLimit(10);

        Label libraryLabel = new Label(composite_prompts, SWT.NONE);
        libraryLabel.setText(Messages.Library_colon);

        libraryText = WidgetFactory.createText(composite_prompts);
        gd = new GridData();
        gd.widthHint = 75;
        libraryText.setLayoutData(gd);
        libraryText.setTextLimit(10);

        Label fromLabel = new Label(composite_prompts, SWT.NONE);
        fromLabel.setText(Messages.From_user_colon);

        userText = WidgetFactory.createText(composite_prompts);
        gd = new GridData();
        gd.widthHint = 75;
        userText.setLayoutData(gd);
        userText.setTextLimit(10);

        Label idLabel = new Label(composite_prompts, SWT.NONE);
        idLabel.setText(Messages.Message_ID_colon);

        idText = WidgetFactory.createText(composite_prompts);
        gd = new GridData();
        gd.widthHint = 75;
        idText.setLayoutData(gd);
        idText.setTextLimit(7);

        Label severityLabel = new Label(composite_prompts, SWT.NONE);
        severityLabel.setText(Messages.Severity_threshold_colon);

        severityText = WidgetFactory.createText(composite_prompts);
        gd = new GridData();
        gd.widthHint = 50;
        severityText.setLayoutData(gd);
        severityText.setTextLimit(2);

        Label fromJobLabel = new Label(composite_prompts, SWT.NONE);
        fromJobLabel.setText(Messages.From_job_colon);

        fromJobText = WidgetFactory.createText(composite_prompts);
        gd = new GridData();
        gd.widthHint = 75;
        fromJobText.setLayoutData(gd);
        fromJobText.setTextLimit(10);

        Label fromJobNumberLabel = new Label(composite_prompts, SWT.NONE);
        fromJobNumberLabel.setText(Messages.From_job_number_colon);

        fromJobNumberText = WidgetFactory.createText(composite_prompts);
        gd = new GridData();
        gd.widthHint = 75;
        fromJobNumberText.setLayoutData(gd);
        fromJobNumberText.setTextLimit(6);

        Label fromProgramLabel = new Label(composite_prompts, SWT.NONE);
        fromProgramLabel.setText(Messages.From_program_colon);

        fromProgramText = WidgetFactory.createText(composite_prompts);
        gd = new GridData();
        gd.widthHint = 75;
        fromProgramText.setLayoutData(gd);
        fromProgramText.setTextLimit(10);

        Label textLabel = new Label(composite_prompts, SWT.NONE);
        textLabel.setText(Messages.Message_text_contains_colon);

        textText = WidgetFactory.createText(composite_prompts);
        gd = new GridData();
        gd.widthHint = 150;
        textText.setLayoutData(gd);
        textText.setTextLimit(255);

        Label typeLabel = new Label(composite_prompts, SWT.NONE);
        typeLabel.setText(Messages.Message_type_colon);

        messageTypeCombo = WidgetFactory.createReadOnlyCombo(composite_prompts);
        messageTypeCombo.add(Messages.Message_Type_Text_Any);
        messageTypeCombo.add(Messages.Message_Type_Text_Completion);
        messageTypeCombo.add(Messages.Message_Type_Text_Diagnostic);
        messageTypeCombo.add(Messages.Message_Type_Text_Informational);
        messageTypeCombo.add(Messages.Message_Type_Text_Inquiry);
        messageTypeCombo.add(Messages.Message_Type_Text_Senders_copy);
        messageTypeCombo.add(Messages.Message_Type_Text_Request);
        messageTypeCombo.add(Messages.Message_Type_Text_Request_with_prompting);
        messageTypeCombo.add(Messages.Message_Type_Text_Notify);

        resetFields();
        doInitializeFields();

        ModifyListener keyListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateStringInput();
            }
        };

        messageQueueText.addModifyListener(keyListener);
        libraryText.addModifyListener(keyListener);
        userText.addModifyListener(keyListener);
        idText.addModifyListener(keyListener);
        severityText.addModifyListener(keyListener);
        fromJobText.addModifyListener(keyListener);
        fromJobNumberText.addModifyListener(keyListener);
        fromProgramText.addModifyListener(keyListener);
        textText.addModifyListener(keyListener);
        messageTypeCombo.addModifyListener(keyListener);

        return composite_prompts;
    }

    @Override
    public Control getInitialFocusControl() {
        return messageQueueText;
    }

    @Override
    protected void doInitializeFields() {

        if (messageQueueText == null) {
            return;
        }

        if (inputFilterString != null) {

            QueuedMessageFilter filter = new QueuedMessageFilter(inputFilterString);
            if (filter.getMessageQueue() != null) {

                messageQueueText.setText(filter.getMessageQueue());
                if (filter.getLibrary() != null) {
                    libraryText.setText(filter.getLibrary());
                } else {
                    libraryText.setText(QUSRSYS);
                }

                if (filter.getUser() != null) {
                    userText.setText(filter.getUser());
                } else {
                    userText.setText(ASTERISK);
                }

                if (filter.getId() != null) {
                    idText.setText(filter.getId());
                } else {
                    idText.setText(ASTERISK);
                }

                if (filter.getSeverity() == -1) {
                    severityText.setText(ASTERISK);
                } else {
                    severityText.setText(new Integer(filter.getSeverity()).toString());
                }

                if (filter.getFromJobName() != null) {
                    fromJobText.setText(filter.getFromJobName());
                } else {
                    fromJobText.setText(ASTERISK);
                }

                if (filter.getFromJobNumber() != null) {
                    fromJobNumberText.setText(filter.getFromJobNumber());
                } else {
                    fromJobNumberText.setText(ASTERISK);
                }

                if (filter.getFromProgram() != null) {
                    fromProgramText.setText(filter.getFromProgram());
                } else {
                    fromProgramText.setText(ASTERISK);
                }

                if (filter.getText() != null) {
                    textText.setText(filter.getText());
                } else {
                    textText.setText(ASTERISK);
                }

                if (filter.getMessageType() != -1) {
                    messageTypeCombo.select(messageTypeCombo.indexOf(QueuedMessageHelper.getMessageTypeAsText(filter.getMessageType())));
                }
            }
            if (messageTypeCombo.getSelectionIndex() == -1) {
                messageTypeCombo.select(messageTypeCombo.indexOf(Messages.Message_Type_Text_Any));
            }
        }
    }

    @Override
    protected void resetFields() {
        messageQueueText.setText(""); //$NON-NLS-1$
        libraryText.setText(QUSRSYS);
        userText.setText(ASTERISK);
        idText.setText(ASTERISK);
        severityText.setText(ASTERISK);
        fromJobText.setText(ASTERISK);
        fromJobNumberText.setText(ASTERISK);
        fromProgramText.setText(ASTERISK);
        textText.setText(ASTERISK);
        messageTypeCombo.select(messageTypeCombo.indexOf(Messages.Message_Type_Text_Any));
    }

    @Override
    protected boolean areFieldsComplete() {
        return ((messageQueueText.getText() != null) && (messageQueueText.getText().trim().length() > 0) && (libraryText.getText() != null)
            && (libraryText.getText().trim().length() > 0) && (!messageQueueText.getText().equals(ASTERISK)) && (!libraryText.getText().equals(
            ASTERISK)));
    }

    @Override
    public String getFilterString() {

        QueuedMessageFilter filter = new QueuedMessageFilter();

        if ((messageQueueText.getText() != null) && (messageQueueText.getText().length() > 0) && (!messageQueueText.getText().equals(ASTERISK))) {
            filter.setMessageQueue(messageQueueText.getText().toUpperCase());
        }

        if ((libraryText.getText() != null) && (libraryText.getText().length() > 0) && (!libraryText.getText().equals(ASTERISK))) {
            filter.setLibrary(libraryText.getText().toUpperCase());
        }

        if ((userText.getText() != null) && (userText.getText().length() > 0) && (!userText.getText().equals(ASTERISK))) {
            filter.setUser(userText.getText().toUpperCase());
        }

        if ((idText.getText() != null) && (idText.getText().length() > 0) && (!idText.getText().equals(ASTERISK))) {
            filter.setId(idText.getText().toUpperCase());
        }

        if ((severityText.getText() != null) && (severityText.getText().length() > 0) && (!severityText.getText().equals(ASTERISK))) {
            int severity = -1;
            try {
                severity = new Integer(severityText.getText()).intValue();
            } catch (Exception e) {
            }
            filter.setSeverity(severity);
        }

        if ((fromJobText.getText() != null) && (fromJobText.getText().length() > 0) && (!fromJobText.getText().equals(ASTERISK))) {
            filter.setFromJobName(fromJobText.getText().toUpperCase());
        }

        if ((fromJobNumberText.getText() != null) && (fromJobNumberText.getText().length() > 0) && (!fromJobNumberText.getText().equals(ASTERISK))) {
            filter.setFromJobNumber(fromJobNumberText.getText());
        }

        if ((fromProgramText.getText() != null) && (fromProgramText.getText().length() > 0) && (!fromProgramText.getText().equals(ASTERISK))) {
            filter.setFromProgram(fromProgramText.getText().toUpperCase());
        }

        if ((textText.getText() != null) && (textText.getText().length() > 0) && (!textText.getText().equals(ASTERISK))) {
            filter.setText(textText.getText());
        }

        int messageType = -1;
        if (messageTypeCombo.getText().equals(Messages.Message_Type_Text_Completion)) {
            messageType = QueuedMessage.COMPLETION;
        } else if (messageTypeCombo.getText().equals(Messages.Message_Type_Text_Diagnostic)) {
            messageType = QueuedMessage.DIAGNOSTIC;
        } else if (messageTypeCombo.getText().equals(Messages.Message_Type_Text_Informational)) {
            messageType = QueuedMessage.INFORMATIONAL;
        } else if (messageTypeCombo.getText().equals(Messages.Message_Type_Text_Inquiry)) {
            messageType = QueuedMessage.INQUIRY;
        } else if (messageTypeCombo.getText().equals(Messages.Message_Type_Text_Senders_copy)) {
            messageType = QueuedMessage.SENDERS_COPY;
        } else if (messageTypeCombo.getText().equals(Messages.Message_Type_Text_Request)) {
            messageType = QueuedMessage.REQUEST;
        } else if (messageTypeCombo.getText().equals(Messages.Message_Type_Text_Request_with_prompting)) {
            messageType = QueuedMessage.REQUEST_WITH_PROMPTING;
        } else if (messageTypeCombo.getText().equals(Messages.Message_Type_Text_Notify)) {
            messageType = QueuedMessage.NOTIFY;
        }

        filter.setMessageType(messageType);

        return filter.getFilterString();
    }

    @Override
    public SystemMessage verify() {
        if (!areFieldsComplete())
            return SystemMessageDialog.getExceptionMessage(Display.getCurrent().getActiveShell(), new Exception(
                Messages.Message_queue_and_library_must_be_specified));
        return null;
    }

}