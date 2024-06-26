/*******************************************************************************
 * Copyright (c) 2012-2024 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.preferences;

public interface DoNotAskMeAgain {

    /*
     * Values are stored in: biz.isphere.core.prefs
     */

    /*
     * Warning message keys
     */
    public static final String WARNING_COMPARE_FILTERS_NOT_INSTALLED = "COMPARE_FILTERS_NOT_INSTALLED"; //$NON-NLS-1$
    public static final String WARNING_REMOVE_STRPREPRC_SECTIONS = "REMOVE_STRPREPRC_SECTIONS"; //$NON-NLS-1$
    public static final String WARNING_NOT_ALL_JOURNAL_ENTRIES_LOADED = "NOT_ALL_JOURNAL_ENTRIES_LOADED"; //$NON-NLS-1$
    public static final String WARNING_NOT_ALL_JOB_TRACE_ENTRIES_LOADED = "NOT_ALL_JOB_TRACE_ENTRIES_LOADED"; //$NON-NLS-1$

    /*
     * Informational message keys
     */
    public static final String INFORMATION_DATA_SPACE_FIND_REPLACE_INFORMATION = "DATA_SPACE_FIND_REPLACE_INFORMATION"; //$NON-NLS-1$
    public static final String TOO_MANY_SPOOLED_FILES_WARNING = "TOO_MANY_SPOOLED_FILES_WARNING"; //$NON-NLS-1$
    public static final String TN5250_SESSION_GROUPING_CHANGED = "TN5250_SESSION_GROUPING_CHANGED"; //$NON-NLS-1$
    public static final String INFORMATION_USAGE_JOB_LOG_EXPLORER = "INFORMATION_USAGE_JOB_LOG_EXPLORER"; //$NON-NLS-1$
    public static final String LPEX_COMMENT_RESTART_INFORMATION = "LPEX_COMMENT_RESTART_INFORMATION"; //$NON-NLS-1$

    /*
     * Confirmation message keys
     */
    public static final String CONFIRM_REMOVE_STRPREPRC_HEADER = "REMOVE_STRPREPRC_HEADER"; //$NON-NLS-1$
    public static final String TN5250_FAST_CURSOR_MAPPING_CONFLICT = "TN5250_FAST_CURSOR_MAPPING_CONFLICT"; //$NON-NLS-1$
    public static final String CONFIRM_CLOSE_UPLOAD_LIBRARY_DIALOG = "CONFIRM_CLOSE_UPLOAD_LIBRARY_DIALOG"; //$NON-NLS-1$
    public static final String CONFIRM_CREATE_EXAMPLE_DATA_SPACE_EDITORS = "CONFIRM_CREATE_EXAMPLE_DATA_SPACE_EDITORS"; //$NON-NLS-1$

    /*
     * Updater messages.
     */
    public static final String INFORMATION_UPDATE_5_2_10 = "INFORMATION_UPDATE_5_2_10"; //$NON-NLS-1$

    /*
     * Question message keys
     */
    // public static final String CONFIRM_OPEN_SAVED_FILE_SOURCE_FILE_SEARCH =
    // "CONFIRM_OPEN_SAVED_FILE_SOURCE_FILE_SEARCH"; //$NON-NLS-1$
    // public static final String CONFIRM_OPEN_SAVED_FILE_MESSAGE_FILE_SEARCH =
    // "CONFIRM_OPEN_SAVED_FILE_MESSAGE_FILE_SEARCH"; //$NON-NLS-1$
    // public static final String CONFIRM_OPEN_SAVED_FILE_STREAM_FILE_SEARCH =
    // "CONFIRM_OPEN_SAVED_FILE_STREAM_FILE_SEARCH"; //$NON-NLS-1$
}
