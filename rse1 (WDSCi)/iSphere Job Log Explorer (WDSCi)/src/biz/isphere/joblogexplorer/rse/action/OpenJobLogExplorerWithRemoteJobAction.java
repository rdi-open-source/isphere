/*******************************************************************************
 * Copyright (c) 2012-2019 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.joblogexplorer.rse.action;

import biz.isphere.joblogexplorer.action.rse.AbstractOpenJobLogExplorerAction;
import biz.isphere.joblogexplorer.jobs.rse.JobLogActiveJobLoader;

import com.ibm.etools.iseries.comm.interfaces.ISeriesJobName;
import com.ibm.etools.iseries.core.api.ISeriesJob;
import com.ibm.etools.systems.dstore.core.model.DataElement;

public class OpenJobLogExplorerWithRemoteJobAction extends AbstractOpenJobLogExplorerAction {

    public static final String ID = "biz.isphere.joblogexplorer.rse.action.OpenJobLogExplorerWithRemoteJobAction"; //$NON-NLS-1$

    @Override
    protected void execute(Object object) {
        if (object instanceof DataElement) {
            DataElement dataElement = (DataElement)object;
            ISeriesJob remoteJob = new ISeriesJob(dataElement);
            String connectionName = remoteJob.getCommandSubSystem().getSystemConnectionName();
            ISeriesJobName jobName = new ISeriesJobName(remoteJob.getFullJobName());
            JobLogActiveJobLoader job = new JobLogActiveJobLoader(connectionName, jobName.getName(), jobName.getUser(), jobName.getNumber());
            job.run();
        }
    }

}
