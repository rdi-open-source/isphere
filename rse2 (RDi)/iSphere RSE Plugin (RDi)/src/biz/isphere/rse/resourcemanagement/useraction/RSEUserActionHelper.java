/*******************************************************************************
 * Copyright (c) 2012-2018 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.resourcemanagement.useraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;

import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.core.model.SystemStartHere;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionElement;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionManager;
import org.eclipse.rse.internal.useractions.ui.uda.SystemUDActionSubsystem;

import biz.isphere.core.ISpherePlugin;
import biz.isphere.core.resourcemanagement.filter.RSEProfile;
import biz.isphere.core.resourcemanagement.useraction.RSEDomain;
import biz.isphere.core.resourcemanagement.useraction.RSEUserAction;
import biz.isphere.rse.resourcemanagement.AbstractSystemHelper;

import com.ibm.etools.iseries.rse.ui.uda.QSYSUDActionSubsystemAdapter;

@SuppressWarnings("restriction")
public class RSEUserActionHelper extends AbstractSystemHelper {

    private static final String UA_ATTR_RUNENV = "RunEnv";
    private static final String UA_RUNENV_NORMAL = "normal";
    private static final String UA_RUNENV_BATCH = "batch";
    private static final String UA_RUNENV_INTERACTIVE = "interactive";

    public static RSEDomain[] getDomains(RSEProfile rseProfile) {

        ArrayList<RSEDomain> rseDomains = new ArrayList<RSEDomain>();

        ISystemProfile systemProfile = getSystemProfile(rseProfile.getName());
        if (systemProfile != null) {
            SystemUDActionManager userActionManager = getUserActionManager(systemProfile);

            if (userActionManager != null) {
                String[] domainNames = userActionManager.getActionSubSystem().getDomainNames();
                for (String domainName : domainNames) {
                    String profileName = rseProfile.getName();
                    int domainIndex = userActionManager.getActionSubSystem().mapDomainName(domainName);
                    String xlatedDomainName = RSEUserActionHelper.mapDomainName(profileName, domainIndex);
                    rseDomains.add(produceDomain(rseProfile, domainIndex, xlatedDomainName));
                }
            }
        }

        return rseDomains.toArray(new RSEDomain[rseDomains.size()]);
    }

    public static String mapDomainName(RSEDomain rseDomain) {

        String profileName = rseDomain.getProfile().getName();
        int domainIndex = rseDomain.getDomainType();

        return mapDomainName(profileName, domainIndex);
    }

    public static String mapDomainName(String profileName, int domainIndex) {

        try {
            return getUserActionManager(getSystemProfile(profileName)).getActionSubSystem().mapDomainXlatedName(domainIndex);
        } catch (Exception e) {
            ISpherePlugin.logError("*** Could not map domain type to domain name ***", e); //$NON-NLS-1$
            return Integer.toString(domainIndex);
        }
    }

    public static RSEUserAction[] getUserActions(RSEProfile rseProfile) {

        ArrayList<RSEUserAction> allUserActions = new ArrayList<RSEUserAction>();

        RSEDomain[] domains = getDomains(rseProfile);
        for (int idx1 = 0; idx1 < domains.length; idx1++) {
            RSEUserAction[] userActions = getUserActions(domains[idx1]);
            for (int idx2 = 0; idx2 < userActions.length; idx2++) {
                allUserActions.add(userActions[idx2]);
            }
        }

        RSEUserAction[] _userActions = new RSEUserAction[allUserActions.size()];
        allUserActions.toArray(_userActions);

        return _userActions;
    }

    public static RSEUserAction[] getUserActions(RSEDomain rseDomain) {

        ArrayList<RSEUserAction> rseUserActions = new ArrayList<RSEUserAction>();

        ISystemProfile systemProfile = getSystemProfile(rseDomain.getProfile().getName());
        if (systemProfile != null) {
            SystemUDActionManager userActionManager = getUserActionManager(systemProfile);
            if (userActionManager != null) {
                SystemUDActionElement[] userActions = userActionManager.getActions(new Vector(), systemProfile, rseDomain.getDomainType());
                for (SystemUDActionElement userAction : userActions) {
                    RSEUserAction rseUserAction = produceUserAction(rseDomain, userAction);
                    // Not required for RDi
                    // rseUserAction.setOrder(rseUserActions.size());
                    rseUserActions.add(rseUserAction);
                }
            }
        }

        return rseUserActions.toArray(new RSEUserAction[rseUserActions.size()]);
    }

    public static RSEUserAction getUserAction(RSEDomain rseDomain, String label) {

        RSEUserAction[] rseUserActions = getUserActions(rseDomain);
        for (RSEUserAction rseUserAction : rseUserActions) {
            if (rseUserAction.getLabel().equals(label)) {
                return rseUserAction;
            }
        }

        return null;
    }

    private static RSEDomain produceDomain(RSEProfile rseProfile, int domain, String name) {
        return new RSEDomain(rseProfile, domain, name);
    }

    private static RSEUserAction produceUserAction(RSEDomain domain, SystemUDActionElement systemUserAction) {

        RSEUserAction rseUserAction = new RSEUserAction(domain, systemUserAction.getLabel(), systemUserAction.getCommand(),
            systemUserAction.getAttribute(UA_ATTR_RUNENV, UA_RUNENV_NORMAL), systemUserAction.getPrompt(), systemUserAction.getRefresh(),
            systemUserAction.getShow(), systemUserAction.getSingleSelection(), systemUserAction.getCollect(), systemUserAction.getComment(),
            systemUserAction.getFileTypes(), systemUserAction.getVendor(), systemUserAction.getOriginalName(), systemUserAction.getOrder(),
            systemUserAction);

        return rseUserAction;
    }

    public static void createUserAction(RSEDomain rseDomain, String label, String commandString, String runEnvironment, boolean isPromptFirst,
        boolean isRefreshAfter, boolean isShowAction, boolean isSingleSelection, boolean isInvokeOnce, String comment, String[] fileTypes,
        String vendor, int order) {

        ISystemProfile systemProfile = getSystemProfile(rseDomain.getProfile().getName());
        if (systemProfile != null) {
            SystemUDActionManager userActionManager = getUserActionManager(systemProfile);
            if (userActionManager != null) {

                SystemUDActionElement userAction = userActionManager.addAction(systemProfile, label, rseDomain.getDomainType());
                userAction.setVendor(vendor);
                // Not required for RDi
                // userAction.setOrder(getNextOrderNumber(userActionManager,
                // rseDomain));

                setUserActionAttributes(commandString, runEnvironment, isPromptFirst, isRefreshAfter, isShowAction, isSingleSelection, isInvokeOnce,
                    comment, fileTypes, vendor, userAction);

                moveUserActionTo(userActionManager, userAction, order);

                saveUserActions(userActionManager, systemProfile);
            }
        }
    }

    private static void moveUserActionTo(SystemUDActionManager userActionManager, SystemUDActionElement userAction, int order) {

        SystemUDActionElement[] actions = userActionManager.getActions(new Vector<Object>(), userAction.getProfile(), userAction.getDomain());
        if (actions == null || actions.length == 0) {
            return;
        }

        Arrays.sort(actions, new Comparator<SystemUDActionElement>() {
            public int compare(SystemUDActionElement arg0, SystemUDActionElement arg1) {

                if (arg0 == null && arg1 == null) {
                    return 0;
                } else if (arg0 == null) {
                    return -1;
                } else if (arg1 == null) {
                    return 1;
                }

                if (arg0.getOrder() > arg1.getOrder()) {
                    return 1;
                } else if (arg0.getOrder() < arg1.getOrder()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        int i = actions.length - 1;
        while (i >= 0 && userAction.getOrder() > order) {
            userActionManager.moveElementUp(userAction);
            i--;
        }
    }

    private static int getNextOrderNumber(SystemUDActionManager userActionManager, RSEDomain rseDomain) {

        int lastOrderNumber = -1;

        RSEUserAction[] actions = getUserActions(rseDomain);
        if (actions.length == 0) {
            return 0;
        }

        for (RSEUserAction action : actions) {
            lastOrderNumber = Math.max(lastOrderNumber, action.getOrder());
        }

        lastOrderNumber++;

        return lastOrderNumber;
    }

    public static void deleteUserAction(RSEDomain rseDomain, String label) {

        ISystemProfile systemProfile = getSystemProfile(rseDomain.getProfile().getName());
        if (systemProfile != null) {
            SystemUDActionManager userActionManager = getUserActionManager(systemProfile);
            if (userActionManager != null) {
                SystemUDActionElement[] userActions = userActionManager.getActions(new Vector(), systemProfile, rseDomain.getDomainType());
                for (SystemUDActionElement userAction : userActions) {
                    if (userAction.getLabel().equals(label)) {
                        userActionManager.delete(systemProfile, userAction);
                        saveUserActions(userActionManager, systemProfile);
                    }
                }
            }
        }
    }

    public static void updateUserAction(RSEDomain rseDomain, String label, String commandString, String runEnvironment, boolean isPromptFirst,
        boolean isRefreshAfter, boolean isShowAction, boolean isSingleSelection, boolean isInvokeOnce, String comment, String[] fileTypes,
        String vendor, int order) {

        ISystemProfile systemProfile = getSystemProfile(rseDomain.getProfile().getName());
        if (systemProfile != null) {
            SystemUDActionManager userActionManager = getUserActionManager(systemProfile);
            if (userActionManager != null) {
                SystemUDActionElement[] userActions = userActionManager.getActions(new Vector(), systemProfile, rseDomain.getDomainType());
                for (SystemUDActionElement userAction : userActions) {
                    if (userAction.getLabel().equals(label)) {

                        setUserActionAttributes(commandString, runEnvironment, isPromptFirst, isRefreshAfter, isShowAction, isSingleSelection,
                            isInvokeOnce, comment, fileTypes, vendor, userAction);

                        moveUserActionTo(userActionManager, userAction, order);

                        saveUserActions(userActionManager, systemProfile);
                    }
                }
            }
        }
    }

    public static boolean hasUserActionManager(RSEProfile rseProfile) {

        ISystemProfile systemProfile = (ISystemProfile)rseProfile.getOrigin();

        if (getUserActionManager(systemProfile) != null) {
            return true;
        }

        return false;
    }

    private static void setUserActionAttributes(String commandString, String runEnvironment, boolean isPromptFirst, boolean isRefreshAfter,
        boolean isShowAction, boolean isSingleSelection, boolean isInvokeOnce, String comment, String[] fileTypes, String vendor,
        SystemUDActionElement userAction) {

        userAction.setCommand(commandString);
        userAction.setAttribute(UA_ATTR_RUNENV, runEnvironment);
        userAction.setPrompt(isPromptFirst);
        userAction.setRefresh(isRefreshAfter);
        userAction.setShow(isShowAction);
        userAction.setSingleSelection(isSingleSelection);
        userAction.setCollect(isInvokeOnce);
        userAction.setComment(comment);
        userAction.setFileTypes(fileTypes);
    }

    private static void saveUserActions(SystemUDActionManager userActionManager, ISystemProfile systemProfile) {

        userActionManager.saveUserData(systemProfile);
    }

    private static ISystemProfile getSystemProfile(String name) {
        return SystemStartHere.getSystemRegistry().getSystemProfile(name);
    }

    private static SystemUDActionManager getUserActionManager(ISystemProfile systemProfile) {

        ISubSystemConfiguration subSystemFactory = getSubSystemConfiguration();
        SystemUDActionSubsystem udactionSubSystem = new QSYSUDActionSubsystemAdapter().getSystemUDActionSubsystem(subSystemFactory);
        udactionSubSystem.setSubSystemFactory(subSystemFactory);
        SystemUDActionManager userActionManager = udactionSubSystem.getUDActionManager();
        userActionManager.setCurrentProfile(systemProfile);

        return userActionManager;
    }

}
