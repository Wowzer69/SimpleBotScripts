package aiohunter.methods;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import aiohunter.AIOHunter;
import aiohunter.data.Constants;
import aiohunter.data.enums.Salamanders;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import simple.api.Utils;
import simple.api.Variables;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

@RequiredArgsConstructor
public class Sala {

	@NonNull
	private ClientContext ctx;
	@NonNull
	private AIOHunter core;

	public void handle() {
		Salamanders salamander = core.getSalamander();

		core.getMethods().addTrap();

		if (Constants.CURRENT_TILES.size() == 0) {
			Variables.STATUS = "No valid trees found";
			return;
		}

		if (ctx.inventory.inventoryFull()) ctx.inventory.dropItems(ctx.inventory.populate().filter(salamander.getInventoryId()));

		SimpleGroundItem items = ctx.groundItems.populate().filter(ItemID.ROPE, ItemID.SMALL_FISHING_NET)
				.filter(loc -> ourTrap(loc.getLocation())).nearest().next();

		SimpleObject UNUSED_TREE = ctx.objects.populate().filter(salamander.getUnused())
				.filter(obj -> Constants.CURRENT_TILES.contains(obj.getLocation())).nearest().next();

		if (UNUSED_TREE != null && core.getMethods().hasItems("Rope", "Small Fishing Net")) {
			Variables.STATUS = "Setting up trap";
			if (UNUSED_TREE != null && UNUSED_TREE.validateInteractable() && UNUSED_TREE.click("Set-Trap"))
				core.getMethods().delay(
						() -> !ctx.objects.populate().filter(UNUSED_TREE.getLocation()).filter(salamander.getActive()).isEmpty());
			return;
		}

		if (items != null) {
			Variables.STATUS = "Taking ground items";
			if (items.validateInteractable() && items.click("Take"))
				ctx.onCondition(() -> ctx.pathing.onTile(items.getLocation()), 2200);
			return;
		}

		SimpleObject CAUGHT_TREE = ctx.objects.populate().filter(salamander.getCaught()).filter(obj -> ourTrap(obj.getLocation()))
				.nearest().next();
		if (CAUGHT_TREE != null) {
			Variables.STATUS = "Checking trap";
			if (CAUGHT_TREE.validateInteractable() && CAUGHT_TREE.click("Check")) core.getMethods().delay(
					() -> ctx.objects.populate().filter(CAUGHT_TREE.getLocation()).filter(salamander.getCaught()).isEmpty());
		} else {
			Variables.STATUS = "Idling";
		}

	}

	private boolean ourTrap(WorldPoint loc) {
		WorldArea area = Utils.makeArea(loc.getX() + 2, loc.getY() + 2, loc.getX() - 2, loc.getY() - 2, 0);
		return Stream.of(area.getWorldPoints()).anyMatch(tile -> Constants.CURRENT_TILES.contains(tile));
	}

	public void paint(Graphics2D g) {
		if (Variables.PAUSED || !Variables.STARTED)
			Constants.POSSIBLE_TILES.forEach(tile -> ctx.paint.drawTileMatrix(g, tile, Color.RED));
		Constants.CURRENT_TILES.forEach(tile -> ctx.paint.drawTileMatrix(g, tile, Color.GREEN));
	}
}
