package jade.osgi.internal;

import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.osgi.service.runtime.JadeRuntimeService;
import jade.osgi.service.runtime.internal.JadeRuntimeServiceFactory;
import jade.osgi.service.runtime.internal.OsgiEventHandler;
import jade.osgi.service.runtime.internal.OsgiEventHandlerFactory;
import jade.util.Logger;
import jade.util.ObjectManager;
import jade.util.leap.Properties;
import jade.wrapper.ContainerController;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class JadeActivator implements BundleActivator, BundleListener {

	public JadeActivator() {
		super();
	}

	private static final String PROFILE_PARAMETER_PREFIX = "jade.";
	private static final String JADE_CONF = PROFILE_PARAMETER_PREFIX + "conf";
	private static final String RESTART_AGENTS_ON_UPDATE_KEY = "restart-agents-on-update";
	private static final String RESTART_AGENTS_TIMEOUT_KEY = "restart-agents-timeout";
	private static final String SPLIT_CONTAINER_KEY = "split-container";
	
	private static final boolean RESTART_AGENTS_ON_UPDATE_DEFAULT = true;
	private static final long RESTART_AGENTS_TIMEOUT_DEFAULT = 10000;

	private BundleContext context;
	private static JadeActivator instance;
	private ContainerController container;
	private AgentManager agentManager;
	private OsgiEventHandlerFactory handlerFactory;
	
	private static Logger logger = Logger.getMyLogger(JadeActivator.class.getName());
	
	private Object terminationLock = new Object();
	private ServiceRegistration jrs;
	private OSGIAgentLoader agentLoader;

	
	public void start(BundleContext context) throws Exception {
		try {
			instance = this;
			this.context = context;
			this.agentManager = new AgentManager(context);
			
			// Create OsgiEventHandlerFactory 
			boolean restartAgents = RESTART_AGENTS_ON_UPDATE_DEFAULT;
			String restartAgentsOnUpdateS = System.getProperty(RESTART_AGENTS_ON_UPDATE_KEY);
			if(restartAgentsOnUpdateS != null) {
				restartAgents = Boolean.parseBoolean(restartAgentsOnUpdateS);
			}
			long restartTimeout = RESTART_AGENTS_TIMEOUT_DEFAULT;
			String restartAgentsTimeoutS = System.getProperty(RESTART_AGENTS_TIMEOUT_KEY);
			if(restartAgentsTimeoutS != null) {
				try {
					restartTimeout = Long.parseLong(restartAgentsTimeoutS);
				} catch(NumberFormatException e) {}
			}
			logger.log(Logger.CONFIG, RESTART_AGENTS_ON_UPDATE_KEY + " " + restartAgents);
			logger.log(Logger.CONFIG, RESTART_AGENTS_TIMEOUT_KEY + " " + restartTimeout);
			this.handlerFactory = new OsgiEventHandlerFactory(agentManager, restartAgents, restartTimeout);
			
			// Register an osgi agent loader (do that before starting JADE so that bootstrap agents can be loaded from separated bundles too)
			agentLoader = new OSGIAgentLoader(context, agentManager);
			ObjectManager.addLoader(ObjectManager.AGENT_TYPE, agentLoader);

			// Initialize jade container profile and start it
			Properties jadeProperties = new Properties();
			addJadeSystemProperties(jadeProperties);
			addJadeFileProperties(jadeProperties);
			addOSGIBridgeService(jadeProperties);
			if(isSplitContainer()) {
				startJadeSplitContainer(jadeProperties);
			} else {
				startJadeContainer(jadeProperties);
			}
			
			// Register JRS service
			registerJadeRuntimeService();

			// Listen to bundle events
			context.addBundleListener(this);

		} catch(Exception e) {
			logger.log(Logger.SEVERE, "Error during bundle startup", e);
			throw e;
		}
	}

	public void stop(BundleContext context) throws Exception {
		synchronized(terminationLock) {
			if(isSplitContainer()) {
				MicroRuntime.stopJADE();
			} else {
				try {
					// If JADE has already termnated we get an exception and simply ignore it
					container.kill();
				}
				catch (Exception e) {}
			}
		}
		handlerFactory.stop();
		if(jrs != null) {
			jrs.unregister();
		}
		context.removeBundleListener(this);
		if(agentLoader != null && !ObjectManager.removeLoader(ObjectManager.AGENT_TYPE, agentLoader)) {
			logger.log(Logger.SEVERE, "Error removing osgi agent loader");
		}
		logger.log(Logger.INFO, context.getBundle().getSymbolicName() + " stopped!");
	}

	public static JadeActivator getInstance() {
		return instance;
	}
	
	public AgentManager getAgentManager() {
		return agentManager;
	}
	
	public BundleContext getBundleContext() {
		return context;
	}
	
	public synchronized void bundleChanged(BundleEvent event) {
		Bundle b = event.getBundle();
		OsgiEventHandler handler = handlerFactory.getOsgiEventHandler(b.getSymbolicName(), (String) b.getHeaders().get(Constants.BUNDLE_VERSION));
		handler.handleEvent(event);
	}
	
	private void addOSGIBridgeService(Properties pp) {
		String services = pp.getProperty(Profile.SERVICES);
		String defaultServices = isSplitContainer() ? "" : ";"+jade.core.mobility.AgentMobilityService.class.getName()+";"+
				jade.core.event.NotificationService.class.getName();
		String serviceName = isSplitContainer() ? OSGIBridgeFEService.class.getName() : OSGIBridgeService.class.getName();
		if(services == null) {
			pp.setProperty(Profile.SERVICES, serviceName+defaultServices);
		} else if(services.indexOf(serviceName) == -1) {
			pp.setProperty(Profile.SERVICES, services+";"+serviceName);
		}
	}

	private void addJadeSystemProperties(Properties props) {
		Set<Entry<Object, Object>> entrySet = System.getProperties().entrySet();
		for(Entry<Object, Object> entry : entrySet) {
			String key = (String) entry.getKey();
			if(key.startsWith(PROFILE_PARAMETER_PREFIX)) {
				props.setProperty(key.substring(PROFILE_PARAMETER_PREFIX.length()), (String) entry.getValue());
			}
		}
	}

	private void addJadeFileProperties(Properties props) throws Exception {
		String profileConf = System.getProperty(JADE_CONF);
		if(profileConf != null) {
			// find profile configuration in classpath
			InputStream input = ClassLoader.getSystemResourceAsStream(profileConf);
			if(input == null) {
				File f = new File(profileConf);
				if(f.exists()) {
					input = new FileInputStream(f);
				}
			}
			if(input != null) {
				Properties pp = new Properties();
				pp.load(input);
				Iterator it = pp.keySet().iterator();
				while(it.hasNext()) {
					String key = (String) it.next();
					if(!props.containsKey(key)) {
						props.setProperty(key, pp.getProperty(key));
					}
				}

			}
		}

	}

	private void startJadeContainer(Properties props) {
		Profile profile = new ProfileImpl(props);
		Runtime.instance().setCloseVM(false);
		if(profile.getBooleanProperty(Profile.MAIN, true)) {
			container = Runtime.instance().createMainContainer(profile);
		} else {
			container = Runtime.instance().createAgentContainer(profile);
		}
		Runtime.instance().invokeOnTermination(new Terminator());
	}
	
	private void startJadeSplitContainer(Properties jadeProperties) {
		MicroRuntime.startJADE(jadeProperties, new Terminator());
	}

	private void registerJadeRuntimeService() {
		ServiceFactory factory;
		if(isSplitContainer()) {
			factory = new JadeRuntimeServiceFactory(agentManager);
		} else {
			factory = new JadeRuntimeServiceFactory(container, agentManager);
		}
		jrs = context.registerService(JadeRuntimeService.class.getName(), factory, null);
	}
	
	private boolean isSplitContainer() {
		return "true".equalsIgnoreCase(System.getProperty(SPLIT_CONTAINER_KEY));
	}
	
	private class Terminator implements Runnable {
		public void run() {
			synchronized(terminationLock) {
				if(!isSplitContainer()) {
					Runtime.instance().resetTerminators();
				}
				System.out.println("JADE termination invoked!");
				try {
					Bundle myBundle = context.getBundle();
					if(myBundle.getState() == Bundle.ACTIVE) {
						myBundle.stop(Bundle.STOP_TRANSIENT);
					}
				} catch(IllegalStateException ise) {
					// This exception is thrown when jadeOsgi bundle is invalid. This case happens
					// when user stop the bundle from the osgi ui. Depends on the execution time of the
					// thread listening jade termination, jadeOsgi bundle can be already stopped.
				} catch(Exception e) {
					logger.log(Logger.SEVERE, "Error stopping bundle", e);
				}
			}
		}
	}
	
}
