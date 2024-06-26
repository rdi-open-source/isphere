/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.CoreException;

import biz.isphere.base.internal.StringHelper;
import biz.isphere.core.ibmi.contributions.extension.handler.IBMiHostContributionsHandler;

import com.ibm.debug.pdt.internal.core.PDTDebugTarget;
import com.ibm.debug.pdt.internal.core.model.DebuggeeProcess;

public final class IBMiDebugHelper {

    /*
     * Value of
     * com.ibm.etools.iseries.debug.internal.core.AS400ConfigurationConstants.
     * RESID_CONNECTION_NAME
     */
    private static final String RESID_CONNECTION_NAME = "com.ibm.etools.systems.as400.debug.ui.connection.connectionName.";

    private IBMiDebugHelper() {
    }

    // @formatter:off
    //
    // Available after RDi 9.6:
    // ------------------------
    // TODO: for the next RDi release past 9.6
    //
    // Hello Thomas, I just wanted to let you know that your request enhancement is in. I wanted to let you
    // know right away.  It will be delivered in the next release of RDi but that won't be for a few months.
    // (major release)
    //
    // IProcess process;
    // String connectionName = process.getAttribute(IPDTDebugConstants.ATTRIBUTE_CONNECTION_NAME);
    // String jobName = process.getAttribute(IPDTDebugConstants.ATTRIBUTE_JOB_NAME);
    // String jobNumber = process.getAttribute(IPDTDebugConstants.ATTRIBUTE_JOB_NUMBER);
    // String jobUser = process.getAttribute(IPDTDebugConstants.ATTRIBUTE_JOB_USER);
    //
    // Available with RDi 9.6:
    // -----------------------
    // Hello Thomas,
    //  
    // The debug team has looked at your requirements.
    // 
    // For the job name, this is something they have accepted as a future requirement but
    // for the connection name, it is currently officially supported via: 
    // 
    // import com.ibm.etools.iseries.debug.internal.core.AS400ConfigurationConstants;
    // 
    // IDebugTarget pdtdebugTarget = ...
    // String connectionName = pdtdebugTarget.getLaunch().getLaunchConfiguration().getAttribute(AS400ConfigurationConstants.RESID_CONNECTION_NAME, "");
    // 
    // There are no plans to provide an alternative API to get this information.
    //  
    // I hope this helps,
    //
    // Edmund Reinhardt
    // @formatter:on

    /**
     * Returns the name of the RSE connection that is assigned to a given
     * {@link DebuggeeProcess}.
     * 
     * @param debuggeeProcess - The debuggee process that is asked for the RSE
     *        connection name
     * @return connection name
     * @throws UnknownHostException
     */
    public static String getConnectionName(DebuggeeProcess debuggeeProcess) throws UnknownHostException {

        /*
         * Try to get the connection name from the debuggee process as described
         * by Edmund Reinhardt. Works fine for 9.5.1.3+
         */
        if (debuggeeProcess.getDebugTarget() instanceof PDTDebugTarget) {
            try {
                PDTDebugTarget debugTarget = (PDTDebugTarget)debuggeeProcess.getDebugTarget();
                String connectionName = debugTarget.getLaunch().getLaunchConfiguration().getAttribute(RESID_CONNECTION_NAME, "");
                if (!StringHelper.isNullOrEmpty(connectionName)) {
                    return connectionName;
                }
            } catch (CoreException e) {
            }
        }

        /*
         * Fallback to TCP/IP address for RDP 8.0.
         */
        String hostName = getHostName(debuggeeProcess);
        String connectionName = getConnectionName(hostName);

        return connectionName;
    }

    /**
     * Returns the host name of the TCP/IP connection that is assigned to a
     * given {@link DebuggeeProcess}.
     * 
     * @param debuggeeProcess - The debuggee process that is asked for host name
     * @return host name
     */
    public static String getHostName(DebuggeeProcess debuggeeProcess) {

        if (debuggeeProcess.getDebugTarget() instanceof PDTDebugTarget) {
            PDTDebugTarget debugTarget = (PDTDebugTarget)debuggeeProcess.getDebugTarget();
            String hostName = debugTarget.getSocket().getInetAddress().getHostName();
            return hostName;
        }

        return null;
    }

    private static String getConnectionName(String hostName) throws UnknownHostException {

        if (!StringHelper.isNullOrEmpty(hostName)) {
            String tcpAddr = InetAddress.getByName(hostName).getHostAddress();
            if (!StringHelper.isNullOrEmpty(tcpAddr)) {
                String connectionName = IBMiHostContributionsHandler.getConnectionNameByIPAddr(tcpAddr, true);
                return connectionName;
            }
        }

        return null;
    }

}
