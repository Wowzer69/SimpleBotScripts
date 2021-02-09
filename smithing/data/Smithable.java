package smithing.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Smithable {
	DAGGER("Dagger", 1),
	AXE("Axe", 1),
	MEDIUM_HELMET("Medium Helm", 1),
	CROSSBOW_BOLTS("Crossbow Bolts", 1),
	SWORD("Sword", 1),
	DART_TIPS("Dart tips", 1),
	NAILS("Nails", 1),
	ARROWTIPS("Arrowtips", 1),
	JAVELIN_HEADS("Javelin Heads", 1),
	THROWING_KNIVES("Throwing Knives", 1),
	CROSSBOW_LIMBS("Limbs", 1),
	STUDS("Studs", 1),
	BULLSEYE_LAMP("Bullseye lamp", 1),
	OIL_LAMP("Oil lamp", 1),
	GRAPLLE_TIP("Grapple tip", 1),
	IRON_SPIT("Iron spit", 1),
	SCIMITAR("Scimitar", 2),
	LONGSWORD("Longsword", 2),
	FULL_HELMET("Full Helm", 2),
	SQUARE_SHIELD("Square Shield", 2),
	CLAWS("Claws", 2),
	WARHAMMER("Warhammer", 3),
	BATTLEAXE("Battle axe", 3),
	CHAINBODY("Chain body", 3),
	KITESHIELD("Kite shield", 3),
	TWO_HANDED_SWORD("2-hand Sword", 3),
	PLATELEGS("Plate legs", 3),
	PLATESKIRT("Plate skirt", 3),
	PLATEBODY("Plate body", 5);

	private final String friendlyName;
	private final int barCount;

	@Override
	public String toString() {
		return friendlyName;
	}
}