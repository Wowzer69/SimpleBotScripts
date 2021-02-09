package api.simple;

import api.Tasks;
import api.Variables;
import lombok.AllArgsConstructor;
import simple.hooks.wrappers.SimpleItem;

@AllArgsConstructor
public class KSItem {

	public SimpleItem item;

	public synchronized boolean click(String args0) {
		if (Variables.USE_PACKETS) return Tasks.getMenuAction().get(item, args0).invoke();
		return item.click(args0);
	}

	public boolean isNull() {
		return item == null;
	}

	public String getName() {
		return item.getName();
	}

}
