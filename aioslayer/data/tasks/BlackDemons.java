package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class BlackDemons implements MonsterTask {

	@Override
	public String getName() {
		return "Black demons";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Ancient shard", "Dark totem base", "Dark totem middle", "Dark totem top",
				"Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.BLACK_DEMON, NpcID.BLACK_DEMON_7242, NpcID.BLACK_DEMON_7243 };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("taverley");
	}

	@Override
	public void attack() {
		if (!Tasks.getLoot().loot(lootList())) Tasks.getSlayer().fightNpc();
	}

	@Override
	public boolean atLocation() {
		return Constants.TASK != null && !Tasks.getCombat().isMonsterGoneForAWhile(getName());
	}

	@Override
	public void equipGear() {
		// TODO Auto-generated method stub

	}

	public String[] areas() {
		return new String[] { "Brimhaven Dungeon", "Catacombs of Kourend", "Taverley Dungeon" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}

}