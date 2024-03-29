/*******************************************************************************
 * Copyright (c) 2012-2023 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.internal.api.retrieveproductinformation;

import java.beans.PropertyVetoException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ProgramParameter;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.internal.api.APIErrorCode;
import biz.isphere.core.internal.api.APIProgramCallDocument;

/**
 * The Retrieve Product Information (QSZRTVPR) API returns information about a
 * software product. The information is requested by specifying a product ID,
 * release level, option number, and load ID; not by specifying an object name.
 * The Display Software Resources (DSPSFWRSC) command and the Select Product
 * (QSZSLTPR) API will obtain a list of installed products about which you can
 * retrieve information.
 */
public class QSZRTVPR extends APIProgramCallDocument {

    public QSZRTVPR(AS400 system) throws PropertyVetoException {
        super(system, "QSZRTVPR", "QSYS");
    }

    public boolean execute(PRDR0000 productLoadInformation, PRDI0100 productInformation) {

        try {

            if (!execute(createParameterList(productLoadInformation, productInformation))) {
                return false;
            }

            productLoadInformation.setBytes(getParameterList()[0].getOutputData());

            return true;

        } catch (Throwable e) {
            ISpherePlugin.logError(e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Produces the parameter list for calling the QSZRTVPR API.
     */
    protected ProgramParameter[] createParameterList(PRDR0000 productLoadInformation, PRDI0100 productInformation) throws Exception {

        ProgramParameter[] parameterList = new ProgramParameter[5];
        parameterList[0] = new ProgramParameter(productLoadInformation.getLength());
        parameterList[1] = produceIntegerParameter(productLoadInformation.getLength());
        parameterList[2] = produceStringParameter(productLoadInformation.getName(), 8);
        parameterList[3] = produceByteParameter(productInformation.getBytes());
        parameterList[4] = produceByteParameter(new APIErrorCode().getBytes());

        return parameterList;
    }

}
