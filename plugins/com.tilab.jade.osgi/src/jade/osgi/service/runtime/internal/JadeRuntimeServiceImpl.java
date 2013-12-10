package jade.osgi.service.runtime.internal;

import jade.core.Agent;
import jade.osgi.internal.AgentManager;
import jade.osgi.service.agentFactory.AgentFactoryService;
import jade.osgi.service.runtime.JadeRuntimeService;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import org.osgi.framework.Bundle;

public class JadeRuntimeServiceImpl implements JadeRuntimeService {

	private ContainerController container;
	private AgentManager agentManager;
	private Bundle bundle;
	
	private static Logger logger = Logger.getMyLogger(JadeRuntimeServiceImpl.class.getName());

	public JadeRuntimeServiceImpl(ContainerController container, AgentManager agentManager, Bundle bundle) {
		this.container = container;
		this.agentManager = agentManager;
		this.bundle = bundle;
	}

	public AgentController createNewAgent(String name, String className, Object[] args) throws Exception {
		return container.createNewAgent(name, className, args);
	}

	public AgentController createNewAgent(String name, String className, Object[] args, String bundleSymbolicName) throws Exception {
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "createAgent(name = "+name+" bundle = "+bundleSymbolicName+") via JRS");
		}
		String classNameMod = className + "["+AgentFactoryService.BUNDLE_NAME+"=" + bundleSymbolicName + "]";
		return container.createNewAgent(name, classNameMod, args);
	}
	
	public AgentController createNewAgent(String name, String className, Object[] args, String bundleSymbolicName, String bundleVersion) throws Exception {
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "createAgent(name = "+name+" bundle = "+bundleSymbolicName+") via JRS");
		}
		String classNameMod = className + "["+AgentFactoryService.BUNDLE_NAME+"=" + bundleSymbolicName + ";"+
			AgentFactoryService.BUNDLE_VERSION+"=" + bundleVersion + "]";
		return container.createNewAgent(name, classNameMod, args);
	}

	public AgentController getAgent(String localAgentName) throws Exception {
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "Agent Controller requested by " + bundle.getSymbolicName());
		}
		return container.getAgent(localAgentName);
	}

	public AgentController acceptNewAgent(String name, Agent agent) throws Exception {
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "acceptAgent(name = "+name+" bundle = "+bundle.getSymbolicName() +")");
		}
		AgentController myAgent = container.acceptNewAgent(name, agent);
		agentManager.addAgent(bundle, agent, false);
		return myAgent;
	}

	public String getContainerName() throws Exception {
		return container.getContainerName();
	}

	public String getPlatformName() {
		return container.getPlatformName();
	}

	public void kill() throws Exception {
		container.kill();
	}

}
