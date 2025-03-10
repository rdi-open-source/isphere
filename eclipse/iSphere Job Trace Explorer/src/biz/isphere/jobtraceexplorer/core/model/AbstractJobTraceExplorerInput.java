/*******************************************************************************
 * Copyright (c) 2012-2021 iSphere Project Team
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.jobtraceexplorer.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AbstractJobTraceExplorerInput {

    public abstract String getName();

    public abstract String getToolTipText();

    public abstract String getContentId();

    public boolean isSameInput(AbstractJobTraceExplorerInput otherInput) {

        if (otherInput == null) {
            return false;
        }

        String otherContentId = otherInput.getContentId();
        String contentId = getContentId();

        if (otherContentId == null && contentId == null) {
            return true;
        }

        if (contentId == null) {
            return false;
        }

        return contentId.equals(otherContentId);
    }

    public abstract JobTraceSession load(IProgressMonitor arg0) throws Exception;

}
