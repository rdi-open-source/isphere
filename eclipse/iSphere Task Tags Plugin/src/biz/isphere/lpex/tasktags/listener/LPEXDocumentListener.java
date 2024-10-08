/*******************************************************************************
 * Copyright (c) 2012-2022 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.lpex.tasktags.listener;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorPart;

import biz.isphere.base.internal.UIHelper;
import biz.isphere.lpex.tasktags.job.DocumentScannerJob;
import biz.isphere.lpex.tasktags.model.LPEXTaskManager;
import biz.isphere.lpex.tasktags.preferences.Preferences;

/**
 * Class, that works as the adapter between the document and the LPEX Task
 * Manager. The purpose of this class is to update the LPEX Task Tags when a
 * document is opened or saved.
 */
public class LPEXDocumentListener implements IDocumentListener, IFileBufferListener {

    private IDocument document;
    private IResource resource;

    private Job scannerJob;

    public void setDocument(IDocument aDocument) {
        document = aDocument;
    }

    /**
     * IDocumentListener: Added as indicator interface to be able to use this
     * class as a FileBufferListener for a given document.
     * 
     * @see LPEXDocumentSetupParticipant#setup(IDocument)
     */
    public void documentAboutToBeChanged(DocumentEvent anEvent) {
    }

    /**
     * IDocumentListener: Added as indicator interface to be able to use this
     * class as a FileBufferListener for a given document.
     * 
     * @see LPEXDocumentSetupParticipant#setup(IDocument)
     */
    public void documentChanged(DocumentEvent anEvent) {

        if (isScanOnChangeEnabled()) {
            IEditorPart activeEditor = UIHelper.getActiveEditor();
            if (activeEditor != null) {
                processDocument();
            }
        }
    }

    /**
     * Called by the File Buffer Manager, when a file buffer is created, e.g. a
     * document is loaded into the editor. Used to get the resource the marker
     * tags belong to.
     */
    public void bufferCreated(IFileBuffer aBuffer) {
        if (isFileBufferValid(aBuffer)) {
            resource = FileBuffers.getWorkspaceFileAtLocation(aBuffer.getLocation());
            processDocument();
        }
    }

    /**
     * Called by the File Buffer Manager, on a closed document when the file
     * buffer is disposed. used to clear the reference to the document and the
     * resource, as the owner, of the marker tags.
     */
    public void bufferDisposed(IFileBuffer aBuffer) {
        if (isFileBufferValid(aBuffer)) {
            FileBuffers.getTextFileBufferManager().removeFileBufferListener(this);
            processDocumentOnClose();
            document = null;
            resource = null;
        }
    }

    /**
     * Called by the File Buffer Manager when the "dirty" state of the document
     * changes. Used to scan the document for marker tags after the document has
     * been saved (dirty flag changed to false).
     */
    public void dirtyStateChanged(IFileBuffer aBuffer, boolean isDirty) {
        if (!isDirty) {
            if (isFileBufferValid(aBuffer)) {
                processDocument();
            }
        }
    }

    /**
     * Starts a background job in order to process the document.
     */
    private void processDocument() {

        if (scannerJob != null) {
            scannerJob.cancel();
        }

        LPEXTaskManager lpexTaskManager = new LPEXTaskManager(resource, document);
        scannerJob = new DocumentScannerJob(lpexTaskManager);
        scannerJob.schedule(getScanDelayMSecs());
    }

    /**
     * Starts a background job in order to process the document, when the
     * document is closed.
     */
    private void processDocumentOnClose() {

        if (scannerJob != null) {
            scannerJob.cancel();
        }

        LPEXTaskManager lpexTaskManager = new LPEXTaskManager(resource, document);
        boolean isRemoveOnClose = Preferences.getInstance().isRemoveOnCloseEnabled();
        scannerJob = new DocumentScannerJob(lpexTaskManager, isRemoveOnClose);
        scannerJob.schedule(getScanDelayMSecs());
    }

    /**
     * Returns <code>true</code>, if a given file buffer matches the document,
     * else <code>false</code>.
     * 
     * @param aBuffer file buffer.
     * @return <code>true</code>, if the file buffer matches the document.
     */
    private boolean isFileBufferValid(IFileBuffer aBuffer) {
        return ((ITextFileBuffer)aBuffer).getDocument() == document;
    }

    private int getScanDelayMSecs() {

        if (isScanOnChangeEnabled()) {
            return 250;
        } else {
            return 0;
        }
    }

    private boolean isScanOnChangeEnabled() {
        return Preferences.getInstance().isScanOnChangeEnabled();
    }

    public void bufferContentAboutToBeReplaced(IFileBuffer aBuffer) {
    }

    public void bufferContentReplaced(IFileBuffer aBuffer) {
    }

    public void stateChanging(IFileBuffer aBuffer) {
    }

    public void stateValidationChanged(IFileBuffer aBuffer, boolean anIsStateValidated) {
    }

    public void underlyingFileMoved(IFileBuffer aBuffer, IPath aPath) {
    }

    public void underlyingFileDeleted(IFileBuffer aBuffer) {
    }

    public void stateChangeFailed(IFileBuffer aBuffer) {
    }
}
