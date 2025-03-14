/*******************************************************************************
 * Copyright (c) 2012-2024 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.sourcemembercopy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;

import biz.isphere.base.internal.ExceptionHelper;
import biz.isphere.base.internal.IBMiHelper;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;
import biz.isphere.core.internal.ISphereHelper;
import biz.isphere.core.internal.Member;

public class CopyMemberItem implements Comparable<CopyMemberItem> {

    private String fromFile;
    private String fromLibrary;
    private String fromMember;
    private String fromSrcType;

    private String toFile;
    private String toLibrary;
    private String toMember;
    private String toSrcType;

    private Object data;

    private String errorMessage;
    private boolean copied;

    private List<ModifiedListener> modifiedListeners;

    public CopyMemberItem(String fromFile, String fromLibrary, String fromMember, String fromSrcType) {
        this.fromFile = fromFile.trim();
        this.fromLibrary = fromLibrary.trim();
        this.fromMember = fromMember.trim();
        this.fromSrcType = fromSrcType.trim();
        this.toFile = fromFile.trim();
        this.toLibrary = fromLibrary.trim();
        this.toMember = fromMember.trim();
        this.toSrcType = fromSrcType.trim();
        this.errorMessage = null;
        this.copied = false;
        this.data = null;
    }

    public String getFromFile() {
        return fromFile;
    }

    public String getFromLibrary() {
        return fromLibrary;
    }

    public String getFromMember() {
        return fromMember;
    }

    public String getFromSrcType() {
        return fromSrcType;
    }

    public String getToFile() {
        return toFile;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setToFile(String toFile) {
        if (hasChanged(this.toFile, toFile)) {
            this.toFile = toFile;
            notifyModifiedListeners();
        }
    }

    public String getToLibrary() {
        return toLibrary;
    }

    public void setToLibrary(String toLibrary) {
        if (hasChanged(this.toLibrary, toLibrary)) {
            this.toLibrary = toLibrary;
            notifyModifiedListeners();
        }
    }

    public String getToMember() {
        return toMember;
    }

    public void setToMember(String toMember) {
        if (hasChanged(this.toMember, toMember)) {
            this.toMember = toMember;
            notifyModifiedListeners();
        }
    }

    public String getToSrcType() {
        return toSrcType;
    }

    public void setToSrcType(String toSrcType) {
        if (hasChanged(this.toSrcType, toSrcType)) {
            this.toSrcType = toSrcType;
            notifyModifiedListeners();
        }
    }

    public boolean isError() {
        if (!StringHelper.isNullOrEmpty(getErrorMessage())) {
            return true;
        }
        return false;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String message) {
        if (hasChanged(this.errorMessage, message)) {
            this.errorMessage = message;
            notifyModifiedListeners();
        }
    }

    public boolean isCopied() {
        return copied;
    }

    private void setCopyStatus(boolean copied) {
        if (hasChanged(this.copied, copied)) {
            this.copied = copied;
            this.errorMessage = null;
            notifyModifiedListeners();
        }
    }

    public String getFromQSYSName() {
        return getQSYSName(getFromFile(), getFromLibrary(), getFromMember());
    }

    public String getToQSYSName() {
        return getQSYSName(getToFile(), getToLibrary(), getToMember());
    }

    private String getQSYSName(String file, String library, String member) {

        StringBuilder buffer = new StringBuilder();

        buffer.append(library);
        buffer.append("/"); //$NON-NLS-1$
        buffer.append(file);
        buffer.append("("); //$NON-NLS-1$
        buffer.append(member);
        buffer.append(")"); //$NON-NLS-1$

        return buffer.toString();
    }

    private boolean hasChanged(String currentValue, String newValue) {

        if (currentValue == null && newValue == null) {
            return false;
        } else if (currentValue != null && currentValue.equals(newValue)) {
            return false;
        }

        return true;
    }

    private boolean hasChanged(boolean currentValue, boolean newValue) {

        if (currentValue == newValue) {
            return false;
        }

        return true;
    }

    public int compareTo(CopyMemberItem item) {

        if (this.equals(item)) {
            return 0;
        }

        int result;

        result = getFromLibrary().compareTo(item.getFromLibrary());
        if (result != 0) {
            return result;
        }

        if (getToLibrary() == null) {
            return -1;
        } else if (item.getToLibrary() == null) {
            return 1;
        } else {
            result = getToLibrary().compareTo(item.getToLibrary());
            if (result != 0) {
                return result;
            }
        }

        result = getFromFile().compareTo(item.getFromFile());
        if (result != 0) {
            return result;
        }

        if (getToFile() == null) {
            return -1;
        } else if (item.getToFile() == null) {
            return 1;
        } else {
            result = getToFile().compareTo(item.getToFile());
            if (result != 0) {
                return result;
            }
        }

        result = getFromMember().compareTo(item.getFromMember());
        if (result != 0) {
            return result;
        }

        if (getToMember() == null) {
            return -1;
        } else if (item.getToMember() == null) {
            return 1;
        } else {
            result = getToMember().compareTo(item.getToMember());
            if (result != 0) {
                return result;
            }
        }

        result = getFromMember().compareTo(item.getFromMember());
        if (result != 0) {
            return result;
        }

        if (getToSrcType() == null) {
            return -1;
        } else if (item.getToSrcType() == null) {
            return 1;
        } else {
            result = getToSrcType().compareTo(item.getToSrcType());
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(fromLibrary); // $NON-NLS-1$
        buffer.append("/"); //$NON-NLS-1$
        buffer.append(fromFile); // $NON-NLS-1$
        buffer.append("("); //$NON-NLS-1$
        buffer.append(fromMember); // $NON-NLS-1$
        buffer.append(")"); //$NON-NLS-1$
        buffer.append(".");
        buffer.append(fromSrcType);
        buffer.append(" -> ");
        buffer.append(toLibrary); // $NON-NLS-1$
        buffer.append("/"); //$NON-NLS-1$
        buffer.append(toFile); // $NON-NLS-1$
        buffer.append("("); //$NON-NLS-1$
        buffer.append(toMember); // $NON-NLS-1$
        buffer.append(")"); //$NON-NLS-1$
        buffer.append("."); //$NON-NLS-1$
        buffer.append(toSrcType);
        return buffer.toString();
    }

    public boolean performCopyOperation(String fromConnectionName, String toConnectionName) {

        String message;

        if (fromConnectionName.equalsIgnoreCase(toConnectionName)) {
            message = performLocalCopy(fromConnectionName);
        } else {
            message = performCopyBetweenConnections(fromConnectionName, toConnectionName);
        }

        if (message != null) {
            setErrorMessage(message);
            return false;
        }

        setCopyStatus(true);

        return true;
    }

    public void reset() {

        setErrorMessage(null);
        setCopyStatus(false);
    }

    private String performLocalCopy(String connectionName) {

        try {

            String message = null;
            AS400 system = IBMiHostContributionsHandler.getSystem(connectionName);

            Member fromSourceMember = IBMiHostContributionsHandler.getMember(connectionName, getFromLibrary(), getFromFile(), getFromMember());
            if (fromSourceMember == null) {
                return Messages.bind(Messages.Member_2_of_file_1_in_library_0_not_found,
                    new Object[] { getFromLibrary(), getFromFile(), getFromMember() });
            }

            // TODO: remove obsolete code
            // if (!ISphereHelper.checkMember(system, getToLibrary(),
            // getToFile(), getToMember())) {
            // message = addSourceMember(connectionName, getToLibrary(),
            // getToFile(), getToMember());
            // } else {
            // message = prepareSourceMember(connectionName, getToLibrary(),
            // getToFile(), getToMember());
            // }
            // if (message != null) {
            // return message;
            // }

            List<AS400Message> rtnMessages = new ArrayList<AS400Message>();
            StringBuilder command = new StringBuilder();

            command.append("CPYF"); //$NON-NLS-1$
            command.append(" FROMFILE("); //$NON-NLS-1$
            command.append(getFromLibrary());
            command.append("/"); //$NON-NLS-1$
            command.append(getFromFile());
            command.append(")"); //$NON-NLS-1$
            command.append(" TOFILE("); //$NON-NLS-1$
            command.append(getToLibrary());
            command.append("/"); //$NON-NLS-1$
            command.append(getToFile());
            command.append(")"); //$NON-NLS-1$
            command.append(" FROMMBR("); //$NON-NLS-1$
            command.append(getFromMember());
            command.append(")"); //$NON-NLS-1$
            command.append(" TOMBR("); //$NON-NLS-1$
            command.append(getToMember());
            command.append(")"); //$NON-NLS-1$
            command.append(" MBROPT(*REPLACE)"); //$NON-NLS-1$
            command.append(" CRTFILE(*NO)"); //$NON-NLS-1$
            command.append(" FMTOPT(*MAP)"); //$NON-NLS-1$

            message = ISphereHelper.executeCommand(system, command.toString(), rtnMessages);
            if (message != null) {
                return buildMessageString(rtnMessages);
            }

            Member toSourceMember = IBMiHostContributionsHandler.getMember(connectionName, getToLibrary(), getToFile(), getToMember());
            if (toSourceMember == null) {
                return Messages.bind(Messages.Member_2_of_file_1_in_library_0_not_found, new Object[] { getToLibrary(), getToFile(), getToMember() });
            }

            message = setToMemberAttributes(fromSourceMember, toSourceMember);
            if (message != null) {
                return message;
            }

        } catch (Throwable e) {
            ISpherePlugin.logError("*** Unexpected error when copying member ***", e);
            return ExceptionHelper.getLocalizedMessage(e);
        }

        return null;
    }

    private String performCopyBetweenConnections(String fromConnectionName, String toConnectionName) {

        try {

            String message = null;

            debugPrint("\nProcessing member: " + getFromMember());

            long startTime = System.currentTimeMillis();
            long startTime2 = startTime;

            Member fromSourceMember = IBMiHostContributionsHandler.getMember(fromConnectionName, getFromLibrary(), getFromFile(), getFromMember());
            if (fromSourceMember == null) {
                return Messages.bind(Messages.Member_2_of_file_1_in_library_0_not_found,
                    new Object[] { getFromLibrary(), getFromFile(), getFromMember() });
            }

            debugPrint("Retrieving the source member attributes took " + (System.currentTimeMillis() - startTime) + " mSecs.");
            startTime = System.currentTimeMillis();

            IFile localResource = fromSourceMember.downloadMember(null);
            if (localResource == null) {
                return Messages.bind(Messages.Could_not_download_member_2_of_file_1_of_library_0,
                    new Object[] { getFromLibrary(), getFromFile(), getFromMember() });
            }

            debugPrint("Downloading the source member took " + (System.currentTimeMillis() - startTime) + " mSecs.");
            startTime = System.currentTimeMillis();

            if (!ISphereHelper.checkMember(IBMiHostContributionsHandler.getSystem(toConnectionName), getToLibrary(), getToFile(), getToMember())) {
                message = addSourceMember(toConnectionName, getToLibrary(), getToFile(), getToMember());
                debugPrint("Adding the target member took " + (System.currentTimeMillis() - startTime) + " mSecs.");
                startTime = System.currentTimeMillis();
            } else {
                message = prepareSourceMember(toConnectionName, getToLibrary(), getToFile(), getToMember());
                debugPrint("Preparing the target member took " + (System.currentTimeMillis() - startTime) + " mSecs.");
                startTime = System.currentTimeMillis();
            }

            if (message != null) {
                return message;
            }

            Member toSourceMember = IBMiHostContributionsHandler.getMember(toConnectionName, getToLibrary(), getToFile(), getToMember());
            if (toSourceMember == null) {
                return Messages.bind(Messages.Member_2_of_file_1_in_library_0_not_found, new Object[] { getToLibrary(), getToFile(), getToMember() });
            }

            debugPrint("Retrieving the target member attributes took " + (System.currentTimeMillis() - startTime) + " mSecs.");
            startTime = System.currentTimeMillis();

            message = toSourceMember.uploadMember(null, localResource);
            if (message != null) {
                return message;
            }

            debugPrint("Uploading the source member took " + (System.currentTimeMillis() - startTime) + " mSecs.");
            startTime = System.currentTimeMillis();

            message = setToMemberAttributes(fromSourceMember, toSourceMember);
            if (message != null) {
                return message;
            }

            debugPrint("Changing the text of the target source member took " + (System.currentTimeMillis() - startTime) + " mSecs.");
            startTime = System.currentTimeMillis();

            debugPrint("Total time used: " + (System.currentTimeMillis() - startTime2) + " mSecs.");

        } catch (Throwable e) {
            ISpherePlugin.logError("*** Unexpected error when copying member ***", e); //$NON-NLS-1$
            return ExceptionHelper.getLocalizedMessage(e);
        }

        return null;
    }

    private void debugPrint(String message) {
        // Xystem.out.println(message);
    }

    private String addSourceMember(String connectionName, String libraryName, String fileName, String memberName) {

        StringBuilder command = new StringBuilder();
        command.append("ADDPFM"); //$NON-NLS-1$
        command.append(" FILE("); //$NON-NLS-1$
        command.append(libraryName);
        command.append("/"); //$NON-NLS-1$
        command.append(fileName);
        command.append(")"); //$NON-NLS-1$
        command.append(" MBR("); //$NON-NLS-1$
        command.append(memberName);
        command.append(")"); //$NON-NLS-1$
        command.append(" TEXT('*** iSphere Copying Member ***')"); //$NON-NLS-1$
        command.append(" SRCTYPE('*NONE')"); //$NON-NLS-1$

        List<AS400Message> rtnMessages = new ArrayList<AS400Message>();
        String message = ISphereHelper.executeCommand(IBMiHostContributionsHandler.getSystem(connectionName), command.toString(), rtnMessages);
        if (message != null) {
            return buildMessageString(rtnMessages);
        }

        return null;
    }

    private String prepareSourceMember(String connectionName, String libraryName, String fileName, String memberName) {

        String message = null;
        List<AS400Message> rtnMessages = new ArrayList<AS400Message>();
        StringBuilder command = new StringBuilder();

        rtnMessages.clear();
        AS400 system = IBMiHostContributionsHandler.getSystem(connectionName);

        command.delete(0, command.length());

        command.append("CHGPFM"); //$NON-NLS-1$
        command.append(" FILE("); //$NON-NLS-1$
        command.append(libraryName);
        command.append("/"); //$NON-NLS-1$
        command.append(fileName);
        command.append(")"); //$NON-NLS-1$
        command.append(" MBR("); //$NON-NLS-1$
        command.append(memberName);
        command.append(")"); //$NON-NLS-1$
        command.append(" TEXT('*** iSphere Copying Member ***')"); //$NON-NLS-1$
        command.append(" SRCTYPE('*NONE')"); //$NON-NLS-1$

        message = ISphereHelper.executeCommand(system, command.toString(), rtnMessages);
        if (message != null) {
            return buildMessageString(rtnMessages);
        }

        rtnMessages.clear();
        command.delete(0, command.length());

        command.append("CLRPFM"); //$NON-NLS-1$
        command.append(" FILE("); //$NON-NLS-1$
        command.append(libraryName);
        command.append("/"); //$NON-NLS-1$
        command.append(fileName);
        command.append(")"); //$NON-NLS-1$
        command.append(" MBR("); //$NON-NLS-1$
        command.append(memberName);
        command.append(")"); //$NON-NLS-1$

        message = ISphereHelper.executeCommand(system, command.toString(), rtnMessages);
        if (message != null) {
            return buildMessageString(rtnMessages);
        }

        return null;
    }

    private String setToMemberAttributes(Member fromSourceMember, Member toSourceMember) {

        String description = fromSourceMember.getDescription();
        String sourceType = fromSourceMember.getSourceType();

        StringBuilder command = new StringBuilder();
        command.append("CHGPFM"); //$NON-NLS-1$
        command.append(" FILE("); //$NON-NLS-1$
        command.append(getToLibrary());
        command.append("/"); //$NON-NLS-1$
        command.append(getToFile());
        command.append(") "); //$NON-NLS-1$
        command.append("MBR("); //$NON-NLS-1$
        command.append(getToMember());
        command.append(") "); //$NON-NLS-1$
        command.append("TEXT("); //$NON-NLS-1$
        command.append(IBMiHelper.quote(description));
        command.append(")"); //$NON-NLS-1$
        command.append(" SRCTYPE("); //$NON-NLS-1$
        command.append(sourceType);
        command.append(")"); //$NON-NLS-1$

        List<AS400Message> rtnMessages = new ArrayList<AS400Message>();
        String message = ISphereHelper.executeCommand(IBMiHostContributionsHandler.getSystem(toSourceMember.getConnection()), command.toString(),
            rtnMessages);
        if (message != null) {
            return buildMessageString(rtnMessages);
        }

        return null;
    }

    private String buildMessageString(List<AS400Message> rtnMessages) {

        StringBuilder message = new StringBuilder();

        Iterator<AS400Message> iterator = rtnMessages.iterator();
        while (iterator.hasNext()) {
            if (message.length() > 0) {
                // message.append("\n"); //$NON-NLS-1$
                message.append(" "); //$NON-NLS-1$
            }
            message.append(iterator.next().getText());
        }

        return message.toString();
    }

    public void addModifiedListener(ModifiedListener listener) {

        if (modifiedListeners == null) {
            modifiedListeners = new ArrayList<ModifiedListener>();
        }

        modifiedListeners.add(listener);
    }

    public void removeModifiedListener(ModifiedListener listener) {

        if (modifiedListeners != null) {
            modifiedListeners.remove(listener);
        }
    }

    private void notifyModifiedListeners() {

        if (modifiedListeners == null) {
            return;
        }

        for (int i = 0; i < modifiedListeners.size(); ++i) {
            modifiedListeners.get(i).modified(this);
        }
    }

    public interface ModifiedListener {
        public void modified(CopyMemberItem item);
    }
}
