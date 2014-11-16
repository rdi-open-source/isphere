/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.dataspaceeditor.action;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.ui.action.WorkbenchWindowActionDelegate;
import org.eclipse.jface.action.IAction;

import biz.isphere.core.dataspaceeditordesigner.rse.OpenDataSpaceEditorDesignerHandler;

public class OpenDataSpaceEditorDesignerAction extends WorkbenchWindowActionDelegate {

    public static final String ID = "biz.isphere.rse.dataspaceeditor.action.OpenDataSpaceEditorDesignerAction";

    public void run(IAction action) {
        try {
            OpenDataSpaceEditorDesignerHandler handler = new OpenDataSpaceEditorDesignerHandler();
            ExecutionEvent event = new ExecutionEvent();
            handler.execute(event);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
