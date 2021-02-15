package blastfurnace.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import simple.robot.api.ClientContext;

@RequiredArgsConstructor
@Getter
public enum Ore {

	BRONZE_BAR("Bronze bar", new int[] { ItemID.TIN_ORE, ItemID.COPPER_ORE }, Varbits.BLAST_FURNACE_TIN_ORE,
			Varbits.BLAST_FURNACE_COPPER_ORE, Varbits.BLAST_FURNACE_BRONZE_BAR),
	IRON_BAR("Iron bar", new int[] { ItemID.IRON_ORE, ItemID.IRON_ORE }, Varbits.BLAST_FURNACE_IRON_ORE,
			Varbits.BLAST_FURNACE_IRON_ORE, Varbits.BLAST_FURNACE_IRON_BAR),
	STEEL_BAR("Steel bar", new int[] { ItemID.IRON_ORE, ItemID.COAL }, Varbits.BLAST_FURNACE_IRON_ORE, Varbits.BLAST_FURNACE_COAL,
			Varbits.BLAST_FURNACE_IRON_BAR),
	GOLD_BAR("Gold bar", new int[] { ItemID.GOLD_ORE, ItemID.GOLD_ORE }, Varbits.BLAST_FURNACE_GOLD_ORE,
			Varbits.BLAST_FURNACE_GOLD_ORE, Varbits.BLAST_FURNACE_GOLD_BAR),
	MITHRIL_BAR("Mithril bar", new int[] { ItemID.MITHRIL_ORE, ItemID.COAL }, Varbits.BLAST_FURNACE_MITHRIL_ORE,
			Varbits.BLAST_FURNACE_COAL, Varbits.BLAST_FURNACE_MITHRIL_BAR),
	ADAMANTITE_BAR("Adamantite bar", new int[] { ItemID.ADAMANTITE_ORE, ItemID.COAL }, Varbits.BLAST_FURNACE_ADAMANTITE_ORE,
			Varbits.BLAST_FURNACE_COAL, Varbits.BLAST_FURNACE_ADAMANTITE_BAR),
	RUNITE_BAR("Runite bar", new int[] { ItemID.RUNITE_ORE, ItemID.COAL }, Varbits.BLAST_FURNACE_RUNITE_ORE,
			Varbits.BLAST_FURNACE_COAL, Varbits.BLAST_FURNACE_RUNITE_BAR),

	;
	
	private final ClientContext ctx = ClientContext.instance();

	private final String barName;
	private final int[] oreId;
	private final Varbits oreVarb, oreSVarb, barVarb;

	public int getPrimaryOre() {
		return oreId[0];
	}

	public int getSecondaryOre() {
		return oreId[1];
	}

	public int getOres() {
		return ctx.varpbits.varpbit(oreVarb);
	}

	public int getBars() {
		return ctx.varpbits.varpbit(barVarb);
	}

	public int getSecondary() {
		return ctx.varpbits.varpbit(oreSVarb);
	}

}
