/*******************************************************************************
 * Copyright (c) 2012-2024 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.internal;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.CharacterDataArea;
import com.ibm.as400.access.CommandCall;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.IllegalObjectTypeException;
import com.ibm.as400.access.Job;
import com.ibm.as400.access.ObjectDescription;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ObjectList;
import com.ibm.as400.access.QSYSObjectPathName;
import com.ibm.as400.access.RequestNotSupportedException;

import biz.isphere.base.internal.ExceptionHelper;
import biz.isphere.base.internal.IntHelper;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.annotations.CMOne;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.internal.api.retrievememberdescription.MBRD0100;
import biz.isphere.core.internal.api.retrievememberdescription.QUSRMBRD;

public class ISphereHelper {

    private static final String ASP_GROUP_NONE_VALUE = "*NONE"; //$NON-NLS-1$

    public static String getISphereLibraryVersion(AS400 as400, String library) {

        String dataAreaISphereContent = readISphereDataAreaChecked(as400, library);
        if (dataAreaISphereContent == null) {
            return null;
        }

        String libraryVersion = retrieveServerVersion(dataAreaISphereContent);
        if (libraryVersion == null) {
            return null;
        }

        return getVersionFormatted(libraryVersion);
    }

    public static String getISphereLibraryBuildDate(AS400 as400, String library) {

        String dataAreaISphereContent = readISphereDataAreaChecked(as400, library);
        if (dataAreaISphereContent == null) {
            return null;
        }

        String buildDate = retrieveBuildDate(dataAreaISphereContent);
        if (buildDate == null || buildDate.trim().length() == 0) {
            return null;
        }

        return buildDate;
    }

    public static String checkISphereLibrary(String connectionName) {
        if (IBMiHostContributionsHandler.isOffline(connectionName)) {
            return Messages.bind(Messages.Connection_A_does_not_exist_or_is_currently_offline_and_cannot_be_connected, connectionName);
        }
        AS400 as400 = IBMiHostContributionsHandler.getSystem(connectionName);
        if (as400 == null) {
            return Messages.bind(Messages.Connection_A_does_not_exist_or_is_currently_offline_and_cannot_be_connected, connectionName);
        }
        return checkISphereLibraryWithMessage(as400, ISpherePlugin.getISphereLibrary(connectionName));
    }

    public static boolean checkISphereLibrary(Shell shell, String connectionName) {
        if (IBMiHostContributionsHandler.isOffline(connectionName)) {
            if (shell != null) {
                new DisplayMessage(shell, Messages.Error,
                    Messages.bind(Messages.Connection_A_does_not_exist_or_is_currently_offline_and_cannot_be_connected, connectionName)).start();
            }
            return false;
        }
        AS400 as400 = IBMiHostContributionsHandler.getSystem(connectionName);
        return checkISphereLibrary(shell, as400, ISpherePlugin.getISphereLibrary(connectionName));
    }

    @CMOne(info = "This method is used by CMOne by MessageFileEditor.openEditor()")
    public static boolean checkISphereLibrary(Shell shell, AS400 as400) {
        return checkISphereLibrary(shell, as400, ISpherePlugin.getISphereLibrary()); // CHECKED
    }

    public static boolean checkISphereLibrary(Shell shell, AS400 as400, String library) {

        String message = checkISphereLibraryWithMessage(as400, library);
        if (message != null) {
            if (shell != null && message.length() > 0) {
                new DisplayMessage(shell, Messages.E_R_R_O_R, message).start();
            }
            return false;
        }

        return true;
    }

    private static String checkISphereLibraryWithMessage(AS400 as400, String library) {

        if (as400 == null) {
            return Messages.The_connection_does_not_exist_or_could_be_established;
        }

        String dataAreaISphereContent;

        try {
            dataAreaISphereContent = readISphereDataArea(as400, library);
        } catch (Exception e) {
            return e.getMessage();
        }

        String serverProvided = retrieveServerVersion(dataAreaISphereContent);
        String serverNeedsClient = retrieveRequiredClientVersion(dataAreaISphereContent);

        String clientProvided = comparableVersion(ISpherePlugin.getDefault().getVersion());
        String clientNeedsServer = comparableVersion(ISpherePlugin.getDefault().getMinServerVersion());

        if (serverProvided.compareTo(clientNeedsServer) < 0) {

            String message = Messages.bind(
                Messages.iSphere_library_A_on_System_B_is_of_version_C_but_at_least_version_D_is_needed_Please_transfer_the_current_iSphere_library_A_to_system_B,
                new String[] { library, as400.getSystemName(), getVersionFormatted(serverProvided), getVersionFormatted(clientNeedsServer) });

            return message;
        }

        if (clientProvided.compareTo(serverNeedsClient) < 0) {

            String message = Messages.bind(
                Messages.The_current_installed_iSphere_client_is_of_version_A_but_the_iSphere_server_needs_at_least_version_B_Please_install_the_current_iSphere_client,
                new String[] { getVersionFormatted(clientProvided), getVersionFormatted(serverNeedsClient) });

            return message;
        }

        return null;
    }

    private static String retrieveServerVersion(String dataAreaISphereContent) {
        return dataAreaISphereContent.substring(7, 13);
    }

    private static String retrieveRequiredClientVersion(String dataAreaISphereContent) {
        return dataAreaISphereContent.substring(21, 27);
    }

    private static String retrieveBuildDate(String dataAreaISphereContent) {
        return dataAreaISphereContent.substring(39, 49);
    }

    private static String readISphereDataAreaChecked(AS400 as400, String library) {

        try {
            return readISphereDataArea(as400, library);
        } catch (Exception e) {
            return null;
        }

    }

    private static String readISphereDataArea(AS400 as400, String library) throws Exception {

        if (!checkLibrary(as400, library)) {

            String message = Messages.bind(Messages.iSphere_library_A_does_not_exist_on_system_B_Please_transfer_iSphere_library_A_to_system_B,
                new String[] { library, as400.getSystemName() });

            throw new Exception(message);
        }

        String dataAreaISphereContent = null;
        CharacterDataArea dataAreaISphere = new CharacterDataArea(as400, "/QSYS.LIB/" + library + ".LIB/ISPHERE.DTAARA"); //$NON-NLS-1$ //$NON-NLS-2$

        try {
            dataAreaISphereContent = dataAreaISphere.read();
        } catch (AS400SecurityException e) {
            e.printStackTrace();
        } catch (ErrorCompletingRequestException e) {
            e.printStackTrace();
        } catch (IllegalObjectTypeException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ObjectDoesNotExistException e) {
            e.printStackTrace();
        }
        if (dataAreaISphereContent == null) {

            String message = Messages.bind(Messages.Specified_iSphere_library_A_on_System_B_is_not_a_iSphere_library,
                new String[] { library, as400.getSystemName() });

            throw new Exception(message);
        }

        return dataAreaISphereContent;
    }

    private static String getVersionFormatted(String aVersionNumber) {
        return Integer.parseInt(aVersionNumber.substring(0, 2)) + "." + Integer.parseInt(aVersionNumber.substring(2, 4)) + "." //$NON-NLS-1$ //$NON-NLS-2$
            + Integer.parseInt(aVersionNumber.substring(4, 6));
    }

    public static String executeCommand(AS400 as400, String command) throws Exception {

        CommandCall commandCall = new CommandCall(as400);
        commandCall.run(command);
        AS400Message[] messageList = commandCall.getMessageList();
        if (messageList.length > 0) {
            for (int idx = 0; idx < messageList.length; idx++) {
                if (messageList[idx].getType() == AS400Message.ESCAPE) {
                    return messageList[idx].getID();
                }
            }
        }
        return ""; //$NON-NLS-1$
    }

    public static String executeCommand(AS400 as400, String command, List<AS400Message> rtnMessages) {

        try {

            String escapeMessage = null;
            CommandCall commandCall = new CommandCall(as400);
            if (!commandCall.run(command)) {
                AS400Message[] messageList = commandCall.getMessageList();
                if (messageList.length > 0) {
                    for (int idx = 0; idx < messageList.length; idx++) {
                        if (messageList[idx].getType() == AS400Message.ESCAPE) {
                            escapeMessage = messageList[idx].getHelp();
                        }
                        if (rtnMessages != null) {
                            rtnMessages.add(messageList[idx]);
                        }
                    }
                }

                if (escapeMessage == null) {
                    escapeMessage = Messages.bind(Messages.Failed_to_execute_command_A, command);
                }
            }

            return escapeMessage;

        } catch (Throwable e) {
            ISpherePlugin.logError("*** Failed to execute command: " + command + " ***", e); //$NON-NLS-1$//$NON-NLS-2$
            return ExceptionHelper.getLocalizedMessage(e);
        }
    }

    public static void displayCommandExecutionError(String command, List<AS400Message> errorMessages) {

        String message = buildErrorMessage(command, errorMessages);
        MessageDialogAsync.displayBlockingError(Messages.E_R_R_O_R, message);
    }

    public static void displayCommandExecutionError(Shell shell, String command, List<AS400Message> errorMessages) {

        String message = buildErrorMessage(command, errorMessages);
        MessageDialog.openError(shell, Messages.E_R_R_O_R, message);
    }

    private static String buildErrorMessage(String command, List<AS400Message> errorMessages) {

        final String INDENTION = "  ";

        StringBuilder buffer = new StringBuilder();
        buffer.append("Could not execute command:");
        buffer.append("\n");
        buffer.append(INDENTION);
        buffer.append(command);
        buffer.append("\n\n");

        buffer.append("Reason:");
        buffer.append(INDENTION);

        for (AS400Message as400Message : errorMessages) {
            buffer.append("\n");
            buffer.append(INDENTION);
            buffer.append(as400Message.getText());
        }

        return buffer.toString();
    }

    public static String getCurrentLibrary(AS400 _as400) throws Exception {

        String currentLibrary = null;

        Job[] jobs = _as400.getJobs(AS400.COMMAND);

        if (jobs.length == 1) {

            if (!jobs[0].getCurrentLibraryExistence()) {
                currentLibrary = "*CRTDFT"; //$NON-NLS-1$
            } else {
                currentLibrary = jobs[0].getCurrentLibrary();
            }

        }

        return currentLibrary;

    }

    public static boolean setCurrentLibrary(AS400 _as400, String currentLibrary) throws Exception {

        String command = "CHGCURLIB CURLIB(" + currentLibrary + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        CommandCall commandCall = new CommandCall(_as400);

        if (commandCall.run(command)) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Changes a given version string of type "v.r.m" to a comparable version
     * string of type "VVRRMM".
     * 
     * @param version Version String of type "v.r.m".
     * @return Comparable version String.
     */
    public static String comparableVersion(String version) {
        String comparableVersion = version;
        String[] parts = new String[3];
        parts = comparableVersion.split("\\."); //$NON-NLS-1$
        DecimalFormat formatter = new DecimalFormat("00"); //$NON-NLS-1$
        for (int i = 0; i < parts.length; i++) {
            if (parts[i] == null) {
                parts[i] = formatter.format(0L);
            } else {
                parts[i] = formatter.format(IntHelper.tryParseInt(parts[i], 0));
            }
            if (i == 0) {
                comparableVersion = parts[i];
            } else {
                comparableVersion = comparableVersion + parts[i];
            }
        }
        return comparableVersion;
    }

    public static boolean canTransformSpooledFile(AS400 as400) {

        // if
        // (ISpherePlugin.getDefault().getIBMiRelease(as400).compareTo("V5R3M0")
        // <= 0) {
        // return false;
        // }

        return true;
    }

    public static boolean checkUserProfile(AS400 system, String userProfile) {
        return checkObject(system, new QSYSObjectPathName("QSYS", userProfile, "USRPRF")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static boolean checkLibrary(AS400 system, String library) {
        return checkObject(system, new QSYSObjectPathName("QSYS", library, "LIB")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static boolean checkObject(AS400 system, String library, String object, String type) {
        if (type.startsWith("*")) { //$NON-NLS-1$
            type = type.substring(1);
        }
        return checkObject(system, new QSYSObjectPathName(library, object, type));
    }

    public static boolean checkFile(AS400 system, String library, String file) {
        return checkObject(system, new QSYSObjectPathName(library, file, "FILE"));
    }

    public static boolean checkMember(AS400 system, String library, String file, String member) {
        return checkObject(system, new QSYSObjectPathName(library, file, member, "MBR"));
    }

    public static boolean checkObject(AS400 system, String path) {
        return checkObject(system, new QSYSObjectPathName(path));
    }

    public static boolean checkObject(AS400 system, QSYSObjectPathName pathName) {

        StringBuilder command = new StringBuilder();
        command.append("CHKOBJ OBJ("); //$NON-NLS-1$

        command.append(pathName.getLibraryName());
        command.append("/"); //$NON-NLS-1$
        command.append(pathName.getObjectName());

        command.append(") OBJTYPE("); //$NON-NLS-1$
        if (isMember(pathName)) {
            command.append("*FILE"); //$NON-NLS-1$
        } else {
            command.append("*"); //$NON-NLS-1$
            command.append(pathName.getObjectType());
        }
        command.append(")"); //$NON-NLS-1$

        if (isMember(pathName)) {
            command.append(" MBR(");
            command.append(pathName.getMemberName());
            command.append(")");
        }

        try {
            String message = executeCommand(system, command.toString());
            if (StringHelper.isNullOrEmpty(message)) {
                return true;
            }
        } catch (NullPointerException e) {
            throw e;
        } catch (Exception e) {
            ISpherePlugin.logError(e.getLocalizedMessage(), e);
        }

        return false;
    }

    public static RemoteObject resolveFile(AS400 system, String libraryName, String fileName) throws AS400Exception, AS400SecurityException,
        ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException, RequestNotSupportedException {

        ObjectList listObjects = new ObjectList(system, libraryName, fileName, ISeries.FILE);
        listObjects.addObjectAttributeToRetrieve(ObjectDescription.EXTENDED_ATTRIBUTE);
        Enumeration<?> objects = listObjects.getObjects();
        while (objects.hasMoreElements()) {
            Object object = (Object)objects.nextElement();
            if (object instanceof ObjectDescription) {
                ObjectDescription objectDescription = (ObjectDescription)object;

                String library = objectDescription.getValueAsString(ObjectDescription.LIBRARY);
                String name = objectDescription.getValueAsString(ObjectDescription.NAME);
                String description = objectDescription.getValueAsString(ObjectDescription.TEXT_DESCRIPTION);
                String type = objectDescription.getValueAsString(ObjectDescription.TYPE);
                String extAttr = objectDescription.getValueAsString(ObjectDescription.EXTENDED_ATTRIBUTE);

                if ("FILE".equals(type) && "PF".equals(extAttr)) {
                    return new RemoteObject(system, name, library, ISeries.FILE, description);
                }
            }
        }

        return null;
    }

    public static String resolveMemberName(AS400 system, String libraryName, String fileName, String memberName) throws AS400Exception,
        AS400SecurityException, ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException {

        try {

            MBRD0100 mbrd0100 = new MBRD0100(system);

            QUSRMBRD memberDescription = new QUSRMBRD(system);
            memberDescription.setFile(fileName, libraryName, memberName);
            if (memberDescription.execute(mbrd0100)) {
                return mbrd0100.getMemberName();
            }

        } catch (PropertyVetoException e) {
            ISpherePlugin.logError("*** Failed to retrieve member description " + libraryName + "/" + fileName + "(" + memberName + ")" + " ***", e);
        }

        return null;
    }

    private static boolean isMember(QSYSObjectPathName pathName) {

        if ("MBR".equals(pathName.getObjectType())) {
            if (!StringHelper.isNullOrEmpty(pathName.getMemberName())) {
                return true;
            }
        }

        return false;
    }

    public static boolean isASPGroupSpecified(String aspGroup) {

        if (StringHelper.isNullOrEmpty(aspGroup)) {
            return false;
        }

        if (ASP_GROUP_NONE_VALUE.equals(aspGroup)) {
            return false;
        }

        return true;
    }

}
