package aiohunter.data.enums;

import java.util.Arrays;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.ObjectID;

@AllArgsConstructor
@Getter
public enum Trapping {
	BIRD_SNARE("Lay", "Check", ItemID.BIRD_SNARE, ObjectID.BIRD_SNARE_9345, ObjectID.BOX_TRAP_9385,
			new int[] { ObjectID.BIRD_SNARE_9373, ObjectID.BIRD_SNARE_9375, ObjectID.BIRD_SNARE_9377, ObjectID.BIRD_SNARE_9379,
					ObjectID.BIRD_SNARE_9348 },
			new int[] { ItemID.BONES, ItemID.RAW_BIRD_MEAT }),
	BOX_TRAP("Lay", "Reset", ItemID.BOX_TRAP, ObjectID.BOX_TRAP_9380, ObjectID.SHAKING_BOX,
			new int[] { ObjectID.BOX_TRAP_9385, ObjectID.SHAKING_BOX_9382, ObjectID.SHAKING_BOX_9383 }, new int[] {});

	private String setAction, caughtAction;

	private int inventoryId, activeTrap, failedTrap;

	private int[] caughtTrap, junkId;

	public static int[] getActiveTraps() {
		return Stream.of(Trapping.values()).mapToInt(Trapping::getActiveTrap).toArray();
	}

	public static int[] getFailedTraps() {
		return Stream.of(Trapping.values()).mapToInt(Trapping::getFailedTrap).toArray();
	}

	public static int[] getCaughtTraps() {
		return Stream.of(Trapping.values()).flatMapToInt(i -> Arrays.stream(i.getCaughtTrap())).toArray();
	}
}
