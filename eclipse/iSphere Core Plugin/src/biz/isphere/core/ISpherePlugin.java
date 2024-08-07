/*******************************************************************************
 * Copyright (c) 2012-2024 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import com.ibm.as400.access.AS400;

import biz.isphere.base.internal.UIHelper;
import biz.isphere.base.versioncheck.PluginCheck;
import biz.isphere.core.dataspaceeditordesigner.repository.DataSpaceEditorRepository;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.internal.IEditor;
import biz.isphere.core.internal.IMessageFileSearchObjectFilterCreator;
import biz.isphere.core.internal.ISourceFileSearchMemberFilterCreator;
import biz.isphere.core.internal.IStreamFileEditor;
import biz.isphere.core.internal.IStreamFileSearchIFSFilterCreator;
import biz.isphere.core.internal.SearchForUpdates;
import biz.isphere.core.internal.api.retrieveproductinformation.PRDI0100;
import biz.isphere.core.internal.api.retrieveproductinformation.PRDR0100;
import biz.isphere.core.internal.api.retrieveproductinformation.QSZRTVPR;
import biz.isphere.core.preferences.Preferences;
import biz.isphere.core.search.ISearchArgumentsListEditorProvider;
import biz.isphere.core.swt.widgets.WidgetFactory;

public class ISpherePlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "biz.isphere.core"; //$NON-NLS-1$

    // WDSCi 7.0 = Eclipse 3.2
    private static final Version WDSCI70 = new Version(3, 2, 0);

    private static final String MIN_SERVER_VERSION = "6.0.1"; //$NON-NLS-1$

    private static ISpherePlugin plugin;
    private static URL installURL;
    private static Set<String> loggedCommand = new HashSet<String>();

    public static IEditor editor = null;
    public static IStreamFileEditor streamFileEditor = null;
    public static ISourceFileSearchMemberFilterCreator sourceFileSearchMemberFilterCreator = null;
    public static IMessageFileSearchObjectFilterCreator messageFileSearchObjectFilterCreator = null;
    public static IStreamFileSearchIFSFilterCreator streamFileSearchIFSFilterCreator = null;
    private static String ISPHERE_SPOOLED_FILES_PROJECT_NAME = "iSphereSpooledFiles";
    private static String ECLIPSE_PROJECT_FILE_NAME = ".project";

    private File spooledFilesDirectory;
    private IProject spooledFilesProject;

    public static final String IMAGE_ERROR = "error.gif";
    public static final String IMAGE_NEW = "new.gif";
    public static final String IMAGE_CHANGE = "change.gif";
    public static final String IMAGE_COPY = "copy.gif";
    public static final String IMAGE_DELETE = "delete.gif";
    public static final String IMAGE_DISPLAY = "display.gif";
    public static final String IMAGE_REFRESH = "refresh.gif";
    public static final String IMAGE_MESSAGE_FILE = "message_file.gif";
    public static final String IMAGE_MESSAGE = "message.gif";
    public static final String IMAGE_DATA_AREA = "data_area.gif";
    public static final String IMAGE_USER_SPACE = "user_space.gif";
    public static final String IMAGE_COMPARE = "compare.gif";
    public static final String IMAGE_BINDING_DIRECTORY = "binding_directory.gif";
    public static final String IMAGE_MINUS = "minus.gif";
    public static final String IMAGE_MINUS_MINUS = "minus_minus.gif";
    public static final String IMAGE_SOURCE_FILE_SEARCH = "source_file_search.gif";
    public static final String IMAGE_MESSAGE_FILE_SEARCH = "message_file_search.png";
    public static final String IMAGE_SELECT_ALL = "select_all.gif";
    public static final String IMAGE_DESELECT_ALL = "deselect_all.gif";
    public static final String IMAGE_OPEN_EDITOR = "open_editor.gif";
    public static final String IMAGE_SPOOLED_FILE = "spooled_file.gif";
    public static final String IMAGE_SPOOLED_FILE_FILTER = "spooled_file_filter.gif";
    public static final String IMAGE_EXCEL = "excel.png";
    public static final String IMAGE_MEMBER_FILTER = "member_filter.gif";
    public static final String IMAGE_OBJECT_FILTER = "object_filter.gif";
    public static final String IMAGE_IFS_FILTER = "ifs_filter.gif";
    public static final String IMAGE_SWITCH_MEMBER = "switch_member.gif";
    public static final String IMAGE_EXPAND_ALL = "expandall.gif";
    public static final String IMAGE_COLLAPSE_ALL = "collapseall.gif";
    public static final String IMAGE_NEW_DIALOG = "newdialog.gif";
    public static final String IMAGE_ADD_DATA_AREA = "add_data_area.gif";
    public static final String IMAGE_ADD_USER_SPACE = "add_user_space.gif";
    public static final String IMAGE_REMOVE_DATA_SPACE = "remove_data_space.gif";
    public static final String IMAGE_WATCHING = "watching.gif";
    public static final String IMAGE_DATA_AREA_MONITOR = "data_monitor.gif";
    public static final String IMAGE_USER_SPACE_MONITOR = "data_monitor.gif";
    public static final String IMAGE_DATA_QUEUE_MONITOR = "data_monitor.gif";
    public static final String IMAGE_VALUE_CHANGED = "value_changed.gif";
    public static final String IMAGE_SYSTEM_HELP = "systemhelp.gif";
    public static final String IMAGE_AUTO_REFRESH_OFF = "auto_refresh_off.gif";
    public static final String IMAGE_UP = "up.gif";
    public static final String IMAGE_DOWN = "down.gif";
    public static final String IMAGE_RENAME = "rename.gif";
    public static final String IMAGE_PIN = "pin.gif";
    public static final String IMAGE_REMOVE = "remove.gif";
    public static final String IMAGE_REMOVE_ALL = "removeAll.gif";
    public static final String IMAGE_INVERT_SELECTION = "invert_selection.gif";
    public static final String IMAGE_TRANSFER_LIBRARY_32 = "transfer_library_32.gif";
    public static final String IMAGE_KEY = "key.gif";
    public static final String IMAGE_VIEW_IN_HEX = "view_as_hex.gif";
    public static final String IMAGE_EDIT_DISABLED = "edit_disabled.gif";
    public static final String IMAGE_OPEN_VIEWER = "open_viewer.gif";
    public static final String IMAGE_SAVE = "save.gif";
    public static final String IMAGE_SAVE_ALL = "save_all.gif";
    public static final String IMAGE_COPY_TO_CLIPBOARD = "copy_to_clipboard.gif";
    public static final String IMAGE_COPY_LEFT = "copy_left.png";
    public static final String IMAGE_COPY_RIGHT = "copy_right.png";
    public static final String IMAGE_COPY_NOT_EQUAL = "copy_not_equal.png";
    public static final String IMAGE_COPY_EQUAL = "copy_equal.png";
    public static final String IMAGE_FILTERED_ITEMS = "filtered_items.png";
    public static final String IMAGE_COMPARE_MESSAGE_FILES = "compare_message_files.png";
    public static final String IMAGE_OPEN = "open.png";
    public static final String IMAGE_COPY_MEMBERS_TO = "copy_members_to.png";
    public static final String IMAGE_KEY_WARNING = "key_warning.gif";
    public static final String IMAGE_MESSAGE_WARNING = "message_warning.gif";
    public static final String IMAGE_SEND_MESSAGE = "send_message.png";
    public static final String IMAGE_LOCKED = "locked.png";
    public static final String IMAGE_UNLOCKED = "unlocked.png";
    public static final String IMAGE_DISPLAY_MODULE_VIEW = "module_view.gif";
    public static final String IMAGE_RETRIEVE_BINDER_SOURCE = "retrieve_binder_source.gif";
    public static final String IMAGE_SYSTEM = "system.gif";
    public static final String IMAGE_LIBRARY = "library.gif";
    public static final String IMAGE_CURRENT_LIBRARY = "current_library.gif";
    public static final String IMAGE_LIBRARY_LIST = "library_list.gif";
    public static final String IMAGE_SYNCHRONIZE_MEMBERS = "synchronize_members.gif";
    public static final String IMAGE_PHYSICAL_FILE = "physicalfile.gif";
    public static final String IMAGE_SOURCE_FILE = "sourcefile.gif";

    private static boolean searchArgumentsListEditor = false;
    private static ISearchArgumentsListEditorProvider searchArgumentsListEditorProvider = null;

    private static boolean saveNeededHandling = false;

    public ISpherePlugin() {
        super();
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {

        super.start(context);

        installURL = context.getBundle().getEntry("/");

        spooledFilesDirectory = new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString(), ISPHERE_SPOOLED_FILES_PROJECT_NAME);

        try {

            spooledFilesProject = ResourcesPlugin.getWorkspace().getRoot().getProject(ISPHERE_SPOOLED_FILES_PROJECT_NAME);
            if (spooledFilesProject.exists()) {
                File projectConfigFile = new File(getSpooledFilesDirectory(), ECLIPSE_PROJECT_FILE_NAME);
                if (!spooledFilesDirectory.exists() || !projectConfigFile.exists()) {
                    spooledFilesProject.delete(true, true, null);
                }
            }

            if (!spooledFilesProject.exists()) {
                IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(spooledFilesProject.getName());
                spooledFilesProject.create(description, null);
            }

        } catch (Throwable e) {
            logError("*** Could not create '" + ISPHERE_SPOOLED_FILES_PROJECT_NAME + "' project ***", e);
        }

        if (!spooledFilesDirectory.exists()) {
            spooledFilesDirectory.mkdirs();
        }

        if (Preferences.getInstance().isSearchForUpdates()) {
            SearchForUpdates search = new SearchForUpdates();
            search.setUser(false);
            search.schedule();
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {

        super.stop(context);

        File[] files = getSpooledFilesDirectory().listFiles();
        for (int idx = 0; idx < files.length; idx++) {
            if (!files[idx].getName().equals(ECLIPSE_PROJECT_FILE_NAME)) {
                files[idx].delete();
            }
        }

        Preferences.dispose();
        DataSpaceEditorRepository.dispose();
        WidgetFactory.dispose();

    }

    public static ISpherePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the name of the plugin, as assigned to "Bundle-Name" in
     * "MANIFEST.MF".
     * 
     * @return Name of the plugin.
     */
    public String getName() {
        String name = (String)getBundle().getHeaders().get(Constants.BUNDLE_NAME);
        if (name == null) {
            name = "";
        }
        return name;
    }

    /**
     * Returns the version of the plugin, as assigned to "Bundle-Version" in
     * "MANIFEST.MF".
     * 
     * @return Version of the plugin.
     */
    public String getVersion() {
        String version = (String)getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        if (version == null) {
            version = "0.0.0";
        }
        return version;
    }

    /**
     * Returns the version of the plugin, as assigned to "Bundle-Version" in
     * "MANIFEST.MF" formatted as "vvrrmm".
     * 
     * @return Version of the plugin.
     */
    public String getMinServerVersion() {
        return MIN_SERVER_VERSION;
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        super.initializeImageRegistry(reg);
        reg.put(IMAGE_ERROR, getImageDescriptor(IMAGE_ERROR));
        reg.put(IMAGE_NEW, getImageDescriptor(IMAGE_NEW));
        reg.put(IMAGE_CHANGE, getImageDescriptor(IMAGE_CHANGE));
        reg.put(IMAGE_COPY, getImageDescriptor(IMAGE_COPY));
        reg.put(IMAGE_DELETE, getImageDescriptor(IMAGE_DELETE));
        reg.put(IMAGE_DISPLAY, getImageDescriptor(IMAGE_DISPLAY));
        reg.put(IMAGE_REFRESH, getImageDescriptor(IMAGE_REFRESH));
        reg.put(IMAGE_MESSAGE_FILE, getImageDescriptor(IMAGE_MESSAGE_FILE));
        reg.put(IMAGE_MESSAGE, getImageDescriptor(IMAGE_MESSAGE));
        reg.put(IMAGE_DATA_AREA, getImageDescriptor(IMAGE_DATA_AREA));
        reg.put(IMAGE_USER_SPACE, getImageDescriptor(IMAGE_USER_SPACE));
        reg.put(IMAGE_COMPARE, getImageDescriptor(IMAGE_COMPARE));
        reg.put(IMAGE_BINDING_DIRECTORY, getImageDescriptor(IMAGE_BINDING_DIRECTORY));
        reg.put(IMAGE_MINUS, getImageDescriptor(IMAGE_MINUS));
        reg.put(IMAGE_MINUS_MINUS, getImageDescriptor(IMAGE_MINUS_MINUS));
        reg.put(IMAGE_SOURCE_FILE_SEARCH, getImageDescriptor(IMAGE_SOURCE_FILE_SEARCH));
        reg.put(IMAGE_MESSAGE_FILE_SEARCH, getImageDescriptor(IMAGE_MESSAGE_FILE_SEARCH));
        reg.put(IMAGE_SELECT_ALL, getImageDescriptor(IMAGE_SELECT_ALL));
        reg.put(IMAGE_DESELECT_ALL, getImageDescriptor(IMAGE_DESELECT_ALL));
        reg.put(IMAGE_OPEN_EDITOR, getImageDescriptor(IMAGE_OPEN_EDITOR));
        reg.put(IMAGE_SPOOLED_FILE, getImageDescriptor(IMAGE_SPOOLED_FILE));
        reg.put(IMAGE_SPOOLED_FILE_FILTER, getImageDescriptor(IMAGE_SPOOLED_FILE_FILTER));
        reg.put(IMAGE_EXCEL, getImageDescriptor(IMAGE_EXCEL));
        reg.put(IMAGE_MEMBER_FILTER, getImageDescriptor(IMAGE_MEMBER_FILTER));
        reg.put(IMAGE_OBJECT_FILTER, getImageDescriptor(IMAGE_OBJECT_FILTER));
        reg.put(IMAGE_IFS_FILTER, getImageDescriptor(IMAGE_IFS_FILTER));
        reg.put(IMAGE_SWITCH_MEMBER, getImageDescriptor(IMAGE_SWITCH_MEMBER));
        reg.put(IMAGE_EXPAND_ALL, getImageDescriptor(IMAGE_EXPAND_ALL));
        reg.put(IMAGE_COLLAPSE_ALL, getImageDescriptor(IMAGE_COLLAPSE_ALL));
        reg.put(IMAGE_NEW_DIALOG, getImageDescriptor(IMAGE_NEW_DIALOG));
        reg.put(IMAGE_ADD_DATA_AREA, getImageDescriptor(IMAGE_ADD_DATA_AREA));
        reg.put(IMAGE_ADD_USER_SPACE, getImageDescriptor(IMAGE_ADD_USER_SPACE));
        reg.put(IMAGE_REMOVE_DATA_SPACE, getImageDescriptor(IMAGE_REMOVE_DATA_SPACE));
        reg.put(IMAGE_WATCHING, getImageDescriptor(IMAGE_WATCHING));
        reg.put(IMAGE_DATA_AREA_MONITOR, getImageDescriptor(IMAGE_DATA_AREA_MONITOR));
        reg.put(IMAGE_USER_SPACE_MONITOR, getImageDescriptor(IMAGE_USER_SPACE_MONITOR));
        reg.put(IMAGE_DATA_QUEUE_MONITOR, getImageDescriptor(IMAGE_DATA_QUEUE_MONITOR));
        reg.put(IMAGE_VALUE_CHANGED, getImageDescriptor(IMAGE_VALUE_CHANGED));
        reg.put(IMAGE_SYSTEM_HELP, getImageDescriptor(IMAGE_SYSTEM_HELP));
        reg.put(IMAGE_AUTO_REFRESH_OFF, getImageDescriptor(IMAGE_AUTO_REFRESH_OFF));
        reg.put(IMAGE_UP, getImageDescriptor(IMAGE_UP));
        reg.put(IMAGE_DOWN, getImageDescriptor(IMAGE_DOWN));
        reg.put(IMAGE_RENAME, getImageDescriptor(IMAGE_RENAME));
        reg.put(IMAGE_PIN, getImageDescriptor(IMAGE_PIN));
        reg.put(IMAGE_REMOVE, getImageDescriptor(IMAGE_REMOVE));
        reg.put(IMAGE_REMOVE_ALL, getImageDescriptor(IMAGE_REMOVE_ALL));
        reg.put(IMAGE_INVERT_SELECTION, getImageDescriptor(IMAGE_INVERT_SELECTION));
        reg.put(IMAGE_TRANSFER_LIBRARY_32, getImageDescriptor(IMAGE_TRANSFER_LIBRARY_32));
        reg.put(IMAGE_KEY, getImageDescriptor(IMAGE_KEY));
        reg.put(IMAGE_VIEW_IN_HEX, getImageDescriptor(IMAGE_VIEW_IN_HEX));
        reg.put(IMAGE_EDIT_DISABLED, getImageDescriptor(IMAGE_EDIT_DISABLED));
        reg.put(IMAGE_OPEN_VIEWER, getImageDescriptor(IMAGE_OPEN_VIEWER));
        reg.put(IMAGE_SAVE, getImageDescriptor(IMAGE_SAVE));
        reg.put(IMAGE_SAVE_ALL, getImageDescriptor(IMAGE_SAVE_ALL));
        reg.put(IMAGE_COPY_TO_CLIPBOARD, getImageDescriptor(IMAGE_COPY_TO_CLIPBOARD));
        reg.put(IMAGE_COPY_LEFT, getImageDescriptor(IMAGE_COPY_LEFT));
        reg.put(IMAGE_COPY_RIGHT, getImageDescriptor(IMAGE_COPY_RIGHT));
        reg.put(IMAGE_COPY_NOT_EQUAL, getImageDescriptor(IMAGE_COPY_NOT_EQUAL));
        reg.put(IMAGE_COPY_EQUAL, getImageDescriptor(IMAGE_COPY_EQUAL));
        reg.put(IMAGE_FILTERED_ITEMS, getImageDescriptor(IMAGE_FILTERED_ITEMS));
        reg.put(IMAGE_COMPARE_MESSAGE_FILES, getImageDescriptor(IMAGE_COMPARE_MESSAGE_FILES));
        reg.put(IMAGE_OPEN, getImageDescriptor(IMAGE_OPEN));
        reg.put(IMAGE_COPY_MEMBERS_TO, getImageDescriptor(IMAGE_COPY_MEMBERS_TO));
        reg.put(IMAGE_KEY_WARNING, getImageDescriptor(IMAGE_KEY_WARNING));
        reg.put(IMAGE_MESSAGE_WARNING, getImageDescriptor(IMAGE_MESSAGE_WARNING));
        reg.put(IMAGE_SEND_MESSAGE, getImageDescriptor(IMAGE_SEND_MESSAGE));
        reg.put(IMAGE_LOCKED, getImageDescriptor(IMAGE_LOCKED));
        reg.put(IMAGE_UNLOCKED, getImageDescriptor(IMAGE_UNLOCKED));
        reg.put(IMAGE_DISPLAY_MODULE_VIEW, getImageDescriptor(IMAGE_DISPLAY_MODULE_VIEW));
        reg.put(IMAGE_RETRIEVE_BINDER_SOURCE, getImageDescriptor(IMAGE_RETRIEVE_BINDER_SOURCE));
        reg.put(IMAGE_SYSTEM, getImageDescriptor(IMAGE_SYSTEM));
        reg.put(IMAGE_LIBRARY, getImageDescriptor(IMAGE_LIBRARY));
        reg.put(IMAGE_CURRENT_LIBRARY, getImageDescriptor(IMAGE_CURRENT_LIBRARY));
        reg.put(IMAGE_LIBRARY_LIST, getImageDescriptor(IMAGE_LIBRARY_LIST));
        reg.put(IMAGE_SYNCHRONIZE_MEMBERS, getImageDescriptor(IMAGE_SYNCHRONIZE_MEMBERS));
        reg.put(IMAGE_PHYSICAL_FILE, getImageDescriptor(IMAGE_PHYSICAL_FILE));
        reg.put(IMAGE_SOURCE_FILE, getImageDescriptor(IMAGE_SOURCE_FILE));
    }

    public static ImageDescriptor getImageDescriptor(String name) {
        String iconPath = "icons/";
        try {
            URL url = new URL(installURL, iconPath + name);
            return ImageDescriptor.createFromURL(url);
        } catch (MalformedURLException e) {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    public Image getImage(String name) {
        return getImageRegistry().getDescriptor(name).createImage();
    }

    public static URL getInstallURL() {
        return installURL;
    }

    public static String getISphereLibrary(String connectionName) {
        String library = IBMiHostContributionsHandler.getISphereLibrary(connectionName);
        return library;
    }

    public static String getISphereLibrary() {
        String library = Preferences.getInstance().getISphereLibrary(); // CHECKED
        return library;
    }

    public static IEditor getEditor() {
        return editor;
    }

    public static void setEditor(IEditor _editor) {
        editor = _editor;
    }

    public static IStreamFileEditor getStreamFileEditor() {
        return streamFileEditor;
    }

    public static void setStreamFileEditor(IStreamFileEditor _streamFileEditor) {
        streamFileEditor = _streamFileEditor;
    }

    public static ISourceFileSearchMemberFilterCreator getSourceFileSearchMemberFilterCreator() {
        return sourceFileSearchMemberFilterCreator;
    }

    public static void setSourceFileSearchMemberFilterCreator(ISourceFileSearchMemberFilterCreator _sourceFileSearchMemberFilterCreator) {
        sourceFileSearchMemberFilterCreator = _sourceFileSearchMemberFilterCreator;
    }

    public static IMessageFileSearchObjectFilterCreator getMessageFileSearchObjectFilterCreator() {
        return messageFileSearchObjectFilterCreator;
    }

    public static void setMessageFileSearchObjectFilterCreator(IMessageFileSearchObjectFilterCreator _messageFileSearchObjectFilterCreator) {
        messageFileSearchObjectFilterCreator = _messageFileSearchObjectFilterCreator;
    }

    public static IStreamFileSearchIFSFilterCreator getStreamFileSearchIFSFilterCreator() {
        return streamFileSearchIFSFilterCreator;
    }

    public static void setStreamFileSearchIFSFilterCreator(IStreamFileSearchIFSFilterCreator _streamFileSearchIFSFilterCreator) {
        streamFileSearchIFSFilterCreator = _streamFileSearchIFSFilterCreator;
    }

    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    public File getSpooledFilesDirectory() {
        return spooledFilesDirectory;
    }

    public IProject getSpooledFilesProject() {
        return spooledFilesProject;
    }

    /**
     * Convenience method to log error messages to the application log.
     * 
     * @param message Message
     * @param e The exception that has produced the error
     */
    public static void logError(String message, Throwable e) {
        if (plugin == null) {
            System.err.println(message);
            if (e != null) {
                e.printStackTrace();
            }
            return;
        }
        plugin.getLog().log(new Status(Status.ERROR, PLUGIN_ID, Status.ERROR, message, e));
        showErrorLog(false);
    }

    /**
     * Convenience method to log error messages to the application log. Only the
     * first occurrence of the message is logged.
     * 
     * @param message Message
     * @param e The exception that has produced the error
     */
    public static void logErrorOnce(String message, Throwable e) {

        message = message + " (logged only once)"; // $NON-NLS-1$

        if (!loggedCommand.contains(message)) {
            logError(message, e);
            loggedCommand.add(message);
        }
    }

    public static void showErrorLog() {
        showErrorLog(true);
    }

    private static void showErrorLog(final boolean logError) {

        if (!Preferences.getInstance().isShowErrorLog()) {
            return;
        }

        UIJob job = new UIJob("") {

            @Override
            public IStatus runInUIThread(IProgressMonitor arg0) {

                try {

                    final String ERROR_LOG_VIEW = "org.eclipse.pde.runtime.LogView"; // $NON-NLS-1$

                    IWorkbenchPage activePage = UIHelper.getActivePage();
                    if (activePage != null) {
                        if (activePage.findView(ERROR_LOG_VIEW) == null) {
                            activePage.showView(ERROR_LOG_VIEW);
                        }
                    }

                } catch (Throwable e) {
                    if (logError) {
                        logError("*** Could not open error log view ***", e); //$NON-NLS-1$
                    }
                }

                return Status.OK_STATUS;
            }
        };

        job.schedule();
    }

    public static boolean isSearchArgumentsListEditor() {
        return searchArgumentsListEditor;
    }

    public static void setSearchArgumentsListEditor(boolean searchArgumentsListEditor) {
        ISpherePlugin.searchArgumentsListEditor = searchArgumentsListEditor;
    }

    public static ISearchArgumentsListEditorProvider getSearchArgumentsListEditorProvider() {
        return searchArgumentsListEditorProvider;
    }

    public static void setSearchArgumentsListEditorProvider(ISearchArgumentsListEditorProvider searchArgumentsListEditorProvider) {
        ISpherePlugin.searchArgumentsListEditorProvider = searchArgumentsListEditorProvider;
    }

    public boolean isWDSCiDevelomentEnvironment() {

        Version platformVersion = PluginCheck.getPlatformVersion();
        if (platformVersion.getMajor() <= WDSCI70.getMajor()) {
            if (platformVersion.getMinor() <= WDSCI70.getMinor()) {
                return true;
            }
        }

        return false;
    }

    public String getIBMiRelease(String connectionName) {
        return getIBMiRelease(IBMiHostContributionsHandler.getSystem(connectionName));
    }

    public String getIBMiRelease(AS400 system) {

        try {

            PRDI0100 prdi0100 = new PRDI0100(system);
            PRDR0100 prdr0100 = new PRDR0100(system);
            QSZRTVPR main = new QSZRTVPR(system);
            if (!main.execute(prdr0100, prdi0100)) {
                logError(main.getMessageList()[0].getText(), null);
                return "V0R0M0";
            }

            return prdr0100.getReleaseLevel();

        } catch (Throwable e) {
            logError(e.getLocalizedMessage(), e);
        }

        return "V0R0M0";
    }

    public static boolean isSaveNeededHandling() {
        return saveNeededHandling;
    }

    public static void setSaveNeededHandling(boolean _saveNeededHandling) {
        saveNeededHandling = _saveNeededHandling;
    }

}
