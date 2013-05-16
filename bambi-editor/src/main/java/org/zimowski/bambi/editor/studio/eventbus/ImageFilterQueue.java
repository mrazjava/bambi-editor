package org.zimowski.bambi.editor.studio.eventbus;

import static org.zimowski.bambi.editor.filters.ImageFilterOps.Blue;
import static org.zimowski.bambi.editor.filters.ImageFilterOps.Green;
import static org.zimowski.bambi.editor.filters.ImageFilterOps.Red;

import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zimowski.bambi.editor.filters.ImageFilterOps;
import org.zimowski.bambi.editor.studio.eventbus.events.AbortFilterQueueEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ImageFilterQueueEvent;
import org.zimowski.bambi.editor.studio.eventbus.events.ModelLifecycleEvent;
import org.zimowski.bambi.editor.studio.image.AbstractFilterWorker;
import org.zimowski.bambi.editor.studio.resources.toolbar.ToolbarIcons;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Image filter execution queue. Manages sequential filter processing off an 
 * EDT so that GUI remains responsive, therefore its elements are instances of 
 * {@link SwingWorker}. This queue automatically fires event on add if it is 
 * empty and no event is executing, in all other cases event is queued and 
 * scheduled for execution based on FIFO principle.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class ImageFilterQueue extends LinkedList<AbstractFilterWorker> {

	private static final long serialVersionUID = 5963177948505705678L;
	
	private static final Logger log = LoggerFactory.getLogger(ImageFilterQueue.class);
	
	/**
	 * null if no event is currently executing, otherwise a valid instance of 
	 * the icon for which event is currently executed. We use icon rather than 
	 * boolean flag because it's needed to properly construct list as executing 
	 * event is no longer in the queue.
	 */
	private ToolbarIcons eventIcon;

	
	public ImageFilterQueue() {
		EventBusManager.getInstance().registerWithBus(this);
	}
	
	@Override
	public void addFirst(AbstractFilterWorker e) {
		if(!preprocessAdd(e)) return;
		super.addFirst(e);
		getBus().post(new ImageFilterQueueEvent(buildIconList()));
	}

	@Override
	public void addLast(AbstractFilterWorker e) {
		if(!preprocessAdd(e)) return;
		super.addLast(e);
		getBus().post(new ImageFilterQueueEvent(buildIconList()));
	}
	
	@Override
	public boolean add(AbstractFilterWorker e) {
		if(!preprocessAdd(e)) return false;
		boolean result = super.add(e);
		getBus().post(new ImageFilterQueueEvent(buildIconList()));
		return result;
	}
	
	/**
	 * Verifies if worker should be added to the queue. If queue is empty, the 
	 * worker is executed and false is returned it should not be queued.
	 * 
	 * @param e
	 * @return true if worker should be queued; false otherwise
	 */
	private boolean preprocessAdd(final AbstractFilterWorker e) {
		log.debug("queue size: {}", size());
		if(size() == 0 && eventIcon == null) {
			eventIcon = e.getToolbarIcon();
			if(eventIcon != null) {
				eventIcon.setMetaInfo(e.getDisplayValue());
				getBus().post(new ImageFilterQueueEvent(buildIconList()));
			}
			e.execute();
			
			return false;
		}
		ImageFilterOps meta = e.getMetaData();
		if(Red.equals(meta) || Green.equals(meta) || Blue.equals(meta)) {
			for(AbstractFilterWorker w : this) {
				if(e.getMetaData().equals(w.getMetaData())) {
					remove(w);
					break;
				}
			}
		}
		return true;
	}

	@Subscribe
	public void onModelChanged(ModelLifecycleEvent ev) {
		if(ModelLifecycleEvent.ModelPhase.AfterChange.equals(ev.getPhase())) {
			if(size() > 0) {
				AbstractFilterWorker worker = removeFirst();
				eventIcon = worker.getToolbarIcon();
				getBus().post(new ImageFilterQueueEvent(buildIconList()));
				worker.execute();
			}
			else {
				getBus().post(new ImageFilterQueueEvent(new LinkedList<ToolbarIcons>()));
				eventIcon = null;
			}
		}
	}
	
	@Subscribe
	public void onAbort(AbortFilterQueueEvent ev) {
		ImageFilterQueueEvent abortEvent = new ImageFilterQueueEvent(buildIconList());
		abortEvent.setAborted(true);
		getBus().post(abortEvent);
		clear();
	}
	
	private List<ToolbarIcons> buildIconList() {
		List<ToolbarIcons> list = new LinkedList<ToolbarIcons>();
		if(eventIcon != null) list.add(eventIcon);
		for(AbstractFilterWorker worker : this) {
			ToolbarIcons icon = worker.getToolbarIcon();
			if(icon != null) {
				icon.setMetaInfo(worker.getDisplayValue());
			}
			else {
				log.warn("missing icon for {}", worker.getMetaData().toString());
			}
			list.add(icon);
		}
		return list;
	}
	
	private EventBus eventBus = null;
	private EventBus getBus() {
		if(eventBus == null) eventBus = EventBusManager.getInstance().getBus();
		return eventBus;
	}
}