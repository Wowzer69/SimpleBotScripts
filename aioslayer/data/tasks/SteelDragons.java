package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import api.tasks.Supplies.PotionType;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimplePrayers.Prayers;

public class SteelDragons implements MonsterTask {

	@Override
	public String getName() {
		return "Steel dragons";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Coins", "Draconic visage" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.STEEL_DRAGON_7255 };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("taverley");
	}

	@Override
	public void attack() {
		if (!Tasks.getLoot().loot(lootList())) {
			if (!Tasks.getSupplies().antiFire.isRunning()) Tasks.getSupplies().drink(PotionType.ANTIFIRE);
			Tasks.getSlayer().fightNpc();
		}

	}

	@Override
	public boolean atLocation() {
		return Constants.TASK != null && !Tasks.getCombat().isMonsterGoneForAWhile(getId());
	}

	@Override
	public void equipGear() {
		boolean equipped = Tasks.getInventory().isWearing(EquipmentSlot.SHIELD, "dragon shield", "dragonfire");
		if (!equipped && Tasks.getInventory().contains("dragon shield", "dragonfire"))
			Tasks.getInventory().equip("dragon shield", "dragonfire");
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}

	@Override
	public String[] areas() {
		return new String[] { "Taverly" };
	}
}