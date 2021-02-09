package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Locations;
import api.Tasks;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimplePrayers.Prayers;

public class Kalphite implements MonsterTask {

	@Override
	public String getName() {
		return "Kalphite";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.KALPHITE_GUARDIAN, NpcID.KALPHITE_GUARDIAN_960, NpcID.KALPHITE_GUARDIAN_962,
				NpcID.KALPHITE_WORKER };
	}

	@Override
	public void travel() {
		if (Locations.EDGEVILLE_AREA.containsPoint(ctx.players.getLocal().getLocation())) {
			Tasks.getSlayer().useRing("cave");
		} else {
			ctx.pathing.walkPath(path);
		}

	}

	@Override
	public void attack() {
		if (!Tasks.getLoot().loot(lootList())) Tasks.getSlayer().fightNpc();
	}

	@Override
	public boolean atLocation() {
		return Constants.TASK != null && !Tasks.getCombat().isMonsterGoneForAWhile(getId())
				&& ctx.pathing.distanceTo(path[path.length - 1]) < 5;
	}

	@Override
	public void equipGear() {
		// TODO Auto-generated method stub

	}

	public String[] areas() {
		return new String[] { "Kalphite Lair", "Kalphite Cave" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}

	WorldPoint[] path = { new WorldPoint(3296, 9500, 0), new WorldPoint(3292, 9500, 0), new WorldPoint(3288, 9500, 0),
			new WorldPoint(3285, 9499, 0), new WorldPoint(3282, 9499, 0), new WorldPoint(3280, 9496, 0),
			new WorldPoint(3279, 9492, 0), new WorldPoint(3279, 9489, 0) };

}
