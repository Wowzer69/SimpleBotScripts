package aiohunter.data;

import java.util.concurrent.CopyOnWriteArrayList;

import net.runelite.api.coords.WorldPoint;

public class Constants {

	public static CopyOnWriteArrayList<WorldPoint> CURRENT_TILES = new CopyOnWriteArrayList<WorldPoint>();
	public static CopyOnWriteArrayList<WorldPoint> POSSIBLE_TILES = new CopyOnWriteArrayList<WorldPoint>();

	public static WorldPoint LAST_LOCATION = null;

}
