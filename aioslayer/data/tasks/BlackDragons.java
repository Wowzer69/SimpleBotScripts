package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import api.tasks.Supplies.PotionType;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimplePrayers.Prayers;

public class BlackDragons implements MonsterTask {

	@Override
	public String getName() {
		return "Black dragons";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Coins", "Draconic visage" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.BLACK_DRAGON, NpcID.BLACK_DRAGON_253, NpcID.BLACK_DRAGON_254, NpcID.BLACK_DRAGON_255,
				NpcID.BLACK_DRAGON_256, NpcID.BLACK_DRAGON_257, NpcID.BLACK_DRAGON_258, NpcID.BLACK_DRAGON_259 };
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

	private final String[] shield = { "dragon shield", "dragonfire" };

	@Override
	public void equipGear() {
		boolean equipped = Tasks.getInventory().isWearing(EquipmentSlot.SHIELD, shield);
		if (!equipped && Tasks.getInventory().contains(shield)) Tasks.getInventory().equip(shield);
	}

	@Override
	public String[] areas() {
		return new String[] { "Taverly Dungeon (expansion)", "Taverly Dungeon", "Catacombs of Kourend" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}
}
