package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class Dagannoth implements MonsterTask {

	@Override
	public String getName() {
		return "Dagannoth";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Ancient shard", "Dark totem base", "Dark totem middle", "Dark totem top",
				"Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.DAGANNOTH_7259, NpcID.DAGANNOTH_7260 };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("Catacombs of Kourend");
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
		return new String[] { "Waterbirth", "Catacombs of Kourend", "Lighthouse Dungeon" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}
}
