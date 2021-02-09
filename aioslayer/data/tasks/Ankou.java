package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class Ankou implements MonsterTask {

	@Override
	public String getName() {
		return "Ankou";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Torstol seed", "Ancient shard", "Dark totem base", "Dark totem middle",
				"Dark totem top", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.ANKOU, NpcID.ANKOU_2516, NpcID.ANKOU_7257 };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("nieve");
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
		return new String[] { "Stronghold of Security", "Catacombs of Kourend" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}

}
