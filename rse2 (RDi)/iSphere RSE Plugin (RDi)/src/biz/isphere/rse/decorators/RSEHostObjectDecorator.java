/*******************************************************************************
 * Copyright (c) 2012-2016 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.decorators;

import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

import biz.isphere.rse.preferences.Preferences;

import com.ibm.etools.iseries.comm.interfaces.IISeriesHostObjectBrief;

/**
 * This class decorates the RSE tree with the object decoration of objects of
 * type <i>IISeriesHostObjectBrief</i>. It shows the object description next to
 * the element.
 * <p>
 * This class has been inspired by <a
 * href="https://www.eclipse.org/articles/Article-Decorators/decorators.html"
 * >Understanding Decorators in Eclipse</a>.
 */
public class RSEHostObjectDecorator implements ILightweightLabelDecorator {

    public static final String ID = "biz.isphere.core.decorators.RSEHostObjectDecorator";

    /**
     * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
     *      org.eclipse.jface.viewers.IDecoration)
     */
    public void decorate(Object object, IDecoration decoration) {

        // Get the resource
        IISeriesHostObjectBrief tResource = getResource(object);
        if (tResource == null) {
            return;
        }

        // System.out.println(tResource.getName() + ", type/sub type: " +
        // tResource.getType() + "/" + tResource.getSubType());

        String mask;
        if (isLibrary(tResource)) {
            mask = " - \"&T\""; //$NON-NLS-1$
        } else if (isExtendedSourceMemberDecoration(tResource) || isExtendedDataMemberDecoration(tResource)) {
            mask = " - \"&T\" - (&L/&F)"; //$NON-NLS-1$
        } else if (isExtendedObjectDecoration(tResource)) {
            mask = " - \"&T\" - (&L)"; //$NON-NLS-1$
        } else {
            mask = " - \"&T\""; //$NON-NLS-1$
        }

        try {

            StringBuilder buffer = new StringBuilder(mask);

            int i = 0;
            int s = 0;
            while ((i = buffer.indexOf("&", s)) >= 0 && i < buffer.length() - 1) {
                String replacement = null;
                String ch = buffer.substring(i + 1, i + 2).toUpperCase();
                if ("&".equals(ch)) {
                    replacement = "&";
                } else if ("T".equals(ch)) {
                    replacement = tResource.getDescription();
                } else if ("L".equals(ch)) {
                    replacement = tResource.getLibrary();
                } else if ("F".equals(ch)) {
                    replacement = tResource.getFile();
                }
                if (replacement != null) {
                    buffer.replace(i, i + 2, replacement);
                }
                if (replacement != null && replacement.length() > 0) {
                    s = i + replacement.length();
                } else {
                    s = i + 1;
                }
            }

            if (buffer.length() > 0) {
                decoration.addSuffix(buffer.toString());
            }

            return;

        } catch (Exception e) {
            // ignore all errors
        }
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener arg0) {
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
        /*
         * Disposal of images present in the image registry can be performed in
         * this method
         */
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
     *      java.lang.String)
     */
    public boolean isLabelProperty(Object arg0, String arg1) {
        return false;
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener arg0) {
    }

    /**
     * Returns the resource for the given input object, or null if there is no
     * resource associated with it.
     * 
     * @param object the object to find the resource for
     * @return the resource for the given object, or null
     */
    private IISeriesHostObjectBrief getResource(Object object) {
        if (object instanceof IISeriesHostObjectBrief) {
            return (IISeriesHostObjectBrief)object;
        }
        return null;
    }

    private boolean isExtendedObjectDecoration(IISeriesHostObjectBrief tResource) {

        Preferences preferences = Preferences.getInstance();
        if (!isLibrary(tResource) && preferences.isObjectDecorationExtension()) {
            return true;
        }

        return false;
    }

    private boolean isExtendedSourceMemberDecoration(IISeriesHostObjectBrief tResource) {

        Preferences preferences = Preferences.getInstance();
        if (isSourceMember(tResource) && preferences.isSourceMemberDecorationExtension()) {
            return true;
        }

        return false;
    }

    private boolean isExtendedDataMemberDecoration(IISeriesHostObjectBrief tResource) {

        Preferences preferences = Preferences.getInstance();
        if (isDataMember(tResource) && preferences.isDataMemberDecorationExtension()) {
            return true;
        }

        return false;
    }

    private boolean isLibrary(IISeriesHostObjectBrief tResource) {

        if ("*LIB".equals(tResource.getType())) {
            return true;
        }

        return false;
    }

    private boolean isSourceMember(IISeriesHostObjectBrief tResource) {

        if (tResource.getFile() != null && "SRC".equals(tResource.getSubType())) {
            return true;
        }

        return false;
    }

    private boolean isDataMember(IISeriesHostObjectBrief tResource) {

        if (tResource.getFile() != null && "DTA".equals(tResource.getSubType())) {
            return true;
        }

        return false;
    }

}
