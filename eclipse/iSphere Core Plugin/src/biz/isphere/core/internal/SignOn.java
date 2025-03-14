/*******************************************************************************
 * Copyright (c) 2012-2022 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.internal;

import java.beans.PropertyVetoException;
import java.io.IOException;

import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.SecureAS400;

import biz.isphere.base.internal.DialogSettingsManager;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.swt.widgets.WidgetFactory;
import biz.isphere.core.swt.widgets.connectioncombo.ConnectionCombo;

public class SignOn {

    private static final String OVERRIDE_CREDENTIALS = "OVERRIDE_CREDENTIALS";
    private static final String HOST = "HOST";
    private static final String USER = "USER";

    private ConnectionCombo comboConnections;
    private Button btnOverrideCredentials;
    private Text textUser;
    private Text textPassword;
    private StatusLineManager statusLineManager;
    private AS400 as400;
    private DialogSettingsManager dialogSettingsManager;

    public SignOn() {
        as400 = null;
    }

    public void createContents(Composite parent, String aConnectionName) {

        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());

        final Composite compositeGeneral = new Composite(container, SWT.NONE);
        compositeGeneral.setLayout(new GridLayout(2, false));
        compositeGeneral.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label labelHost = new Label(compositeGeneral, SWT.NONE);
        labelHost.setText(Messages.Host_colon);

        comboConnections = WidgetFactory.createConnectionCombo(compositeGeneral, SWT.BORDER);
        comboConnections.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboConnections.setQualifiedConnectionName(aConnectionName);

        WidgetFactory.createLineFiller(compositeGeneral);

        btnOverrideCredentials = WidgetFactory.createCheckbox(compositeGeneral, "&Override credentials");
        btnOverrideCredentials.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        btnOverrideCredentials.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setControlEnablement();
                positionCursor();
            }
        });

        final Label labelUser = new Label(compositeGeneral, SWT.NONE);
        labelUser.setText(Messages.User_colon);

        textUser = WidgetFactory.createText(compositeGeneral);
        textUser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textUser.setText("");

        final Label labelPassword = new Label(compositeGeneral, SWT.NONE);
        labelPassword.setText(Messages.Password_colon);

        textPassword = WidgetFactory.createPassword(compositeGeneral);
        textPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textPassword.setText("");

        statusLineManager = new StatusLineManager();
        statusLineManager.createControl(container, SWT.NONE);
        Control statusLine = statusLineManager.getControl();
        final GridData gridDataStatusLine = new GridData(SWT.FILL, SWT.CENTER, true, false);
        statusLine.setLayoutData(gridDataStatusLine);

        if (!comboConnections.hasConnection()) {
            comboConnections.setFocus();
        } else if (StringHelper.isNullOrEmpty(textUser.getText())) {
            textUser.setFocus();
        } else if (StringHelper.isNullOrEmpty(textPassword.getText())) {
            textPassword.setFocus();
        }

        loadScreenValues();

        positionCursor();
    }

    private void positionCursor() {

        if (!comboConnections.hasConnection()) {
            comboConnections.setFocus();
        } else if (!btnOverrideCredentials.getSelection()) {
            comboConnections.setFocus();
        } else if (StringHelper.isNullOrEmpty(textUser.getText())) {
            textUser.setFocus();
        } else if (StringHelper.isNullOrEmpty(textPassword.getText())) {
            textPassword.setFocus();
        } else {
            textUser.setFocus();
        }
    }

    protected void setErrorMessage(String errorMessage) {
        if (errorMessage != null) {
            statusLineManager.setErrorMessage(ISpherePlugin.getDefault().getImageRegistry().get(ISpherePlugin.IMAGE_ERROR), errorMessage);
        } else {
            statusLineManager.setErrorMessage(null, null);
        }
    }

    public boolean processButtonPressed() {

        storeScreenValues();

        String userId = null;
        String password = null;

        if (!comboConnections.hasConnection()) {
            setErrorMessage(Messages.Enter_a_host);
            comboConnections.setFocus();
            return false;
        }

        if (btnOverrideCredentials.getSelection()) {

            userId = textUser.getText().trim();
            password = textPassword.getText().trim();

            if (StringHelper.isNullOrEmpty(userId)) {
                setErrorMessage(Messages.Enter_a_user);
                textUser.setFocus();
                return false;
            }

            if (StringHelper.isNullOrEmpty(password)) {
                setErrorMessage(Messages.Enter_a_password);
                textPassword.setFocus();
                return false;
            }

        }

        AS400 system = IBMiHostContributionsHandler.getSystem(comboConnections.getQualifiedConnectionName());
        if (system instanceof SecureAS400) {
            as400 = new SecureAS400(system);
        } else {
            as400 = new AS400(system);
        }

        try {

            if (userId != null) {
                as400.setUserId(userId);
            }

            if (password != null) {
                as400.setPassword(password);
            }

            as400.validateSignon();
        } catch (PropertyVetoException e) {
            setErrorMessage(e.getMessage());
            textUser.setFocus();
            return false;
        } catch (AS400SecurityException e) {
            setErrorMessage(e.getMessage());
            textPassword.setFocus();
            return false;
        } catch (IOException e) {
            setErrorMessage(e.getMessage());
            comboConnections.setFocus();
            return false;
        }

        return true;
    }

    private void loadScreenValues() {

        boolean overrideCredentials = getDialogSettingsManager().loadBooleanValue(OVERRIDE_CREDENTIALS, false);
        btnOverrideCredentials.setSelection(overrideCredentials);

        String host = getDialogSettingsManager().loadValue(HOST, "");
        comboConnections.setQualifiedConnectionName(host);

        String user = getDialogSettingsManager().loadValue(USER, "");
        textUser.setText(user);

        setControlEnablement();
    }

    private void storeScreenValues() {

        getDialogSettingsManager().storeValue(OVERRIDE_CREDENTIALS, btnOverrideCredentials.getSelection());
        getDialogSettingsManager().storeValue(HOST, comboConnections.getQualifiedConnectionName());
        getDialogSettingsManager().storeValue(USER, textUser.getText());
    }

    private void setControlEnablement() {

        if (btnOverrideCredentials.getSelection()) {
            textUser.setEnabled(true);
            textPassword.setEnabled(true);
        } else {
            textUser.setEnabled(false);
            textPassword.setEnabled(false);
        }
    }

    public AS400 getAS400() {
        return as400;
    }

    private DialogSettingsManager getDialogSettingsManager() {

        if (dialogSettingsManager == null) {
            dialogSettingsManager = new DialogSettingsManager(ISpherePlugin.getDefault().getDialogSettings(), getClass());
        }

        return dialogSettingsManager;
    }

}
