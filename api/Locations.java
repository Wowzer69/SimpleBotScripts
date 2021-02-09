package api;

import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

public class Locations {

	public final static WorldArea EDGEVILLE_AREA = new WorldArea(new WorldPoint(3074, 3515, 0), new WorldPoint(3105, 3480, 0));
	public final static WorldArea EDGEVILLE_BANK = new WorldArea(new WorldPoint(3098, 3487, 0), new WorldPoint(3090, 3499, 0));

	public final static WorldArea BARROWS_HILLS = new WorldArea(
			new WorldPoint[] { new WorldPoint(3565, 3314, 0), new WorldPoint(3543, 3299, 0), new WorldPoint(3547, 3270, 0),
					new WorldPoint(3566, 3266, 0), new WorldPoint(3584, 3275, 0), new WorldPoint(3583, 3306, 0) });

	public final static WorldArea BURTHORPE_AREA = new WorldArea(new WorldPoint(2892, 3557, 0), new WorldPoint(2934, 3529, 0));;

	public final static WorldArea BARROWS_FINAL_SARCO = new WorldArea(new WorldPoint(3547, 9700, 0),
			new WorldPoint(3558, 9690, 0));
	public static final WorldArea BANDOS_AREA = new WorldArea(new WorldPoint(2860, 5374, 2), new WorldPoint(2878, 5349, 2));;

	public final static WorldArea VORKATH_START_AREA = new WorldArea(new WorldPoint(2270, 4054, 0),
			new WorldPoint(2276, 4035, 0));

}
