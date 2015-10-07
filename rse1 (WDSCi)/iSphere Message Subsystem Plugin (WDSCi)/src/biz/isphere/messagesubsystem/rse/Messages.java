/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     iSphere Project Owners - Maintenance and enhancements
 *******************************************************************************/
package biz.isphere.messagesubsystem.rse;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "biz.isphere.messagesubsystem.rse.messages"; //$NON-NLS-1$

    public static String My_Messages;

    public static String Message_Filter;

    public static String Message_Filter_Dots;

    public static String Create_a_new_filter_to_list_messages;

    public static String Change_Message_Filter;

    public static String Message_queue_colon;

    public static String Library_colon;

    public static String From_user_colon;

    public static String Message_ID_colon;

    public static String Severity_threshold_colon;

    public static String From_job_colon;

    public static String From_job_number_colon;

    public static String From_program_colon;

    public static String Message_text_contains_colon;

    public static String Message_type_colon;

    public static String Message_queue_and_library_must_be_specified;

    public static String Delete_Message_Error;

    public static String From;

    public static String Message_ID;

    public static String Severity;

    public static String Message_type;

    public static String Date_sent;

    public static String From_job;

    public static String From_job_number;

    public static String From_program;

    public static String Reply_status;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
