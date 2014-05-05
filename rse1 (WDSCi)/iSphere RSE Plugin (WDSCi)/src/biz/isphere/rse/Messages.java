/*******************************************************************************
 * Copyright (c) 2012-2013 Task Force IT-Consulting GmbH, Waltrop and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Task Force IT-Consulting GmbH - initial API and implementation
 *******************************************************************************/

package biz.isphere.rse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "biz.isphere.rse.messages";

    public static String iSphere_Message_File_Editor;

    public static String iSphere_Compare_Editor;

    public static String iSphere_Binding_Directory_Editor;

    public static String iSphere_Source_File_Search;

    public static String Right;

    public static String Ancestor;

    public static String E_R_R_O_R;

    public static String Resources_with_different_connections_have_been_selected;

    public static String iSphere_Message_File_Search;

    public static String Deleting_spooled_files;

    public static String Deleting;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

}
