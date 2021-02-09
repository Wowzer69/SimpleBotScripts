package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class AbyssalDemon implements MonsterTask {

	@Override
	public String getName() {
		return "Abyssal demons";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Abyssal head", "Abyssal whip", "Abyssal dagger", "Ancient shard",
				"Dark totem base", "Dark totem middle", "Dark totem top", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.ABYSSAL_DEMON, NpcID.ABYSSAL_DEMON_415, NpcID.ABYSSAL_DEMON_416, NpcID.ABYSSAL_DEMON_7241 };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("basement");
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
		return new String[] { "Slayer Tower (basement)", "Slayer Tower", "Catacombs of Kourend", "Abyssal Area" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}
}
