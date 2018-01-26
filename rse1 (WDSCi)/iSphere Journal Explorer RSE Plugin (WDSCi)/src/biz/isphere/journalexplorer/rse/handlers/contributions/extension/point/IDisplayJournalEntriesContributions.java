/*******************************************************************************
 * Copyright (c) 2012-2018 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.journalexplorer.rse.handlers.contributions.extension.point;

public interface IDisplayJournalEntriesContributions {

    /**
     * Handles displaying of file journal entries.
     * 
     * @param connectionName -
     * @param libraryName -
     * @param fileName -
     * @param memberName -
     */
    public void handleDisplayFileJournalEntries(String connectionName, String libraryName, String fileName, String memberName);
}
