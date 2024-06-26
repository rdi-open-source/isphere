/*******************************************************************************
 * Copyright (c) 2005 SoftLanding Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     SoftLanding - initial API and implementation
 *     iSphere Project Owners - Maintenance and enhancements
 *******************************************************************************/

package biz.isphere.messagesubsystem.rse.internal;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rse.ui.SystemMenuManager;
import org.eclipse.rse.ui.view.AbstractSystemViewAdapter;
import org.eclipse.rse.ui.view.ISystemRemoteElementAdapter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import biz.isphere.messagesubsystem.rse.IQueuedMessageResource;
import biz.isphere.messagesubsystem.rse.IQueuedMessageSubsystem;
import biz.isphere.messagesubsystem.rse.ISphereMessageSubsystemRSEPlugin;
import biz.isphere.messagesubsystem.rse.QueuedMessageResourceAdapterDelegate;

import com.ibm.as400.access.QueuedMessage;

public class QueuedMessageResourceAdapter extends AbstractSystemViewAdapter implements ISystemRemoteElementAdapter {

    private QueuedMessageResourceAdapterDelegate delegate;

    public QueuedMessageResourceAdapter() {
        super();

        delegate = new QueuedMessageResourceAdapterDelegate();
    }

    @Override
    public void addActions(SystemMenuManager menu, IStructuredSelection selection, Shell parent, String menuGroup) {
    }

    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        IQueuedMessageResource queuedMessageResource = (IQueuedMessageResource)object;
        if (queuedMessageResource.isInquiryMessage()) {
            if (queuedMessageResource.isPendingReply()) {
                return ISphereMessageSubsystemRSEPlugin.getDefault().getImageRegistry().getDescriptor(ISphereMessageSubsystemRSEPlugin.IMAGE_INQUIRY);
            } else {
                return ISphereMessageSubsystemRSEPlugin.getDefault().getImageRegistry()
                    .getDescriptor(ISphereMessageSubsystemRSEPlugin.IMAGE_INQUIRY_ANSWERED);
            }
        } else {
            return ISphereMessageSubsystemRSEPlugin.getDefault().getImageRegistry().getDescriptor(ISphereMessageSubsystemRSEPlugin.IMAGE_MESSAGE);
        }
    }

    @Override
    public boolean handleDoubleClick(Object object) {
        return delegate.handleDoubleClick(object);
    }

    public String getText(Object element) {
        return delegate.getText(element);
    }

    public String getAbsoluteName(Object object) {
        return delegate.getAbsoluteName(object);
    }

    @Override
    public String getType(Object element) {
        return delegate.getType();
    }

    @Override
    public Object getParent(Object element) {
        return delegate.getParent();
    }

    @Override
    public boolean hasChildren(IAdaptable element) {
        return delegate.hasChildren();
    }

    @Override
    public boolean showDelete(Object element) {
        return delegate.showDelete();
    }

    @Override
    public boolean canDelete(Object element) {
        return delegate.canDelete();
    }

    @Override
    public boolean doDelete(Shell shell, Object element, IProgressMonitor monitor) {

        QueuedMessageResource queuedMessageResource = (QueuedMessageResource)element;
        QueuedMessage queuedMessage = queuedMessageResource.getQueuedMessage();
        IQueuedMessageSubsystem messageSubSystem = (QueuedMessageSubSystem)queuedMessageResource.getSubSystem();

        return delegate.doDelete(shell, messageSubSystem, queuedMessage);
    }

    @Override
    public Object[] getChildren(IAdaptable paramIAdaptable, IProgressMonitor paramIProgressMonitor) {
        return delegate.getChildren();
    }

    @Override
    protected IPropertyDescriptor[] internalGetPropertyDescriptors() {
        return delegate.internalGetPropertyDescriptors();
    }

    @Override
    public Object internalGetPropertyValue(Object propKey) {

        IQueuedMessageResource queuedMessage = (IQueuedMessageResource)propertySourceInput;

        return delegate.internalGetPropertyValue(queuedMessage, propKey);
    }

    public String getAbsoluteParentName(Object element) {
        return delegate.getAbsoluteParentName();
    }

    public String getSubSystemFactoryId(Object element) {
        return QueuedMessageSubSystemFactory.ID;
    }

    public String getRemoteTypeCategory(Object element) {
        return delegate.getRemoteTypeCategory();
    }

    public String getRemoteType(Object element) {
        return delegate.getRemoteType();
    }

    public String getRemoteSubType(Object arg0) {
        return delegate.getRemoteSubType();
    }

    public boolean refreshRemoteObject(Object oldElement, Object newElement) {
        return delegate.refreshRemoteObject(oldElement, newElement);
    }

    public Object getRemoteParent(Object paramObject, IProgressMonitor paramIProgressMonitor) throws Exception {
        return delegate.getRemoteParent();
    }

    public String[] getRemoteParentNamesInUse(Object paramObject, IProgressMonitor paramIProgressMonitor) throws Exception {
        return delegate.getRemoteParentNamesInUse();
    }

    /*
     * Start of RDi/WDSCi specific methods.
     */

    public String getSubSystemConfigurationId(Object arg0) {
        return null;
    }
}