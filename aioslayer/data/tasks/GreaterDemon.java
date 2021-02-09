package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class GreaterDemon implements MonsterTask {

	@Override
	public String getName() {
		return "Greater demons";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.GREATER_DEMON, NpcID.GREATER_DEMON_7244, NpcID.GREATER_DEMON_7245 };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("kourend");
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
		return new String[] { "Chasm of Fire", "Catacombs of Kourend", "Karuulm Slayer Dungeon" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}
}
