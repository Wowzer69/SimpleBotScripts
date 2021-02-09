package herblore.data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import simple.api.Utils;
import simple.robot.api.ClientContext;

@AllArgsConstructor
@Getter
public enum Herbs {

	GUAM_LEAF(3, 3, ItemID.GRIMY_GUAM_LEAF, ItemID.GUAM_LEAF, ItemID.GUAM_POTION_UNF),
	MARRENTILL(5, 5, ItemID.GRIMY_MARRENTILL, ItemID.MARRENTILL, ItemID.MARRENTILL_POTION_UNF),
	TARROMIN(11, 12, ItemID.GRIMY_TARROMIN, ItemID.TARROMIN, ItemID.TARROMIN_POTION_UNF),
	HARRALANDER(20, 22, ItemID.GRIMY_HARRALANDER, ItemID.HARRALANDER, ItemID.HARRALANDER_POTION_UNF),
	RANARR_WEED(25, 30, ItemID.GRIMY_RANARR_WEED, ItemID.RANARR_WEED, ItemID.RANARR_POTION_UNF),
	TOADFLAX(30, 34, ItemID.GRIMY_TOADFLAX, ItemID.TOADFLAX, ItemID.TOADFLAX_POTION_UNF),
	IRIT_LEAF(40, 45, ItemID.GRIMY_IRIT_LEAF, ItemID.IRIT_LEAF, ItemID.IRIT_POTION_UNF),
	AVANTOE(48, 50, ItemID.GRIMY_AVANTOE, ItemID.AVANTOE, ItemID.AVANTOE_POTION_UNF),
	KWUARM(54, 55, ItemID.GRIMY_KWUARM, ItemID.KWUARM, ItemID.KWUARM_POTION_UNF),
	SNAPDRAGON(59, 63, ItemID.GRIMY_SNAPDRAGON, ItemID.SNAPDRAGON, ItemID.SNAPDRAGON_POTION_UNF),
	CADANTINE(65, 66, ItemID.GRIMY_CADANTINE, ItemID.CADANTINE, ItemID.CADANTINE_POTION_UNF),
	LANTADYME(67, 69, ItemID.GRIMY_LANTADYME, ItemID.LANTADYME, ItemID.LANTADYME_POTION_UNF),
	DWARF_WEED(70, 72, ItemID.GRIMY_DWARF_WEED, ItemID.DWARF_WEED, ItemID.DWARF_WEED_POTION_UNF),
	TORSTOL(75, 78, ItemID.GRIMY_TORSTOL, ItemID.TORSTOL, ItemID.TORSTOL_POTION_UNF);

	;

	private int cleanLevel, unfLevel;
	private int grimyId, cleanId, unfId;

	public String getName() {
		return Utils.formatString(this.name().split("_"));
	}

	public String getString() {
		return String.format("{Name: %s, cleanLevel: %s, unfLevel: %s, grimyId: %s, cleanId: %s, unfId: %s}", getName(),
				cleanLevel, unfLevel, grimyId, cleanId, unfId);
	}

	@Override
	public String toString() {
		return getName();
	}

	public static List<Herbs> getCleanable(int level) {
		return Arrays.asList(Herbs.values()).stream().filter(herb -> {

			return level >= herb.getCleanLevel()
					&& ClientContext.instance().bank.populate().filter(herb.getGrimyId()).population(true) > 0;
		}).collect(Collectors.toList());
	}

	public static List<Herbs> getUnfMakeable(int level) {
		ClientContext.instance().bank.populate();
		return Arrays.asList(Herbs.values()).stream().filter(herb -> {

			return level >= herb.getUnfLevel()
					&& ClientContext.instance().bank.populate().filter(herb.getCleanId()).population(true) > 0;
		}).collect(Collectors.toList());
	}

	public static Herbs find(String name) {
		return Arrays.asList(Herbs.values()).stream().filter(val -> val.getName().equals(name)).findFirst().orElse(null);
	}

	public static String[] getNames() {
		return Arrays.asList(Herbs.values()).stream().map(Herbs::getName).toArray(String[]::new);
	}
}
