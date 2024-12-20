/*******************************************************************************
 * Copyright (c) 2012-2021 iSphere Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.spooledfiles.popupmenu.extension.point;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import biz.isphere.core.spooledfiles.ISpooledFileBrief;

;

public interface ISpooledFilePopupMenuContributionItem {

    public String getText();

    public String getTooltipText();

    public Image getImage();

    public void setSelection(Shell shell, ISpooledFileBrief[] spooledFiles);

    public boolean isEnabled();

    public void execute() throws Exception;

}
