package herblore.data;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import simple.api.Utils;
import simple.robot.api.ClientContext;

@AllArgsConstructor
@Getter
public enum Potions {

	ATTACK(3, ItemID.ATTACK_POTION3, ItemID.EYE_OF_NEWT, Herbs.GUAM_LEAF),
	ANTIPOISON(5, ItemID.ANTIPOISON3, ItemID.UNICORN_HORN_DUST, Herbs.MARRENTILL),
	STRENGTH(12, ItemID.STRENGTH_POTION3, ItemID.LIMPWURT_ROOT, Herbs.TARROMIN),
	SERUM_207(15, ItemID.SERUM_207_3, ItemID.ASHES, Herbs.TARROMIN),
	COMPOST(22, ItemID.COMPOST_POTION3, ItemID.VOLCANIC_ASH, Herbs.HARRALANDER),
	RESTORE(22, ItemID.RESTORE_POTION3, ItemID.RED_SPIDERS_EGGS, Herbs.HARRALANDER),
	ENERGY(26, ItemID.ENERGY_POTION3, ItemID.CHOCOLATE_DUST, Herbs.HARRALANDER),
	DEFENCE(30, ItemID.DEFENCE_POTION3, ItemID.WHITE_BERRIES, Herbs.RANARR_WEED),
	AGILITY(34, ItemID.AGILITY_POTION3, ItemID.TOADS_LEGS, Herbs.TOADFLAX),
	COMBAT(84, ItemID.COMBAT_POTION3, ItemID.GOAT_HORN_DUST, Herbs.HARRALANDER),
	PRAYER(38, ItemID.PRAYER_POTION3, ItemID.SNAPE_GRASS, Herbs.RANARR_WEED),
	SUPER_ATTACK(45, ItemID.SUPER_ATTACK3, ItemID.EYE_OF_NEWT, Herbs.IRIT_LEAF),
	SUPERANTIPOISON(48, ItemID.SUPERANTIPOISON3, ItemID.UNICORN_HORN_DUST, Herbs.IRIT_LEAF),
	SUPER_ENERGY(52, ItemID.SUPER_ENERGY3, ItemID.MORT_MYRE_FUNGUS, Herbs.AVANTOE),
	SUPER_STRENGTH(55, ItemID.SUPER_STRENGTH3, ItemID.LIMPWURT_ROOT, Herbs.KWUARM),
	SUPER_RESTORE(63, ItemID.SUPER_RESTORE3, ItemID.RED_SPIDERS_EGGS, Herbs.SNAPDRAGON),
	SUPER_DEFENCE(66, ItemID.SUPER_DEFENCE3, ItemID.WHITE_BERRIES, Herbs.CADANTINE),
	ANTIFIRE(69, ItemID.ANTIFIRE_POTION3, ItemID.DRAGON_SCALE_DUST, Herbs.LANTADYME),
	RANGING(72, ItemID.RANGING_POTION3, ItemID.WINE_OF_ZAMORAK, Herbs.DWARF_WEED),
	MAGIC(76, ItemID.MAGIC_POTION3, ItemID.POTATO_CACTUS, Herbs.LANTADYME),
	// STAMINA(77, ItemID.STAMINA_POTION3, ItemID.AMYLASE_CRYSTAL, null)
	SARADOMIN_BREW(81, ItemID.SARADOMIN_BREW3, ItemID.CRUSHED_NEST, Herbs.TOADFLAX),

	;

	private int reqLevel, potionId, ingredientId;
	private Herbs reqHerb;

	public String getName() {
		return Utils.formatString(this.name().split("_"));
	}

	public String getString() {
		return String.format("{Name: %s, reqLevel: %s, reqHerb: %s, ingredientId: %s, potionId: %s}", getName(), reqLevel,
				reqHerb.getName(), ingredientId, potionId);
	}

	@Override
	public String toString() {
		return getName();
	}

	public static List<Potions> getMakeable(int level) {
		return Arrays.asList(Potions.values()).stream().sorted(Comparator.reverseOrder()).filter(potion -> {

			return level >= potion.getReqLevel()
					&& ClientContext.instance().bank.populate().filter(potion.getIngredientId()).population(true) > 0
					&& ClientContext.instance().bank.populate().filter(potion.getReqHerb().getUnfId()).population(true) > 0;
		}).collect(Collectors.toList());
	}

	public static Potions find(String name) {
		return Arrays.asList(Potions.values()).stream().filter(val -> val.getName().equals(name)).findFirst().orElse(null);
	}

	public static String[] getNames() {
		return Arrays.asList(Potions.values()).stream().map(Potions::getName).toArray(String[]::new);
	}
}
