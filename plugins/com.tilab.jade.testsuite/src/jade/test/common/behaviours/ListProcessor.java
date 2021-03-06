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

package jade.test.common.behaviours;

import jade.core.*;
import jade.core.behaviours.*;
import jade.util.leap.*;

/**
   @author Giovanni Caire - TILAB
 */
public abstract class ListProcessor extends SimpleBehaviour {
	protected List items ;
	private int cnt = 0;
	private boolean paused = false;
	private boolean stopped = false;
	
	public ListProcessor(Agent a, List l) {
		super(a);
		items = (l != null ? l : new ArrayList());
	}
	
	protected abstract void processItem(Object item, int index);

	public void action() {
		if (!stopped) {
			if (!paused) {
				if (cnt < items.size()) {
					Object i = items.get(cnt);
					processItem(i, cnt);
					cnt++;
				}
			}
			else {
				block();
			}
		}
	}
	
	public boolean done() {
		return (cnt >= items.size() && !paused) || stopped;
	}
	
	public void pause() {
		paused = true;
		block();
	}
	
	public void resume() {
		paused = false;
		restart();
	}
	
	public void stop() {
		stopped = true;
		if (paused) {
			restart();
		}
	}
	
	public boolean isStopped() {
		return stopped;
	}
}