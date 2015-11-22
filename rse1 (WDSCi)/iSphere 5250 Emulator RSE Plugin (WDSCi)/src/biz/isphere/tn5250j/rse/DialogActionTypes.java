/*******************************************************************************
 * Copyright (c) 2012-2015 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.tn5250j.rse;

public class DialogActionTypes {

    public static final int CREATE = 1;
    public static final int CHANGE = 2;
    public static final int COPY = 3;
    public static final int DELETE = 4;
    public static final int DISPLAY = 5;

    public static String getText(int actionType) {
        switch (actionType) {
        case CREATE: {
            return Messages.getString("CREATEX");
        }
        case CHANGE: {
            return Messages.getString("CHANGEX");
        }
        case COPY: {
            return Messages.getString("COPYX");
        }
        case DELETE: {
            return Messages.getString("DELETEX");
        }
        case DISPLAY: {
            return Messages.getString("DISPLAYX");
        }
        }
        return "";
    }
}
