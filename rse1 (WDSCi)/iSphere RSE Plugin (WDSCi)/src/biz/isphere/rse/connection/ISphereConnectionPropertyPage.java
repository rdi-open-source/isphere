/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.connection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import biz.isphere.core.connection.rse.ConnectionProperties;
import biz.isphere.core.connection.rse.ISphereConnectionPropertyPageDelegate;
import biz.isphere.core.internal.IDisplayErrorDialog;

import com.ibm.etools.systems.core.ui.propertypages.SystemBasePropertyPage;
import com.ibm.etools.systems.model.SystemConnection;

// See:
// com.ibm.etools.systems.core.ui.propertypages.SystemConnectionPropertyPage
// c:\Programme
// x86\IBM\SDP_Shared\plugins\com.ibm.etools.systems.core_7.1.1\plugin.xml
public class ISphereConnectionPropertyPage extends SystemBasePropertyPage implements IDisplayErrorDialog {

    private ISphereConnectionPropertyPageDelegate delegate;
    private ConnectionManager manager;
    private ConnectionProperties connectionProperties;

    /**
     * Constructor for SamplePropertyPage.
     */
    public ISphereConnectionPropertyPage() {
        super();

        manager = ConnectionManager.getInstance();
    }

    protected void performDefaults() {

        delegate.performDefaults();

        super.performDefaults();
    }

    public boolean performOk() {

        if (!delegate.performOk()) {
            return false;
        }

        performSave();

        return true;
    }

    @Override
    protected Control createContentArea(Composite container) {

        delegate = new ISphereConnectionPropertyPageDelegate(this, getConnectionName());

        Control parent = delegate.createContentArea(container);

        delegate.setScreenToValues(connectionProperties);
        delegate.setControlEnablement();

        return parent;
    }

    private void performSave() {

        delegate.performSave(connectionProperties);

        manager.saveConnectionProperties(getConnectionName());
    }

    @Override
    protected boolean verifyPageContents() {

        if (!delegate.verifyPageContents()) {
            return false;
        }

        return true;
    }

    private String getConnectionName() {

        Object element = getElement();
        if (element instanceof SystemConnection) {
            SystemConnection host = (SystemConnection)element;
            return ConnectionManager.getConnectionName(host);
        }

        return null;
    }

    @Override
    public void setElement(IAdaptable element) {
        super.setElement(element);

        connectionProperties = manager.getConnectionProperties(getConnectionName());
    }
}