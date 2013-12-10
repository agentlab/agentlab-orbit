package jade.osgi.service.runtime.internal;

import jade.osgi.internal.AgentManager;
import java.util.HashMap;
import java.util.Map;

public class OsgiEventHandlerFactory {

	private boolean restartAgents;
	private long restartAgentsTimeout;
	private AgentManager agentManager;
	private Map<String, OsgiEventHandler> handlers = new HashMap<String, OsgiEventHandler>();
	
	public OsgiEventHandlerFactory(AgentManager am, boolean restartAgents, long restartAgentsTimeout) {
		this.agentManager = am;
		this.restartAgents = restartAgents;
		this.restartAgentsTimeout = restartAgentsTimeout;
	}

	public OsgiEventHandler getOsgiEventHandler(String symbolicName, String bundleVersion) {
		OsgiEventHandler handler = null;
		String key = symbolicName + AgentManager.BUNDLE_NAME_VERSION_SEPARATOR + bundleVersion;
		if(!handlers.containsKey(key)) {
			handler = new OsgiEventHandler(key, agentManager, restartAgents, restartAgentsTimeout);
			handlers.put(key, handler);
		} else {
			handler = handlers.get(key);
		}
		return handler;
	}
	
	public void stop() {
		for(OsgiEventHandler h: handlers.values()) {
			h.stop();
		}
		handlers.clear();
	}

}
