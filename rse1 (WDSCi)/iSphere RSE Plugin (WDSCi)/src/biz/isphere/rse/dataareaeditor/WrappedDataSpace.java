/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.dataareaeditor;

import biz.isphere.core.dataspace.rse.AbstractWrappedDataSpace;
import biz.isphere.core.dataspaceeditor.rse.RemoteObject;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.CharacterDataArea;
import com.ibm.etools.iseries.core.api.ISeriesConnection;

/**
 * Class that serves as a universal data provider for data area and user space
 * objects.
 * <p>
 * In terms of iSphere these objects are called 'data space objects', which is a
 * made-up of 'Data Area' and 'User Space'.
 */
public class WrappedDataSpace extends AbstractWrappedDataSpace {

    public WrappedDataSpace(AS400 as400, RemoteObject remoteObject) throws Exception {
        super(as400, remoteObject);
    }

    public WrappedDataSpace(RemoteObject remoteObject) throws Exception {
        super(remoteObject);
    }

    protected AS400 getSystem(String connection) throws Exception {
        return ISeriesConnection.getConnection(connection).getAS400ToolboxObject(null);
    }

    protected byte[] loadCharacterDataAreaBytes(CharacterDataArea characterDataArea) throws Exception {
        String value = characterDataArea.read(0, characterDataArea.getLength());
        byte[] bytes = value.getBytes(characterDataArea.getSystem().getJobCCSIDEncoding());
        return bytes;
    }
}
