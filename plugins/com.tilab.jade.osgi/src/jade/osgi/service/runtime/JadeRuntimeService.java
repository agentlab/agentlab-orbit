package jade.osgi.service.runtime;

import jade.core.Agent;
import jade.wrapper.AgentController;

/**
 * This is an osgi service provided by jadeOsgi bundle that helps bundle to create agents
 * of different bundles. It wraps ContainerController object that allows access to the
 * JADE container started in the osgi runtime.
 * Against stop and update bundle operations, jadeOsgi provides some automatic mechanism
 * to manage created agents. When a bundle is stopped, jadeOsgi kills agents of that bundle.
 * When a bundle is updated jadeOsgi restarts agents of that bundle. 
 * @author 00918076
 *
 */
public interface JadeRuntimeService {

	/**
	 * Create a jade agent.
	 * @param name A platform-unique nickname for the newly created agent.
	 * @param className The fully qualified name of the class that implements the agent.
	 * @param args An object array, containing initialization parameters to pass to the new agent.
	 * @return A proxy object allowing access to the JADE agent
	 * @throws Exception
	 */
	public AgentController createNewAgent(String name, String className, Object[] args) throws Exception;
	
	/**
	 * Create an agent packaged by the bundle specified as argument. If there are more 
	 * versions of the same bundle, jadeOsgi chooses one of them without any criteria.
	 * @param name A platform-unique nickname for the newly created agent.
	 * @param className The fully qualified name of the class that implements the agent.
	 * @param args An object array, containing initialization parameters to pass to the new agent.
	 * @param bundleSymbolicName name of the bundle containing agent code
	 * @return A proxy object allowing access to the JADE agent
	 * @throws Exception
	 */
	public AgentController createNewAgent(String name, String className, Object [] args, String bundleSymbolicName) throws Exception;
	
	/**
	 * Create an agent packaged by the bundle with name and version passed as arguments.
	 * @param name A platform-unique nickname for the newly created agent.
	 * @param className The fully qualified name of the class that implements the agent.
	 * @param args An object array, containing initialization parameters to pass to the new agent.
	 * @param bundleSymbolicName name of the bundle containing agent code
	 * @param bundleVersion version of the bundle containing agent code
	 * @return A proxy object allowing access to the JADE agent
	 * @throws Exception
	 */
	public AgentController createNewAgent(String name, String className, Object [] args, String bundleSymbolicName, String bundleVersion) throws Exception;
	
	/**
	 * Create an agent owned by the bundle that is calling the method.
	 * @param name A platform-unique nickname for the newly created agent.
	 * @param agent The agent to be added to this agent container.
	 * @return A proxy object allowing access to the JADE agent
	 * @throws Exception
	 */
	public AgentController acceptNewAgent(String name, Agent agent) throws Exception;
	
	/**
	 * Get agent proxy to local agent given its name.
	 * @param localName The short local name of the desired agent.
	 * @return A proxy object allowing access to the JADE agent
	 */
	public AgentController getAgent(String localName) throws Exception;
	
	/**
	 * Shuts down the jade container running in the osgi runtime.
	 * @throws Exception
	 */
	public void kill() throws Exception;
	
	/**
	 * Retrieve the name of the wrapped container.
	 * @return the name of this platform container.
	 * @throws Exception
	 */
	public String getContainerName() throws Exception;
	
	/**
	 * Retrieve the name of the platform the container wrapped by this
	 * ContainerController belongs to.
	 * @return the name of this platform.
	 */
	public String getPlatformName();
}
