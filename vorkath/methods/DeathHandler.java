package vorkath.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import api.Locations;
import api.Tasks;
import api.Variables;
import api.simple.KSNPC;
import api.utils.Utils;
import lombok.AllArgsConstructor;
import simple.hooks.queries.SimpleItemQuery;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;
import vorkath.Vorkath;
import vorkath.data.Constants;

@AllArgsConstructor
public class DeathHandler {

	private ClientContext ctx;
	private Vorkath core;

	private final String[] food = { "shark", "pizza", "fish", "angler", "manta" };
	private final Predicate<SimpleWidget> ignoreFood = widget -> !Stream.of(food)
			.anyMatch(val -> widget.getName().toLowerCase().contains(val));

	public void handle() {
		if (teleport() && collect()) ctx.magic.castSpellOnce("Home Teleport");
	}

	private boolean teleport() {
		if (!ctx.pathing.inArea(Locations.VORKATH_START_AREA)) {
			if (!Utils.directTeleport("Vorkath") && Tasks.getTeleporter().open())
				Tasks.getTeleporter().teleportStringPath("Bosses", "Vorkath");
		}
		return ctx.pathing.inArea(Locations.VORKATH_START_AREA);
	}

	private boolean collect() {
		if (!screenOpen()) {
			openRecoverScreen();
			return false;
		}

		if (isLocked()) {
			if (!ctx.dialogue.dialogueOpen()) openDialogue();
			else if (!ctx.dialogue.populate().filterContains("500k").isEmpty()) {
				ctx.log("Don't have enough coins");
				ctx.sendLogout();
			} else {
				ctx.keyboard.sendKeys("1", false);
				ctx.onCondition(() -> !isLocked());
			}
			return false;
		}

		List<SimpleWidget> items = getItems();
		List<SimpleWidget> noFood = items.stream().filter(ignoreFood).collect(Collectors.toList());

		if (items.size() == 0) {
			Constants.RECOLLECT_ITEMS = false;
			Variables.FORCE_BANK = true;
		} else if (noFood.size() > 0) {
			if (ctx.inventory.inventoryFull()) Tasks.getSupplies().eat();
			else noFood.forEach(item -> item.click(0));
		} else {
			SimpleItemQuery<SimpleItem> equippable = ctx.inventory.populate().filterHasAction("Wear");
			if (equippable.size() > 0) equippable.forEach(item -> item.click(0));
			else if (items.size() > 0) {
				if (ctx.inventory.inventoryFull()) Tasks.getSupplies().eat();
				else items.stream().filter(distinctByKey(p -> p.getName())).forEach(item -> item.click(0));
			}
		}

		return items.size() == 0;
	}

	private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	private void openDialogue() {
		SimpleWidget w = ctx.widgets.getWidget(602, 7);
		if (Utils.validWidget(w) && w.click(0)) ctx.onCondition(() -> ctx.dialogue.dialogueOpen());
	}

	private void openRecoverScreen() {
		KSNPC torfin = new KSNPC(ctx.npcs.populate().filter("Torfinn").next());
		if (torfin != null && torfin.validateInteractable() && torfin.click("Collect")) {
			ctx.onCondition(() -> screenOpen(), 3500);
			if (!ctx.dialogue.populate().filterContains("There are no items").isEmpty()) Constants.RECOLLECT_ITEMS = false;
		}
	}

	private boolean screenOpen() {
		return Utils.validWidget(ctx.widgets.getWidget(602, 3));
	}

	private boolean isLocked() {
		SimpleWidget w = ctx.widgets.getWidget(602, 7);
		return Utils.validWidget(w) && w.getSpriteId() != 1226;
	}

	private List<SimpleWidget> getItems() {
		SimpleWidget w = ctx.widgets.getWidget(602, 3);
		if (!Utils.validWidget(w)) return new ArrayList<SimpleWidget>();
		return Arrays.stream(w.getDynamicChildren()).filter(widget -> !widget.isHidden()).collect(Collectors.toList());
	}

}
