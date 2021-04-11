/*******************************************************************************
 * Copyright (c) 2012-2018 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.resourcemanagement.namedtype;

import java.util.ArrayList;
import java.util.Vector;

import biz.isphere.core.resourcemanagement.filter.RSEProfile;
import biz.isphere.core.resourcemanagement.namedtype.RSENamedType;
import biz.isphere.core.resourcemanagement.useraction.RSEDomain;
import biz.isphere.rse.resourcemanagement.AbstractSystemHelper;

import com.ibm.etools.iseries.core.ui.uda.UDActionSubsystemNFS;
import com.ibm.etools.systems.as400filesubsys.FileSubSystem;
import com.ibm.etools.systems.core.ui.uda.SystemUDTypeElement;
import com.ibm.etools.systems.core.ui.uda.SystemUDTypeManager;
import com.ibm.etools.systems.model.SystemConnection;
import com.ibm.etools.systems.model.SystemProfile;
import com.ibm.etools.systems.model.SystemStartHere;
import com.ibm.etools.systems.subsystems.SubSystem;
import com.ibm.etools.systems.subsystems.SubSystemFactory;

@SuppressWarnings("restriction")
public class RSENamedTypeHelper extends AbstractSystemHelper {

    public static RSEDomain[] getDomains(RSEProfile rseProfile) {

        ArrayList<RSEDomain> rseDomains = new ArrayList<RSEDomain>();

        SystemProfile systemProfile = getSystemProfile(rseProfile.getName());
        if (systemProfile != null) {
            SystemUDTypeManager userActionManager = getNamedTypeManager(systemProfile);

            if (userActionManager != null) {
                String[] domainNames = userActionManager.getActionSubSystem().getDomainNames();
                for (String domainName : domainNames) {
                    int domainIndex = userActionManager.getActionSubSystem().mapDomainName(domainName);
                    rseDomains.add(produceDomain(rseProfile, domainIndex, domainName));
                }
            }
        }

        return rseDomains.toArray(new RSEDomain[rseDomains.size()]);
    }

    public static RSENamedType[] getNamedTypes(RSEProfile rseProfile) {

        ArrayList<RSENamedType> allNamedTypes = new ArrayList<RSENamedType>();

        RSEDomain[] domains = getDomains(rseProfile);
        for (int idx1 = 0; idx1 < domains.length; idx1++) {
            RSENamedType[] userActions = getNamedTypes(domains[idx1]);
            for (int idx2 = 0; idx2 < userActions.length; idx2++) {
                allNamedTypes.add(userActions[idx2]);
            }
        }

        RSENamedType[] _namedTypes = new RSENamedType[allNamedTypes.size()];
        allNamedTypes.toArray(_namedTypes);

        return _namedTypes;
    }

    public static RSENamedType[] getNamedTypes(RSEDomain rseDomain) {

        ArrayList<RSENamedType> rseNamedTypes = new ArrayList<RSENamedType>();

        SystemProfile systemProfile = getSystemProfile(rseDomain.getProfile().getName());
        if (systemProfile != null) {
            SystemUDTypeManager namedTypeManager = getNamedTypeManager(systemProfile);
            if (namedTypeManager != null) {
                int domain = rseDomain.getDomainType();
                SystemUDTypeElement[] namedTypes = getNamedTypes(systemProfile, domain);
                for (SystemUDTypeElement namedType : namedTypes) {
                    RSENamedType rseNamedType = produceNamedType(domain, namedType);
                    rseNamedTypes.add(rseNamedType);
                }
            }
        }

        return rseNamedTypes.toArray(new RSENamedType[rseNamedTypes.size()]);
    }

    private static RSEDomain produceDomain(RSEProfile rseProfile, int domain, String name) {
        return new RSEDomain(rseProfile, domain, name);
    }

    private static RSENamedType produceNamedType(int domain, SystemUDTypeElement systemNamedType) {

        RSENamedType rseNamedType = new RSENamedType(domain, systemNamedType.getName(), systemNamedType.getTypes(), systemNamedType);

        return rseNamedType;
    }

    public static void createNamedType(RSEDomain rseDomain, String label, String types, String vendor) {

        SystemProfile systemProfile = getSystemProfile(rseDomain.getProfile().getName());
        if (systemProfile != null) {
            SystemUDTypeManager userActionManager = getNamedTypeManager(systemProfile);
            if (userActionManager != null) {

                SystemUDTypeElement namedType = userActionManager.addType(rseDomain.getDomainType(), label);
                namedType.setVendor(vendor);
                namedType.setTypes(types);

                saveNamedTypes(userActionManager, systemProfile);
            }
        }
    }

    public static boolean hasNamedTypeManager(RSEProfile rseProfile) {

        SystemProfile systemProfile = (SystemProfile)rseProfile.getOrigin();

        if (getNamedTypeManager(systemProfile) != null) {
            return true;
        }

        return false;
    }

    public static boolean hasNamedType(RSEDomain rseDomain, String label) {

        RSENamedType rseNamedTypes = getNamedType(rseDomain, label);
        if (rseNamedTypes != null) {
            return true;
        }

        return false;
    }

    public static RSENamedType getNamedType(RSEDomain rseDomain, String label) {

        if (label == null) {
            return null;
        }

        RSENamedType[] rseNamedTypes = getNamedTypes(rseDomain);
        for (RSENamedType rseNamedType : rseNamedTypes) {
            if (label.equals(rseNamedType.getLabel())) {
                return rseNamedType;
            }
        }

        return null;
    }

    private static SystemUDTypeElement[] getNamedTypes(SystemProfile systemProfile, int domain) {

        SystemUDTypeManager namedTypeManager = getNamedTypeManager(systemProfile);

        Vector<?> v = namedTypeManager.getXMLWrappers(new Vector<Object>(), domain, systemProfile);
        if (v == null) {
            return new SystemUDTypeElement[0];
        }

        SystemUDTypeElement[] namedTypes = new SystemUDTypeElement[v.size()];
        for (int idx = 0; idx < namedTypes.length; idx++) {
            namedTypes[idx] = ((SystemUDTypeElement)v.elementAt(idx));
        }

        return namedTypes;
    }

    private static void saveNamedTypes(SystemUDTypeManager namedTypeManager, SystemProfile systemProfile) {

        namedTypeManager.saveUserData(systemProfile);
    }

    private static SystemProfile getSystemProfile(String name) {
        return SystemStartHere.getSystemRegistry().getSystemProfile(name);
    }

    private static SystemUDTypeManager getNamedTypeManager(SystemProfile systemProfile) {

        for (SystemConnection connection : systemProfile.getConnections()) {
            if ("iSeries".equals(connection.getSystemType())) {
                for (SubSystem subSystem : connection.getSubSystems()) {
                    if (subSystem instanceof FileSubSystem) {
                        return subSystem.getUDActionSubsystem().getUDTypeManager();
                    }
                }
            }
        }

        SubSystemFactory subSystemFactory = getSubSystemConfiguration();
        UDActionSubsystemNFS actionSubsystemNFS = new UDActionSubsystemNFS();
        actionSubsystemNFS.setSubSystemFactory(subSystemFactory);
        SystemUDTypeManager namedTypeManager = actionSubsystemNFS.getUDTypeManager();
        namedTypeManager.setCurrentProfile(systemProfile);

        return namedTypeManager;
    }

}
