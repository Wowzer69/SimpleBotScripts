package api.simple;

import api.Tasks;
import api.Variables;
import lombok.AllArgsConstructor;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleLocalPlayer;

@AllArgsConstructor
public class KSGroundItem {

	public SimpleGroundItem object;

	public synchronized boolean click(String args0) {
		if (Variables.USE_PACKETS) return Tasks.getMenuAction().get(object, args0).invoke();
		return object.click(args0);
	}

	public boolean isNull() {
		return object == null;
	}

	public synchronized boolean validateInteractable() {
		if (Variables.USE_PACKETS) return true;
		return object.validateInteractable();
	}

	public int distanceTo(SimpleLocalPlayer local) {
		return object.distanceTo(local);
	}

	public WorldPoint getLocation() {
		return object.getLocation();
	}

	public String[] getActions() {
		return object.getActions();
	}

	public String getName() {
		return object.getName();
	}
}
