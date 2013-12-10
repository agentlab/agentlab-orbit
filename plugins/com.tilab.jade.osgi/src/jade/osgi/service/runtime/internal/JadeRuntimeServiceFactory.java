package jade.osgi.service.runtime.internal;

import jade.osgi.internal.AgentManager;
import jade.osgi.service.runtime.JadeRuntimeService;
import jade.wrapper.ContainerController;
import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class JadeRuntimeServiceFactory implements ServiceFactory {

	private ContainerController container;
	private AgentManager agentManager;
	private Map<Long,JadeRuntimeService> usedJadeServices = new HashMap<Long, JadeRuntimeService>();
	private boolean split;

	public JadeRuntimeServiceFactory(ContainerController container, AgentManager agentManager) {
		this.container = container;
		this.agentManager = agentManager;
		this.split = false;
	}
	
	public JadeRuntimeServiceFactory(AgentManager agentManager) {
		this.agentManager = agentManager;
		this.split = true;
	}

	public Object getService(Bundle bundle, ServiceRegistration registration) {
		JadeRuntimeService jadeService;
		if(split) {
			jadeService = new JadeMicroRuntimeServiceImpl(bundle);
		} else {
			jadeService = new JadeRuntimeServiceImpl(container, agentManager, bundle);
		}
		usedJadeServices.put(bundle.getBundleId(), jadeService);
		return jadeService;
	}

	public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
		usedJadeServices.remove(bundle.getBundleId());
		if(service instanceof JadeRuntimeService) {
			// FIXME do something?
//			JadeRuntimeServiceImpl jadeService = (JadeRuntimeServiceImpl) service;
//			try {
//				jadeService.removeAgents();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}

}