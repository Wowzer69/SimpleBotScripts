package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class Hellhounds implements MonsterTask {

	@Override
	public String getName() {
		return "Hellhounds";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.HELLHOUND, NpcID.HELLHOUND_7256 };
	}

	/*
	 * Nieve's Cave Taverly Dungeon Catacombs of Kourdend Revenant Cave (19)
	 * Karuulm Slayer Dungeon
	 */

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("Kourend");
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
		return new String[] { "Nieve's Cave", "Taverly Dungeon", "Karuulm Slayer Dungeon", "Catacombs of Kourend" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}

}
