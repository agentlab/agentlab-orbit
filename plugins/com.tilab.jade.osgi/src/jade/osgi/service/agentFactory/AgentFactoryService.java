package jade.osgi.service.agentFactory;

import jade.core.Agent;
import jade.util.Logger;
import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * This class must be used by bundles that package agets code 
 * to make it available to other bundles. It registers an osgi service 
 * with bundle name and version attributes. When a bundle wants to start
 * an agent packaged in this bundle, jadeOsgi searches for the corresponding
 * AgentFactoryService and ask it to load agent class.
 * Bundle that wants to expose agents code simply instantiate this class
 * and then call method init.
 * Note that this service is called when agent creation is performed
 * by bundles via JadeRuntimeService or by JADE agent AMS.
 * 
 * @author 00918076
 *
 */
public class AgentFactoryService {
	
	public static final String BUNDLE_NAME ="bundle-name";
	public static final String BUNDLE_VERSION ="bundle-version";
	public static final String AFS_BUNDLE_NAME ="afs-bundle-name";
	public static final String AFS_BUNDLE_VERSION ="afs-bundle-version";

	private Bundle myBundle;
	private ServiceRegistration serviceRegistration;
	
	private static Logger logger = Logger.getMyLogger(AgentFactoryService.class.getName());

	/**
	 * Register the AgentFactoryService osgi service specifying
	 * bundle name and version attributes.
	 * @param bundle bundle that packages agents code
	 */
	public void init(Bundle bundle) {
		this.myBundle = bundle;
		BundleContext context = myBundle.getBundleContext();
		Dictionary<String, String> properties = new Hashtable<String, String>();
		properties.put(AFS_BUNDLE_NAME, myBundle.getSymbolicName());
		properties.put(AFS_BUNDLE_VERSION, (String) myBundle.getHeaders().get(Constants.BUNDLE_VERSION));
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "Registering AFS with properties "+ properties);
		}
		this.serviceRegistration = context.registerService(AgentFactoryService.class.getName(), this, properties);
		logger.log(Logger.INFO, myBundle.getSymbolicName()+ " register AFS");
	}
	
	/**
	 * Deregister AgentFactoryService osgi service.
	 */
	public void clean() {
		serviceRegistration.unregister();
	}

	/**
	 * This method is used by the framework. User should not use it.
	 */
	public final Bundle getBundle() {
		return myBundle;
	}

	/**
	 * This method is used by the framework. User should not use it.
	 */
	public final Agent createAgent(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Class c = myBundle.loadClass(className);
		return (Agent) c.newInstance();
	}
}
