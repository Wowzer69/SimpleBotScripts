package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class Bloodvelds implements MonsterTask {

	@Override
	public String getName() {
		return "Bloodveld";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Ancient shard", "Dark totem base", "Dark totem middle", "Dark totem top",
				"Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.BLOODVELD, NpcID.MUTATED_BLOODVELD };
	}

	/*
	 * Nieve's Cave Slayer Tower (basement) Slayer tower Catacombs of Kourend
	 */
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
		return new String[] { "Slayer Tower", "Slayer Tower (basement)", "Catacombs of Kourend", "Stronghold Slayer Cave" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}

}