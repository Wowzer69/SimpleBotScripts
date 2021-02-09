package aiohunter.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.NpcID;

@AllArgsConstructor
@Getter
public enum Butterfly {

	RUBY_HARVEST(NpcID.RUBY_HARVEST, ItemID.RUBY_HARVEST),
	SAPPHIRE_GLACIALIS(NpcID.SAPPHIRE_GLACIALIS, ItemID.SAPPHIRE_GLACIALIS),
	SNOWY_KNIGHT(NpcID.SNOWY_KNIGHT, ItemID.SNOWY_KNIGHT),
	BLACK_WARLOCK(NpcID.BLACK_WARLOCK, ItemID.BLACK_WARLOCK);

	private int npcId, inventoryId;

}