/*******************************************************************************
 * Copyright (c) 2012-2024 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.core.helpbuilder.model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import biz.isphere.core.helpbuilder.configuration.Configuration;
import biz.isphere.core.helpbuilder.exception.JobCanceledException;
import biz.isphere.core.helpbuilder.utils.FileUtil;
import biz.isphere.core.helpbuilder.utils.LogUtil;

public class Project {

    private String pluginPath;
    private String id;
    private String[] tocs;
    private Bundle bundle;
    private String helpDirectory;

    public Project(String configString) throws JobCanceledException {

        String workspacePath = Configuration.getInstance().getWorkspace().getPath();

        if ("\\".equals(File.separator)) {
            pluginPath = configString.replaceAll("/", "\\\\");
        } else {
            pluginPath = configString.replaceAll("/", File.separator);
        }

        pluginPath = workspacePath + File.separator + pluginPath;

        tocs = findTocs(pluginPath);
        id = getProjectId(pluginPath);
        helpDirectory = findHelpDirectory(pluginPath);
    }

    public String getPluginPath() {
        return pluginPath;
    }

    public String getId() {
        return id;
    }

    public String[] getTocs() {
        return tocs;
    }

    public String getHelpDirectory() {
        return helpDirectory;
    }

    private String getProjectId(String projectPath) throws JobCanceledException {
        return getOrLoadBundle(projectPath).getSymbolicName();
    }

    private String[] findTocs(String path) throws JobCanceledException {
        File tocDir = new File(path, "toc");
        String[] tocFiles = tocDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.startsWith("toc") && name.endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });

        if (tocFiles == null || tocFiles.length == 0) {
            throw new JobCanceledException("Table of content file not found.");
        }

        try {

            for (int i = 0; i < tocFiles.length; i++) {
                tocFiles[i] = FileUtil.resolvePath(tocDir.getPath(), tocFiles[i]);
                LogUtil.debug("Added table of content file: " + tocFiles[i]);
            }

            return tocFiles;

        } catch (IOException e) {
            throw new JobCanceledException("Table of content file not found.", e);
        }
    }

    private String findManifest(String path) throws IOException {

        File metainfDir = new File(path);
        String[] manifestFiles = metainfDir.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (name.equalsIgnoreCase("MANIFEST.MF")) {
                    return true;
                }
                return false;
            }
        });

        if (manifestFiles.length != 1) {
            return null;
        }

        return FileUtil.resolvePath(path, manifestFiles[0]);
    }

    private String findHelpDirectory(String path) throws JobCanceledException {

        try {
            LogUtil.debug("Resolving path of help directory: " + path + "...");
            String resolvedPath = FileUtil.resolvePath(path, "html");
            LogUtil.debug("... using help files stored in: " + resolvedPath);
            return resolvedPath;
        } catch (IOException e) {
            throw new JobCanceledException("Table of content file not found.", e);
        }
    }

    private Bundle getOrLoadBundle(String projectPath) throws JobCanceledException {

        if (bundle == null) {
            try {
                String manifest = findManifest(projectPath + File.separator + "META-INF");
                bundle = new Bundle(manifest);
            } catch (IOException e) {
                throw new JobCanceledException(e.getLocalizedMessage(), e);
            }
        }

        return bundle;
    }

    @Override
    public String toString() {
        return getPluginPath() + " (" + getId() + ")";
    }
}
