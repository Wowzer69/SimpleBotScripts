package simple.api;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

import simple.api.listeners.ConfigChangeEvent;
import simple.api.listeners.ConfigChangeListener;

public class EventDispatcher {
	private final List<EventListener> listeners;
	private final Object syncLock = new Object();
	private volatile boolean running;

	public EventDispatcher() {
		this.listeners = new ArrayList<EventListener>();
		this.running = true;
	}

	public void addListener(EventListener listener) {
		synchronized (syncLock) {
			listeners.add(listener);
		}
	}

	public void removeListener(EventListener listener) {
		synchronized (syncLock) {
			listeners.remove(listener);
		}
	}

	public EventDispatcher clearListeners() {
		synchronized (syncLock) {
			listeners.clear();
		}
		return this;
	}

	public void fireEvent(EventObject event) {
		synchronized (syncLock) {
			for (EventListener listener : listeners) {
				if (listener instanceof ConfigChangeListener) {
					((ConfigChangeListener) listener).onChange((ConfigChangeEvent) event);
				}
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

	public EventDispatcher setRunning(boolean running) {
		this.running = running;
		return this;
	}

}