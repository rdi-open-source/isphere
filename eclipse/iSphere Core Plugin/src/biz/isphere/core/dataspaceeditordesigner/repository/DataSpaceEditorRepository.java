/*******************************************************************************
 * Copyright (c) 2012-2023 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.dataspaceeditordesigner.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.FileLocator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import biz.isphere.base.internal.FileHelper;
import biz.isphere.base.internal.StringHelper;
import biz.isphere.base.internal.UIHelper;
import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.Messages;
import biz.isphere.core.dataspaceeditordesigner.model.DBoolean;
import biz.isphere.core.dataspaceeditordesigner.model.DDecimal;
import biz.isphere.core.dataspaceeditordesigner.model.DEditor;
import biz.isphere.core.dataspaceeditordesigner.model.DInteger;
import biz.isphere.core.dataspaceeditordesigner.model.DLongInteger;
import biz.isphere.core.dataspaceeditordesigner.model.DReferencedObject;
import biz.isphere.core.dataspaceeditordesigner.model.DShortInteger;
import biz.isphere.core.dataspaceeditordesigner.model.DText;
import biz.isphere.core.dataspaceeditordesigner.model.DTinyInteger;
import biz.isphere.core.dataspaceeditordesigner.model.DataSpaceEditorManager;
import biz.isphere.core.internal.ISeries;
import biz.isphere.core.internal.ObjectHelper;
import biz.isphere.core.internal.RemoteObject;
import biz.isphere.core.internal.exception.DeleteFileException;
import biz.isphere.core.internal.exception.SaveFileException;
import biz.isphere.core.preferences.DoNotAskMeAgain;
import biz.isphere.core.preferences.DoNotAskMeAgainDialog;

/**
 * Class that manages holds the available editors for 'data space objects'.
 * <p>
 * It is separated from the {@link DataSpaceEditorManager} and the model classes
 * to enforce using the {@link DataSpaceEditorManager}.
 */
public final class DataSpaceEditorRepository {

    private static final String REPOSITORY_LOCATION = "dataSpaceEditors";

    private static final String FILE_EXTENSION = "dtaspced";

    /**
     * The instance of this Singleton class.
     */
    private static DataSpaceEditorRepository instance;

    private String repositoryLocation;
    private Map<String, DEditor> dEditors;
    private DataSpaceEditorManager manager;

    private Date lastAskedTime;

    /**
     * Private constructor to ensure the Singleton pattern.
     */
    private DataSpaceEditorRepository() {
        String path = ISpherePlugin.getDefault().getStateLocation().toFile().getAbsolutePath();
        path = path + File.separator + REPOSITORY_LOCATION + File.separator;
        repositoryLocation = path;

        dEditors = new HashMap<String, DEditor>();
        manager = new DataSpaceEditorManager();
    }

    public void resetLastAsked() {
        lastAskedTime = null;
    }

    /**
     * Thread-safe method that returns the instance of this Singleton class.
     */
    public synchronized static DataSpaceEditorRepository getInstance() {
        if (instance == null) {
            instance = new DataSpaceEditorRepository();
        }
        return instance;
    }

    /**
     * Updates a given dialog or adds the dialog to the repository if the dialog
     * does not yet exist.
     * 
     * @param dEditor - Existing or new dialog
     * @throws SaveFileException
     */
    public void updateOrAddDialog(DEditor dEditor) throws SaveFileException {
        saveToXml(dEditor);
        getOrLoadEditors().put(dEditor.getKey(), dEditor);
    }

    public void deleteEditor(DEditor dEditor) throws DeleteFileException {
        deleteEditorOnDisk(dEditor);
        getOrLoadEditors().remove(dEditor.getKey());
    }

    /**
     * Returns the editor that is assigned to the specified key. The value of
     * key must match {@link DEditor#getKey()}.
     * 
     * @param key - key of an existing editor
     * @return the editor that is assigned to the key
     */
    public DEditor getDataSpaceEditor(String key) {

        return getOrLoadEditors().get(key);
    }

    /**
     * Returns all editors that exist in the repository.
     * 
     * @return list of existing editors
     */
    public DEditor[] getDataSpaceEditors() {
        return getDataSpaceEditorsInternal(false);
    }

    /**
     * Returns a copy of all editors that exist in the repository.
     * 
     * @return list of existing editors
     */
    public DEditor[] getCopyOfDataSpaceEditors() {
        return getDataSpaceEditorsInternal(true);
    }

    public DEditor[] getDataSpaceEditorsForObject(RemoteObject remoteObject) {
        Set<DEditor> dSelectedEditors = new TreeSet<DEditor>();

        for (DEditor dEditor : getDataSpaceEditorsInternal(false)) {
            if (editorSupportsObject(dEditor, remoteObject)) {
                dSelectedEditors.add(dEditor);
            }
        }

        return dSelectedEditors.toArray(new DEditor[dSelectedEditors.size()]);
    }

    public DEditor getDataSpaceEditorsForObject(RemoteObject remoteObject, String editorName) {

        for (DEditor dEditor : getDataSpaceEditorsInternal(false)) {
            if (editorSupportsObject(dEditor, remoteObject) && dEditor.getName().equals(editorName)) {
                return dEditor;
            }
        }

        return null;
    }

    public boolean editorSupportsObject(DEditor dEditor, RemoteObject remoteObject) {
        DReferencedObject[] referencedObjects = dEditor.getReferencedObjects();
        for (DReferencedObject dReferencedObject : referencedObjects) {
            if (remoteObjectMatchesReferencedObject(remoteObject, dReferencedObject)) {
                return true;
            }
        }
        return false;
    }

    private boolean remoteObjectMatchesReferencedObject(RemoteObject remoteObject, DReferencedObject dReferencedObject) {
        if (!remoteObject.getObjectType().equals(dReferencedObject.getType())) {
            return false;
        }
        if (!remoteObject.getName().equals(dReferencedObject.getName())) {
            return false;
        }
        if (!isLibrarySpecialValue(dReferencedObject.getLibrary())) {
            if (!remoteObject.getLibrary().equals(dReferencedObject.getLibrary())) {
                return false;
            }
        }
        return true;
    }

    private boolean isLibrarySpecialValue(String library) {
        if (ISeries.SPCVAL_LIBL.equals(library)) {
            return true;
        }
        return false;
    }

    private void saveToXml(DEditor dEditor) throws SaveFileException {

        File file = null;

        try {
            file = new File(getFileName(dEditor));
            FileOutputStream stream = new FileOutputStream(file);
            serialize(dEditor, stream);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            ISpherePlugin.logError("*** Could not save data space editor " + getNameOrDescription(dEditor) + " to XML ***", e); //$NON-NLS-1$
            throw new SaveFileException(file);
        }
    }

    private String getFileName(DEditor dEditor) {
        return getRepositoryLocation() + dEditor.getKey() + "." + FILE_EXTENSION;
    }

    private DEditor[] getDataSpaceEditorsInternal(boolean clone) {

        DEditor[] editorItems = new DEditor[getOrLoadEditors().size()];

        int i = 0;
        for (DEditor dEditor : getOrLoadEditors().values()) {
            if (clone) {
                editorItems[i] = ObjectHelper.cloneVO(dEditor);
            } else {
                editorItems[i] = dEditor;
            }
            i++;
        }

        manager.resolveObjectReferences(editorItems);

        return editorItems;
    }

    private Map<String, DEditor> getOrLoadEditors() {

        if (dEditors.size() == 0) {
            dEditors = loadEditors(getRepositoryLocation());
            if (dEditors.size() == 0) {
                if (lastAskedTime == null) {

                    lastAskedTime = Calendar.getInstance().getTime();

                    Boolean createExampleDataSpaceEditorsConfirmed = DoNotAskMeAgainDialog.openConfirm(UIHelper.getActiveShell(),
                        DoNotAskMeAgain.CONFIRM_CREATE_EXAMPLE_DATA_SPACE_EDITORS, Messages.Create_example_data_space_editors);

                    if (createExampleDataSpaceEditorsConfirmed) {
                        loadExampleEditors();
                    }
                }
            }
        }

        return dEditors;
    }

    private void loadExampleEditors() {

        try {

            // Load editors and save them to xml.
            Map<String, DEditor> dExampleEditors = loadEditors(getExamplesLocation());
            for (DEditor dEditor : dExampleEditors.values()) {
                saveToXml(dEditor);
            }

            dEditors.putAll(dExampleEditors);

        } catch (Exception e) {
            ISpherePlugin.logError("*** Failed loading data space example editors ***", e); //$NON-NLS-1$
        }
    }

    private String getExamplesLocation() {

        try {
            URL resourceUrl = getClass().getClassLoader().getResource("sample_dataSpaceEditors");
            return FileLocator.toFileURL(resourceUrl).getPath();
        } catch (Exception e) {
            ISpherePlugin.logError("*** Failed getting location of data space example editors ***", e); //$NON-NLS-1$
        }

        return null;
    }

    private Map<String, DEditor> loadEditors(String repositoryPath) {

        Map<String, DEditor> dEditors = new HashMap<String, DEditor>();

        FileFilter filter = new DynamicDialogFileFilter();
        File[] dEditorFiles = new File(repositoryPath).listFiles(filter);
        if (dEditorFiles == null) {
            return dEditors;
        }

        for (File dEditorFile : dEditorFiles) {
            DEditor dialog = loadFromXml(dEditorFile);
            if (!dEditors.containsKey(dialog.getKey())) {
                dEditors.put(dialog.getKey(), dialog);
            }
        }

        return dEditors;
    }

    private String getRepositoryLocation() {
        FileHelper.ensureDirectory(repositoryLocation);
        return repositoryLocation;
    }

    private DEditor loadFromXml(File file) {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            StringBuffer xml = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                xml.append(line);
            }
            reader.close();

            return (DEditor)getXStream().fromXML(xml.toString());

        } catch (Exception e) {
            ISpherePlugin.logError("*** Could not load XML of data space editor from file: " + getPath(file) + " ***", e);
            return null;
        }
    }

    private void deleteEditorOnDisk(DEditor dEditor) throws DeleteFileException {
        File dEditorFile = new File(getFileName(dEditor));
        if (dEditorFile.exists()) {
            dEditorFile.delete();
            if (dEditorFile.exists()) {
                throw new DeleteFileException(dEditorFile);
            }
        }
    }

    private void serialize(DEditor dEditor, OutputStream stream) {
        XStream xstream = getXStream();
        xstream.toXML(dEditor, stream);
    }

    private XStream getXStream() {
        XStream xstream = new XStream(new DomDriver());
        xstream.autodetectAnnotations(true);
        xstream.alias("dialog", DEditor.class);
        xstream.alias("referencedBy", DReferencedObject.class);
        xstream.alias("boolean", DBoolean.class);
        xstream.alias("longInt", DLongInteger.class);
        xstream.alias("integer", DInteger.class);
        xstream.alias("shortInt", DShortInteger.class);
        xstream.alias("tinyInt", DTinyInteger.class);
        xstream.alias("text", DText.class);
        xstream.alias("decimal", DDecimal.class);
        return xstream;
    }

    private String getNameOrDescription(DEditor dEditor) {

        if (dEditor == null) {
            return "[null]"; //$NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(dEditor.getDescription())) {
            return dEditor.getDescription();
        } else {
            return dEditor.getName();
        }
    }

    private String getPath(File file) {

        if (file == null) {
            return "[null]"; //$NON-NLS-1$
        }

        return file.getAbsolutePath();
    }

    /**
     * Thread-safe method that disposes the instance of this Singleton class.
     * <p>
     * This method is intended to be call from
     * {@link org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)}
     * to free the reference to itself.
     */
    public static void dispose() {
        if (instance != null) {
            instance = null;
        }
    }

    /**
     * Filter that filters the content of a directory for dialog files.
     */
    private class DynamicDialogFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            if (FILE_EXTENSION.equalsIgnoreCase(FileHelper.getFileExtension(pathname))) {
                return true;
            }
            return false;
        }
    }
}