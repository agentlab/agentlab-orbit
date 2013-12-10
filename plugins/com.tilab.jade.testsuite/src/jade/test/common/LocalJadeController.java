/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.test.common;

//import jade.core.Agent;
import jade.core.Runtime;
//import jade.core.Profile;
//import jade.core.ProfileImpl;
//import jade.wrapper.*;
import jade.util.leap.*;
//import test.common.*;
import java.io.*;
import java.util.StringTokenizer;

/**
   @author Giovanni Caire - TILAB
 */
class LocalJadeController implements JadeController, OutputHandler {
	private Object lock = new Object();
	private boolean ready = false;
	private List addresses = new ArrayList();
	private String containerName = null;
	private Process proc;
	private OutputHandler outHandler;
	
	public LocalJadeController(String instanceName, String cmdLine, String[] protoNames, OutputHandler handler, String workingDir, String startupTag) throws TestException {
		outHandler = (handler != null ? handler : this);
		workingDir = (workingDir != null ? workingDir : ".");
		try {
			// Start a JADE instance in a different Process
			System.out.println("Starting JADE with command line: "+cmdLine);
			//System.out.println("Environment: "+System.getenv());
			proc = java.lang.Runtime.getRuntime().exec(cmdLine, null, new File(workingDir).getCanonicalFile());
			
			Thread t = new SubProcessManager(instanceName, proc, protoNames, startupTag);
			t.start();
			
			waitForJadeStartup();
		}
		catch (Exception e) {
			if (proc != null) {
				proc.destroy();
			}
			throw new ExecException("Error launching remote JADE", e); 
		}
		
		if (!ready) {
			System.out.println("JADE startup timeout expired");
			proc.destroy();
			throw new TestException("Remote JADE startup was not completed successfully"); 
		}
	}
	
	private void waitForJadeStartup() {
		// Two cases are possible:
		// 1) JADE startup completed succesffully --> 
		//    we exit lock.wait() with ready == true
		// 2) JADE startup failed or stuck (we wait at most 60 sec) -->
		//    we exit lock.wait() with ready == false 
		
		synchronized (lock) {
			while (!ready) {
				try {
					lock.wait(60000); // Wait for 60 sec at most
					break;
				}
				catch (InterruptedException ie) {
					// Should never happen
					ie.printStackTrace();
				}
			}
		}
	}
	
	private void notifyStarted() {
		synchronized (lock) {
			ready = true;
			lock.notifyAll();
		}
	}	

	private void notifyTerminated() {
		// If ready is false then the sub-process exited prematurely
		// Notify the launcher.
		synchronized (lock) {
			if (!ready) {
				lock.notifyAll();
			}
		}
	}
	
	public List getAddresses() {
		return addresses;
	}
	
	public String getContainerName() {
		return containerName;
	}
		
	public void kill() {
		if (proc != null) {
			proc.destroy();
			try {
				Thread.sleep(1000);
			}
			catch (Exception e) {}
		}
	}
	
	class SubProcessManager extends Thread {
		private Process subProc;
		private BufferedReader br;
		private BufferedReader brErr;
		private String[] protoNames;
		private String name;
		private Thread errorManager;
		private String startupTag;
		
		public SubProcessManager(String n, Process p, String[] names, String st) {
			name = n;
			subProc = p;
			startupTag = st;
			br = new BufferedReader(new InputStreamReader(subProc.getInputStream()));
			protoNames = (names != null ? names : new String[0]);
			errorManager = startErrorManager();
		}
		
		public void run() {
			while (true) {
				try {
					// Check if the sub-process is still alive
					subProc.exitValue();
					System.out.println("Remote JADE instance "+name+" terminated");
					
					notifyTerminated();
					break;
				}
				catch (IllegalThreadStateException itse) {
					// The sub-process is still alive --> go on
				}
				
				try {
					String line = br.readLine();
					handleLine(line);
				}
				catch (Exception e) {
					e.printStackTrace();
				}				
			}  // END of while
			
			stopErrorManager();
		}   // END of run()

		private void handleLine(String line) {
			if (line != null) {
				String id = name;
				if (id == null) {
					id = containerName;
				}
				if (id == null) {
					id = "(unknown)";
				}
				// Redirect sub-process output
				outHandler.handleOutput(id, line);

				// Possibly update the list of addresses of this JADE instance
				catchAddress(line);

				// Notify the launcher when JADE startup is completed (startup-tag present in output)
				if (containerName == null && line.startsWith(startupTag)) {
					catchContainerName(line);
					notifyStarted();
				} else {
				}
			}
		}
		
		private void catchAddress(String line) {
			for (int i = 0; i < protoNames.length; ++i) {
				if (line.startsWith(protoNames[i])) {
					addresses.add(line);
					break;
				}
			}
		}	
		
		private void catchContainerName(String line) {
			StringTokenizer st = new StringTokenizer(line, " @");
			st.nextToken(); // UUID
			containerName = st.nextToken();
		}
		
		private Thread startErrorManager() {
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						brErr = new BufferedReader(new InputStreamReader(subProc.getErrorStream()));
						while (true) {
							String line = brErr.readLine();
							handleLine(line);
						}
					}
					catch (Exception e) {
						// The process has terminated. Do nothing
					}
					System.out.println("ErrorManager exiting");
				}
			} );
			t.start();
			return t;
		}
		
		private void stopErrorManager() {
			try {
				brErr.close();
				errorManager.interrupt();
			}
			catch (Exception e) {
				System.out.println("Error closing Error stream");
			}
		}			
	}   // END of inner class SubProcessManager
	
	
	/////////////////////////////////////////////////
	// OutputHandler interface implementation
	/////////////////////////////////////////////////
	public void handleOutput(String source, String msg) {
		System.out.println(source+">> "+msg);
	}
}
