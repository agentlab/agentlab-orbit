package jade.osgi.internal;

import jade.core.AID;
import jade.core.Agent;
import jade.core.AgentContainer;
import jade.core.BaseService;
import jade.core.Filter;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.ServiceException;
import jade.core.ServiceHelper;
import jade.core.VerticalCommand;
import jade.core.management.AgentManagementSlice;
import jade.osgi.OSGIBridgeHelper;

public class OSGIBridgeService extends BaseService {

	public static final String NAME = OSGIBridgeHelper.SERVICE_NAME;

	private CommandIncomingFilter incomingFilter;

	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		incomingFilter = new CommandIncomingFilter();
	}

	public String getName() {
		return NAME;
	}
	
	public ServiceHelper getHelper(Agent a) throws ServiceException {
		return new OSGIBridgeHelperImpl(JadeActivator.getInstance().getAgentManager());
	}
	
	public Filter getCommandFilter(boolean direction) {
		if(direction == Filter.INCOMING) {
			return incomingFilter;
		} else {
			return null;
		}
	}
	
	// Redefine this method to avoid requiring the OSGiBridgeService class in the Main Container classpath
	public  boolean isLocal() {
		return true;
	}
	
	private class CommandIncomingFilter extends Filter {
		public boolean accept(VerticalCommand cmd) {
			if(cmd.getName().equals(AgentManagementSlice.INFORM_KILLED)) {
				handleInformKilled(cmd);
			}
			return true;
		}
		
		private void handleInformKilled(VerticalCommand cmd) {
			Object[] params = cmd.getParams();
			AID aid = (AID) params[0];
			JadeActivator.getInstance().getAgentManager().removeAgent(aid);
		}
	}
	
}
