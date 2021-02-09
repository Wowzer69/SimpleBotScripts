package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;

public class DarkBeasts implements MonsterTask {

	@Override
	public String getName() {
		return "Dark beasts";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Dark bow", "Torstol seed", "Runite ore", "Black d'hide body", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.DARK_BEAST };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("Ardougne");
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
		return new String[] { "Ardougne" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}

}
