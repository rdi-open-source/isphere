/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     iSphere Project Owners - Maintenance and enhancements
 *******************************************************************************/
package biz.isphere.messagesubsystem.rse.internal;

import biz.isphere.base.internal.StringHelper;
import biz.isphere.messagesubsystem.rse.Messages;

public class MonitoringAttributes {

    private final static String VENDOR_ID = "biz.isphere"; //$NON-NLS-1$
    private final static String MONITOR = "biz.isphere.messagesubsystem.internal.monitor"; //$NON-NLS-1$
    private final static String REMOVE = "biz.isphere.messagesubsystem.internal.remove"; //$NON-NLS-1$
    private final static String INQUIRY_NOTIFICATION = "biz.isphere.messagesubsystem.internal.inquiry"; //$NON-NLS-1$
    private final static String INFORMATIONAL_NOTIFICATION = "biz.isphere.messagesubsystem.internal.informational"; //$NON-NLS-1$
    private final static String EMAIL_ADDRESS = "biz.isphere.messagesubsystem.internal.email"; //$NON-NLS-1$
    private final static String EMAIL_FROM = "biz.isphere.messagesubsystem.internal.from"; //$NON-NLS-1$
    private final static String EMAIL_PORT = "biz.isphere.messagesubsystem.internal.port"; //$NON-NLS-1$
    private final static String EMAIL_HOST = "biz.isphere.messagesubsystem.internal.host"; //$NON-NLS-1$

    public static final String NOTIFICATION_TYPE_DIALOG = "*DIALOG"; //$NON-NLS-1$
    public static final String NOTIFICATION_TYPE_EMAIL = "*EMAIL"; //$NON-NLS-1$
    public static final String NOTIFICATION_TYPE_BEEP = "*BEEP"; //$NON-NLS-1$

    private static final String MONITORING_ENABLED = "true"; //$NON-NLS-1$
    private static final String MONITORING_DISABLED = "false"; //$NON-NLS-1$
    private static final String REMOVE_YES = "true"; //$NON-NLS-1$
    private static final String REMOVE_NO = "false"; //$NON-NLS-1$

    private static final String EMAIL_EXAMPLE_FROM = "MyMessages@";
    private static final String EMAIL_EXAMPLE_ADDRESS = "youraddress@yourcompany.com";
    private static final String EMAIL_EXAMPLE_HOST = "mailserver.yourcompany.com";
    private static final String EMAIL_EXAMPLE_PORT = "25";

    private QueuedMessageSubSystem queuedMessageSubSystem;

    public MonitoringAttributes(QueuedMessageSubSystem queuedMessageSubSystem) {

        this.queuedMessageSubSystem = queuedMessageSubSystem;
    }

    public boolean isMonitoringEnabled() {

        String monitorString = getVendorAttribute(MONITOR);
        if (monitorString == null) {
            return false;
        } else {
            if (MONITORING_ENABLED.equals(monitorString)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void setMonitoring(boolean enabled) {

        if (enabled) {
            setVendorAttribute(MONITOR, MONITORING_ENABLED);
        } else {
            setVendorAttribute(MONITOR, MONITORING_DISABLED);
        }
    }

    public boolean removeInformationalMessages() {

        String removeString = getVendorAttribute(REMOVE);
        if (removeString == null) {
            return false;
        } else {
            if (MONITORING_ENABLED.equals(removeString)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public void setRemoveInformationalMessages(boolean enabled) {

        if (enabled) {
            setVendorAttribute(REMOVE, REMOVE_YES);
        } else {
            setVendorAttribute(REMOVE, REMOVE_NO);
        }
    }

    public String getInqueryMessageNotificationType() {

        String typeInternal = getVendorAttribute(INQUIRY_NOTIFICATION);
        if (isValidNotificationType(typeInternal)) {
            return typeInternal;
        }

        return NOTIFICATION_TYPE_DIALOG;
    }

    public void setInqueryMessageNotificationType(String typeInternal) {

        String typeValidated;
        if (isValidNotificationType(typeInternal)) {
            typeValidated = typeInternal;
        } else {
            typeValidated = NOTIFICATION_TYPE_DIALOG;
        }

        setVendorAttribute(INQUIRY_NOTIFICATION, typeValidated);
    }

    public String getInqueryMessageNotificationTypeForGUI() {

        String typeInternal = getInqueryMessageNotificationType();
        return convertToGUINotificationType(typeInternal, Messages.Notification_type_Dialog);
    }

    public void setInqueryMessageNotificationTypeFromGUI(String notificationType) {

        String typeInternal = convertToInternalNotificationType(notificationType);
        setInqueryMessageNotificationType(typeInternal);
    }

    public String getInformationalMessageNotificationType() {

        String typeInternal = getVendorAttribute(INFORMATIONAL_NOTIFICATION);
        if (isValidNotificationType(typeInternal)) {
            return typeInternal;
        }

        return NOTIFICATION_TYPE_EMAIL;
    }

    public void setInformationalMessageNotificationType(String notificationType) {

        String typeInternal;
        if (isValidNotificationType(notificationType)) {
            typeInternal = notificationType;
        } else {
            typeInternal = NOTIFICATION_TYPE_EMAIL;
        }

        setVendorAttribute(INFORMATIONAL_NOTIFICATION, typeInternal);
    }

    public String getInformationalMessageNotificationTypeForGUI() {

        String typeInternal = getInformationalMessageNotificationType();
        return convertToGUINotificationType(typeInternal, Messages.Notification_type_Email);
    }

    public void setInformationalMessageNotificationTypeFromGUI(String notificationType) {

        String typeInternal = convertToInternalNotificationType(notificationType);
        setInformationalMessageNotificationType(typeInternal);
    }

    public String getEmail() {

        String emailString = getVendorAttribute(EMAIL_ADDRESS);
        if (StringHelper.isNullOrEmpty(emailString)) {
            return EMAIL_EXAMPLE_ADDRESS;
        } else {
            return emailString;
        }
    }

    public void setEmail(String email) {

        String emailString;
        if (StringHelper.isNullOrEmpty(email)) {
            emailString = EMAIL_EXAMPLE_ADDRESS;
        } else {
            emailString = email;
        }

        setVendorAttribute(EMAIL_ADDRESS, emailString);
    }

    public String getFrom() {

        String fromString = getVendorAttribute(EMAIL_FROM);
        if (StringHelper.isNullOrEmpty(fromString)) {
            return getExampleFrom();
        } else {
            return fromString;
        }
    }

    public void setFrom(String from) {

        String fromString;
        if (StringHelper.isNullOrEmpty(from)) {
            fromString = getExampleFrom();
        } else {
            fromString = from;
        }

        setVendorAttribute(EMAIL_FROM, fromString);
    }

    public String getHost() {

        String hostString = getVendorAttribute(EMAIL_HOST);
        if (StringHelper.isNullOrEmpty(hostString)) {
            return EMAIL_EXAMPLE_HOST;
        } else {
            return hostString;
        }
    }

    public void setHost(String host) {

        String hostString;
        if (StringHelper.isNullOrEmpty(host)) {
            hostString = EMAIL_EXAMPLE_HOST;
        } else {
            hostString = host;
        }

        setVendorAttribute(EMAIL_HOST, hostString);
    }

    public String getPort() {

        String portString = getVendorAttribute(EMAIL_PORT);
        if (StringHelper.isNullOrEmpty(portString)) {
            return EMAIL_EXAMPLE_PORT;
        } else {
            return portString;
        }
    }

    public void setPort(String port) {

        String portString;
        if (StringHelper.isNullOrEmpty(port)) {
            portString = EMAIL_EXAMPLE_PORT;
        } else {
            portString = port;
        }

        setVendorAttribute(EMAIL_PORT, portString);
    }

    public boolean isValid() {

        if (EMAIL_EXAMPLE_ADDRESS.equals(getEmail())) {
            return false;
        }

        if (EMAIL_EXAMPLE_HOST.equals(getHost())) {
            return false;
        }
        
        return true;
    }

    private String convertToInternalNotificationType(String notificationType) {
        String typeInternal;
        if (Messages.Notification_type_Dialog.equals(notificationType)) {
            typeInternal = NOTIFICATION_TYPE_DIALOG;
        } else if (Messages.Notification_type_Email.equals(notificationType)) {
            typeInternal = NOTIFICATION_TYPE_EMAIL;
        } else if (Messages.Notification_type_Beep.equals(notificationType)) {
            typeInternal = NOTIFICATION_TYPE_BEEP;
        } else {
            typeInternal = null;
        }
        return typeInternal;
    }

    private String convertToGUINotificationType(String typeInternal, String defaultGUIType) {

        if (NOTIFICATION_TYPE_DIALOG.equals(typeInternal)) {
            return Messages.Notification_type_Dialog;
        } else if (NOTIFICATION_TYPE_EMAIL.equals(typeInternal)) {
            return Messages.Notification_type_Email;
        } else if (NOTIFICATION_TYPE_BEEP.equals(typeInternal)) {
            return Messages.Notification_type_Beep;
        }

        return defaultGUIType;
    }

    private boolean isValidNotificationType(String typeInternal) {
        if (NOTIFICATION_TYPE_DIALOG.equals(typeInternal)) {
            return true;
        }

        if (NOTIFICATION_TYPE_EMAIL.equals(typeInternal)) {
            return true;
        }
        if (NOTIFICATION_TYPE_BEEP.equals(typeInternal)) {
            return true;
        }

        return false;
    }

    private String getExampleFrom() {
        return EMAIL_EXAMPLE_FROM + queuedMessageSubSystem.getHostName();
    }
    
    private String getVendorAttribute(String key) {

        if (queuedMessageSubSystem == null) {
            return null;
        }

        return queuedMessageSubSystem.getVendorAttribute(VENDOR_ID, key);
    }

    private void setVendorAttribute(String key, String value) {
        queuedMessageSubSystem.setVendorAttribute(VENDOR_ID, key, value);
    }

    /*
     * Start of RDi/WDSCi specific methods.
     */
}
