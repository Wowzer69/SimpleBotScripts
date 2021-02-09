package warriorguild.data;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import net.runelite.api.coords.WorldPoint;
import simple.api.Variables;
import simple.api.panel.Config;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import warriorguild.WarriorGuild;

public class Methods {

	private ClientContext ctx;
	private WarriorGuild core;

	public Methods(ClientContext ctx, WarriorGuild core) {
		this.ctx = ctx;
		this.core = core;
	}

	Predicate<SimpleNpc> GET_AGGRESSOR = npc -> !npc.isDead()
			&& (npc.getInteracting() == null || npc.getInteracting().equals(ctx.players.getLocal().getPlayer()));

	public void handleTokens() {
		if (!ctx.pathing.inArea(Constants.TOKEN_AREA)) {
			travel(false);
			return;
		}

		SimpleNpc enemy = ctx.npcs.populate().filter(Constants.RUNE_ANIMATED_ARMOR).filter(GET_AGGRESSOR).nearest().next();
		if (enemy != null) {
			if (Config.getB("usePrayer")) enablePrayer();
			Variables.STATUS = "Killing animated armor";
			if (ctx.players.getLocal().getInteracting() == null && enemy.click("Attack"))
				ctx.onCondition(() -> ctx.players.getLocal().getInteracting() != null);
		} else if (hasArmorSet()) {
			Variables.STATUS = "Spawning animated armor";
			SimpleObject statue = ctx.objects.populate().filter("Magical animator").nearest().next();
			if (statue != null && statue.validateInteractable() && statue.click(0))
				ctx.onCondition(() -> ctx.players.getLocal().getInteracting() != null, 100);
		} else {
			Variables.STATUS = "You have no full armor sets";
		}
	}

	public void handleDefenders() {

		if (Constants.CURRENT_TASK == 0) return;

		if (Config.getB("getToken") && !hasAttackCape()) {
			if (getTokens() > 300) {
				if (ctx.pathing.inArea(Constants.CYCLOPS_ENTRY)) Constants.CURRENT_TASK = 0;
				else if (ctx.pathing.inArea(Constants.CYCLOPS2_ENTRY)) Variables.STOP = true;
			}
		}
		if (Constants.CURRENT_TASK == 2
				&& (!ctx.pathing.inArea(Constants.CYCLOPS2_AREA) || ctx.pathing.inArea(Constants.CYCLOPS2_ENTRY))) {
			travelToDragonCyclops();
		} else if (Constants.CURRENT_TASK == 1
				&& (!ctx.pathing.inArea(Constants.CYCLOPS_AREA) || ctx.pathing.inArea(Constants.CYCLOPS_ENTRY))) {
					travel(true);
				} else {
					if (Config.getB("usePrayer")) enablePrayer();
					SimpleNpc enemy = ctx.npcs.populate().filter("Cyclops").filter(GET_AGGRESSOR).nearest().next();
					Variables.STATUS = "Killing cyclops";
					if (enemy != null && ctx.players.getLocal().getInteracting() == null) {
						Variables.STATUS = "Attacking cyclops";
						if (enemy.click("Attack")) ctx.onCondition(() -> ctx.players.getLocal().getInteracting() != null);
					}

				}
	}

	public boolean insideWarriorGuild() {
		WorldPoint loc = ctx.players.getLocal().getLocation();
		return (loc.getX() >= 2832 && loc.getX() <= 2876) && (loc.getY() >= 3534 && loc.getY() <= 3555)
				|| ctx.pathing.inArea(Constants.CYCLOPS2_AREA);
	}

	public void thirdFloor(boolean up) {
		if (!up && !ctx.pathing.inArea(Constants.CYCLOPS_ENTRY)) {
			if (ctx.pathing.distanceTo(Constants.LEVEL_3_DOOR) > 10) {
				Variables.STATUS = "Walking closer to door";
				ctx.pathing.walkPath(ctx.pathing.createLocalPath(Constants.LEVEL_3_DOOR));
				ctx.onCondition(() -> ctx.pathing.distanceTo(Constants.LEVEL_3_DOOR) > 5);
				return;
			}
			Variables.STATUS = "Going through cyclops door";
			SimpleObject door = ctx.objects.populate().filter(Constants.LEVEL_3_DOOR).nearest().next();
			if (door != null && door.validateInteractable() && door.click("Open"))
				ctx.onCondition(() -> ctx.pathing.inArea(Constants.CYCLOPS_ENTRY));
			return;
		}

		if (ctx.pathing.inArea(Constants.CYCLOPS_ENTRY)) {
			if (up) {
				Variables.STATUS = "Entering cyclops door";
				SimpleObject door = ctx.objects.populate().filter(Constants.LEVEL_3_DOOR).nearest().next();
				if (door != null && door.validateInteractable() && door.click("Open"))
					ctx.onCondition(() -> !ctx.pathing.inArea(Constants.CYCLOPS_ENTRY));
			} else {
				Variables.STATUS = "Going down first stairs";
				SimpleObject stairs = ctx.objects.populate().filter(24303).nearest().next();
				if (stairs != null && stairs.validateInteractable() && stairs.click("Climb-down"))
					ctx.onCondition(() -> !ctx.pathing.inArea(Constants.CYCLOPS_ENTRY));
			}
		}
	}

	public void secondFloor(boolean up) {
		String action = up ? "up" : "down";
		Variables.STATUS = "Going " + action + " second stairs";
		SimpleObject stairs = ctx.objects.populate().filter(16672).nearest().next();
		if (stairs != null && stairs.validateInteractable() && stairs.click("Climb-" + action))
			ctx.onCondition(() -> ctx.players.getLocal().getLocation().getPlane() != 1);
	}

	private final WorldPoint STAIRS_1 = new WorldPoint(2839, 3537, 0);

	public void firstFloor(boolean up) {
		if (up) {
			if (ctx.pathing.inArea(Constants.TOKEN_AREA)) {
				SimpleObject tokenDoor = ctx.objects.populate().filter(Constants.TOKEN_DOOR).nearest().next();
				if (tokenDoor != null && tokenDoor.validateInteractable() && tokenDoor.click("Open"))
					ctx.onCondition(() -> !ctx.pathing.inArea(Constants.TOKEN_AREA));
			}
			Variables.STATUS = "Walking up first stairs";
			SimpleObject stairs = ctx.objects.populate().filter(16671).nearest().next();
			if (stairs == null || ctx.pathing.distanceTo(stairs.getLocation()) > 10) {
				ctx.pathing.step(STAIRS_1);
				ctx.onCondition(() -> !ctx.pathing.inMotion());
			} else if (stairs.validateInteractable() && stairs.click("Climb-up"))
				ctx.onCondition(() -> ctx.players.getLocal().getLocation().getPlane() == 1);
		} else {
			if (ctx.pathing.inArea(Constants.TOKEN_AREA)) {
				SimpleObject tokenDoor = ctx.objects.populate().filter(Constants.TOKEN_DOOR).nearest().next();
				if (tokenDoor != null && tokenDoor.validateInteractable() && tokenDoor.click("Open"))
					ctx.onCondition(() -> !ctx.pathing.inArea(Constants.TOKEN_AREA));
				return;
			}
			Variables.STATUS = "Walking to token door";
			SimpleObject tokenDoor = ctx.objects.populate().filter(Constants.TOKEN_DOOR).nearest().next();
			if (tokenDoor != null && tokenDoor.validateInteractable() && tokenDoor.click("Open"))
				ctx.onCondition(() -> ctx.pathing.inArea(Constants.TOKEN_AREA));
		}
	}

	public void travel(boolean up) {
		ctx.prayers.prayer(Prayers.PROTECT_FROM_MELEE, false);
		int plane = ctx.players.getLocal().getLocation().getPlane();
		if (plane == 2) thirdFloor(up);
		else if (plane == 1) secondFloor(up);
		else firstFloor(up);
	}

	private void travelToDragonCyclops() {
		if (ctx.players.getLocal().getLocation().getPlane() != 0 || ctx.pathing.inArea(Constants.TOKEN_AREA)) {
			travel(false);
			return;
		}

		if (ctx.pathing.inArea(Constants.CYCLOPS2_ENTRY)) {
			Variables.STATUS = "Entering cyclops door";
			SimpleObject door2 = ctx.objects.populate().filter(10043).filterHasAction("Open").nearest().next();
			if (door2 != null && door2.validateInteractable() && door2.click("Open"))
				ctx.onCondition(() -> !ctx.pathing.inArea(Constants.CYCLOPS2_ENTRY));
			return;
		}

		SimpleObject firstDoor = ctx.objects.populate().filter(new WorldPoint(2837, 3549, 0)).filterHasAction("Open").nearest()
				.next();
		if (ctx.players.getLocal().getLocation().getX() > 2837 && firstDoor != null) {
			Variables.STATUS = "Walking to first door";
			if (firstDoor.validateInteractable() && firstDoor.click("Open"))
				ctx.onCondition(() -> ctx.players.getLocal().getLocation().getX() == 2838);
			return;
		}

		Variables.STATUS = "Walking to ladder";
		SimpleObject secondDoor = ctx.objects.populate().filter(10042).nearest().next();
		if (secondDoor != null && secondDoor.validateInteractable() && secondDoor.click("Climb-down"))
			ctx.onCondition(() -> ctx.pathing.inArea(Constants.CYCLOPS2_ENTRY));
	}

	public boolean hasArmorSet() {
		boolean helm = ctx.inventory.populate().filter(Pattern.compile("Rune full helm")).population() > 0;
		boolean plate = ctx.inventory.populate().filter(Pattern.compile("Rune platebody")).population() > 0;
		boolean legs = ctx.inventory.populate().filter(Pattern.compile("Rune platelegs")).population() > 0;
		return helm && plate && legs;
	}

	public boolean hasAttackCape() {
		return ctx.equipment.populate().filter(Pattern.compile("Attack cape(.*)")).population() > 0;
	}

	public int getTokens() {
		SimpleItem tokens = ctx.inventory.populate().filter(8851).next();
		return tokens != null ? tokens.getQuantity() : 0;
	}

	public boolean has(String name) {
		return ctx.inventory.populate().filter(name).population() > 0;
	}

	public boolean loot(String... strings) {
		SimpleEntityQuery<SimpleGroundItem> possible = ctx.groundItems.populate();
		if (possible.population() == 0) return false;

		SimpleGroundItem loot = possible.filter(strings).nearest().next();
		if (loot == null) return false;

		SimpleItem food = ctx.inventory.populate().filterHasAction("Eat").next();

		if (ctx.inventory.canPickupItem(loot)) {
			Variables.STATUS = "Picking up loot";
			int population = ctx.inventory.populate().filter(loot.getId()).population(true);
			if (loot.click("Take"))
				ctx.onCondition(() -> population != ctx.inventory.populate().filter(loot.getId()).population(true), 75);
			return true;
		} else if (ctx.inventory.inventoryFull() && food != null) {
			if (food.click(0)) ctx.sleep(150, 200);
			return true;
		}
		return false;
	}

	public void enablePrayer() {
		if (ctx.skills.level(Skills.PRAYER) == 0 && ctx.skills.realLevel(Skills.PRAYER) >= 43) return;
		ctx.prayers.prayer(Prayers.PROTECT_FROM_MELEE, true);
	}

}
