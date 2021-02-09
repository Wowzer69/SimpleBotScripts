package barrow.methods;

import java.util.Arrays;

import api.utils.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.api.Varbits;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

@RequiredArgsConstructor
@Getter
public enum Brothers {

	VERAC(Utils.makeArea(3553, 3302, 3560, 3296, 0), Utils.makeArea(3567, 9711, 3582, 9701, 3), Prayers.PROTECT_FROM_MELEE,
			Varbits.BARROWS_KILLED_VERAC),
	TORAG(Utils.makeArea(3550, 3285, 3557, 3279, 0), Utils.makeArea(3563, 9692, 3576, 9681, 3), Prayers.PROTECT_FROM_MELEE,
			Varbits.BARROWS_KILLED_TORAG),
	KARIL(Utils.makeArea(3562, 3278, 3568, 3273, 0), Utils.makeArea(3543, 9689, 3558, 9677, 3), Prayers.PROTECT_FROM_MISSILES,
			Varbits.BARROWS_KILLED_KARIL),
	AHRIM(Utils.makeArea(3562, 3290, 3567, 3286, 0), Utils.makeArea(3550, 9704, 3562, 9694, 3), Prayers.PROTECT_FROM_MAGIC,
			Varbits.BARROWS_KILLED_AHRIM),
	GUTHAN(Utils.makeArea(3573, 3284, 3578, 3279, 0), Utils.makeArea(3533, 9708, 3545, 9699, 3), Prayers.PROTECT_FROM_MELEE,
			Varbits.BARROWS_KILLED_GUTHAN),
	DHAROK(Utils.makeArea(3571, 3300, 3575, 3296, 0), Utils.makeArea(3549, 9720, 3561, 9710, 3), Prayers.PROTECT_FROM_MELEE,
			Varbits.BARROWS_KILLED_DHAROK);

	private final WorldArea digArea, tunnelArea;
	private final Prayers prayer;
	private final Varbits bit;
	@Setter
	private boolean tunnel = false;

	public boolean isKilled() {
		return ClientContext.instance().varpbits.varpbit(this.bit) == 1;
	}

	public static long getTotalKilled() {
		return Arrays.stream(Brothers.values()).filter(b -> b.isKilled()).count();
	}

	public static Brothers getNextBrother() {
		for (Brothers b : Brothers.values()) {

			if (getTotalKilled() == 5 && !b.isKilled() && b.isTunnel()) return b;
			if (!b.isKilled() && !b.isTunnel()) return b;
		}
		return null;
	}

	public static Brothers getTunnelBrother() {
		long killed = Brothers.getTotalKilled();
		return Arrays.stream(Brothers.values()).filter(bro -> {
			if (killed == 5) { return bro.isTunnel() || (!bro.isTunnel() && !bro.isKilled()); }
			return bro.isTunnel();
		}).findFirst().orElse(null);
	}

	public static void update() {
		if (Brothers.getTotalKilled() == 5)
			Arrays.stream(Brothers.values()).filter(b -> !b.isKilled()).forEach(b -> b.setTunnel(true));
	}

	public static void reset() {
		Arrays.stream(Brothers.values()).forEach(b -> b.setTunnel(false));
	}

}