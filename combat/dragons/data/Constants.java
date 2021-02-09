package combat.dragons.data;

import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

public class Constants {
	public final static WorldArea DRAGON_LOBBY_AREA = new WorldArea(new WorldPoint(1536, 5090, 0), new WorldPoint(1605, 5056, 0));

	public final static WorldPoint RUNE_GATE_LOCATION = new WorldPoint(1574, 5074, 0);
	public final static WorldArea RUNE_DRAGON_AREA = new WorldArea(new WorldPoint(1574, 5085, 0), new WorldPoint(1599, 5061, 0));

	public final static WorldPoint ADAMANT_GATE_LOCATION = new WorldPoint(1561, 5073, 0);
	public final static WorldArea ADAMANT_DRAGON_AREA = new WorldArea(new WorldPoint(1561, 5061, 0),
			new WorldPoint(1538, 5087, 0));

	public static TYPES TYPE;

	public enum TYPES {
		RUNE_DRAGON,
		ADAMANT_DRAGON
	}

}
