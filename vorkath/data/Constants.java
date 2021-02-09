package vorkath.data;

import java.util.ArrayList;
import java.util.List;

import api.utils.Timer;
import net.runelite.api.coords.WorldPoint;

public class Constants {

	public static List<WorldPoint> CURRENT_ACID_TILES = new ArrayList<WorldPoint>();
	public static boolean RECOLLECT_ITEMS = false;
	public static Timer LAST_FIRE_BALL = new Timer(2000);

	public static boolean PATH_SET = false;

}
