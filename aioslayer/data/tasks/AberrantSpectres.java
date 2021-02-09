package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class AberrantSpectres implements MonsterTask {

	@Override
	public String getName() {
		return "Aberrant spectres";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Torstol seed", "Ancient shard", "Dark totem base", "Dark totem middle",
				"Dark totem top", "Torstol seed", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.ABERRANT_SPECTRE, NpcID.DEVIANT_SPECTRE };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("catacomb");
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

	@Override
	public String[] areas() {
		return new String[] { "Slayer Tower", "Nieve's Cave", "Catacombs of Kourend" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MAGIC;
	}

}
