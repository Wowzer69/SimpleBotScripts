package aiohunter.methods;

import java.awt.Color;
import java.awt.Graphics2D;

import aiohunter.AIOHunter;
import aiohunter.data.Constants;
import aiohunter.data.enums.Trapping;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.util.Text;
import simple.api.Variables;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.Random;

@RequiredArgsConstructor
public class Traps {

	@NonNull
	private ClientContext ctx;
	@NonNull
	private AIOHunter core;

	public void handle() {
		Trapping trap = core.getTrap();

		core.getMethods().addTrap();

		if (Constants.CURRENT_TILES.size() == 0) {
			Variables.STATUS = "No more valid tiles";
			return;
		}

		if (ctx.inventory.getFreeSlots() < 3) ctx.inventory.dropItems(ctx.inventory.populate().filter(trap.getJunkId()));

		WorldPoint spot = core.getMethods().getNextTile();

		if (spot != null) {

			int[] objects = core.getMethods().merge(Trapping.getFailedTraps(), Trapping.getCaughtTraps());

			SimpleObject obj = ctx.objects.populate().filter(spot).filter(objects).next();

			SimpleGroundItem groundItem = ctx.groundItems.populate().filter(spot).filter(trap.getInventoryId()).nearest().next();

			if (groundItem != null && groundItem.validateInteractable()) {
				Variables.STATUS = "Laying " + Text.titleCase(trap);
				if (groundItem.click("Lay")) core.getMethods()
						.delay(() -> !ctx.objects.populate().filter(spot).filter(trap.getActiveTrap()).isEmpty(), 2000);
			} else if (obj == null && !ctx.inventory.populate().filter(trap.getInventoryId()).isEmpty()) {
				if (!ctx.pathing.onTile(spot)) {
					Variables.STATUS = "Walking to next tile";
					ctx.pathing.step(spot);
					ctx.onCondition(() -> ctx.pathing.onTile(spot), 1500);
					return;
				}

				if (!ctx.pathing.onTile(spot)) return;
				SimpleItem item = ctx.inventory.filter(trap.getInventoryId()).next();

				Variables.STATUS = "Setting " + Text.titleCase(trap);
				if (item != null && item.click(0)) core.getMethods()
						.delay(() -> !ctx.objects.populate().filter(spot).filter(trap.getActiveTrap()).isEmpty(), 1500);
			} else if (obj != null && Random.containsId(obj.getId(), trap.getCaughtTrap())) {
				Variables.STATUS = "Checking " + Text.titleCase(trap);
				if (obj.validateInteractable() && obj.click(trap.getCaughtAction())) core.getMethods()
						.delay(() -> !ctx.objects.populate().filter(spot).filter(trap.getActiveTrap()).isEmpty(), 1500);
			} else if (obj != null && Random.containsId(obj.getId(), trap.getFailedTrap())) {
				Variables.STATUS = "Resetting " + Text.titleCase(trap);
				if (obj.validateInteractable() && obj.click(trap.getSetAction())) core.getMethods()
						.delay(() -> !ctx.objects.populate().filter(spot).filter(trap.getActiveTrap()).isEmpty(), 2500);
			}
		} else {
			Variables.STATUS = "Idling";
		}
	}

	public void paint(Graphics2D g) {
		if (Variables.PAUSED || !Variables.STARTED)
			Constants.POSSIBLE_TILES.forEach(tile -> ctx.paint.drawTileMatrix(g, tile, Color.RED));
		Constants.CURRENT_TILES.forEach(tile -> ctx.paint.drawTileMatrix(g, tile, Color.GREEN));
	}
}
