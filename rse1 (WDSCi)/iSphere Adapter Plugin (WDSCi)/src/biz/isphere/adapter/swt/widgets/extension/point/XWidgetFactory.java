/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.adapter.swt.widgets.extension.point;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import biz.isphere.adapter.swt.widgets.XFileDialog;
import biz.isphere.core.swt.widgets.extension.point.IDateEdit;
import biz.isphere.core.swt.widgets.extension.point.IFileDialog;
import biz.isphere.core.swt.widgets.extension.point.ITimeEdit;
import biz.isphere.core.swt.widgets.extension.point.IWidgetFactory;

public class XWidgetFactory implements IWidgetFactory {

    public IFileDialog getDialog(Shell aParent, int aStyle) {
        return new XFileDialog(aParent, aStyle);
    }

    public IFileDialog getDialog(Shell aParent) {
        return new XFileDialog(aParent);
    }

    public IDateEdit getDateEdit(Composite aParent, int style) {
        return null;
    }

    public ITimeEdit getTimeEdit(Composite aParent, int style) {
        return null;
    }

}
