/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.dataareaeditor;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.APIProgramCallDocument;
import biz.isphere.core.internal.exception.ErrorLoggedException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;

public class QXXCHGDA {

    private AS400 system;
    private String dataArea;
    private String library;

    public QXXCHGDA(AS400 anAS400, String aLibrary, String aDataArea) {
        this.system = anAS400;
        this.dataArea = aDataArea;
        this.library = aLibrary;
    }

    public void run(byte[] bytes) throws Exception {

        APIProgramCallDocument pcml = new APIProgramCallDocument(system, "biz.isphere.rse.dataareaeditor.QXXCHGDA", getClass().getClassLoader()); //$NON-NLS-1$
        pcml.setQualifiedObjectName("QXXCHGDA.dataArea", library, dataArea); //$NON-NLS-1$
        pcml.setValue("QXXCHGDA.start", new Integer(1)); //$NON-NLS-1$
        pcml.setValue("QXXCHGDA.length", new Integer(bytes.length)); //$NON-NLS-1$
        pcml.setValue("QXXCHGDA.value", bytes); //$NON-NLS-1$

        boolean rc = pcml.callProgram("QXXCHGDA"); //$NON-NLS-1$

        if (rc == false) {

            AS400Message[] msgs = pcml.getMessageList("QXXCHGDA"); //$NON-NLS-1$
            for (int idx = 0; idx < msgs.length; idx++) {
                ISpherePlugin.logError(msgs[idx].getID() + " - " + msgs[idx].getText(), null); //$NON-NLS-1$
            }

            String message = "*** Call to QXXCHGDA failed. See previous messages ***";
            throw new ErrorLoggedException(message);
        }
    }
}
