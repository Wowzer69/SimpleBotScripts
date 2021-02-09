package simple.api;

import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

public class Locations {

	public final static WorldArea EDGEVILLE_AREA = new WorldArea(new WorldPoint(3074, 3515, 0), new WorldPoint(3105, 3480, 0));

	public static final WorldArea BARROWS_HILLS = new WorldArea(
			new WorldPoint[] { new WorldPoint(3565, 3314, 0), new WorldPoint(3543, 3299, 0), new WorldPoint(3547, 3270, 0),
					new WorldPoint(3566, 3266, 0), new WorldPoint(3584, 3275, 0), new WorldPoint(3583, 3306, 0) });

	public static final WorldArea BARROWS_FINAL_SARCO = new WorldArea(new WorldPoint(3547, 9700, 0),
			new WorldPoint(3558, 9690, 0));

}
