/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.lpex.tasktags.listener;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;

/**
 * This class is used to setup the LPEX Document Listener when a document is
 * created or opened.
 * 
 * @author Thomas Raddatz
 */
public class LPEXDocumentSetupParticipant implements IDocumentSetupParticipant {

    /**
     * Called by the Resource Text File Buffer Manager when a new instance of a
     * document is created. The purpose of this procedure is register a File
     * Buffer Listener, which is responsible to scan the document for LPEX task
     * tags.
     */
    public void setup(IDocument document) {
        LPEXDocumentListener tListener = new LPEXDocumentListener();
        tListener.setDocument(document);

        FileBuffers.getTextFileBufferManager().addFileBufferListener(tListener);
        document.addDocumentListener(tListener);
    }
}