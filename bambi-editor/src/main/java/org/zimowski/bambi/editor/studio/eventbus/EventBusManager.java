package org.zimowski.bambi.editor.studio.eventbus;

import java.awt.Window;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.studio.MainWindow;

import com.google.common.eventbus.EventBus;

/**
 * Centralized manager for Guaava {@link EventBus} which by default is  
 * context unaware. This manager defines the context as a top level window 
 * from which events originate. Each window's {@link Window#hashCode()} is 
 * used as context bus number, therefore guaranteeing every window will have 
 * its own event bus. It is critical that all EventBus requests originate with 
 * this manager if context management is required. Top level windows should 
 * register with the manager first by letting it know immedialy after they 
 * are constructed ({@link #setWindowInstantiated(MainWindow)} - best called 
 * from inside the constructor, then each time when window gains focus 
 * manager must be told so via {@link #switchContext(MainWindow)}. Also 
 * all subscribers, rather than registering directly with the bus, must 
 * register with this manager instead, via {@link #registerWithBus(Object)}.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class EventBusManager {

	private static final Logger log = LoggerFactory.getLogger(EventBusManager.class);
	
	/**
	 * hash of the top level window in focus (context)
	 */
	private int currentHash = 0;
	
	private int nextHash = 0;
	
	private static final Map<Integer, EventBus> busMap = 
			new HashMap<Integer, EventBus>();
	
	private static EventBusManager manager = new EventBusManager();
	
	private Queue<Object> subscribers = new LinkedList<Object>();
	
	
	private EventBusManager() {
		// singleton
	}

	public static EventBusManager getInstance() {
		return manager;
	}

	public EventBus getBus() {
		
		if(currentHash == 0) throw new IllegalStateException("bad hash");
		
		EventBus bus = busMap.get(currentHash);
		if(bus == null) {
			log.debug("created new bus # {}", currentHash);
			bus = new EventBus();
			busMap.put(currentHash, bus);
		}
		return bus;
	}
	
	public void switchContext(MainWindow busOwner) {
		
		final int hash = busOwner.hashCode();
		
		if(nextHash == hash)
			nextHash = 0;
		else if(nextHash > 0)
			throw new IllegalStateException("hash mismatch!");
		
		if(currentHash != hash) {
			currentHash = hash;
			log.debug("switched to bus # {}", currentHash);
			registerQueuedSubscribers();
		}
	}
	
	private void registerQueuedSubscribers() {
		for(Object subscriber : subscribers) {
			log.debug("registering {}", subscriber.getClass().getSimpleName());
			getBus().register(subscriber);
		}
		subscribers.clear();
	}
	
	public void setWindowInstantiated(MainWindow busOwner) {
		nextHash = busOwner.hashCode();
		log.debug("preparing new bus # {}", nextHash);
	}
	
	private boolean isBusReady() {
		return nextHash == 0 && currentHash != 0;
	}
	
	public void registerWithBus(Object subscriber) {
		if(isBusReady()) {
			log.debug(subscriber.getClass().getSimpleName() + " registered");
			getBus().register(subscriber);
		}
		else {
			log.debug(subscriber.getClass().getSimpleName() + " queued");
			subscribers.add(subscriber);
		}
	}
	
}