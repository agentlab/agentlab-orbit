package jade.osgi.internal;

import jade.core.Agent;
import jade.core.FEListener;
import jade.core.FEService;
import jade.core.MicroRuntime;
import jade.core.ServiceHelper;
import jade.core.event.ContainerEvent;
import jade.core.event.JADEEvent;
import jade.osgi.OSGIBridgeHelper;

public class OSGIBridgeFEService extends FEService {
	
	private AgentManager agentManager;
	
	public OSGIBridgeFEService() {
		MicroRuntime.addListener(new FEListener() {
			
			public void handleEvent(JADEEvent ev) {
				if(ev instanceof ContainerEvent && ev.getType() == ContainerEvent.DEAD_AGENT) {
					ContainerEvent ce = (ContainerEvent) ev;
					agentManager.removeAgent(ce.getAgent());
				}
			}
		});
	}

	@Override
	public String getBEServiceClassName() {
		return OSGIBridgeHelper.SERVICE_NAME;
	}

	@Override
	public ServiceHelper getHelper(Agent a) {
		return new OSGIBridgeHelperImpl(getAgentManager());
	}

	@Override
	public String getName() {
		return OSGIBridgeHelper.SERVICE_NAME;
	}
	
	private AgentManager getAgentManager() {
		if(agentManager == null) {
			agentManager = JadeActivator.getInstance().getAgentManager();
		}
		return agentManager;
	}

}
