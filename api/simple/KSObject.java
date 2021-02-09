package api.simple;

import api.Tasks;
import api.Variables;
import lombok.AllArgsConstructor;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.wrappers.SimpleLocalPlayer;
import simple.hooks.wrappers.SimpleObject;

@AllArgsConstructor
public class KSObject {

	public SimpleObject object;

	public synchronized boolean click(String args0) {
		System.out.println(Variables.USE_PACKETS);
		if (Variables.USE_PACKETS) return Tasks.getMenuAction().get(object, args0).invoke();
		return args0 == null ? object.click(0) : object.click(args0);
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
