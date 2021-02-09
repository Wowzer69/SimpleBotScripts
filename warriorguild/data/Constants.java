package warriorguild.data;

import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

public class Constants {

	public static int CURRENT_TASK = 0;

	public final static WorldPoint LEVEL_3_DOOR = new WorldPoint(2847, 3540, 2);
	public final static WorldArea TOKEN_AREA = new WorldArea(new WorldPoint(2849, 3546, 0), new WorldPoint(2861, 3534, 0));

	public final static WorldArea CYCLOPS_AREA = new WorldArea(new WorldPoint(2836, 3557, 2), new WorldPoint(2876, 3533, 2));
	public final static WorldArea CYCLOPS_ENTRY = new WorldArea(new WorldPoint(2838, 3543, 2), new WorldPoint(2847, 3536, 2));

	public final static WorldArea CYCLOPS2_AREA = new WorldArea(new WorldPoint(2905, 9974, 0), new WorldPoint(2941, 9957, 0));
	public final static WorldArea CYCLOPS2_ENTRY = new WorldArea(new WorldPoint(2905, 9974, 0), new WorldPoint(2912, 9966, 0));

	public final static int RUNE_ANIMATED_ARMOR = 5432;
	public final static int TOKEN_DOOR = 24306;

}
