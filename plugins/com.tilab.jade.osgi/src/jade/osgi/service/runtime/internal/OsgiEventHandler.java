package jade.osgi.service.runtime.internal;

import jade.osgi.internal.AgentManager;
import jade.util.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.osgi.framework.BundleEvent;

public class OsgiEventHandler {

	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private String bundleIdentifier;
	private AgentManager agentManager;
	private boolean restartAgents;
	private long restartAgentsTimeout;
	private ScheduledFuture<?> task;
	
	private static Logger logger = Logger.getMyLogger(OsgiEventHandler.class.getName());

	public OsgiEventHandler(String bundleIdentifier, AgentManager am, boolean restartAgents, long restartAgentsTimeout) {
		this.bundleIdentifier = bundleIdentifier;
		this.agentManager = am;
		this.restartAgents = restartAgents;
		this.restartAgentsTimeout = restartAgentsTimeout;
	}

	public void handleEvent(BundleEvent event) {
		int eventType = event.getType();

		switch(eventType) {
			case BundleEvent.INSTALLED:
				if(logger.isLoggable(Logger.FINE)) {
					logger.log(Logger.FINE, bundleIdentifier + " INSTALLED");
				}
				break;

			case BundleEvent.STARTED:
				if(logger.isLoggable(Logger.FINE)) {
					logger.log(Logger.FINE, bundleIdentifier + " STARTED");
				}
				if(restartAgents && task != null) { // FIXME task null ? add comment
					if(logger.isLoggable(Logger.FINE)) {
						logger.log(Logger.FINE, "AgentRemover for " + bundleIdentifier + " CANCELED");
					}
					task.cancel(true);
					agentManager.restartAgents(bundleIdentifier);
				}
				break;

			case BundleEvent.STOPPED:
				if(logger.isLoggable(Logger.FINE)) {
					logger.log(Logger.FINE, bundleIdentifier + " STOPPED");
				}
				if(agentManager.killAgents(bundleIdentifier)) {
					if(restartAgents) {
						task = scheduler.schedule(new AgentRemover(), restartAgentsTimeout, TimeUnit.MILLISECONDS);
						if(logger.isLoggable(Logger.FINE)) {
							logger.log(Logger.FINE, "AgentRemover for " + bundleIdentifier + " SCHEDULED");
						}
					}
				}
				break;

			case BundleEvent.UPDATED:
				if(logger.isLoggable(Logger.FINE)) {
					logger.log(Logger.FINE, bundleIdentifier + " UPDATED");
				}
				if(restartAgents && task != null) { // FIXME task null ? add comment
					agentManager.bundleUpdated(bundleIdentifier);
				}
				break;

			default:
				if(logger.isLoggable(Logger.FINE)) {
					logger.log(Logger.FINE, "Bundle " + bundleIdentifier + " EVENT " + eventType);
				}
				break;
		}

	}

	class AgentRemover implements Runnable {

		public void run() {
			if(logger.isLoggable(Logger.FINE)) {
				logger.log(Logger.FINE, "AgentRemover for " + bundleIdentifier + " EXECUTED");
			}
			try {
				agentManager.removeAgents(bundleIdentifier);
			} catch(Exception e) {
				logger.log(Logger.SEVERE, "Cannot remove agents for bundle " + bundleIdentifier, e);
			}
		}

	}

	public void stop() {
		if(task != null) {
			task.cancel(true);
		}
	}
}
