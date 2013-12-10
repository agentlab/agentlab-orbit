package jade.osgi.internal;

import jade.core.AID;
import jade.core.Agent;
import jade.osgi.service.runtime.JadeRuntimeService;
import jade.util.Logger;
import jade.wrapper.AgentController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

public class AgentManager {

	private BundleContext context;
	private Map<String, List<AgentWrapper>> agents = new HashMap<String, List<AgentWrapper>>();
	private Map<String, List<AgentInfo>> agentsToRestart = new HashMap<String, List<AgentInfo>>();

	public static final String BUNDLE_NAME_VERSION_SEPARATOR = "_";
	
	private static Logger logger = Logger.getMyLogger(AgentManager.class.getName());
	
	public AgentManager(BundleContext context) {
		this.context = context;
	}

	public synchronized void addAgent(Bundle bundle, Agent agent, boolean created) {
		String bundleIdentifier = bundle.getSymbolicName() + BUNDLE_NAME_VERSION_SEPARATOR + bundle.getHeaders().get(Constants.BUNDLE_VERSION);
		if(!agents.containsKey(bundleIdentifier)) {
			agents.put(bundleIdentifier, new ArrayList<AgentWrapper>());
		}
		agents.get(bundleIdentifier).add(new AgentWrapper(agent, bundle, created));
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "ADDED agent: agents "+ agents);
		}
	}

	public synchronized void removeAgent(AID aid) {
		found:
		for(Entry<String, List<AgentWrapper>> entry: agents.entrySet()) {
			List<AgentWrapper> agentWrappers = entry.getValue();
			Iterator<AgentWrapper> it = agentWrappers.iterator();
			while(it.hasNext()) {
				AgentWrapper aw = it.next();
				if(aw.agent.getAID().equals(aid)) {
					if(logger.isLoggable(Logger.FINE)) {
						logger.log(Logger.FINE, "Killed agent "+ aw);
					}
					it.remove();
					break found;
				}
			}
		}
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "REMOVED agent: agents " + agents);
		}
	}
	
	public synchronized Bundle getAgentBundle(AID aid) {
		Bundle result = null;
		found: 
		for(Entry<String, List<AgentWrapper>> entry : agents.entrySet()) {
			for(AgentWrapper aw : entry.getValue()) {
				if(aw.agent.getAID().equals(aid)) {
					result = aw.bundle;
					break found;
				}
			}
		}
		return result;
	}

	public synchronized boolean killAgents(String bundleIdentifier) {
		boolean res = false;
		agentsToRestart.put(bundleIdentifier, new ArrayList<AgentInfo>());
		if(agents.containsKey(bundleIdentifier)) {
			List<AgentWrapper> agentWrappers = agents.get(bundleIdentifier);
			for(AgentWrapper aw: agentWrappers) {
				Agent a = aw.agent;
				if(aw.created) {
					Bundle b = aw.bundle;
					AgentInfo ai = new AgentInfo(a.getLocalName(), a.getClass().getName(), a.getArguments(),
							b.getSymbolicName(), (String) b.getHeaders().get(Constants.BUNDLE_VERSION));
					agentsToRestart.get(bundleIdentifier).add(ai);
					res = true;
				}
				if(logger.isLoggable(Logger.FINE)) {
					logger.log(Logger.FINE, "KILLING " + a.getLocalName());
				}
				a.doDelete();
			}
		}
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "KILL AGENTS: agentsToRestart " + agentsToRestart);
		}
		agents.remove(bundleIdentifier);
		return res;
	}
	
	public synchronized void bundleUpdated(String bundleIdentifier) {
		if(agentsToRestart.containsKey(bundleIdentifier)) {
			List<AgentInfo> agents = agentsToRestart.get(bundleIdentifier);
			for(AgentInfo ai: agents) {
				ai.updated = true;
			}
		}
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "BUNDLE UPDATED: agentsToRestart " + agentsToRestart);
		}
	}

	public synchronized void restartAgents(String bundleIdentifier) {
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "RESTART AGENTS: agentsToRestart " + agentsToRestart);
		}
		if(agentsToRestart.containsKey(bundleIdentifier)) {
			ServiceReference jrsReference = context.getServiceReference(JadeRuntimeService.class.getName());
			JadeRuntimeService jrs = (JadeRuntimeService) context.getService(jrsReference);
			if(jrs != null) {
	    		for(AgentInfo ai: agentsToRestart.get(bundleIdentifier)) {
	    			try {
	    				if(ai.updated) {
	    					if(logger.isLoggable(Logger.FINE)) {
	    						logger.log(Logger.FINE, "RESTARTING agent "+ai);
	    					}
	        				AgentController ac = jrs.createNewAgent(ai.name, ai.className, ai.args, ai.symbolicName, ai.version);
	        				ac.start();
	    				}
	    			} catch(Exception e) {
	    				logger.log(Logger.SEVERE, "Agent " + ai.name + " cannot be restarted!", e);
	    			}
	    		}
			} else {
				logger.log(Logger.WARNING, "JadeRuntimeService for "+bundleIdentifier+" no more active! Cannot restart agents!");
			}
    		agentsToRestart.remove(bundleIdentifier);
		}
	}
	
	public synchronized void removeAgents(String bundleIdentifier) throws Exception {
		agentsToRestart.remove(bundleIdentifier);
		if(logger.isLoggable(Logger.FINE)) {
			logger.log(Logger.FINE, "CLEAR AGENTS: agentsToRestart "+ agentsToRestart);
			logger.log(Logger.FINE, "CLEAR AGENTS: agents "+ agents);
		}
	}
	
	private class AgentWrapper {
		private final Agent agent;
		private final Bundle bundle;
		private final boolean created;
		
		public AgentWrapper(Agent agent, Bundle bundle, boolean created) {
			this.agent = agent;
			this.bundle = bundle;
			this.created = created;
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("(");
			sb.append("agent: " + agent.getLocalName());
			sb.append(" bundle-name: " + bundle.getSymbolicName());
			sb.append(" bundle-version: " + bundle.getHeaders().get(Constants.BUNDLE_VERSION));
			sb.append(" created: " + created);
			sb.append(")");
			return sb.toString();
		}
	}
	
	private class AgentInfo {
		private final String name;
		private final String className;
		private final Object[] args;
		private boolean updated;
		private final String symbolicName;
		private final String version;

		public AgentInfo(String name, String className, Object[] args, String symbolicName, String version) {
			this.name = name;
			this.className = className;
			this.args = args;
			this.updated = false;
			this.symbolicName = symbolicName;
			this.version = version;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("(");
			sb.append("name: " + name);
			sb.append(" updated: " + updated);
			sb.append(")");
			return sb.toString();
		}
		
	}

}
