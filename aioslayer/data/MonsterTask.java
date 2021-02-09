package aioslayer.data;

import simple.hooks.filters.SimplePrayers.Prayers;
import simple.robot.api.ClientContext;

public interface MonsterTask {

	ClientContext ctx = ClientContext.instance();

	String getName();

	String[] lootList();

	int[] getId();

	void equipGear();

	boolean atLocation();

	void travel();

	void attack();

	String[] areas();

	Prayers getProtection();
}
