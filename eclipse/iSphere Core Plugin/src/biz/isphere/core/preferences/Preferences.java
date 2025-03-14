/*******************************************************************************
 * Copyright (c) 2012-2025 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.preferences;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;
import org.eclipse.jface.preference.IPreferenceStore;

import biz.isphere.base.internal.FileHelper;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.compareeditor.LoadPreviousMemberValue;
import biz.isphere.core.compareeditor.LoadPreviousStreamFileValue;
import biz.isphere.core.dataqueue.action.MessageLengthAction;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.memberrename.adapters.IMemberRenamingRuleAdapter;
import biz.isphere.core.memberrename.factories.MemberRenamingRuleFactory;
import biz.isphere.core.memberrename.rules.IMemberRenamingRule;
import biz.isphere.core.memberrename.rules.MemberRenamingRuleNumber;
import biz.isphere.core.preferencepages.IPreferences;
import biz.isphere.core.spooledfiles.SpooledFile;
import biz.isphere.core.spooledfiles.SpooledFileTransformerPDF.PageSize;

/**
 * Class to manage access to the preferences of the plugin.
 * <p>
 * Eclipse stores the preferences as <i>diffs</i> to their default values in
 * directory
 * <code>[workspace]\.metadata\.plugins\org.eclipse.core.runtime\.settings\</code>.
 * 
 * @author Thomas Raddatz
 */
public final class Preferences {

    /**
     * The instance of this Singleton class.
     */
    private static Preferences instance;

    /**
     * Global preferences of the plugin.
     */
    private IPreferenceStore preferenceStore;

    /**
     * List of date formats.
     */
    private Map<String, String> dateFormats;

    /**
     * List of time formats.
     */
    private Map<String, String> timeFormats;

    /**
     * List of suggested spooled file names.
     */
    private Map<String, String> suggestedSpooledFileNames;

    /**
     * List of file extensions that are supported by the date compare filter.
     */
    private HashSet<String> fileExtensionsSet;

    private static final String TOKEN_SEPARATOR = "|"; //$NON-NLS-1$

    /**
     * Unlimited depth of subdirectories when searching stream file.
     */
    private static final int UNLIMITTED_DEPTH = -1;
    public static final String UNLIMITTED = "*UNLIMITED";

    /*
     * Preferences keys:
     */

    private static final String DOMAIN = ISpherePlugin.PLUGIN_ID + "."; //$NON-NLS-1$

    private static final String WARNING_BASE_KEY = DOMAIN + "SHOW_WARNING."; //$NON-NLS-1$

    private static final String SPOOLED_FILE_NAME_DEFAULT = "*DEFAULT"; //$NON-NLS-1$
    private static final String SPOOLED_FILE_NAME_SIMPLE = "*SIMPLE"; //$NON-NLS-1$
    private static final String SPOOLED_FILE_NAME_QUALIFIED = "*QUALIFIED"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_MERGE_FILTERS = DOMAIN + "SPOOLED_FILES.MERGE.FILTERS"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_LOAD_ASYNCHRONOUSLY = DOMAIN + "SPOOLED_FILES.LOAD.ASYNCHRONOUSLY"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_SUGGESTED_FILE_NAME = DOMAIN + "SPOOLED_FILES.SUGGESTED.FILE_NAME"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_SAVE_DIRECTORY = DOMAIN + "SPOOLED_FILES.SAVE.DIRECTORY"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_PDF_COMMAND = DOMAIN + "SPOOLED_FILES.CONVERSION_PDF.COMMAND"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_PDF_LIBRARY = DOMAIN + "SPOOLED_FILES.CONVERSION_PDF.LIBRARY"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_PDF = DOMAIN + "SPOOLED_FILES.CONVERSION_PDF"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_HTML_COMMAND = DOMAIN + "SPOOLED_FILES.CONVERSION_HTML.COMMAND"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_HTML_LIBRARY = DOMAIN + "SPOOLED_FILES.CONVERSION_HTML.LIBRARY"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_HTML = DOMAIN + "SPOOLED_FILES.CONVERSION_HTML"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_HTML_EDIT_ALLOWED = DOMAIN + "SPOOLED_FILES.CONVERSION_HTML.EDIT_ALLOWED"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_TEXT_COMMAND = DOMAIN + "SPOOLED_FILES.CONVERSION_TEXT.COMMAND"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_TEXT_LIBRARY = DOMAIN + "SPOOLED_FILES.CONVERSION_TEXT.LIBRARY"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_TEXT = DOMAIN + "SPOOLED_FILES.CONVERSION_TEXT"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_CONVERSION_TEXT_EDIT_ALLOWED = DOMAIN + "SPOOLED_FILES.CONVERSION_TEXT.EDIT_ALLOWED"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_PAGE_SIZE = DOMAIN + "SPOOLED_FILES.PAGE_SIZE"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_ADJUST_FONT_SIZE = DOMAIN + "SPOOLED_FILES.ADJUST_FONT_SIZE"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_DEFAULT_FORMAT = DOMAIN + "SPOOLED_FILES.DEFAULT_FORMAT"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_MAX_FILES_TO_LOAD = DOMAIN + "MAX_FILES_TO_LOAD"; //$NON-NLS-1$
    private static final String SPOOLED_FILES_RSE_DESCRIPTION = DOMAIN + "RSE_DESCRIPTION"; //$NON-NLS-1$

    private static final String BACKUP_MEMBER_NAME_CURRENT_RULE = DOMAIN + "BACKUP_MEMBER_NAME.CURRENT_RULE"; //$NON-NLS-1$
    private static final String BACKUP_MEMBER_NAME_ENABLE_MEMBER_PRECHECK = DOMAIN + "BACKUP_MEMBER_NAME.ENABLE_MEMBER_PRECHECK"; //$NON-NLS-1$

    private static final String SOURCE_FILE_SEARCH_SEARCHSTRING = DOMAIN + "SOURCEFILESEARCH.SEARCHSTRING"; //$NON-NLS-1$
    private static final String SOURCE_FILE_SEARCH_EXPORT_DIRECTORY = DOMAIN + "SOURCEFILESEARCH.EXPORT_DIRECTORY"; //$NON-NLS-1$

    private static final String MESSAGE_FILE_SEARCH_SEARCHSTRING = DOMAIN + "MESSAGEFILESEARCH.SEARCHSTRING"; //$NON-NLS-1$
    private static final String MESSAGE_FILE_SEARCH_EXPORT_DIRECTORY = DOMAIN + "MESSAGEFILESEARCH.EXPORT_DIRECTORY"; //$NON-NLS-1$

    private static final String STREAM_FILE_SEARCH_SEARCHSTRING = DOMAIN + "STREAMFILESEARCH.SEARCHSTRING"; //$NON-NLS-1$
    private static final String STREAM_FILE_SEARCH_EXPORT_DIRECTORY = DOMAIN + "STREAMFILESEARCH.STREAM_FILE_SEARCH_EXPORT_DIRECTORY"; //$NON-NLS-1$

    // Upload library properties
    private static final String ISPHERE_LIBRARY = DOMAIN + "LIBRARY"; //$NON-NLS-1$
    private static final String ASP_GROUP = "ASP_GROUP"; //$NON-NLS-1$
    private static final String CONNECTION_NAME = DOMAIN + "HOST_NAME"; //$NON-NLS-1$
    private static final String FTP_PORT_NUMBER = DOMAIN + "FTP_PORT_NUMBER"; //$NON-NLS-1$

    private static final String SYSTEM_CCSID = DOMAIN + "SYSTEM_CCSID"; //$NON-NLS-1$

    private static final String SEARCH_FOR_UPDATES = DOMAIN + "SEARCH_FOR_UPDATES"; //$NON-NLS-1$
    private static final String SEARCH_FOR_BETA_VERSIONS = DOMAIN + "SEARCH_FOR_BETA_VERSIONS"; //$NON-NLS-1$
    private static final String URL_FOR_UPDATES = DOMAIN + "URL_FOR_UPDATES"; //$NON-NLS-1$
    private static final String LAST_VERSION_FOR_UPDATES = DOMAIN + "LAST_VERSION_FOR_UPDATES"; //$NON-NLS-1$

    private static final String MONITOR = DOMAIN + "MONITOR."; //$NON-NLS-1$
    private static final String MONITOR_DTAQ = MONITOR + "DTAQ."; //$NON-NLS-1$
    public static final String MONITOR_DTAQ_LENGTH = MONITOR_DTAQ + "LENGTH"; //$NON-NLS-1$
    public static final String MONITOR_DTAQ_VIEW_IN_HEX = MONITOR_DTAQ + "VIEW_IN_HEX"; //$NON-NLS-1$
    public static final String MONITOR_DTAQ_DISPLAY_END_OF_DATA = MONITOR_DTAQ + "DISPLAY_END_OF_DATA"; //$NON-NLS-1$
    public static final String MONITOR_DTAQ_REPLACEMENT_CHARACTER = MONITOR_DTAQ + "REPLACEMENT_CHARACTER"; //$NON-NLS-1$
    public static final String MONITOR_DTAQ_NUMBER_OF_MESSAGES = MONITOR_DTAQ + "NUMBER_OF_MESSAGES"; //$NON-NLS-1$

    private static final String SOURCE_FILE_SEARCH = DOMAIN + "SOURCE_FILE_SEARCH."; //$NON-NLS-1$
    private static final String SOURCE_FILE_SEARCH_IS_BATCH_RESOLVE_ENABLED = SOURCE_FILE_SEARCH + "IS_BATCH_RESOLVE"; //$NON-NLS-1$
    private static final String SOURCE_FILE_SEARCH_RESULTS = DOMAIN + "SOURCE_FILE_SEARCH_RESULTS."; //$NON-NLS-1$
    private static final String SOURCE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED = SOURCE_FILE_SEARCH_RESULTS + "IS_EDIT_ENABLED"; //$NON-NLS-1$
    private static final String SOURCE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY = SOURCE_FILE_SEARCH_RESULTS + "SAVE_DIRECTORY"; //$NON-NLS-1$
    private static final String SOURCE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME = SOURCE_FILE_SEARCH_RESULTS + "LAST_USED_FILE_NAME"; //$NON-NLS-1$
    public static final String SOURCE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED = SOURCE_FILE_SEARCH_RESULTS + "IS_AUTO_SAVE_ENABLED"; //$NON-NLS-1$
    private static final String SOURCE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE = SOURCE_FILE_SEARCH_RESULTS + "AUTO_SAVE_FILE"; //$NON-NLS-1$
    private static final String SOURCE_FILE_SEARCH_DIRECTORY = "sourceFileSearch"; //$NON-NLS-1$
    private static final String SOURCE_FILE_SEARCH_FILE_NAME = "SourceFileSearchResult"; //$NON-NLS-1$

    private static final String MESSAGE_FILE_SEARCH_RESULTS = DOMAIN + "MESSAGE_FILE_SEARCH_RESULTS."; //$NON-NLS-1$
    private static final String MESSAGE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED = MESSAGE_FILE_SEARCH_RESULTS + "IS_EDIT_ENABLED"; //$NON-NLS-1$
    private static final String MESSAGE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY = MESSAGE_FILE_SEARCH_RESULTS + "SAVE_DIRECTORY"; //$NON-NLS-1$
    private static final String MESSAGE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME = MESSAGE_FILE_SEARCH_RESULTS + "LAST_USED_FILE_NAME"; //$NON-NLS-1$
    public static final String MESSAGE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED = MESSAGE_FILE_SEARCH_RESULTS + "IS_AUTO_SAVE_ENABLED"; //$NON-NLS-1$
    private static final String MESSAGE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE = MESSAGE_FILE_SEARCH_RESULTS + "AUTO_SAVE_FILE"; //$NON-NLS-1$
    private static final String MESSAGE_FILE_SEARCH_DIRECTORY = "messageFileSearch"; //$NON-NLS-1$
    private static final String MESSAGE_FILE_SEARCH_FILE_NAME = "MessageFileSearchResult"; //$NON-NLS-1$

    private static final String STREAM_FILE_SEARCH = DOMAIN + "STREAM_FILE_SEARCH."; //$NON-NLS-1$
    private static final String STREAM_FILE_SEARCH_IS_BATCH_RESOLVE_ENABLED = STREAM_FILE_SEARCH + "IS_BATCH_RESOLVE"; //$NON-NLS-1$
    private static final String STREAM_FILE_SEARCH_MAX_DEPTH = STREAM_FILE_SEARCH + "STREAM_FILE_SEARCH_MAX_DEPTH"; //$NON-NLS-1$
    private static final String STREAM_FILE_SEARCH_RESULTS = DOMAIN + "STREAM_FILE_SEARCH_RESULTS."; //$NON-NLS-1$
    private static final String STREAM_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED = STREAM_FILE_SEARCH_RESULTS + "IS_EDIT_ENABLED"; //$NON-NLS-1$
    private static final String STREAM_FILE_SEARCH_RESULTS_SAVE_DIRECTORY = STREAM_FILE_SEARCH_RESULTS + "SAVE_DIRECTORY"; //$NON-NLS-1$
    private static final String STREAM_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME = STREAM_FILE_SEARCH_RESULTS + "LAST_USED_FILE_NAME"; //$NON-NLS-1$
    public static final String STREAM_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED = STREAM_FILE_SEARCH_RESULTS + "IS_AUTO_SAVE_ENABLED"; //$NON-NLS-1$
    private static final String STREAM_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE = STREAM_FILE_SEARCH_RESULTS + "AUTO_SAVE_FILE"; //$NON-NLS-1$
    private static final String STREAM_FILE_SEARCH_DIRECTORY = "streamFileSearch"; //$NON-NLS-1$
    private static final String STREAM_FILE_SEARCH_FILE_NAME = "StreamFileSearchResult"; //$NON-NLS-1$

    private static final String MESSAGE_FILE_COMPARE = DOMAIN + "MESSAGE_FILE_COMPARE."; //$NON-NLS-1$
    private static final String MESSAGE_FILE_COMPARE_LINE_WIDTH = MESSAGE_FILE_COMPARE + "LINE_WIDTH"; //$NON-NLS-1$

    private static final String SOURCE_MEMBER_COMPARE = DOMAIN + "SOURCE_MEMBER_COMPARE."; //$NON-NLS-1$
    private static final String SOURCE_MEMBER_COMPARE_LOAD_PREVIOUS_VALUES_RIGHT_MEMBER = SOURCE_MEMBER_COMPARE + "LOAD_PREVIOUS_VALUES_RIGHT_MEMBER"; //$NON-NLS-1$
    private static final String SOURCE_MEMBER_COMPARE_LOAD_PREVIOUS_VALUES_ANCESTOR_MEMBER = SOURCE_MEMBER_COMPARE
        + "LOAD_PREVIOUS_VALUES_ANCESTOR_MEMBER"; //$NON-NLS-1$
    private static final String SOURCE_MEMBER_COMPARE_IGNORE_WHITE_SPACES = SOURCE_MEMBER_COMPARE + "SOURCE_MEMBER_COMPARE_IGNORE_WHITE_SPACES"; //$NON-NLS-1$

    private static final String SOURCE_STREAM_FILE_COMPARE = DOMAIN + "SOURCE_STREAM_FILE_COMPARE."; //$NON-NLS-1$
    private static final String SOURCE_STREAM_FILE_COMPARE_LOAD_PREVIOUS_VALUES_RIGHT_STREAM_FILE = SOURCE_STREAM_FILE_COMPARE
        + "LOAD_PREVIOUS_VALUES_RIGHT_STREAM_FILE"; //$NON-NLS-1$
    private static final String SOURCE_STREAM_FILE_COMPARE_LOAD_PREVIOUS_VALUES_ANCESTOR_STREAM_FILE = SOURCE_STREAM_FILE_COMPARE
        + "LOAD_PREVIOUS_VALUES_ANCESTOR_STREAM_FILE"; //$NON-NLS-1$

    private static final String APPEARANCE = DOMAIN + "APPEARANCE."; //$NON-NLS-1$
    private static final String APPEARANCE_DATE_FORMAT = APPEARANCE + "DATE_FORMAT"; //$NON-NLS-1$
    private static final String APPEARANCE_TIME_FORMAT = APPEARANCE + "TIME_FORMAT"; //$NON-NLS-1$
    private static final String APPEARANCE_FORMAT_RESOURCE_DATES = APPEARANCE + "FORMAT_RESOURCE_DATES"; //$NON-NLS-1$
    private static final String APPEARANCE_OPEN_FILE_AFTER_SAVING = APPEARANCE + "OPEN_FILE_AFTER_SAVING"; //$NON-NLS-1$
    private static final String APPEARANCE_DATE_FORMAT_LOCALE = "*LOCALE"; //$NON-NLS-1$
    private static final String APPEARANCE_TIME_FORMAT_LOCALE = "*LOCALE"; //$NON-NLS-1$
    private static final String APPEARANCE_AUTO_REFRESH = APPEARANCE + "AUTO_REFRESH."; //$NON-NLS-1$
    private static final String APPEARANCE_AUTO_REFRESH_DELAY = APPEARANCE_AUTO_REFRESH + "DELAY"; //$NON-NLS-1$
    private static final String APPEARANCE_AUTO_REFRESH_THRESHOLD = APPEARANCE_AUTO_REFRESH + "THRESHOLD"; //$NON-NLS-1$
    private static final String APPEARANCE_SHOW_ERROR_LOG = APPEARANCE_AUTO_REFRESH + "APPEARANCE_SHOW_ERROR_LOG"; //$NON-NLS-1$

    private static final String JDBC_USE_ISPHERE_MANAGER = "USE_ISPHERE_MANAGER"; //$NON-NLS-1$

    private static final String COMPARE_FILTER_FILE_EXTENSIONS = DOMAIN + "fileextensions"; //$NON-NLS-1$
    private static final String COMPARE_FILTER_IMPORT_EXPORT_LOCATION = DOMAIN + "importexportlocation"; //$NON-NLS-1$

    private static final String SYNC_MEMBERS = DOMAIN + "SYNC_MEMBERS."; //$NON-NLS-1$
    private static final String SYNC_MEMBERS_EDITOR_DETACHED = SYNC_MEMBERS + "EDITOR_DETACHED"; //$NON-NLS-1$
    private static final String SYNC_MEMBERS_EDITOR_CENTER_ON_SCREEN = SYNC_MEMBERS + "EDITOR_CENTER_ON_SCREEN"; //$NON-NLS-1$
    private static final String SYNC_MEMBERS_EDITOR_SIDE_BY_SIDE = SYNC_MEMBERS + "EDITOR_SIDE_BY_SIDE"; //$NON-NLS-1$
    private static final String SYNC_MEMBERS_FILES_EXCLUDED = SYNC_MEMBERS + "FILES_EXCLUDED"; //$NON-NLS-1$

    /**
     * Private constructor to ensure the Singleton pattern.
     */
    private Preferences() {
    }

    /**
     * Thread-safe method that returns the instance of this Singleton class.
     */
    public synchronized static Preferences getInstance() {
        if (instance == null) {
            ISpherePlugin plugin = ISpherePlugin.getDefault();
            if (plugin == null) {
                // JUnit test
                return null;
            }
            instance = new Preferences();
            instance.preferenceStore = plugin.getPreferenceStore();
        }
        return instance;
    }

    /**
     * Thread-safe method that disposes the instance of this Singleton class.
     * <p>
     * This method is intended to be call from
     * {@link org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)}
     * to free the reference to itself.
     */
    public static void dispose() {
        if (instance != null) {
            instance = null;
        }
    }

    /*
     * Preferences: GETTER
     */

    public boolean isShowWarningMessage(String showWarningKey) {
        String key = getShowWarningKey(showWarningKey);
        if (!preferenceStore.contains(key)) {
            return true;
        }
        return preferenceStore.getBoolean(key);
    }

    public String getISphereLibrary() {
        return preferenceStore.getString(ISPHERE_LIBRARY);
    }

    public String getASPGroup() {
        return preferenceStore.getString(ASP_GROUP);
    }

    public String getConnectionName() {
        return preferenceStore.getString(CONNECTION_NAME);
    }

    public int getFtpPortNumber() {
        return preferenceStore.getInt(FTP_PORT_NUMBER);
    }

    public int getSystemCcsid() {
        return preferenceStore.getInt(SYSTEM_CCSID);
    }

    public boolean isSearchForUpdates() {
        return preferenceStore.getBoolean(SEARCH_FOR_UPDATES);
    }

    public boolean isSearchForBetaVersions() {
        return preferenceStore.getBoolean(SEARCH_FOR_BETA_VERSIONS);
    }

    public String getURLForUpdates() {
        return preferenceStore.getString(URL_FOR_UPDATES);
    }

    public String getLastVersionForUpdates() {
        return preferenceStore.getString(LAST_VERSION_FOR_UPDATES);
    }

    public String getMessageFileSearchString() {
        return preferenceStore.getString(MESSAGE_FILE_SEARCH_SEARCHSTRING);
    }

    public String getMessageFileSearchExportDirectory() {
        return preferenceStore.getString(MESSAGE_FILE_SEARCH_EXPORT_DIRECTORY);
    }

    public String getSourceFileSearchString() {
        return preferenceStore.getString(SOURCE_FILE_SEARCH_SEARCHSTRING);
    }

    public String getSourceFileSearchExportDirectory() {
        return preferenceStore.getString(SOURCE_FILE_SEARCH_EXPORT_DIRECTORY);
    }

    public String getStreamFileSearchString() {
        return preferenceStore.getString(STREAM_FILE_SEARCH_SEARCHSTRING);
    }

    public String getStreamFileSearchExportDirectory() {
        return preferenceStore.getString(STREAM_FILE_SEARCH_EXPORT_DIRECTORY);
    }

    public boolean isMergeSpooledFileFilters() {
        return preferenceStore.getBoolean(SPOOLED_FILES_MERGE_FILTERS);
    }

    public boolean isLoadSpooledFilesAsynchronousliy() {
        return preferenceStore.getBoolean(SPOOLED_FILES_LOAD_ASYNCHRONOUSLY);
    }

    public String getSpooledFilesSuggestedFileName() {
        return preferenceStore.getString(SPOOLED_FILES_SUGGESTED_FILE_NAME);
    }

    public String getSpooledFileConversionDefaultFormat() {
        return preferenceStore.getString(SPOOLED_FILES_DEFAULT_FORMAT);
    }

    public int getSpooledFilesMaxFilesToLoad() {
        return preferenceStore.getInt(SPOOLED_FILES_MAX_FILES_TO_LOAD);
    }

    public String getSpooledFileRSEDescription() {
        return preferenceStore.getString(SPOOLED_FILES_RSE_DESCRIPTION);
    }

    public IMemberRenamingRule getMemberRenamingRule() {

        IMemberRenamingRule rule = null;

        String className = preferenceStore.getString(BACKUP_MEMBER_NAME_CURRENT_RULE);

        try {

            Class clazz = Class.forName(className);
            rule = MemberRenamingRuleFactory.getInstance().getMemberRenamingRule(clazz);

        } catch (Exception e) {
            // Ignore exceptions
        }

        if (rule == null) {
            rule = getDefaultMemberRenamingRule();
        }

        return rule;
    }

    public IMemberRenamingRuleAdapter getMemberRenamingRuleAdapter(Class<? extends IMemberRenamingRule> clazz) {

        IMemberRenamingRuleAdapter adapters = MemberRenamingRuleFactory.getInstance().getMemberRenamingRuleAdapter(clazz);
        if (adapters == null) {
            return MemberRenamingRuleFactory.getInstance().getMemberRenamingRuleAdapter(MemberRenamingRuleNumber.class);
        }

        return adapters;
    }

    public boolean isMemberRenamingPrecheck() {
        return preferenceStore.getBoolean(BACKUP_MEMBER_NAME_ENABLE_MEMBER_PRECHECK);
    }

    public String getSpooledFileConversionText() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_TEXT);
    }

    public String getSpooledFileConversionTextLibrary() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_TEXT_LIBRARY);
    }

    public String getSpooledFileConversionTextCommand() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_TEXT_COMMAND);
    }

    public boolean isSpooledFileConversionTextEditAllowed() {
        return preferenceStore.getBoolean(SPOOLED_FILES_CONVERSION_TEXT_EDIT_ALLOWED);
    }

    public String getSpooledFileConversionHTML() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_HTML);
    }

    public String getSpooledFileConversionHTMLLibrary() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_HTML_LIBRARY);
    }

    public String getSpooledFileConversionHTMLCommand() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_HTML_COMMAND);
    }

    public boolean isSpooledFileConversionHTMLEditAllowed() {
        return preferenceStore.getBoolean(SPOOLED_FILES_CONVERSION_HTML_EDIT_ALLOWED);
    }

    public String getSpooledFileConversionPDF() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_PDF);
    }

    public String getSpooledFileConversionPDFLibrary() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_PDF_LIBRARY);
    }

    public String getSpooledFileConversionPDFCommand() {
        return preferenceStore.getString(SPOOLED_FILES_CONVERSION_PDF_COMMAND);
    }

    public String getSpooledFileSaveDirectory() {
        String directory = preferenceStore.getString(SPOOLED_FILES_SAVE_DIRECTORY);
        if (StringHelper.isNullOrEmpty(directory)) {
            return FileHelper.getDefaultRootDirectory();
        }
        return directory;
    }

    public String getSpooledFilePageSize() {
        return preferenceStore.getString(SPOOLED_FILES_PAGE_SIZE);
    }

    public boolean getSpooledFileAdjustFontSize() {
        return preferenceStore.getBoolean(SPOOLED_FILES_ADJUST_FONT_SIZE);
    }

    public int getDataQueueMaximumMessageLength() {
        return preferenceStore.getInt(MONITOR_DTAQ_LENGTH);
    }

    public boolean isDataQueueViewInHex() {
        return preferenceStore.getBoolean(MONITOR_DTAQ_VIEW_IN_HEX);
    }

    public boolean isDataQueueDisplayEndOfData() {
        return preferenceStore.getBoolean(MONITOR_DTAQ_DISPLAY_END_OF_DATA);
    }

    public String getDataQueueReplacementCharacter() {
        return preferenceStore.getString(MONITOR_DTAQ_REPLACEMENT_CHARACTER);
    }

    public int getDataQueueNumberOfMessagesToRetrieve() {
        return preferenceStore.getInt(MONITOR_DTAQ_NUMBER_OF_MESSAGES);
    }

    public boolean isSourceFileSearchBatchResolveEnabled() {
        return preferenceStore.getBoolean(SOURCE_FILE_SEARCH_IS_BATCH_RESOLVE_ENABLED);
    }

    public boolean isSourceFileSearchResultsEditEnabled() {
        return preferenceStore.getBoolean(SOURCE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED);
    }

    public boolean isStreamFileSearchBatchResolveEnabled() {
        return preferenceStore.getBoolean(STREAM_FILE_SEARCH_IS_BATCH_RESOLVE_ENABLED);
    }

    public boolean isStreamFileSearchResultsEditEnabled() {
        return preferenceStore.getBoolean(STREAM_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED);
    }

    public String getSourceFileSearchResultsAutoSaveDirectory() {
        String directory = preferenceStore.getString(SOURCE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY);
        if (!directory.endsWith(File.separator)) {
            directory = directory + File.separator;
        }
        return directory;
    }

    public String getSourceFileSearchResultsLastUsedFileName() {
        String filename = preferenceStore.getString(SOURCE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME);
        if (!StringHelper.isNullOrEmpty(filename)) {
            File file = new File(filename);
            if (!file.exists()) {
                filename = null;
            } else {
                if (file.isDirectory()) {
                    filename = filename + SOURCE_FILE_SEARCH_FILE_NAME; // $NON-NLS-1$
                }
            }
        } else {
            filename = null;
        }

        if (filename == null) {
            return getDefaultSourceFileSearchResultsSaveDirectory() + SOURCE_FILE_SEARCH_FILE_NAME; // $NON-NLS-1$
        }

        return filename;
    }

    public boolean isSourceFileSearchResultsAutoSaveEnabled() {

        /*
         * Does not work without the contribution, because we cannot create an
         * AS400 object, when loading a search result.
         */
        if (!IBMiHostContributionsHandler.hasContribution()) {
            return false;
        }

        return preferenceStore.getBoolean(SOURCE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED);
    }

    public String getSourceFileSearchResultsAutoSaveFileName() {
        return preferenceStore.getString(SOURCE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE);
    }

    public boolean isMessageFileSearchResultsEditEnabled() {
        return preferenceStore.getBoolean(MESSAGE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED);
    }

    public String getMessageFileSearchResultsAutoSaveDirectory() {
        String directory = preferenceStore.getString(MESSAGE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY);
        if (!directory.endsWith(File.separator)) {
            directory = directory + File.separator;
        }
        return directory;
    }

    public String getMessageFileSearchResultsLastUsedFileName() {
        String filename = preferenceStore.getString(MESSAGE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME);
        if (!StringHelper.isNullOrEmpty(filename)) {
            File file = new File(filename);
            if (!file.exists()) {
                filename = null;
            } else {
                if (file.isDirectory()) {
                    filename = filename + MESSAGE_FILE_SEARCH_FILE_NAME; // $NON-NLS-1$
                }
            }
        } else {
            filename = null;
        }

        if (filename == null) {
            return getDefaultMessageFileSearchResultsSaveDirectory() + MESSAGE_FILE_SEARCH_FILE_NAME; // $NON-NLS-1$
        }

        return filename;
    }

    public boolean isStreamFileSearchResultsAutoSaveEnabled() {

        /*
         * Does not work without the contribution, because we cannot create an
         * AS400 object, when loading a search result.
         */
        if (!IBMiHostContributionsHandler.hasContribution()) {
            return false;
        }

        return preferenceStore.getBoolean(STREAM_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED);
    }

    public String getStreamFileSearchResultsAutoSaveFileName() {
        return preferenceStore.getString(STREAM_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE);
    }

    public boolean isStreamFileSearchUnlimitedMaxDepth(int maxDepth) {
        return maxDepth == UNLIMITTED_DEPTH;
    }

    public int getStreamFileSearchMaxDepth() {
        return preferenceStore.getInt(STREAM_FILE_SEARCH_MAX_DEPTH);
    }

    public int getStreamFileSearchMaxDepthSpecialValueUnlimited() {
        return UNLIMITTED_DEPTH;
    }

    public String getStreamFileSearchResultsAutoSaveDirectory() {
        String directory = preferenceStore.getString(STREAM_FILE_SEARCH_RESULTS_SAVE_DIRECTORY);
        if (!directory.endsWith(File.separator)) {
            directory = directory + File.separator;
        }
        return directory;
    }

    public String getStreamFileSearchResultsLastUsedFileName() {
        String filename = preferenceStore.getString(STREAM_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME);
        if (!StringHelper.isNullOrEmpty(filename)) {
            File file = new File(filename);
            if (!file.exists()) {
                filename = null;
            } else {
                if (file.isDirectory()) {
                    filename = filename + STREAM_FILE_SEARCH_FILE_NAME; // $NON-NLS-1$
                }
            }
        } else {
            filename = null;
        }

        if (filename == null) {
            return getDefaultStreamFileSearchResultsSaveDirectory() + STREAM_FILE_SEARCH_FILE_NAME; // $NON-NLS-1$
        }

        return filename;
    }

    public boolean isMessageFileSearchResultsAutoSaveEnabled() {

        /*
         * Does not work without the contribution, because we cannot create an
         * AS400 object, when loading a search result.
         */
        if (!IBMiHostContributionsHandler.hasContribution()) {
            return false;
        }

        return preferenceStore.getBoolean(MESSAGE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED);
    }

    public String getMessageFileSearchResultsAutoSaveFileName() {
        return preferenceStore.getString(MESSAGE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE);
    }

    public int getMessageFileCompareLineWidth() {
        return preferenceStore.getInt(MESSAGE_FILE_COMPARE_LINE_WIDTH);
    }

    public boolean isSourceMemberCompareLoadingPreviousValuesOfRightMemberEnabled() {
        String value = preferenceStore.getString(SOURCE_MEMBER_COMPARE_LOAD_PREVIOUS_VALUES_RIGHT_MEMBER);
        if ("true".equals(value)) { //$NON-NLS-1$
            return true;
        } else if (!LoadPreviousMemberValue.NONE.name().equals(value)) { // $NON-NLS-1$
            return true;
        } else {
            return false;
        }
    }

    public LoadPreviousMemberValue getSourceMemberCompareLoadingPreviousValuesOfRightMember() {
        try {
            String value = preferenceStore.getString(SOURCE_MEMBER_COMPARE_LOAD_PREVIOUS_VALUES_RIGHT_MEMBER);
            if ("true".equals(value)) {
                return LoadPreviousMemberValue.CONNECTION_LIBRARY_FILE_MEMBER;
            }
            return LoadPreviousMemberValue.valueOf(value);
        } catch (Throwable e) {
            return LoadPreviousMemberValue.NONE;
        }
    }

    public boolean isSourceMemberCompareLoadingPreviousValuesOfAncestorMemberEnabled() {
        String value = preferenceStore.getString(SOURCE_MEMBER_COMPARE_LOAD_PREVIOUS_VALUES_ANCESTOR_MEMBER);
        if ("true".equals(value)) { //$NON-NLS-1$
            return true;
        } else if (!LoadPreviousMemberValue.NONE.name().equals(value)) { // $NON-NLS-1$
            return true;
        } else {
            return false;
        }
    }

    public LoadPreviousMemberValue getSourceMemberCompareLoadingPreviousValuesOfAncestorMember() {
        try {
            String value = preferenceStore.getString(SOURCE_MEMBER_COMPARE_LOAD_PREVIOUS_VALUES_ANCESTOR_MEMBER);
            if ("true".equals(value)) {
                return LoadPreviousMemberValue.CONNECTION_LIBRARY_FILE_MEMBER;
            }
            return LoadPreviousMemberValue.valueOf(value);
        } catch (Throwable e) {
            return LoadPreviousMemberValue.NONE;
        }
    }

    public boolean isSourceStreamFileCompareLoadingPreviousValuesOfRightStreamFileEnabled() {
        String value = preferenceStore.getString(SOURCE_STREAM_FILE_COMPARE_LOAD_PREVIOUS_VALUES_RIGHT_STREAM_FILE);
        if ("true".equals(value)) { //$NON-NLS-1$
            return true;
        } else if (!LoadPreviousStreamFileValue.NONE.name().equals(value)) { // $NON-NLS-1$
            return true;
        } else {
            return false;
        }
    }

    public LoadPreviousStreamFileValue getSourceStreamFileCompareLoadingPreviousValuesOfRightStreamFile() {
        try {
            String value = preferenceStore.getString(SOURCE_STREAM_FILE_COMPARE_LOAD_PREVIOUS_VALUES_RIGHT_STREAM_FILE);
            if ("true".equals(value)) {
                return LoadPreviousStreamFileValue.CONNECTION_DIRECTORY_FILE;
            }
            return LoadPreviousStreamFileValue.valueOf(value);
        } catch (Throwable e) {
            return LoadPreviousStreamFileValue.NONE;
        }
    }

    public boolean isSourceStreamFileCompareLoadingPreviousValuesOfAncestorStreamFileEnabled() {
        String value = preferenceStore.getString(SOURCE_STREAM_FILE_COMPARE_LOAD_PREVIOUS_VALUES_ANCESTOR_STREAM_FILE);
        if ("true".equals(value)) { //$NON-NLS-1$
            return true;
        } else if (!LoadPreviousStreamFileValue.NONE.name().equals(value)) { // $NON-NLS-1$
            return true;
        } else {
            return false;
        }
    }

    public LoadPreviousStreamFileValue getSourceStreamFileCompareLoadingPreviousValuesOfAncestorStreamFile() {
        try {
            String value = preferenceStore.getString(SOURCE_STREAM_FILE_COMPARE_LOAD_PREVIOUS_VALUES_ANCESTOR_STREAM_FILE);
            if ("true".equals(value)) {
                return LoadPreviousStreamFileValue.CONNECTION_DIRECTORY_FILE;
            }
            return LoadPreviousStreamFileValue.valueOf(value);
        } catch (Throwable e) {
            return LoadPreviousStreamFileValue.NONE;
        }
    }

    public boolean isSourceMemberCompareIgnoreWhiteSpaces() {
        return preferenceStore.getBoolean(SOURCE_MEMBER_COMPARE_IGNORE_WHITE_SPACES);
    }

    public String getDateFormatLabel() {
        return preferenceStore.getString(APPEARANCE_DATE_FORMAT);
    }

    public String getTimeFormatLabel() {
        return preferenceStore.getString(APPEARANCE_TIME_FORMAT);
    }

    public boolean isFormatResourceDates() {
        return preferenceStore.getBoolean(APPEARANCE_FORMAT_RESOURCE_DATES);
    }

    public boolean isOpenFilesAfterSaving() {
        return preferenceStore.getBoolean(APPEARANCE_OPEN_FILE_AFTER_SAVING);
    }

    public int getAutoRefreshDelay() {
        return preferenceStore.getInt(APPEARANCE_AUTO_REFRESH_DELAY);
    }

    public int getAutoRefreshThreshold() {
        return preferenceStore.getInt(APPEARANCE_AUTO_REFRESH_THRESHOLD);
    }

    public boolean isShowErrorLog() {
        return preferenceStore.getBoolean(APPEARANCE_SHOW_ERROR_LOG);
    }

    public boolean isISphereJdbcConnectionManager() {
        return preferenceStore.getBoolean(JDBC_USE_ISPHERE_MANAGER);
    }

    public boolean isKerberosAuthentication() {

        return IBMiHostContributionsHandler.isKerberosAuthentication();
    }

    public String[] getFileExtensions() {
        return getFileExtensions(false);
    }

    public String[] getDefaultFileExtensions() {
        String tList = getDefaultFileExtensionsAsString();
        return StringHelper.getTokens(tList, TOKEN_SEPARATOR);
    }

    public boolean supportsFileExtension(String fileExtension) {
        if (fileExtension != null && getOrCreateFileExtensionsSet().contains(fileExtension.toUpperCase())) {
            return true;
        }
        return false;
    }

    public String getImportExportLocation() {
        return preferenceStore.getString(COMPARE_FILTER_IMPORT_EXPORT_LOCATION);
    }

    public boolean isSyncMembersEditorDetached() {
        return preferenceStore.getBoolean(SYNC_MEMBERS_EDITOR_DETACHED);
    }

    public boolean isSyncMembersCenterOnScreen() {
        return preferenceStore.getBoolean(SYNC_MEMBERS_EDITOR_CENTER_ON_SCREEN);
    }

    public boolean isSyncMembersSideBySide() {
        return preferenceStore.getBoolean(SYNC_MEMBERS_EDITOR_SIDE_BY_SIDE);
    }

    public String[] getSyncMembersFilesExcluded() {
        return getFilesExcluded();
    }

    /*
     * Preferences: SETTER
     */

    public void setShowWarningMessage(String showWarningKey, boolean enable) {
        preferenceStore.setValue(getShowWarningKey(showWarningKey), enable);
    }

    public void setISphereLibrary(String aLibrary) {
        preferenceStore.setValue(ISPHERE_LIBRARY, aLibrary.trim());
    }

    public void setASPGroup(String aASPGroup) {
        preferenceStore.setValue(ASP_GROUP, aASPGroup.trim());
    }

    public void setConnectionName(String aHostName) {
        preferenceStore.setValue(CONNECTION_NAME, aHostName);
    }

    public void setFtpPortNumber(int aPortNumber) {
        preferenceStore.setValue(FTP_PORT_NUMBER, aPortNumber);
    }

    public void setSystemCcsid(int ccsid) {
        preferenceStore.setValue(SYSTEM_CCSID, ccsid);
    }

    public void setSearchForUpdates(boolean aSearchForUpdates) {
        preferenceStore.setValue(SEARCH_FOR_UPDATES, aSearchForUpdates);
    }

    public void setSearchForBetaVersions(boolean aSearchForUpdates) {
        preferenceStore.setValue(SEARCH_FOR_BETA_VERSIONS, aSearchForUpdates);
    }

    public void setURLForUpdates(String aURLForUpdates) {
        preferenceStore.setValue(URL_FOR_UPDATES, aURLForUpdates);
    }

    public void setLastVersionForUpdates(String aLastVersionForUpdates) {
        preferenceStore.setValue(LAST_VERSION_FOR_UPDATES, aLastVersionForUpdates);
    }

    public void setMessageFileSearchString(String aSearchString) {
        preferenceStore.setValue(MESSAGE_FILE_SEARCH_SEARCHSTRING, aSearchString.trim());
    }

    public void setMessageFileSearchExportDirectory(String aPath) {
        preferenceStore.setValue(MESSAGE_FILE_SEARCH_EXPORT_DIRECTORY, aPath);
    }

    public void setSourceFileSearchString(String aSearchString) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_SEARCHSTRING, aSearchString.trim());
    }

    public void setSourceFileSearchExportDirectory(String aPath) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_EXPORT_DIRECTORY, aPath);
    }

    public void setStreamFileSearchString(String aSearchString) {
        preferenceStore.setValue(STREAM_FILE_SEARCH_SEARCHSTRING, aSearchString.trim());
    }

    public void setStreamFileSearchExportDirectory(String aPath) {
        preferenceStore.setValue(STREAM_FILE_SEARCH_EXPORT_DIRECTORY, aPath);
    }

    public void setMergeSpooledFileFilters(boolean mergeFilters) {
        preferenceStore.setValue(SPOOLED_FILES_MERGE_FILTERS, mergeFilters);
    }

    public void setLoadSpooledFilesAsynchronousliy(boolean asynchronously) {
        preferenceStore.setValue(SPOOLED_FILES_LOAD_ASYNCHRONOUSLY, asynchronously);
    }

    public void setSpooledFilesSuggestedFileName(String fileName) {
        preferenceStore.setValue(SPOOLED_FILES_SUGGESTED_FILE_NAME, fileName);
    }

    public void setSpooledFileDefaultFormat(String aFormat) {
        preferenceStore.setValue(SPOOLED_FILES_DEFAULT_FORMAT, aFormat);
    }

    public void setSpooledFileMaxFilesToLoad(int count) {
        preferenceStore.setValue(SPOOLED_FILES_MAX_FILES_TO_LOAD, count);
    }

    public void setSpooledFileRSEDescription(String description) {
        preferenceStore.setValue(SPOOLED_FILES_RSE_DESCRIPTION, description);
    }

    public void setMemberRenamingRule(IMemberRenamingRule rule) {
        preferenceStore.setValue(BACKUP_MEMBER_NAME_CURRENT_RULE, rule.getClass().getName());
    }

    public void setMemberRenamingPrechek(boolean enabled) {
        preferenceStore.setValue(BACKUP_MEMBER_NAME_ENABLE_MEMBER_PRECHECK, enabled);
    }

    public void setSpooledFileConversionText(String aConversionType) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_TEXT, aConversionType);
    }

    public void setSpooledFileConversionLibraryText(String aLibrary) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_TEXT_LIBRARY, aLibrary);
    }

    public void setSpooledFileConversionCommandText(String aCommand) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_TEXT_COMMAND, aCommand);
    }

    public void setSpooledFileConversionTextEditAllowed(boolean enableEdit) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_TEXT_EDIT_ALLOWED, enableEdit);
    }

    public void setSpooledFileConversionHTML(String aConversionType) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_HTML, aConversionType);
    }

    public void setSpooledFileConversionLibraryHTML(String aLibrary) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_HTML_LIBRARY, aLibrary);
    }

    public void setSpooledFileConversionCommandHTML(String aCommand) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_HTML_COMMAND, aCommand);
    }

    public void setSpooledFileConversionHTMLEditAllowed(boolean enableEdit) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_HTML_EDIT_ALLOWED, enableEdit);
    }

    public void setSpooledFileConversionPDF(String aConversionType) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_PDF, aConversionType);
    }

    public void setSpooledFileConversionLibraryPDF(String aLibrary) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_PDF_LIBRARY, aLibrary);
    }

    public void setSpooledFileConversionCommandPDF(String aCommand) {
        preferenceStore.setValue(SPOOLED_FILES_CONVERSION_PDF_COMMAND, aCommand);
    }

    public void setSpooledFileSaveDirectory(String aDirectory) {
        preferenceStore.setValue(SPOOLED_FILES_SAVE_DIRECTORY, aDirectory);
    }

    public void setSpooledFilePageSize(String aPageSize) {
        preferenceStore.setValue(SPOOLED_FILES_PAGE_SIZE, aPageSize);
    }

    public void setSpooledFileAdjustFontSize(boolean anAdjustSize) {
        preferenceStore.setValue(SPOOLED_FILES_ADJUST_FONT_SIZE, anAdjustSize);
    }

    public void setDataQueueMaximumMessageLength(int length) {
        preferenceStore.setValue(MONITOR_DTAQ_LENGTH, length);
    }

    public void setDataQueueViewInHex(boolean viewInHex) {
        preferenceStore.setValue(MONITOR_DTAQ_VIEW_IN_HEX, viewInHex);
    }

    public void setDataQueueDisplayEndOfData(boolean viewInHex) {
        preferenceStore.setValue(MONITOR_DTAQ_DISPLAY_END_OF_DATA, viewInHex);
    }

    public void setDataQueueReplacementCharacter(String replacementCharacter) {
        preferenceStore.setValue(MONITOR_DTAQ_REPLACEMENT_CHARACTER, replacementCharacter);
    }

    public void setDataQueueNumberOfMessagesToRetrieve(int numberOfMessages) {
        preferenceStore.setValue(MONITOR_DTAQ_NUMBER_OF_MESSAGES, numberOfMessages);
    }

    public void setSourceFileSearchBatchResolveEnabled(boolean enabled) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_IS_BATCH_RESOLVE_ENABLED, enabled);
    }

    public void setSourceFileSearchResultsEditEnabled(boolean editable) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED, editable);
    }

    public void setSourceFileSearchResultsSaveDirectory(String directory) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY, directory);
    }

    public void setSourceFileSearchResultsLastUsedFileName(String directory) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME, directory);
    }

    public void setSourceFileSearchResultsAutoSaveEnabled(boolean enabled) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED, enabled);
    }

    public void setSourceFileSearchResultsAutoSaveFileName(String filename) {
        preferenceStore.setValue(SOURCE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE, filename);
    }

    public void setMessageFileSearchResultsSaveDirectory(String directory) {
        preferenceStore.setValue(MESSAGE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY, directory);
    }

    public void setMessageFileSearchResultsLastUsedFileName(String directory) {
        preferenceStore.setValue(MESSAGE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME, directory);
    }

    public void setMessageFileSearchResultsAutoSaveEnabled(boolean enabled) {
        preferenceStore.setValue(MESSAGE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED, enabled);
    }

    public void setMessageFileSearchResultsAutoSaveFileName(String filename) {
        preferenceStore.setValue(MESSAGE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE, filename);
    }

    public void setStreamFileSearchBatchResolveEnabled(boolean enabled) {
        preferenceStore.setValue(STREAM_FILE_SEARCH_IS_BATCH_RESOLVE_ENABLED, enabled);
    }

    public void setStreamFileSearchResultsEditEnabled(boolean editable) {
        preferenceStore.setValue(STREAM_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED, editable);
    }

    public void setStreamFileSearchResultsSaveDirectory(String directory) {
        preferenceStore.setValue(STREAM_FILE_SEARCH_RESULTS_SAVE_DIRECTORY, directory);
    }

    public void setStreamFileSearchResultsLastUsedFileName(String directory) {
        preferenceStore.setValue(STREAM_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME, directory);
    }

    public void setStreamFileSearchResultsAutoSaveEnabled(boolean enabled) {
        preferenceStore.setValue(STREAM_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED, enabled);
    }

    public void setStreamFileSearchResultsAutoSaveFileName(String filename) {
        preferenceStore.setValue(STREAM_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE, filename);
    }

    public void setStreamFileSearchMaxDepth(int maxDepth) {
        preferenceStore.setValue(STREAM_FILE_SEARCH_MAX_DEPTH, maxDepth);
    }

    public void setMessageFileCompareLineWidth(int lineWidth) {
        preferenceStore.setValue(MESSAGE_FILE_COMPARE_LINE_WIDTH, lineWidth);
    }

    public void setSourceMemberCompareLoadingPreviousValuesOfRightMemberEnabled(LoadPreviousMemberValue value) {
        preferenceStore.setValue(SOURCE_MEMBER_COMPARE_LOAD_PREVIOUS_VALUES_RIGHT_MEMBER, value.name());
    }

    public void setSourceMemberCompareLoadingPreviousValuesOfAncestorMemberEnabled(LoadPreviousMemberValue value) {
        preferenceStore.setValue(SOURCE_MEMBER_COMPARE_LOAD_PREVIOUS_VALUES_ANCESTOR_MEMBER, value.name());
    }

    public void setSourceStreamFileCompareLoadingPreviousValuesOfRightStreamFileEnabled(LoadPreviousStreamFileValue value) {
        preferenceStore.setValue(SOURCE_STREAM_FILE_COMPARE_LOAD_PREVIOUS_VALUES_RIGHT_STREAM_FILE, value.name());
    }

    public void setSourceStreamFileCompareLoadingPreviousValuesOfAncestorStreamFileEnabled(LoadPreviousStreamFileValue value) {
        preferenceStore.setValue(SOURCE_STREAM_FILE_COMPARE_LOAD_PREVIOUS_VALUES_ANCESTOR_STREAM_FILE, value.name());
    }

    public void setSourceMemberCompareIgnoreWhiteSpaces(boolean enabled) {
        preferenceStore.setValue(SOURCE_MEMBER_COMPARE_IGNORE_WHITE_SPACES, enabled);
    }

    public void setDateFormatLabel(String dateFormatLabel) {
        preferenceStore.setValue(APPEARANCE_DATE_FORMAT, dateFormatLabel);
    }

    public void setTimeFormatLabel(String dateFormatLabel) {
        preferenceStore.setValue(APPEARANCE_TIME_FORMAT, dateFormatLabel);
    }

    public void setFormatResourceDates(boolean format) {
        preferenceStore.setValue(APPEARANCE_FORMAT_RESOURCE_DATES, format);
    }

    public void setOpenFilesAfterSaving(boolean enabled) {
        preferenceStore.setValue(APPEARANCE_OPEN_FILE_AFTER_SAVING, enabled);
    }

    public void setAutoRefreshDelay(int delayMillis) {
        preferenceStore.setValue(APPEARANCE_AUTO_REFRESH_DELAY, delayMillis);
    }

    public void setAutoRefreshThreshold(int threshold) {
        preferenceStore.setValue(APPEARANCE_AUTO_REFRESH_THRESHOLD, threshold);
    }

    public void setShowErrorLog(boolean show) {
        preferenceStore.setValue(APPEARANCE_SHOW_ERROR_LOG, show);
    }

    public void setUseISphereJdbcConnectionManager(boolean enabled) {
        preferenceStore.setValue(JDBC_USE_ISPHERE_MANAGER, enabled);
    }

    public void setFileExtensions(String[] anExtensions) {
        saveFileExtensions(anExtensions);
    }

    public void setImportExportLocation(String aLocation) {
        saveImportExportLocation(aLocation);
    }

    public void setSyncMembersEditorDetached(boolean enabled) {
        preferenceStore.setValue(SYNC_MEMBERS_EDITOR_DETACHED, enabled);
    }

    public void setSyncMembersCenterOnScreen(boolean enabled) {
        preferenceStore.setValue(SYNC_MEMBERS_EDITOR_CENTER_ON_SCREEN, enabled);
    }

    public void setSyncMembersSideBySide(boolean enabled) {
        preferenceStore.setValue(SYNC_MEMBERS_EDITOR_SIDE_BY_SIDE, enabled);
    }

    public void setSyncMembersFilesExcluded(String[] filesExcluded) {
        saveFilesExcluded(filesExcluded);
    }

    /*
     * Preferences: Default Initializer
     */

    public void initializeDefaultPreferences() {

        String[] showWarningKeys = DoNotAskMeAgainDialog.getKeys();
        for (String showWarningKey : showWarningKeys) {
            preferenceStore.setDefault(getShowWarningKey(showWarningKey), DoNotAskMeAgainDialog.getDefaultShowWarning());
        }

        preferenceStore.setDefault(ISPHERE_LIBRARY, getDefaultISphereLibrary());
        preferenceStore.setDefault(ASP_GROUP, getDefaultASPGroup());
        preferenceStore.setDefault(CONNECTION_NAME, getDefaultConnectionName());
        preferenceStore.setDefault(FTP_PORT_NUMBER, getDefaultFtpPortNumber());
        preferenceStore.setDefault(SYSTEM_CCSID, getDefaultSystemCcsid());

        preferenceStore.setDefault(SEARCH_FOR_UPDATES, getDefaultSearchForUpdates());
        preferenceStore.setDefault(SEARCH_FOR_BETA_VERSIONS, getDefaultSearchForBetaVersions());
        preferenceStore.setDefault(URL_FOR_UPDATES, getDefaultURLForUpdates());
        preferenceStore.setDefault(LAST_VERSION_FOR_UPDATES, getDefaultLastVersionForUpdates());

        preferenceStore.setDefault(SPOOLED_FILES_MERGE_FILTERS, getDefaultMergeSpooledFileFilters());
        preferenceStore.setDefault(SPOOLED_FILES_LOAD_ASYNCHRONOUSLY, getDefaultLoadSpooledFilesAsynchronously());
        preferenceStore.setDefault(SPOOLED_FILES_SUGGESTED_FILE_NAME, getDefaultSpooledFilesSuggestedFileName());
        preferenceStore.setDefault(SPOOLED_FILES_DEFAULT_FORMAT, getDefaultSpooledFileConversionDefaultFormat());

        preferenceStore.setDefault(SPOOLED_FILES_MAX_FILES_TO_LOAD, getDefaultSpooledFileMaxFilesToLoad());
        preferenceStore.setDefault(SPOOLED_FILES_RSE_DESCRIPTION, getDefaultSpooledFileRSEDescription());

        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_TEXT, getDefaultSpooledFileConversionText());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_TEXT_COMMAND, getDefaultSpooledFileConversionTextCommand());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_TEXT_LIBRARY, getDefaultSpooledFileConversionTextLibrary());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_TEXT_EDIT_ALLOWED, getDefaultSpooledFileConversionTextEditAllowed());

        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_HTML, getDefaultSpooledFileConversionHTML());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_HTML_COMMAND, getDefaultSpooledFileConversionHTMLCommand());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_HTML_LIBRARY, getDefaultSpooledFileConversionHTMLLibrary());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_HTML_EDIT_ALLOWED, getDefaultSpooledFileConversionHTMLEditAllowed());

        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_PDF, getDefaultSpooledFileConversionPDF());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_PDF_COMMAND, getDefaultSpooledFileConversionPDFCommand());
        preferenceStore.setDefault(SPOOLED_FILES_CONVERSION_PDF_LIBRARY, getDefaultSpooledFileConversionPDFLibrary());

        preferenceStore.setDefault(SPOOLED_FILES_PAGE_SIZE, getDefaultSpooledFilePageSize());
        preferenceStore.setDefault(SPOOLED_FILES_ADJUST_FONT_SIZE, getDefaultSpooledFileAdjustFontSize());

        preferenceStore.setDefault(MONITOR_DTAQ_LENGTH, getDefaultDataQueueMaximumMessageLength());
        preferenceStore.setDefault(MONITOR_DTAQ_VIEW_IN_HEX, getDefaultDataQueueViewInHex());
        preferenceStore.setDefault(MONITOR_DTAQ_DISPLAY_END_OF_DATA, getDefaultDataQueueDisplayEndOfData());
        preferenceStore.setDefault(MONITOR_DTAQ_REPLACEMENT_CHARACTER, getDefaultDataQueueReplacementCharacter());
        preferenceStore.setDefault(MONITOR_DTAQ_NUMBER_OF_MESSAGES, getDefaultDataQueueNumberOfMessagesToRetrieve());

        preferenceStore.setDefault(SOURCE_FILE_SEARCH_IS_BATCH_RESOLVE_ENABLED, getDefaultSourceFileSearchBatchResolveEnabled());

        preferenceStore.setDefault(SOURCE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED, getDefaultSourceFileSearchResultsEditEnabled());
        preferenceStore.setDefault(SOURCE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY, getDefaultSourceFileSearchResultsSaveDirectory());
        preferenceStore.setDefault(SOURCE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME, getDefaultSourceFileSearchResultsLastUsedFileName());
        preferenceStore.setDefault(SOURCE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED, getDefaultSourceFileSearchResultsAutoSaveEnabled());
        preferenceStore.setDefault(SOURCE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE, getDefaultSourceFileSearchResultsAutoSaveFileName());
        preferenceStore.setDefault(SOURCE_FILE_SEARCH_SEARCHSTRING, getDefaultSourceFileSearchString());
        preferenceStore.setDefault(SOURCE_FILE_SEARCH_EXPORT_DIRECTORY, getDefaultSourceFileSearchExportDirectory());

        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED, getDefaultMessageFileSearchResultsEditEnabled());
        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_RESULTS_SAVE_DIRECTORY, getDefaultMessageFileSearchResultsSaveDirectory());
        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME, getDefaultMessageFileSearchResultsLastUsedFileName());
        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED, getDefaultMessageFileSearchResultsAutoSaveEnabled());
        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE, getDefaultMessageFileSearchResultsAutoSaveFileName());
        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_SEARCHSTRING, getDefaultMessageFileSearchString());
        preferenceStore.setDefault(MESSAGE_FILE_SEARCH_EXPORT_DIRECTORY, getDefaultMessageFileSearchExportDirectory());

        preferenceStore.setDefault(STREAM_FILE_SEARCH_IS_BATCH_RESOLVE_ENABLED, getDefaultStreamFileSearchBatchResolveEnabled());
        preferenceStore.setDefault(STREAM_FILE_SEARCH_MAX_DEPTH, getDefaultStreamFileSearchMaxDepth());

        preferenceStore.setDefault(STREAM_FILE_SEARCH_RESULTS_IS_EDIT_ENABLED, getDefaultStreamFileSearchResultsEditEnabled());
        preferenceStore.setDefault(STREAM_FILE_SEARCH_RESULTS_SAVE_DIRECTORY, getDefaultStreamFileSearchResultsSaveDirectory());
        preferenceStore.setDefault(STREAM_FILE_SEARCH_RESULTS_LAST_USED_FILE_NAME, getDefaultStreamFileSearchResultsLastUsedFileName());
        preferenceStore.setDefault(STREAM_FILE_SEARCH_RESULTS_IS_AUTO_SAVE_ENABLED, getDefaultStreamFileSearchResultsAutoSaveEnabled());
        preferenceStore.setDefault(STREAM_FILE_SEARCH_RESULTS_AUTO_SAVE_FILE, getDefaultStreamFileSearchResultsAutoSaveFileName());
        preferenceStore.setDefault(STREAM_FILE_SEARCH_SEARCHSTRING, getDefaultStreamFileSearchString());
        preferenceStore.setDefault(STREAM_FILE_SEARCH_EXPORT_DIRECTORY, getDefaultStreamFileSearchExportDirectory());

        preferenceStore.setDefault(MESSAGE_FILE_COMPARE_LINE_WIDTH, getDefaultMessageFileCompareMinLineWidth());
        preferenceStore.setDefault(SOURCE_MEMBER_COMPARE_LOAD_PREVIOUS_VALUES_RIGHT_MEMBER,
            getDefaultSourceMemberCompareLoadingPreviousValuesEnabled().name());
        preferenceStore.setDefault(SOURCE_MEMBER_COMPARE_LOAD_PREVIOUS_VALUES_ANCESTOR_MEMBER,
            getDefaultSourceMemberCompareLoadingPreviousValuesEnabled().name());

        preferenceStore.setDefault(SOURCE_STREAM_FILE_COMPARE_LOAD_PREVIOUS_VALUES_RIGHT_STREAM_FILE,
            getDefaultSourceStreamFileCompareLoadingPreviousValuesEnabled().name());
        preferenceStore.setDefault(SOURCE_STREAM_FILE_COMPARE_LOAD_PREVIOUS_VALUES_ANCESTOR_STREAM_FILE,
            getDefaultSourceStreamFileCompareLoadingPreviousValuesEnabled().name());

        preferenceStore.setDefault(SOURCE_MEMBER_COMPARE_IGNORE_WHITE_SPACES, getDefaultSourceMemberCompareIgnoreWhiteSpaces());

        preferenceStore.setDefault(APPEARANCE_DATE_FORMAT, getDefaultDateFormatLabel());
        preferenceStore.setDefault(APPEARANCE_TIME_FORMAT, getDefaultTimeFormatLabel());
        preferenceStore.setDefault(APPEARANCE_FORMAT_RESOURCE_DATES, getDefaultFormatResourceDates());
        preferenceStore.setDefault(APPEARANCE_OPEN_FILE_AFTER_SAVING, getDefaultOpenFilesAfterSaving());

        preferenceStore.setDefault(APPEARANCE_AUTO_REFRESH_DELAY, getDefaultAutoRefreshDelay());
        preferenceStore.setDefault(APPEARANCE_AUTO_REFRESH_THRESHOLD, getDefaultAutoRefreshThreshold());
        preferenceStore.setDefault(APPEARANCE_SHOW_ERROR_LOG, getDefaultShowErrorLog());

        preferenceStore.setDefault(JDBC_USE_ISPHERE_MANAGER, getDefaultUseISphereJdbcConnectionManager());

        preferenceStore.setDefault(BACKUP_MEMBER_NAME_CURRENT_RULE, getDefaultMemberRenamingRule().getClass().getName());

        IMemberRenamingRuleAdapter[] adapters = MemberRenamingRuleFactory.getInstance().getMemberRenamingRuleAdapters();
        for (IMemberRenamingRuleAdapter adapter : adapters) {
            adapter.initializeDefaultPreferences(preferenceStore);
        }

        preferenceStore.setDefault(BACKUP_MEMBER_NAME_ENABLE_MEMBER_PRECHECK, getDefaultIsMemberRenamingPrecheck());

        preferenceStore.setDefault(COMPARE_FILTER_FILE_EXTENSIONS, getDefaultFileExtensionsAsString());
        preferenceStore.setDefault(COMPARE_FILTER_IMPORT_EXPORT_LOCATION, getDefaultImportExportLocation());

        preferenceStore.setDefault(SYNC_MEMBERS_EDITOR_DETACHED, getDefaultSyncMembersEditorDetached());
        preferenceStore.setDefault(SYNC_MEMBERS_EDITOR_CENTER_ON_SCREEN, getDefaultSyncMembersCenterOnScreen());
        preferenceStore.setDefault(SYNC_MEMBERS_EDITOR_SIDE_BY_SIDE, getDefaultSyncMembersEditorSideBySide());
        preferenceStore.setDefault(SYNC_MEMBERS_FILES_EXCLUDED, getDefaultSyncMembersFilesExcluded());
    }

    /*
     * Preferences: Default Values
     */

    /**
     * Returns the default iSphere library name.
     * 
     * @return default iSphere library name
     */
    public String getDefaultISphereLibrary() {
        return "ISPHERE"; //$NON-NLS-1$
    }

    /**
     * Returns the default asp group.
     * 
     * @return default asp group
     */
    public String getDefaultASPGroup() {
        return "*NONE"; //$NON-NLS-1$
    }

    /**
     * Returns the default host name where to upload the iSphere library.
     * 
     * @return default host name
     */
    public String getDefaultConnectionName() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the default FTP port number.
     * 
     * @return default FTPport number
     */
    public int getDefaultFtpPortNumber() {
        return 21;
    }

    /**
     * Returns the default system ccsid.
     * 
     * @return default system ccsid
     */
    public int getDefaultSystemCcsid() {
        // return com.ibm.as400.access.NLS.localeToCCSID(Locale.getDefault());
        return 273; // keep backward compatibility to previous releases
    }

    /**
     * Returns the default search for updates flag.
     * 
     * @return default search for updates flag.
     */
    public boolean getDefaultSearchForUpdates() {
        return true;
    }

    /**
     * Returns the default 'search for beta versions' flag.
     * 
     * @return default 'search for beta versions' flag.
     */
    public boolean getDefaultSearchForBetaVersions() {
        return false;
    }

    /**
     * Returns the default URL for updates.
     * 
     * @return default URL for updates.
     */
    public String getDefaultURLForUpdates() {
        return "https://rdi-open-source.github.io/isphere/files/MANIFEST.MF";
    }

    /**
     * Returns the default last version for updates.
     * 
     * @return default last version for updates.
     */
    public String getDefaultLastVersionForUpdates() {
        return "0.0.0.r";
    }

    public boolean getDefaultMergeSpooledFileFilters() {
        return false;
    }

    public boolean getDefaultLoadSpooledFilesAsynchronously() {
        return false;
    }

    public String getDefaultSpooledFilesSuggestedFileName() {
        return SPOOLED_FILE_NAME_SIMPLE;
    }

    /**
     * Returns the default format for spooled file conversion on double-click on
     * a spooled file.
     * 
     * @return default format on spooled file double-click
     */
    public String getDefaultSpooledFileConversionDefaultFormat() {
        return IPreferences.OUTPUT_FORMAT_TEXT;
    }

    /**
     * Returns the default value for maximum number of spooled files to load.
     * 
     * @return default maximum number of spooled files to load
     */
    public int getDefaultSpooledFileMaxFilesToLoad() {
        return 5000;
    }

    /**
     * Returns the default spooled file description.
     * 
     * @return default maximum number of spooled files to load
     */
    public String getDefaultSpooledFileRSEDescription() {
        return SpooledFile.VARIABLE_SPLF + " - " + SpooledFile.VARIABLE_STATUS; //$NON-NLS-1$
    }

    /**
     * Returns the default backup member name rule.
     * 
     * @return default backup member name rule to use
     */
    public IMemberRenamingRule getDefaultMemberRenamingRule() {
        IMemberRenamingRule rule = MemberRenamingRuleFactory.getInstance().getMemberRenamingRule(MemberRenamingRuleNumber.class);
        return rule;
    }

    /**
     * Returns the default width of the 'member' column of SearchResultViewer of
     * the iSphere Source File Search.
     * 
     * @return default column width
     */
    // public int getDefaultSourceFileSearchMemberColumnWidth() {
    // return 400;
    // }

    /**
     * Returns the default width of the 'type' column of SearchResultViewer of
     * the iSphere Source File Search.
     * 
     * @return default column width
     */
    // public int getDefaultSourceFileSearchSrcTypeColumnWidth() {
    // return 80;
    // }

    /**
     * Returns the default width of the 'last changed' column of
     * SearchResultViewer of the iSphere Source File Search.
     * 
     * @return default column width
     */
    // public int getDefaultSourceFileSearchLastChangedDateColumnWidth() {
    // return 120;
    // }

    /**
     * Returns the default width of the 'statements count' column of
     * SearchResultViewer of the iSphere Source File Search.
     * 
     * @return default column width
     */
    // public int getDefaultSourceFileSearchStatementsCountColumnWidth() {
    // return 80;
    // }

    /**
     * Returns the default conversion type for spooled file to text conversion.
     * 
     * @return default spooled file to TEXT conversion type
     */
    public String getDefaultSpooledFileConversionText() {
        return IPreferences.SPLF_CONVERSION_TRANSFORM;
    }

    /**
     * Return the default library name for user defined spooled file to text
     * conversion.
     * 
     * @return library name for user defined spooled file to text conversion
     */
    public String getDefaultSpooledFileConversionTextLibrary() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Return the default command for user defined spooled file to text
     * conversion.
     * 
     * @return command for user defined spooled file to text conversion
     */
    public String getDefaultSpooledFileConversionTextCommand() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Return the default for "allow edit" for spooled files converted to text.
     * 
     * @return <code>true</code>, when editing is allowed, else
     *         <code>false</code>.
     */
    public boolean getDefaultSpooledFileConversionTextEditAllowed() {
        return false;
    }

    /**
     * Returns the default conversion type for spooled file to HTML conversion.
     * 
     * @return default spooled file to HTML conversion type
     */
    public String getDefaultSpooledFileConversionHTML() {
        return IPreferences.SPLF_CONVERSION_TRANSFORM;
    }

    /**
     * Return the default library name for user defined spooled file to HTML
     * conversion.
     * 
     * @return library name for user defined spooled file to HTML conversion
     */
    public String getDefaultSpooledFileConversionHTMLLibrary() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Return the default command for user defined spooled file to HTML
     * conversion.
     * 
     * @return command for user defined spooled file to HTML conversion
     */
    public String getDefaultSpooledFileConversionHTMLCommand() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Return the default for "allow edit" for spooled files converted to HTML.
     * 
     * @return <code>true</code>, when editing is allowed, else
     *         <code>false</code>.
     */
    public boolean getDefaultSpooledFileConversionHTMLEditAllowed() {
        return false;
    }

    /**
     * Returns the default conversion type for spooled file to PDF conversion.
     * 
     * @return default spooled file to PDF conversion type
     */
    public String getDefaultSpooledFileConversionPDF() {
        return IPreferences.SPLF_CONVERSION_TRANSFORM;
    }

    /**
     * Return the default library name for user defined spooled file to PDF
     * conversion.
     * 
     * @return library name for user defined spooled file to PDF conversion
     */
    public String getDefaultSpooledFileConversionPDFLibrary() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Return the default command for user defined spooled file to PDF
     * conversion.
     * 
     * @return command for user defined spooled file to PDF conversion
     */
    public String getDefaultSpooledFileConversionPDFCommand() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Return the default page size for spooled file conversion to PDF.
     * 
     * @return default page size for PDF conversion
     */
    public String getDefaultSpooledFilePageSize() {
        return PageSize.PAGE_SIZE_CALCULATE;
    }

    /**
     * Return whether the font size is adjusted to the page size for spooled
     * file conversion to PDF.
     * 
     * @return default for adjusting the font size
     */
    public boolean getDefaultSpooledFileAdjustFontSize() {
        return false;
    }

    /**
     * Return the default command for user defined spooled file to PDF
     * conversion.
     * 
     * @return command for user defined spooled file to PDF conversion
     */
    public int getDefaultDataQueueMaximumMessageLength() {
        return 2048;
    }

    /**
     * Returns the default 'view hex' flag of the data queue view.
     * 
     * @return default 'view hex' flag.
     */
    public boolean getDefaultDataQueueViewInHex() {
        return true;
    }

    /**
     * Returns the default 'display end of data' flag of the data queue view.
     * 
     * @return default 'display end of data' flag.
     */
    public boolean getDefaultDataQueueDisplayEndOfData() {
        return false;
    }

    /**
     * Returns the default 'replacement character' that is used to replace
     * non-displayable characters.
     * 
     * @return default 'replacement character'.
     */
    public String getDefaultDataQueueReplacementCharacter() {
        return "÷";
    }

    /**
     * Returns the default 'number of messages' that are retrieved in the
     * iSphere data queue monitor view.
     * 
     * @return default 'number of messages'.
     */
    public int getDefaultDataQueueNumberOfMessagesToRetrieve() {
        return 10;
    }

    /**
     * Returns the default 'is batch resolve enabled' flag of the source file
     * search.
     * 
     * @return default 'is batch resolve enabled' flag.
     */
    public boolean getDefaultSourceFileSearchBatchResolveEnabled() {
        return false;
    }

    /**
     * Returns the default 'is edit mode' flag of the view search results view.
     * 
     * @return default 'is edit mode' flag.
     */
    public boolean getDefaultSourceFileSearchResultsEditEnabled() {
        return true;
    }

    /**
     * Returns the default 'source file search save path'.
     * 
     * @return default path for saving source file search results
     */
    public String getDefaultSourceFileSearchResultsSaveDirectory() {

        String path = ISpherePlugin.getDefault().getStateLocation().toFile().getAbsolutePath();
        path = path + File.separator + SOURCE_FILE_SEARCH_DIRECTORY + File.separator;

        try {
            FileHelper.ensureDirectory(path);
        } catch (Throwable e) {
            ISpherePlugin.logError("Failed to create directory: " + path, e); //$NON-NLS-1$
        }

        return path;
    }

    /**
     * Returns the default 'save path' that was last selected to save source
     * file search results.
     * 
     * @return default path last used for saving
     */
    public String getDefaultSourceFileSearchResultsLastUsedFileName() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the default 'is auto save' flag of the view search results view.
     * 
     * @return default 'is auto save' flag.
     */
    public boolean getDefaultSourceFileSearchResultsAutoSaveEnabled() {
        return false;
    }

    /**
     * Returns the default 'source file search auto save file name'.
     * 
     * @return default file name for saving search results
     */
    public String getDefaultSourceFileSearchResultsAutoSaveFileName() {

        return "iSphereSourceFileSearchResultAutoSave." + biz.isphere.core.sourcefilesearch.SearchResultManager.FILE_EXTENSION;
    }

    /**
     * Returns the default 'search string' for the iSphere Source File Search.
     * 
     * @return search string
     */
    public String getDefaultSourceFileSearchString() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the default export directory of the iSphere Source File Search
     * option.
     * 
     * @return default directory
     */
    public String getDefaultSourceFileSearchExportDirectory() {
        return FileHelper.getDefaultRootDirectory();
    }

    /**
     * Returns the default 'is edit mode' flag of the view search results view.
     * 
     * @return default 'is edit mode' flag.
     */
    public boolean getDefaultMessageFileSearchResultsEditEnabled() {
        return true;
    }

    /**
     * Returns the default 'message file search save path'.
     * 
     * @return default path for saving message file search results
     */
    public String getDefaultMessageFileSearchResultsSaveDirectory() {

        String path = ISpherePlugin.getDefault().getStateLocation().toFile().getAbsolutePath();
        path = path + File.separator + MESSAGE_FILE_SEARCH_DIRECTORY + File.separator;

        try {
            FileHelper.ensureDirectory(path);
        } catch (Throwable e) {
            ISpherePlugin.logError("Failed to create directory: " + path, e); //$NON-NLS-1$
        }

        return path;
    }

    /**
     * Returns the default 'save path' that was last selected to save message
     * file search results.
     * 
     * @return default path last used for saving
     */
    public String getDefaultMessageFileSearchResultsLastUsedFileName() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the default 'is auto save' flag of the view search results view.
     * 
     * @return default 'is auto save' flag.
     */
    public boolean getDefaultMessageFileSearchResultsAutoSaveEnabled() {
        return false;
    }

    /**
     * Returns the default 'message file search auto save file name'.
     * 
     * @return default file name for saving search results
     */
    public String getDefaultMessageFileSearchResultsAutoSaveFileName() {

        return "iSphereMessageFileSearchResultAutoSave." + biz.isphere.core.messagefilesearch.SearchResultManager.FILE_EXTENSION;
    }

    /**
     * Returns the default 'search string' for the iSphere Message File Search.
     * 
     * @return search string
     */
    public String getDefaultMessageFileSearchString() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the default export directory of the iSphere Message File Search
     * option.
     * 
     * @return default directory
     */
    public String getDefaultMessageFileSearchExportDirectory() {
        return FileHelper.getDefaultRootDirectory();
    }

    /**
     * Returns the default 'is batch resolve enabled' flag of the stream file
     * search.
     * 
     * @return default 'is batch resolve enabled' flag.
     */
    public boolean getDefaultStreamFileSearchBatchResolveEnabled() {
        return false;
    }

    /**
     * Returns the default 'maximum depth' value of the stream file search.
     * 
     * @return default 'maximum depth' value.
     */
    public int getDefaultStreamFileSearchMaxDepth() {
        return 1;
    }

    /**
     * Returns the default 'is edit mode' flag of the view search results view.
     * 
     * @return default 'is edit mode' flag.
     */
    public boolean getDefaultStreamFileSearchResultsEditEnabled() {
        return true;
    }

    /**
     * Returns the default stream file 'search save path'.
     * 
     * @return default path for saving stream file search results
     */
    public String getDefaultStreamFileSearchResultsSaveDirectory() {

        String path = ISpherePlugin.getDefault().getStateLocation().toFile().getAbsolutePath();
        path = path + File.separator + STREAM_FILE_SEARCH_DIRECTORY + File.separator;

        try {
            FileHelper.ensureDirectory(path);
        } catch (Throwable e) {
            ISpherePlugin.logError("Failed to create directory: " + path, e); //$NON-NLS-1$
        }

        return path;
    }

    /**
     * Returns the default 'save path' that was last selected to save stream
     * file search results.
     * 
     * @return default path last used for saving
     */
    public String getDefaultStreamFileSearchResultsLastUsedFileName() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the default 'is auto save' flag of the view search results view.
     * 
     * @return default 'is auto save' flag.
     */
    public boolean getDefaultStreamFileSearchResultsAutoSaveEnabled() {
        return false;
    }

    /**
     * Returns the default stream file search 'auto save file name'.
     * 
     * @return default file name for saving search results
     */
    public String getDefaultStreamFileSearchResultsAutoSaveFileName() {

        return "iSphereStreamFileSearchResultAutoSave." + biz.isphere.core.streamfilesearch.SearchResultManager.FILE_EXTENSION;
    }

    /**
     * Returns the default 'search string' for the iSphere Stream File Search.
     * 
     * @return search string
     */
    public String getDefaultStreamFileSearchString() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Returns the default export directory of the iSphere Stream File Search
     * option.
     * 
     * @return default directory
     */
    public String getDefaultStreamFileSearchExportDirectory() {
        return FileHelper.getDefaultRootDirectory();
    }

    /**
     * Returns the default 'line width' for comparing message files.
     * 
     * @return default line width of first and second level text
     */
    public int getDefaultMessageFileCompareMinLineWidth() {

        return 70;
    }

    public LoadPreviousMemberValue getDefaultSourceMemberCompareLoadingPreviousValuesEnabled() {
        return LoadPreviousMemberValue.CONNECTION_LIBRARY_FILE_MEMBER;
    }

    public LoadPreviousStreamFileValue getDefaultSourceStreamFileCompareLoadingPreviousValuesEnabled() {
        return LoadPreviousStreamFileValue.CONNECTION_DIRECTORY_FILE;
    }

    public boolean getDefaultSourceMemberCompareIgnoreWhiteSpaces() {
        return true;
    }

    public String getDefaultDateFormatLabel() {
        return APPEARANCE_DATE_FORMAT_LOCALE;
    }

    public String getDefaultTimeFormatLabel() {
        return APPEARANCE_TIME_FORMAT_LOCALE;
    }

    public boolean getDefaultFormatResourceDates() {
        return false;
    }

    public boolean getDefaultOpenFilesAfterSaving() {
        return false;
    }

    public int getDefaultAutoRefreshDelay() {
        return 400;
    }

    public int getDefaultAutoRefreshThreshold() {
        return 5000;
    }

    public boolean getDefaultShowErrorLog() {
        return false;
    }

    public boolean getDefaultObjectDecorationExtension() {
        return false;
    }

    public boolean getDefaultSourceMemberDecorationExtension() {
        return false;
    }

    public boolean getDefaultDataMemberDecorationExtension() {
        return false;
    }

    public boolean getDefaultUseISphereJdbcConnectionManager() {
        return false;
    }

    public boolean getDefaultIsMemberRenamingPrecheck() {
        return true;
    }

    public String getDefaultFileExtensionsAsString() {
        return "bnd,c,cle,cbl,cblle,clle,clp,cmd,dspf,lf,menu,mnu,mnucmd,mnudds,pf,pnlgrp,prtf,rexx,rpg,rpgle,sqlc,sqlcbl,sqlcblle,sqlcpp,sqlrpg,sqlrpgle,tbl,txt" //$NON-NLS-1$
            .replaceAll(",", TOKEN_SEPARATOR);
    }

    public String getDefaultImportExportLocation() {
        return "";
    }

    public boolean getDefaultSyncMembersEditorDetached() {
        return true;
    }

    public boolean getDefaultSyncMembersCenterOnScreen() {
        return true;
    }

    public boolean getDefaultSyncMembersEditorSideBySide() {
        return true;
    }

    /*
     * See:
     * https://www.ibm.com/support/pages/why-are-source-files-evftempf01-and-
     * evftempf02-created
     */
    public String getDefaultSyncMembersFilesExcluded() {
        return "EVFEVENT,EVFTEMPF01,EVFTEMPF02".replaceAll(",", TOKEN_SEPARATOR); //$NON-NLS-1$
    }

    /*
     * Preferences: Save Values
     */

    private void saveFileExtensions(String[] anExtensions) {
        saveStringArray(COMPARE_FILTER_FILE_EXTENSIONS, anExtensions);
        fileExtensionsSet = null;
    }

    private void saveImportExportLocation(String aLocation) {
        if (new File(aLocation).exists()) {
            preferenceStore.setValue(COMPARE_FILTER_IMPORT_EXPORT_LOCATION, aLocation);
        }
    }

    private HashSet<String> getOrCreateFileExtensionsSet() {
        if (fileExtensionsSet == null) {
            fileExtensionsSet = new HashSet<String>(Arrays.asList(getFileExtensions(true)));
        }
        return fileExtensionsSet;
    }

    private String[] getFileExtensions(boolean anUpperCase) {
        return loadStringArray(COMPARE_FILTER_FILE_EXTENSIONS, anUpperCase);
    }

    private void saveFilesExcluded(String[] aFilesExcludedList) {
        saveStringArray(SYNC_MEMBERS_FILES_EXCLUDED, aFilesExcludedList);
    }

    private String[] getFilesExcluded() {
        return loadStringArray(SYNC_MEMBERS_FILES_EXCLUDED, true);
    }

    private void saveStringArray(String aKey, String[] aStringArray) {
        preferenceStore.setValue(aKey, StringHelper.concatTokens(aStringArray, TOKEN_SEPARATOR));
    }

    private String[] loadStringArray(String aKey, boolean anUpperCase) {
        String tList = preferenceStore.getString(aKey);
        if (anUpperCase) {
            return StringHelper.getTokens(tList.toUpperCase(), TOKEN_SEPARATOR);
        } else {
            return StringHelper.getTokens(tList, TOKEN_SEPARATOR);
        }
    }

    /**
     * Returns an arrays of maximum lengths values for retrieving data queue
     * entries.
     * 
     * @return message length values
     */
    public int[] getDataQueueMaximumMessageLengthValues() {

        int[] lengths = new int[6];
        lengths[0] = -1;
        lengths[1] = 64;
        lengths[2] = 512;
        lengths[3] = 2048;
        lengths[4] = 8196;
        lengths[5] = MessageLengthAction.MAX_LENGTH;

        Arrays.sort(lengths);
        if (Arrays.binarySearch(lengths, getDataQueueMaximumMessageLength()) < 0) {
            lengths[0] = getDataQueueMaximumMessageLength();
            Arrays.sort(lengths);
            return lengths;
        }

        int[] lengths2 = new int[lengths.length - 1];
        System.arraycopy(lengths, 1, lengths2, 0, lengths2.length);
        return lengths2;
    }

    public SimpleDateFormat getDateFormatter() {

        String pattern = getDateFormatPattern();
        if (pattern == null) {
            pattern = getDateFormatsMap().get(getDefaultDateFormatLabel());
        }

        if (pattern == null) {
            return new SimpleDateFormat(FastDateFormat.getDateInstance(FastDateFormat.SHORT).getPattern());
        }

        return new SimpleDateFormat(pattern);
    }

    public String getDateFormatPattern() {
        return getDateFormatsMap().get(getDateFormatLabel());
    }

    public String[] getSpooledFileAllowEditLabels() {

        return new String[] { Messages.Label_Viewer, Messages.Label_Editor };
    }

    public String[] getDateFormatLabels() {

        Set<String> formats = getDateFormatsMap().keySet();

        String[] dateFormats = formats.toArray(new String[formats.size()]);
        Arrays.sort(dateFormats);

        return dateFormats;
    }

    private Map<String, String> getDateFormatsMap() {

        if (dateFormats != null) {
            return dateFormats;
        }

        dateFormats = new HashMap<String, String>();

        dateFormats.put(getDefaultDateFormatLabel(), null);
        dateFormats.put("de (dd.mm.yyyy)", "dd.MM.yyyy");
        dateFormats.put("us (mm/dd/yyyy)", "MM/dd/yyyy");
        dateFormats.put("iso (yyyy.mm.dd)", "yyyy.MM.dd");

        return dateFormats;
    }

    public SimpleDateFormat getTimeFormatter() {

        String pattern = getTimeFormatPattern();
        if (pattern == null) {
            pattern = getTimeFormatsMap().get(getDefaultTimeFormatLabel());
        }

        if (pattern == null) {
            return new SimpleDateFormat(FastDateFormat.getTimeInstance(FastDateFormat.SHORT).getPattern());
        }

        return new SimpleDateFormat(pattern);
    }

    public String getTimeFormatPattern() {
        return getTimeFormatsMap().get(getTimeFormatLabel());
    }

    public String[] getTimeFormatLabels() {

        Set<String> formats = getTimeFormatsMap().keySet();

        String[] timeFormats = formats.toArray(new String[formats.size()]);
        Arrays.sort(timeFormats);

        return timeFormats;
    }

    public String[] getSpooledFileSuggestedNames() {

        Set<String> names = getSpooledFileSuggestedNamesMap().keySet();

        String[] suggestedNames = names.toArray(new String[names.size()]);
        Arrays.sort(suggestedNames);

        return suggestedNames;
    }

    public String getQualifiedSpooledFileName() {
        return getSpooledFileSuggestedNamesMap().get(SPOOLED_FILE_NAME_QUALIFIED);
    }

    public String getSuggestedSpooledFileName() {

        String key = getSpooledFilesSuggestedFileName();
        if (!getSpooledFileSuggestedNamesMap().containsKey(key)) {
            return key;
        }

        return getSpooledFileSuggestedNamesMap().get(key);
    }

    public String[] getDataQueueNumberOfMessagesToRetrieveItems() {
        return new String[] { "1", "5", "10", "50", "100" };
    }

    private Map<String, String> getTimeFormatsMap() {

        if (timeFormats != null) {
            return timeFormats;
        }

        timeFormats = new HashMap<String, String>();

        timeFormats.put(getDefaultDateFormatLabel(), null);
        timeFormats.put("de (hh:mm:ss)", "HH:mm:ss"); //$NON-NLS-1$
        timeFormats.put("us (hh:mm:ss AM/PM)", "KK:mm:ss a"); //$NON-NLS-1$
        timeFormats.put("iso (hh.mm.ss)", "HH.mm.ss");

        return timeFormats;
    }

    private Map<String, String> getSpooledFileSuggestedNamesMap() {

        if (suggestedSpooledFileNames != null) {
            return suggestedSpooledFileNames;
        }

        final String UNDERSCORE = "_"; //$NON-NLS-1$

        suggestedSpooledFileNames = new HashMap<String, String>();

        suggestedSpooledFileNames.put(SPOOLED_FILE_NAME_DEFAULT, "spooled_file"); //$NON-NLS-1$
        suggestedSpooledFileNames.put(SPOOLED_FILE_NAME_SIMPLE, SpooledFile.VARIABLE_SPLF);
        suggestedSpooledFileNames.put(SPOOLED_FILE_NAME_QUALIFIED,
            SpooledFile.VARIABLE_SPLF + UNDERSCORE + SpooledFile.VARIABLE_SPLFNBR + UNDERSCORE + SpooledFile.VARIABLE_JOBNBR + UNDERSCORE
                + SpooledFile.VARIABLE_JOBUSR + UNDERSCORE + SpooledFile.VARIABLE_JOBNAME + UNDERSCORE + SpooledFile.VARIABLE_JOBSYS);

        return suggestedSpooledFileNames;
    }

    private String getShowWarningKey(String showWarningKey) {
        return WARNING_BASE_KEY + showWarningKey;
    }
}