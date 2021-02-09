package api.listeners;

import java.util.EventObject;

import api.panel.Config;
import lombok.Getter;

@SuppressWarnings("serial")
public class ConfigChangeEvent extends EventObject {

	public ConfigChangeEvent(Config newConfig, Config oldConfig) {
		super(newConfig);
		this.newConfig = newConfig;
		this.oldConfig = oldConfig;
	}

	@Getter
	public Config newConfig;
	@Getter
	public Config oldConfig;
}
