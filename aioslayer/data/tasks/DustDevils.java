package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class DustDevils implements MonsterTask {

	@Override
	public String getName() {
		return "Dust Devils";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Dragon chainbody", "Dust battlestaff", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.DUST_DEVIL, 7249 };
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
		return new String[] { "Smoke Dungeon", "Pollnivneach", "Catacombs of Kourend" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}

}
