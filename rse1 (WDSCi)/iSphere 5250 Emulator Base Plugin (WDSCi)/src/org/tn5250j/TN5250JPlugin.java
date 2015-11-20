// ISPHERE - NEW - START

package org.tn5250j;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class TN5250JPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.tn5250j";
	private static TN5250JPlugin plugin;

	public TN5250JPlugin() {
		plugin = this;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static TN5250JPlugin getDefault() {
		return plugin;
	}

}

//ISPHERE - NEW - END