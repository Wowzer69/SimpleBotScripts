package simple.api;

public class Variables {

	public static String DEBUG = "";
	public static Timer START_TIME = new Timer();
	public static String STATUS = "Booting up";
	public static boolean STOP;
	public static boolean STARTED;
	public static boolean PAUSED;

	public static EventDispatcher DISPATCHER;

	public static int COUNT = 0;
	public static boolean[] CACHED_BOOLEANS = new boolean[10];
	public static String LAST_MESSAGE = "";

	public static void reset() {
		START_TIME.restart();
		LAST_MESSAGE = "";
		DISPATCHER = new EventDispatcher();
		STOP = false;
		STARTED = false;
		PAUSED = true;
		COUNT = 0;
		DEBUG = "";
		CACHED_BOOLEANS = new boolean[10];
		STATUS = "Booting up";
	}

}
