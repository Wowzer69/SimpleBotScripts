package aiohunter.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;

@AllArgsConstructor
@Getter
public enum Salamanders {

	SWAMP_LIZARD(ItemID.SWAMP_LIZARD, ObjectID.YOUNG_TREE_9341, ObjectID.YOUNG_TREE_9257, ObjectID.NET_TRAP_9004),
	ORANGE_SALAMANDER(ItemID.ORANGE_SALAMANDER, ObjectID.YOUNG_TREE_8732, ObjectID.YOUNG_TREE, ObjectID.NET_TRAP_8734),
	RED_SALAMANDER(ItemID.RED_SALAMANDER, ObjectID.YOUNG_TREE_8990, ObjectID.YOUNG_TREE_8989, ObjectID.NET_TRAP_8986),
	BLACK_SALAMANDER(ItemID.BLACK_SALAMANDER, ObjectID.YOUNG_TREE_9000, ObjectID.YOUNG_TREE_8999, ObjectID.NET_TRAP_8996);

	private int inventoryId, unused, active, caught;

}