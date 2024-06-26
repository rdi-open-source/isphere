/*******************************************************************************
 * Copyright (c) 2012-2023 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer.api.listjoblog;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ProgramParameter;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.api.APIErrorCode;
import biz.isphere.core.internal.api.APIProgramCallDocument;

/**
 * The Close List (QGYCLST) API closes a previously opened list. Any internal
 * storage associated with that list is freed. The handle specified on the call
 * to this API is no longer valid after the call completes.
 */
public class QGYCLST extends APIProgramCallDocument {

    private String requestHandle;

    public QGYCLST(AS400 system, String requestHandle) {
        super(system, "QGYCLST", "QSYS"); //$NON-NLS-1$ //$NON-NLS-2$

        this.requestHandle = requestHandle;
    }

    public boolean execute() {

        try {

            if (!execute(createParameterList(requestHandle))) {
                return false;
            }

            return true;

        } catch (Throwable e) {
            ISpherePlugin.logError(e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Produces the parameter list for calling the QUSRMBRD API.
     */
    protected ProgramParameter[] createParameterList(String requestHandle) throws Exception {

        ProgramParameter[] parameterList = new ProgramParameter[2];
        parameterList[0] = produceStringParameter(requestHandle, 4);
        parameterList[1] = produceByteParameter(new APIErrorCode().getBytes());

        return parameterList;
    }

}
