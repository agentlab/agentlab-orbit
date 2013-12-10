package jade.osgi.service.runtime.internal;

import jade.core.Agent;
import jade.core.MicroRuntime;
import jade.osgi.internal.AgentManager;
import jade.osgi.service.agentFactory.AgentFactoryService;
import jade.osgi.service.runtime.JadeRuntimeService;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import org.osgi.framework.Bundle;

public class JadeMicroRuntimeServiceImpl implements JadeRuntimeService {

	private Bundle bundle;
	
	private static Logger logger = Logger.getMyLogger(JadeMicroRuntimeServiceImpl.class.getName());

	public JadeMicroRuntimeServiceImpl(Bundle bundle) {
		this.bundle = bundle;
	}

	public AgentController createNewAgent(String name, String className, Object[] args) throws Exception {
		if(args == null || args instanceof String[]) {
			MicroRuntime.startAgent(name, className, (String[]) args);
			return MicroRuntime.getAgent(name);
		} else {
			throw new IllegalArgumentException("Split container only supports String[] agent arguments");
		}
	}

	public AgentController createNewAgent(String name, String className, Object[] args, String bundleSymbolicName) throws Exception {
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "createAgent(name = "+name+" bundle = "+bundleSymbolicName+") via JRS");
		}
		String classNameMod = className + "["+AgentFactoryService.BUNDLE_NAME+"=" + bundleSymbolicName + "]";
		return createNewAgent(name, classNameMod, args);
	}
	
	public AgentController createNewAgent(String name, String className, Object[] args, String bundleSymbolicName, String bundleVersion) throws Exception {
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "createAgent(name = "+name+" bundle = "+bundleSymbolicName+") via JRS");
		}
		String classNameMod = className + "["+AgentFactoryService.BUNDLE_NAME+"=" + bundleSymbolicName + ";"+
			AgentFactoryService.BUNDLE_VERSION+"=" + bundleVersion + "]";
		return createNewAgent(name, classNameMod, args);
	}

	public AgentController getAgent(String localAgentName) throws Exception {
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "Agent Controller requested by " + bundle.getSymbolicName());
		}
		return MicroRuntime.getAgent(localAgentName);
	}

	public AgentController acceptNewAgent(String name, Agent agent) throws Exception {
		throw new UnsupportedOperationException("Split container does not support AcceptNewAgent");
	}

	public String getContainerName() throws Exception {
		// FIXME TO BE IMPLEMENTED 
		return null;
	}

	public String getPlatformName() {
		// FIXME TO BE IMPLEMENTED
		return null;
	}

	public void kill() throws Exception {
		MicroRuntime.stopJADE();
	}
	
	public static void main(String[] args) {
		Object[] vector = new String[3];
		System.out.println(vector instanceof String[]);
		vector = null;
		System.out.println(vector instanceof String[]);
	}

}
