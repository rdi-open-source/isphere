/*******************************************************************************
 * Copyright (c) 2012-2014 iSphere Project Owners
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package biz.isphere.rse;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


import biz.isphere.core.ISpherePlugin;
import biz.isphere.rse.internal.Editor;
import biz.isphere.rse.internal.MessageFileSearchObjectFilterCreator;
import biz.isphere.rse.internal.SourceFileSearchMemberFilterCreator;
import biz.isphere.rse.spooledfiles.SpooledFileAdapterFactory;
import biz.isphere.rse.spooledfiles.SpooledFileResource;
import biz.isphere.rse.spooledfiles.SpooledFileSubSystemConfigurationAdapterFactory;

public class ISphereRSEPlugin extends AbstractUIPlugin {

	private static ISphereRSEPlugin plugin;
	private static URL installURL;
	
	public ISphereRSEPlugin() {
		super();
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		installURL = context.getBundle().getEntry("/");
		ISpherePlugin.setEditor(new Editor());
		ISpherePlugin.setSourceFileSearchMemberFilterCreator(new SourceFileSearchMemberFilterCreator());
		ISpherePlugin.setMessageFileSearchObjectFilterCreator(new MessageFileSearchObjectFilterCreator());
		setupAdapters();	
	}

	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	public static ISphereRSEPlugin getDefault() {
		return plugin;
	}
	
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
	}
	
	public static ImageDescriptor getImageDescriptor(String name) {
		String iconPath = "icons/"; 
		try {
			URL url = new URL(installURL, iconPath + name);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private void setupAdapters() {
		
	    IAdapterManager manager = Platform.getAdapterManager();
	    SpooledFileAdapterFactory spooledFactory = new SpooledFileAdapterFactory();
	    manager.registerAdapters(spooledFactory, SpooledFileResource.class);

	    SpooledFileSubSystemConfigurationAdapterFactory spooledFileSubSystemConfigurationAdapterFactory = new SpooledFileSubSystemConfigurationAdapterFactory();
	    spooledFileSubSystemConfigurationAdapterFactory.registerWithManager(manager);
		
	}

}
