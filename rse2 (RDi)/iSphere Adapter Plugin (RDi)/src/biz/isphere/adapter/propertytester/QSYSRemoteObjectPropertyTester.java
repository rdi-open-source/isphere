/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.adapter.propertytester;

import org.eclipse.core.expressions.PropertyTester;

import com.ibm.etools.iseries.comm.interfaces.ISeriesHostObjectBrief;

public class QSYSRemoteObjectPropertyTester extends PropertyTester {

    public static final String PROPERTY_NAMESPACE = "biz.isphere.adapter.propertytester.hostobjectbrief";

    public static final String PROPERTY_TYPE = "type";

    public static final String PROPERTY_SUBTYPE = "subtype";

    public boolean test(Object aReceiver, String aProperty, Object[] anArgs, Object anExpectedValue) {

        if (!(aReceiver instanceof ISeriesHostObjectBrief)) {
            return false;
        }

        ISeriesHostObjectBrief remoteObject = (ISeriesHostObjectBrief)aReceiver;

        if (anExpectedValue instanceof String) {
            String expectedValue = (String)anExpectedValue;
            if (PROPERTY_TYPE.equals(aProperty)) {
                // TODO: remove me, needed only for debugging
                // System.out.println("Property-Tester (type): " +
                // remoteObject.getType() + "=" + expectedValue);
                return expectedValue.equalsIgnoreCase(remoteObject.getType());
            } else if (PROPERTY_SUBTYPE.equals(aProperty)) {
                // TODO: remove me, needed only for debugging
                // System.out.println("Property-Tester (subtype): " +
                // remoteObject.getSubType() + "=" + expectedValue);
                return expectedValue.equalsIgnoreCase(remoteObject.getSubType());
            }
        }

        return false;
    }

}
