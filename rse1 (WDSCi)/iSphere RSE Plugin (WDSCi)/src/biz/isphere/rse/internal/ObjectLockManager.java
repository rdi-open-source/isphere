package biz.isphere.rse.internal;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.AbstractObjectLockManager;
import biz.isphere.core.internal.ObjectLock;
import biz.isphere.rse.Messages;

import com.ibm.etools.iseries.core.api.ISeriesConnection;
import com.ibm.etools.iseries.core.api.ISeriesMessage;
import com.ibm.etools.systems.core.messages.SystemMessageException;

public class ObjectLockManager extends AbstractObjectLockManager {

    public ObjectLockManager(int waitSeconds) {
        super(waitSeconds);
    }

    @Override
    protected String[] allocateObject(ObjectLock objectLock, int lockWaitTime) {

        return executeCommand(objectLock.getConnectionName(), objectLock.getAllocateCommand(lockWaitTime));
    }

    @Override
    protected String[] deallocateObject(ObjectLock objectLock) {

        return executeCommand(objectLock.getConnectionName(), objectLock.getDeallocateCommand());
    }

    protected String[] executeCommand(String connectionName, String command) {

        ISeriesConnection connection = ISeriesConnection.getConnection(connectionName);
        if (connection == null) {
            return new String[] { Messages.Failed_to_get_connection_colon + " " + connectionName }; //$NON-NLS-1$
        }

        ISeriesMessage[] cmdMessages = null;

        try {

            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getSite().getShell();
            cmdMessages = connection.runCommand(shell, command);
            if (cmdMessages == null) {
                cmdMessages = new ISeriesMessage[] {};
            }

        } catch (SystemMessageException e) {
            ISpherePlugin.logError(e.getMessage(), e);
            return new String[] { e.getLocalizedMessage() };
        }

        String[] errorMessages = new String[cmdMessages.length];
        for (int i = 0; i < cmdMessages.length; i++) {
            ISeriesMessage cmdMessage = cmdMessages[i];
            errorMessages[i] = cmdMessage.getMessageID() + ": " + cmdMessage.getMessageText(); //$NON-NLS-1$
        }
        return errorMessages;
    }
}
