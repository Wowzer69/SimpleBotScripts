package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class CaveHorrors implements MonsterTask {

	@Override
	public String getName() {
		return "Cave horror";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Black mask", "Torstol seed", "Curved bone", "Long bone", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.CAVE_HORROR, NpcID.CAVE_HORROR_1048, NpcID.CAVE_HORROR_1049, NpcID.CAVE_HORROR_1050,
				NpcID.CAVE_HORROR_1051 };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("Mos'le Harmless");
	}

	@Override
	public void attack() {
		if (!Tasks.getLoot().loot(lootList())) Tasks.getSlayer().fightNpc();
	}

	@Override
	public boolean atLocation() {
		return Constants.TASK != null && !Tasks.getCombat().isMonsterGoneForAWhile(getId());
	}

	@Override
	public void equipGear() {
		// TODO Auto-generated method stub

	}

	public String[] areas() {
		return new String[] { "Mos'le Harmless" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}
}