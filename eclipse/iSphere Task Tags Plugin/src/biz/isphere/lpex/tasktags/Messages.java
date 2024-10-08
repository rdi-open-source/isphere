/*******************************************************************************
 * Copyright (c) 2012-2022 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.lpex.tasktags;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "biz.isphere.lpex.tasktags.messages"; //$NON-NLS-1$

    public static String PreferencesPage_title;

    public static String PreferencesPage_headline;

    public static String PreferencesPage_help;

    public static String PreferencesPage_configureTaskTags;

    public static String PreferencesPage_tableHeadline;

    public static String PreferencesPage_btnNew;

    public static String PreferencesPage_btnEdit;

    public static String PreferencesPage_btnRemove;

    public static String PreferencesPage_btnExport;

    public static String PreferencesPage_btnImport;

    public static String PreferencesPage_ExportDialog_headline;

    public static String PreferencesPage_ImportDialog_headline;

    public static String PreferencesPage_btnEnableTaskTags;

    public static String PreferencesPage_btnRemoveOnCloseEnableTaskTags;

    public static String PreferencesPage_btnScanOnChangeEnableTaskTags;

    public static String TaskTagEditor_headline_new;

    public static String TaskTagEditor_headline_edit;

    public static String TaskTagEditor_label_fileExtension;

    public static String TaskTagEditor_btnOK;

    public static String TaskTagEditor_btnCancel;

    public static String TaskTag_exists;

    public static String TaskTag_empty;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
