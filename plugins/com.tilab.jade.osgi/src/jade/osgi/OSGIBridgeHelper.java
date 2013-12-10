package jade.osgi;

import jade.core.ServiceHelper;
import org.osgi.framework.BundleContext;

public interface OSGIBridgeHelper extends ServiceHelper {
	
	public static final String SERVICE_NAME = "jade.osgi.internal.OSGIBridge";
	
	/**
	 * Give JADE agents access to OSGI features.
	 * @return context of the bundle from which they were created
	 */
	public BundleContext getBundleContext();

}
