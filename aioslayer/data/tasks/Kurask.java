package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimplePrayers.Prayers;

public class Kurask implements MonsterTask {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Kurask";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Leaf-bladed sword", "Leaf-bladed battleaxe", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.KURASK, NpcID.KURASK_410, NpcID.KURASK_411 };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("fremm");
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
		boolean equipped = Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "Leaf-bladed");
		;
		if (!equipped && Tasks.getInventory().contains("Leaf-bladed")) Tasks.getInventory().equip("Leaf-bladed");
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}

	@Override
	public String[] areas() {
		return new String[] { "Fremennik Slayer Cave" };
	}
}
