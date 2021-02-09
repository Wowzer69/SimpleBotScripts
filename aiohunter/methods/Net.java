package aiohunter.methods;

import java.awt.Color;
import java.awt.Graphics2D;

import aiohunter.AIOHunter;
import aiohunter.data.enums.Butterfly;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import simple.api.Variables;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.queries.SimpleItemQuery;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.robot.api.ClientContext;

@RequiredArgsConstructor
public class Net {

	@NonNull
	private ClientContext ctx;
	@NonNull
	private AIOHunter core;

	public void handle() {
		Butterfly bf = core.getButterfly();

		int empty = ctx.inventory.populate().filter(ItemID.BUTTERFLY_JAR).population();

		if (empty == 0) {
			Variables.STATUS = "Releasing butterflies";
			SimpleItemQuery<SimpleItem> query = ctx.inventory.populate().filter(bf.getInventoryId());

			if (query.isEmpty()) {
				Variables.STATUS = "No butterfly jars left";
				ctx.log("No butterfly jars left");
				ctx.stopScript();
				return;
			}

			query.forEach(item -> item.click("Release"));
		}

		SimpleNpc butterfly = ctx.npcs.populate().filter(bf.getNpcId()).nearest().next();
		Variables.STATUS = "Catching butterflies";
		if (butterfly != null && butterfly.validateInteractable() && butterfly.click("Catch"))
			core.getMethods().delay(() -> true);
	}

	public void paint(Graphics2D g) {
		SimpleEntityQuery<SimpleNpc> npcs = ctx.npcs.populate().filter(core.getButterfly().getNpcId()).filterWithin(20);
		npcs.forEach(npc -> core.draw(g, npc.getActor(), "", Color.GREEN));
	}
}
