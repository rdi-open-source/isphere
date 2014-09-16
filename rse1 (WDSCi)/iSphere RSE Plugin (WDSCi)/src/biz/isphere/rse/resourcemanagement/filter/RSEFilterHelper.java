/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse.resourcemanagement.filter;

import java.util.ArrayList;
import java.util.Vector;

import com.ibm.etools.iseries.core.IISeriesFilterTypes;
import com.ibm.etools.iseries.core.ISeriesSubSystemHelpers;

import com.ibm.etools.systems.filters.SystemFilter;
import com.ibm.etools.systems.filters.SystemFilterPool;
import com.ibm.etools.systems.model.SystemProfile;
import com.ibm.etools.systems.model.impl.SystemProfileManagerImpl;

import org.eclipse.emf.common.util.EList;

import biz.isphere.core.resourcemanagement.filter.RSEFilter;
import biz.isphere.core.resourcemanagement.filter.RSEFilterPool;

public class RSEFilterHelper {

	public static RSEFilterPool[] getFilterPools() {

		ArrayList<RSEFilterPool> allFilterPools = new ArrayList<RSEFilterPool>();
        
		SystemProfile[] profiles = SystemProfileManagerImpl.getSystemProfileManager().getSystemProfiles();
		for (int idx1 = 0; idx1 < profiles.length; idx1++) {
			SystemFilterPool[] filterPools = profiles[idx1].getFilterPools(ISeriesSubSystemHelpers.getISeriesObjectsSubSystemFactory());
			for (int idx2 = 0; idx2 < filterPools.length; idx2++) {
            	RSEFilterPool rseFilterPool = new RSEFilterPool(
            			profiles[idx1].getName() + "." + filterPools[idx2].getName(),
                        filterPools[idx2].isDefault(),
            			filterPools[idx2]);
				allFilterPools.add(rseFilterPool);
			}
		}
        
		RSEFilterPool[] rseFilterPools = new RSEFilterPool[allFilterPools.size()];
        allFilterPools.toArray(rseFilterPools);
        
        return rseFilterPools;
		
	}

	public static RSEFilter[] getFilters(RSEFilterPool filterPool) {
        
        EList filters = ((SystemFilterPool)filterPool.getOrigin()).getFilters();

        ArrayList<RSEFilter> rseFilters = new ArrayList<RSEFilter>();

        for (int idx = 0; idx < filters.size(); idx++) {

            SystemFilter filter = (SystemFilter)filters.get(idx);
            
        	String type;
        	if (filter.getType().equals(IISeriesFilterTypes.FILTERTYPE_LIBRARY)) {
        		type = RSEFilter.TYPE_LIBRARY;
        	}
        	else if (filter.getType().equals(IISeriesFilterTypes.FILTERTYPE_OBJECT)) {
        		type = RSEFilter.TYPE_OBJECT;
        	}
        	else if (filter.getType().equals(IISeriesFilterTypes.FILTERTYPE_MEMBER)) {
        		type = RSEFilter.TYPE_MEMBER;
        	}
        	else {
        		type = RSEFilter.TYPE_UNKNOWN;
        	}

            if (!type.equals(RSEFilter.TYPE_UNKNOWN)) {

                RSEFilter rseFilter = new RSEFilter(
                        filterPool,
                        filter.getName(),
                        type,
                        filter.getFilterStrings(),
                        true,
                        filter);
 
                rseFilters.add(rseFilter);
                
            }
        	
        }
				
        RSEFilter[] _rseFilters = new RSEFilter[rseFilters.size()];
        rseFilters.toArray(_rseFilters);
        return _rseFilters;
		
	}
	 
	public static void createFilter(RSEFilterPool filterPool, String name, String type, Vector<String> filterStrings) {
		String newType = null;
		if (type.equals(RSEFilter.TYPE_LIBRARY)) {
			newType = IISeriesFilterTypes.FILTERTYPE_LIBRARY;
		}
		else if (type.equals(RSEFilter.TYPE_OBJECT)) {
			newType = IISeriesFilterTypes.FILTERTYPE_OBJECT;
		}
		else if (type.equals(RSEFilter.TYPE_MEMBER)) {
			newType = IISeriesFilterTypes.FILTERTYPE_MEMBER;
		}
		if (newType != null) {
	        try {
	            ((SystemFilterPool)filterPool.getOrigin()).getSystemFilterPoolManager().createSystemFilter(((SystemFilterPool)filterPool.getOrigin()), name, filterStrings, newType);
	        } catch (Exception e) {
				e.printStackTrace();
	        }
		}
	}
	
	public static void deleteFilter(RSEFilterPool filterPool, String name) {
        SystemFilter[] filters = ((SystemFilterPool)filterPool.getOrigin()).getSystemFilters();
        for (int idx = 0; idx < filters.length; idx++) {
            if (filters[idx].getName().equals(name)) {
            	try {
            		((SystemFilterPool)filterPool.getOrigin()).getSystemFilterPoolManager().deleteSystemFilter(filters[idx]);
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        }
	}
	
}
