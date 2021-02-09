package api.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import api.Tasks;
import lombok.Getter;
import net.runelite.api.ItemID;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.wrappers.SimpleItem;
import simple.robot.api.ClientContext;

public class Weapons {

	public static ArrayList<Integer> MAGE_WEAPONS = new ArrayList<Integer>(
			Arrays.asList(ItemID.ANCIENT_STAFF, ItemID.TOXIC_STAFF_OF_THE_DEAD, 24423, 24424, 24425, ItemID.STAFF_OF_THE_DEAD,
					ItemID.STAFF_OF_LIGHT, ItemID.STAFF_OF_WATER, ItemID.AHRIMS_STAFF, ItemID.MASTER_WAND, ItemID.KODAI_WAND,
					ItemID.ZURIELS_STAFF, ItemID.THAMMARONS_SCEPTRE_U, ItemID.THAMMARONS_SCEPTRE, ItemID.VOID_KNIGHT_MACE,
					ItemID.VOID_KNIGHT_MACE_BROKEN, ItemID.VOID_KNIGHT_MACE_L));

	public static SPECIAL_WEAPONS SPECIAL_WEAPON = null;

	public enum SPEED {
		FAST,
		SLOW,
		MODERATE
	}

	public enum SPECIAL_WEAPONS {
		DRAGON_DAGGER("ragon dagger", SPEED.SLOW, 25, false),
		DRAGON_CLAWS("claws", SPEED.FAST, 50, false),
		A_GODSWORD("rmadyl godsword", SPEED.FAST, 50, true),
		B_GODSWORD("andos godsword", SPEED.FAST, 50, true),
		S_GODSWORD("aradomin godsword", SPEED.FAST, 50, true),
		Z_GODSWORD("amorak  godsword", SPEED.FAST, 50, true),
		D_HALLY("ragon halberd", SPEED.MODERATE, 30, true),
		VESTA_LONGSWORD("esta's longsword", SPEED.FAST, 25, false),
		STATIUS_WARHAMMER("s warhammer", SPEED.MODERATE, 25, false),
		DRAGON_WARHAMMER("warhammer", SPEED.FAST, 50, false),
		DRAGON_MACE("ragon mace", SPEED.SLOW, 25, false),
		DRAGON_LONGSWORD("ragon longsword", SPEED.SLOW, 25, false),
		DRAGON_HALBERD("ragon halberd", SPEED.SLOW, 30, true);

		@Getter
		private String itemName;
		@Getter
		private SPEED speed;
		@Getter
		private int energy;
		@Getter
		private boolean twoHanded;

		SPECIAL_WEAPONS(String itemName, SPEED speed, int energy, boolean twoHanded) {
			this.itemName = itemName;
			this.speed = speed;
			this.energy = energy;
			this.twoHanded = twoHanded;
		}

		public SimpleItem item() {
			return Tasks.getInventory().getItem(this.itemName);
		}

		public boolean itemEquiped() {
			return Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, this.itemName);
		}

		public boolean needsRoom() {
			ClientContext ctx = ClientContext.instance();
			return isTwoHanded() && (ctx.equipment.getEquippedItem(EquipmentSlot.WEAPON) != null
					&& ctx.equipment.getEquippedItem(EquipmentSlot.SHIELD) != null);
		}
	}

	public static SPECIAL_WEAPONS getSpecialWeapon() {
		return Stream.of(SPECIAL_WEAPONS.values()).filter(weapon -> weapon.itemEquiped() || weapon.item() != null).findFirst()
				.orElse(null);
	}

	public static boolean canSpecial(boolean equipped) {
		ClientContext ctx = ClientContext.instance();
		if (SPECIAL_WEAPON == null) return false;
		if (SPECIAL_WEAPON.isTwoHanded() && ctx.inventory.inventoryFull()) return false;
		boolean can = ctx.combat.getSpecialAttackPercentage() >= SPECIAL_WEAPON.getEnergy();
		return equipped ? SPECIAL_WEAPON.itemEquiped() && can : can;
	}

	public static boolean isEquipped() {
		return SPECIAL_WEAPON != null && SPECIAL_WEAPON.itemEquiped();
	}

}
