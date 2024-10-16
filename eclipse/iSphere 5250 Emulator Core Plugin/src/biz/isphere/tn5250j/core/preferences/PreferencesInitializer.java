/*******************************************************************************
 * Copyright (c) 2012-2024 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.tn5250j.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import biz.isphere.core.versionupdate.PreferencesUpdater;

/**
 * Class used to initialize default preference values.
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

    private static boolean performUpdate_v5210 = false;

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
     * initializeDefaultPreferences()
     */
    @Override
    public void initializeDefaultPreferences() {
        Preferences.getInstance().initializeDefaultPreferences();
        PreferencesUpdater.update();
        PreferencesUpdater.displayUpdateInformation();
    }
}
