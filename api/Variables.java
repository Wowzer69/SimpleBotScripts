package api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import api.utils.Timer;
import simple.hooks.filters.SimplePrayers.Prayers;

public class Variables {
	public static Timer START_TIME;
	public static boolean STARTED, STOP, PAUSED;
	public static String STATUS, LAST_MESSAGE;

	public static long COUNT;

	public static boolean FORCE_BANK, USE_PRAYER;
	public static CopyOnWriteArrayList<Prayers> ACTIVE_PRAYERS;
	public static List<String> LOOTABLES;

	public static boolean USE_PACKETS = true;
	public static EventDispatcher DISPATCHER;

	public static void reset() {
		ACTIVE_PRAYERS = new CopyOnWriteArrayList<Prayers>();
		LOOTABLES = new ArrayList<String>();
		STARTED = STOP = PAUSED = false;
		FORCE_BANK = USE_PRAYER = false;
		COUNT = 0;
		START_TIME = new Timer();
		STATUS = "Booting up";
		LAST_MESSAGE = "";
		DISPATCHER = new EventDispatcher();
	}
}
