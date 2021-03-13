package molten.data;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import simple.api.Utils;
import simple.robot.api.ClientContext;

@AllArgsConstructor
@Getter
public enum Shops {

	CATHERBY(9312, 10355, false,
			new WorldPoint[] { new WorldPoint(2808, 3439, 0), new WorldPoint(2804, 3432, 0), new WorldPoint(2803, 3420, 0),
					new WorldPoint(2795, 3413, 0) }),
	PORT_SARIM(9379, 26254, true,
			new WorldPoint[] { new WorldPoint(3045, 3235, 0), new WorldPoint(3035, 3235, 0), new WorldPoint(3027, 3234, 0),
					new WorldPoint(3027, 3226, 0), new WorldPoint(3027, 3218, 0), new WorldPoint(3027, 3210, 0),
					new WorldPoint(3027, 3204, 0), new WorldPoint(3031, 3198, 0), new WorldPoint(3035, 3193, 0),
					new WorldPoint(3040, 3193, 0)
			}),
	PORT_PHASMATYS(9314, 29106, true,
			new WorldPoint[] { new WorldPoint(3689, 3469, 0), new WorldPoint(3690, 3473, 0), new WorldPoint(3699, 3474, 0),
					new WorldPoint(3699, 3482, 0), new WorldPoint(3699, 3490, 0), new WorldPoint(3699, 3496, 0),
					new WorldPoint(3702, 3500, 0) }),
	PORT_KHAZARD(9317, 26707, false, new WorldPoint[] {

			new WorldPoint(2661, 3159, 0), new WorldPoint(2662, 3154, 0), new WorldPoint(2667, 3149, 0),
			new WorldPoint(2673, 3148, 0) })
	;

	private int npcId, bankId;
	private boolean depositBox;
	private WorldPoint[] path;

	public boolean inDistance() {
		try {
			double bankDistance = ClientContext.instance().pathing.distanceTo(this.getPath()[0]);
			double shopDistance = ClientContext.instance().pathing.distanceTo(this.getPath()[this.getPath().length - 1]);
			return bankDistance < 30 && shopDistance < 30;
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

	public String getName() {
		return Utils.formatString(this.name().split("_"));
	}

	public static Shops find(String name) {
		return Arrays.asList(Shops.values()).stream().filter(val -> val.getName().equals(name)).findFirst().orElse(null);
	}

	public static String[] getNames() {
		return Arrays.asList(Shops.values()).stream().map(Shops::getName).toArray(String[]::new);
	}

}
