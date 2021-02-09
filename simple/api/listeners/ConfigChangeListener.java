package simple.api.listeners;

import java.util.EventListener;

public interface ConfigChangeListener extends EventListener {
	void onChange(ConfigChangeEvent event);
}
