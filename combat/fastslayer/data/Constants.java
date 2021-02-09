package combat.fastslayer.data;

import java.util.ArrayList;
import java.util.List;

import net.runelite.api.coords.WorldPoint;
import simple.robot.utils.WorldArea;

public class Constants {

	public static final WorldPoint[] BURTHORPE_PATH = new WorldPoint[] { new WorldPoint(2899, 3552, 0),
			new WorldPoint(2899, 3545, 0), new WorldPoint(2907, 3545, 0), new WorldPoint(2916, 3545, 0),
			new WorldPoint(2923, 3540, 0), new WorldPoint(2931, 3536, 0) };
	public static final WorldPoint[] EDGEVILLE_PATH = new WorldPoint[] { new WorldPoint(3086, 3493, 0),
			new WorldPoint(3087, 3489, 0), new WorldPoint(3091, 3484, 0) };;
	public static SlayerTask TASK = null;
	public final static WorldArea EDGEVILLE_AREA = new WorldArea(new WorldPoint(3081, 3502, 0), new WorldPoint(3098, 3481, 0));
	public final static WorldArea BURTHORPE_AREA = new WorldArea(new WorldPoint(2892, 3557, 0), new WorldPoint(2934, 3529, 0));;

	public static boolean CHECK_TASK = false;
	public static int TOTAL_TASKS = -1;

	public static List<String> SKIP_TASKS = new ArrayList<String>();
	public static boolean SKIP = false;

}
