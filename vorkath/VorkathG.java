package vorkath;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Stream;

import api.Locations;
import api.Tasks;
import api.Variables;
import api.simple.KSNPC;
import api.utils.Timer;
import api.utils.Utils;
import net.runelite.api.ChatMessageType;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

@ScriptManifest(author = "KremeSickle", category = Category.COMBAT, description = "Does vorkath", name = "Glitch Vorkath", servers = {
		"Zaros" }, version = "0.1", discord = "")

public class VorkathG extends Script implements LoopingScript {

	public boolean RECOLLECT_ITEMS = false;
	public Timer LAST_FIRE_BALL = new Timer(2000);

	@Override
	public void paint(Graphics Graphs) {

		Graphics2D g = (Graphics2D) Graphs;

		g.setColor(Color.BLACK);
		g.fillRect(0, 230, 170, 75);
		g.setColor(Color.BLACK);
		g.drawRect(0, 230, 170, 75);
		g.setColor(Color.white);

		g.drawString("Private Vorkath v0.3", 7, 245);
		g.drawString("Uptime: " + Variables.START_TIME.toElapsedString(), 7, 257);
		g.drawString("Status: " + Variables.STATUS, 7, 269);

		g.drawString("Vorkath kills: " + Variables.COUNT + " ("
				+ ctx.paint.valuePerHour((int) Variables.COUNT, Variables.START_TIME.getStart()) + ")", 7, 281);

	}

	@Override
	public void onChatMessage(ChatMessage e) {
		if (e.getType() == ChatMessageType.GAMEMESSAGE) {
			if (e.getMessage().contains("you are dead")) {
				Variables.STOP = true;
			}
			if (e.getMessage().contains("kill count")) Variables.COUNT++;
			if (e.getMessage().contains("not be found")) ctx.stopScript();
		}
	}

	@Override
	public void onExecute() {
		try {
			Variables.reset();
			Tasks.init(ctx);
			Utils.setZoom(1);
			Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MAGIC);
			Tasks.getSkill().addPrayer(Prayers.EAGLE_EYE);
			Variables.FORCE_BANK = ctx.pathing.inArea(Locations.EDGEVILLE_AREA);
			Variables.STARTED = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	WorldArea area = new WorldArea(new WorldPoint(3098, 3487, 0), new WorldPoint(3090, 3499, 0));

	@Override
	public void onProcess() {

		if (!Variables.STARTED) return;
		if (Variables.STOP) {
			Tasks.getAntiban().panic();
			return;
		}

		if ((!ctx.pathing.inArea(Locations.EDGEVILLE_AREA) && Tasks.getAntiban().staffNearby())
				|| (Tasks.getAntiban().staffUnder() && !ctx.pathing.inArea(area))) {
			ctx.log("Staff found at " + ctx.players.getLocal().getLocation());
			Variables.STOP = true;
			return;
		}

		if (ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {

		} else if (ctx.pathing.inArea(Locations.VORKATH_START_AREA)) {

		} else if (ctx.getClient().isInInstancedRegion()) {
			if (loot()) return;
			/*
			 * if (getAlch() != null) {
			 * ctx.magic.castSpellOnItem("High Level Alchemy",
			 * getAlch().getId()); ctx.onCondition(() ->
			 * ctx.players.getLocal().getAnimation() != -1); ctx.onCondition(()
			 * -> ctx.players.getLocal().getAnimation() == -1); return; }
			 */
			if (!setup()) return;
			if (containsAcid(ctx.players.getLocal().getLocation())) {
				ctx.pathing.step(getSortedPoint());
			}
			SimpleNpc zombified = ctx.npcs.populate().filter(NpcID.ZOMBIFIED_SPAWN_8063).next();
			if (zombified != null) {
				Tasks.getCombat().attack(zombified);
			} else if (ctx.players.getLocal().getInteracting() == null) Tasks.getCombat().attack("Vorkath");
		}

	}

	public boolean containsAcid(WorldPoint w) {
		SimpleEntityQuery<SimpleObject> acids = ctx.objects.populate().filter(ObjectID.ACID_POOL_32000).filter(w);
		return acids.size() > 0;
	}

	private String[] v = { "ragon battleaxe", "ragon longsword", "ragon plateleg", "ragon plateskirt", "visage", "decay",
			"necklace" };

	private String[] bad = { "diamond", "ruby", "watermelon", "seed", "chaos", "dragonhide", "crushed", "bolt tip",
			"adamantite ore", "wrath", "magic log" };

	public SimpleItem getAlch() {
		return Tasks.getInventory().getItem("ragon battleaxe", "ragon longsword", "ragon plateleg", "ragon plateskirt");
	}

	public boolean valid(SimpleGroundItem item) {
		if (Stream.of(bad).anyMatch(val2 -> item.getName().toLowerCase().contains(val2))) return false;
		if (item.isStackable()) return true;
		return Stream.of(v).anyMatch(val -> item.getName().toLowerCase().contains(val));
	}

	public boolean loot() {
		SimpleEntityQuery<SimpleGroundItem> possible = ctx.groundItems.populate();
		if (possible.population() == 0) return false;

		SimpleGroundItem loot = possible.populate().filter(item -> valid(item)).next();

		if (loot == null) return false;

		if (ctx.inventory.canPickupItem(loot)) {
			Variables.STATUS = "Picking up loot " + loot.getName();
			int population = ctx.inventory.populate().filter(loot.getId()).population(true);

			if (Tasks.getMenuAction().get(loot, "Take").invoke()) {
				ctx.sleep(200);
				ctx.onCondition(() -> population != ctx.inventory.populate().filter(loot.getId()).population(true));
				return true;
			}
		} else if (ctx.inventory.inventoryFull()) {
			Variables.STOP = true;
			ctx.sendLogout();
			return true;
		}
		return false;

	}

	private boolean setup() {
		SimpleNpc vorkath = ctx.npcs.populate().filter(NpcID.VORKATH_8059).next();
		if (vorkath == null) return true;

		Variables.STATUS = "Waking up vorkath";
		if (new KSNPC(vorkath).click("Poke")) {
			WorldPoint initial = ctx.players.getLocal().getLocation();
			ctx.sleepCondition(() -> vorkath.distanceTo(ctx.players.getLocal()) <= 5, 5000);
			ctx.sleep(1000);
			WorldPoint newWP = new WorldPoint(initial.getX(), initial.getY() - 5, initial.getPlane());
			ctx.pathing.step(newWP);
		}
		return false;
	}

	public WorldArea getArea() {
		WorldArea w = null;
		if (ctx.getClient().isInInstancedRegion()) {
			SimpleObject rock = ctx.objects.populate().filter(ObjectID.ICE_CHUNKS_31990).next();
			if (rock != null) {
				WorldPoint southWest = new WorldPoint(rock.getLocation().getX() - 10, rock.getLocation().getY() + 1, 0);
				WorldPoint northEast = new WorldPoint(rock.getLocation().getX() + 10, rock.getLocation().getY() + 24, 0);
				w = new WorldArea(southWest, northEast);
			}
		}
		return w;
	}

	public ArrayList<WorldPoint> getStartingPointAcid() {
		ArrayList<WorldPoint> safe = new ArrayList<WorldPoint>();
		WorldArea a = getArea();

		for (WorldPoint w : a.getWorldPoints()) {
			if (w.distanceTo(ctx.players.getLocal().getLocation()) >= 8) {
				continue;
			}

			if (containsAcid(w)) {
				continue;
			}

			WorldPoint east1 = new WorldPoint(w.getX() + 1, w.getY(), 0);
			if (containsAcid(east1)) {
				continue;
			}

			WorldPoint east2 = new WorldPoint(w.getX() + 2, w.getY(), 0);
			if (containsAcid(east2)) {
				continue;
			}

			WorldPoint east3 = new WorldPoint(w.getX() + 3, w.getY(), 0);
			if (containsAcid(east3)) {
				continue;
			}

			WorldPoint east4 = new WorldPoint(w.getX() + 4, w.getY(), 0);
			if (containsAcid(east4)) {
				continue;
			}

			safe.add(w);

		}
		return safe;
	}

	public WorldPoint getSortedPoint() {
		ArrayList<WorldPoint> wl = this.getStartingPointAcid();
		if (wl != null && wl.size() > 0) {
			Collections.sort(wl, new Comparator<WorldPoint>() {
				@Override
				public int compare(WorldPoint z1, WorldPoint z2) {
					if (z1.distanceTo(ctx.players.getLocal().getLocation()) > z2.distanceTo(ctx.players.getLocal().getLocation()))
						return 1;
					if (z1.distanceTo(ctx.players.getLocal().getLocation()) < z2.distanceTo(ctx.players.getLocal().getLocation()))
						return -1;
					return 0;
				}
			});
			return wl.get(0);
		}
		return null;
	}

	@Override
	public void onTerminate() {
		Tasks.getSkill().disablePrayers();
		Variables.reset();
	}

	@Override
	public int loopDuration() {
		return 150;
	}
}
