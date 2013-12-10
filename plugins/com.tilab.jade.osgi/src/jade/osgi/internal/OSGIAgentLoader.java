package jade.osgi.internal;

import jade.core.Agent;
import jade.osgi.service.agentFactory.AgentFactoryService;
import jade.util.Logger;
import jade.util.ObjectManager;
import jade.util.leap.Properties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class OSGIAgentLoader implements ObjectManager.Loader {
	
	private BundleContext context;
	private AgentManager agentManager;
	
	private static Logger logger = Logger.getMyLogger(OSGIAgentLoader.class.getName());
	
	public OSGIAgentLoader(BundleContext bc, AgentManager am) {
		this.context = bc;
		this.agentManager = am;
	}

	public Object load(String className, Properties pp) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		Agent agent = null;
		String bundleName = pp.getProperty(AgentFactoryService.BUNDLE_NAME);
		String bundleVersion = pp.getProperty(AgentFactoryService.BUNDLE_VERSION);
		if(bundleName != null) {
			ServiceReference sr = getAgentFactoryService(bundleName, bundleVersion);
			if(sr != null) {
				AgentFactoryService afs = (AgentFactoryService) context.getService(sr);
				agent = afs.createAgent(className);
				if(logger.isLoggable(Logger.FINE)) {
					logger.log(Logger.FINE, "AFS("+bundleName+") created agent of class "+className);
				}
				agentManager.addAgent(afs.getBundle(), agent, true);
				context.ungetService(sr);
			}
		}
		return agent;
	}

	private ServiceReference getAgentFactoryService(String bundleName, String bundleVersion) throws ClassNotFoundException {
		ServiceReference af = null;
		String filter = "("+AgentFactoryService.AFS_BUNDLE_NAME+"="+bundleName+")";
		if(bundleVersion != null) {
			filter = "(&"+filter+"("+AgentFactoryService.AFS_BUNDLE_VERSION+"="+bundleVersion+"))";
		}
		try {
			ServiceReference[] srs = context.getServiceReferences(AgentFactoryService.class.getName(), filter);
			if(srs != null) {
				af = srs[0];
				// TODO se non e' specificata la version prendere il piu' recente
			}
		} catch(InvalidSyntaxException e) {
			logger.log(Logger.SEVERE, "Error retrieving AFS for bundle "+bundleName, e);
		}
		return af;
	}
}
