/*******************************************************************************
 * Copyright (c) 2012-2021 iSphere Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer.spooledfiles.popupmenu.extension;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import biz.isphere.core.spooledfiles.ISpooledFileBrief;
import biz.isphere.core.spooledfiles.popupmenu.extension.point.ISpooledFilePopupMenuContributionItem;
import biz.isphere.joblogexplorer.ISphereJobLogExplorerPlugin;
import biz.isphere.joblogexplorer.Messages;
import biz.isphere.joblogexplorer.externalapi.Access;

public class OpenJobLogExplorerContributionItem implements ISpooledFilePopupMenuContributionItem {

    private static final String QPJOBLOG = "QPJOBLOG"; //$NON-NLS-1$

    private Shell shell;
    private ISpooledFileBrief[] spooledFiles;

    public String getText() {
        return Messages.Job_Log_Explorer;
    }

    public String getTooltipText() {
        return null;
    }

    public Image getImage() {
        return ISphereJobLogExplorerPlugin.getDefault().getImage(ISphereJobLogExplorerPlugin.IMAGE_JOB_LOG_EXPLORER);
    }

    public void setSelection(Shell shell, final ISpooledFileBrief[] spooledFiles) {
        this.shell = shell;
        this.spooledFiles = spooledFiles;
    }

    public boolean isEnabled() {

        if (spooledFiles.length != 1) {
            return false;
        }

        for (ISpooledFileBrief spooledFile : spooledFiles) {
            if (!QPJOBLOG.equals(spooledFile.getFile())) { //$NON-NLS-1$
                return false;
            }
        }

        return true;
    }

    public void execute() throws Exception {

        if (spooledFiles == null || spooledFiles.length == 0) {
            return;
        }

        for (ISpooledFileBrief spooledFile : spooledFiles) {
            Access.openJobLogExplorer(shell, spooledFile);
        }
    }
}
