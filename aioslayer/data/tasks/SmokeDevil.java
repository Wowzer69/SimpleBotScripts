package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;

public class SmokeDevil implements MonsterTask {

	@Override
	public String getName() {
		return "Smoke devils";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Occult necklace", "Dragon chainbody", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.SMOKE_DEVIL, NpcID.SMOKE_DEVIL_6639, NpcID.SMOKE_DEVIL_8482, NpcID.SMOKE_DEVIL_8483 };
	}

	@Override
	public void travel() {
		SimpleObject cave = ClientContext.instance().objects.populate().filter("Smoky cave").next();
		if (cave != null) {
			if (cave.validateInteractable()) cave.click("Enter");
		} else {
			Tasks.getSlayer().useRing("wars");
		}
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
		return new String[] { "Smoke Devil Dungeon" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MISSILES;
	}

}
