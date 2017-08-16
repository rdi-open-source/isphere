/*******************************************************************************
 * Copyright (c) 2012-2017 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.rse.shared.model;

import com.ibm.etools.iseries.core.api.ISeriesConnection;

public class ConnectionDelegate {

    private ISeriesConnection connection;

    public ConnectionDelegate(Object connection) {
        this.connection = (ISeriesConnection)connection;
    }

    public static boolean instanceOf(Object object) {
        return (object instanceof ISeriesConnection);
    }

    public static Object[] getConnections() {
        return ISeriesConnection.getConnections();
    }

    public static Object getConnection(String connectionName) {
        return ISeriesConnection.getConnection(connectionName);
    }

    public static String getConnectionName(Object connection) {
        return ((ISeriesConnection)connection).getConnectionName();
    }

    public String getConnectionName() {
        return connection.getConnectionName();
    }

}
