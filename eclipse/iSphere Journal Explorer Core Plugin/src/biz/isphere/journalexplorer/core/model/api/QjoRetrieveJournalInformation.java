/*******************************************************************************
 * Copyright (c) 2012-2018 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.core.model.api;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import biz.isphere.base.internal.IntHelper;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Bin4;
import com.ibm.as400.access.AS400Bin8;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.AS400Text;
import com.ibm.as400.access.BinaryConverter;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;

/**
 * Class, representing the QjoRetrieveJournalInformation API. Used to retrieve
 * information about a given journal.
 */
public class QjoRetrieveJournalInformation {

    private AS400 system;
    private JrnInfToRtv jJrnInfToRtv;

    private List<AS400Message> messages;

    public QjoRetrieveJournalInformation(AS400 aSystem, JrnInfToRtv aJrnInfToRtv) {
        system = aSystem;
        jJrnInfToRtv = aJrnInfToRtv;
        messages = new ArrayList<AS400Message>();
    }

    /**
     * Calls the API and returns the retrieved journal entries.<br>
     * Updates the selection criteria for the next call when there are more
     * journal entries available, but there is no room available in the return
     * structure.
     * 
     * @return retrieved journal entries
     */
    public RJRN0100 execute() throws Exception {

        int bufferSize = IntHelper.align16Bytes(500);

        RJRN0100 rjrn0100 = new RJRN0100(system, bufferSize);

        if (retrieveJournalEntries(rjrn0100.getProgramParameters(jJrnInfToRtv))) {
            // just fine.
        } else {
            rjrn0100 = null;
        }

        return rjrn0100;
    }

    /**
     * Returns the error messages on an API error.
     * 
     * @return list of API error messages
     */
    public List<IBMiMessage> getMessages() {

        List<IBMiMessage> ibmiMessages = new LinkedList<IBMiMessage>();

        for (AS400Message message : messages) {
            ibmiMessages.add(new IBMiMessage(message));
        }

        return ibmiMessages;
    }

    /**
     * Calls the QjoRetrieveJournalEntries API and retrieves error messages if
     * the API failed working.
     * <p>
     * We have to go the hard way and call program 'QZRUCLSP' instead of using
     * class ServiceProgramCall, because WDSCi does not support method
     * setAlignOn16Bytes(). That method seems to be introduced with 6.1.
     * 
     * @param serviceProgramParameters - parameters passed to the API
     * @return <code>true</code> on success, else <code>false</code>.
     * @throws PropertyVetoException
     * @throws ObjectDoesNotExistException
     * @throws InterruptedException
     * @throws IOException
     * @throws ErrorCompletingRequestException
     * @throws AS400SecurityException
     * @throws Exception
     */
    private boolean retrieveJournalEntries(ProgramParameter[] serviceProgramParameters) throws PropertyVetoException, AS400SecurityException,
        ErrorCompletingRequestException, IOException, InterruptedException, ObjectDoesNotExistException {

        ProgramParameter[] programParameters = new ProgramParameter[7 + serviceProgramParameters.length];

        // Parameter 1: Qualified service program name
        programParameters[0] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new AS400Text(20).toBytes("QJOURNAL  QSYS      "));

        // Parameter 2: Export name
        programParameters[1] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new AS400Text(29).toBytes("QjoRetrieveJournalInformation"));

        // Parameter 3: Return value format
        programParameters[2] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new AS400Bin4().toBytes(0));

        // Parameter 4: Parameter formats
        byte[] parameterFormatBytes = new byte[serviceProgramParameters.length * 4];
        for (int i = 0; i < serviceProgramParameters.length; ++i) {
            BinaryConverter.intToByteArray(serviceProgramParameters[i].getParameterType(), parameterFormatBytes, i * 4);
        }

        programParameters[3] = new ProgramParameter(parameterFormatBytes);

        // Parameter 5: Number of parameters
        programParameters[4] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new AS400Bin4().toBytes(serviceProgramParameters.length));

        // Parameter 6: Error code
        programParameters[5] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new AS400Bin8().toBytes(0));

        // Parameter 7: Return Value
        programParameters[6] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new AS400Bin4().toBytes(0));

        int totalLengthOfProgramParameters = 0;
        for (int i = 0; i <= 6; i++) {
            totalLengthOfProgramParameters += programParameters[i].getInputData().length;
        }

        int paddedErrorCodeLength = 8 + (16 - totalLengthOfProgramParameters % 16);
        programParameters[5] = new ProgramParameter(ProgramParameter.PASS_BY_REFERENCE, new byte[paddedErrorCodeLength]);

        // Append service program parameters to parameter list of QZRUCLSP.
        for (int i = 0; i < serviceProgramParameters.length; i++) {
            programParameters[7 + i] = serviceProgramParameters[i];
        }

        messages.clear();

        ProgramCall programCall = new ProgramCall(system, "/QSYS.LIB/QZRUCLSP.PGM", programParameters);
        if (programCall.run() != true) {
            messages.addAll(Arrays.asList(programCall.getMessageList()));
            return false;
        } else {
            return true;
        }
    }
}
