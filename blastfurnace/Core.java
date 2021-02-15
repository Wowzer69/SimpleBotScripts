package blastfurnace;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import api.Locations;
import api.MenuActions;
import api.Tasks;
import api.Variables;
import api.utils.Timer;
import api.utils.Utils;
import blastfurnace.data.Ore;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import net.runelite.api.MenuAction;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.SMITHING, description = "Does blast furnace", name = "Blast furnace", servers = {
		"Zaros" }, version = "0.1", discord = "")

public class Core extends Script implements LoopingScript {

	// upstairs [ region-11679 stairs-9084 ]
	// downstairs [ region-7757 stairs-9138]
	// Bar dispense
	// Converyor - 9100

	boolean goldgaunts, bucket, teleport, stamina, iceglove, coffer, coalbag;

	Ore ore = Ore.RUNITE_BAR;

	@Override
	public void onExecute() {
		Tasks.init(ctx);
		Variables.STOP = false;
		goldgaunts = false;
		bucket = false;
		teleport = false;
		stamina = true;
		iceglove = true;
		coffer = false;
		coalbag = true;

		if (coalbag && ore.getSecondaryOre() != ItemID.COAL) coalbag = false;
		if (goldgaunts && ore != Ore.GOLD_BAR) goldgaunts = false;

		full = ore.getBars() > 0;
	}

	boolean full;
	boolean COALBAG_FULL = false;

	@Override
	public void onProcess() {

		if (Variables.STOP) return;
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA) && Tasks.getAntiban().staffNearby()) {
			System.out.println("Staff found at " + ctx.players.getLocal().getLocation());
			Variables.STOP = true;
			return;
		}
		if (ctx.players.getLocal().getLocation().getRegionID() == 11679) {
			// ctx.viewport.angle(58);
			SimpleObject stairs = ctx.objects.populate().filter(9084).nearest().next();
			if (Tasks.getMenuAction().get(stairs, "Climb-down").setWidget(52).invoke())
				// if (stairs != null /* && stairs.validateInteractable() */ &&
				// MenuActions.invoke(m))
				ctx.onCondition(() -> ctx.players.getLocal().getLocation().getRegionID() == 7757);
		} else if (ctx.players.getLocal().getLocation().getRegionID() == 7757) {
			if (!ctx.pathing.running() && ctx.pathing.energyLevel() > 10) ctx.pathing.running(true);

			if (getCoffer() <= 5000) {
				fillCoffer();
				return;
			}

			if (shouldStamina()) {
				drinkStamina();
				return;
			}

			if (!full) {
				full = isFull();
				if (full) return;

				while (!handleCoalBag(false))
					return;

				if (ctx.inventory.populate().filter(ore.getSecondaryOre()).population() < 27) {
					Variables.STATUS = "Retrieving secondary ore";
					if (!withdrawBucket()) return;
					withdraw(ore.getSecondaryOre(), 27, ItemID.BUCKET);
				} else {
					Variables.STATUS = "Depositing secondary ore";
					deposit(true);
				}
			} else {
				if (ore.getSecondary() < 10 && ore.getBars() == 0) full = false;

				if (pendingBars() == 2 || pendingBars() == 3) {
					Variables.STATUS = "Retrieving bars";
					retrieve();
				} else if (ctx.inventory.populate().filter(ore.getPrimaryOre()).isEmpty()) {
					Variables.STATUS = "Retrieving primary ore";
					if (!withdrawBucket()) return;

					withdraw(ore.getPrimaryOre(), 27, ItemID.BUCKET, ItemID.BUCKET_OF_WATER);
				} else {
					Variables.STATUS = "Depositing primary ore";
					if (deposit(false)) cool();
				}
			}
		}
	}

	public boolean deposit(boolean coal) {
		SimpleObject convey = ctx.objects.populate().filter(9100).nearest().next();

		if (convey == null) {
			Variables.STATUS = "Unable to find Conveyor";
			return false;
		}

		if (ctx.pathing.distanceTo(convey.getLocation()) > 8) {
			ctx.pathing.step(new WorldPoint(1942, 4967, 0));
			ctx.onCondition(() -> ctx.pathing.distanceTo(convey.getLocation()) < 5);
			return false;
		}
		if (Tasks.getMenuAction().get(convey, "Put-ore-on").invoke()) {
			/*
			 * if (/*convey.validateInteractable() &&
			 * convey.click("Put-ore-on")) {
			 */
			ctx.onCondition(() -> ctx.inventory.populate().population() < 20, 2500);

			while (coal && !handleCoalBag(true))
				return false;

			return true;
		}
		return false;
	}

	public boolean withdraw(int id, int amt, int... ignore) {
		if (openBank(id == ItemID.COAL)) {
			if (!ctx.inventory.populate().isEmpty()) {
				ctx.bank.depositAllExcept(id, ItemID.GOLDSMITH_GAUNTLETS, ItemID.BUCKET_OF_WATER, ItemID.ICE_GLOVES,
						ItemID.COAL_BAG_12019);
			}
			SimpleItem item = ctx.bank.populate().filter(id).next();
			if (item != null && !Tasks.getBanking().withdrawItem(item.getName(), amt)) return false;
			ctx.sleepCondition(() -> !ctx.inventory.populate().isEmpty(), 250);
			return true;
		}
		return false;
	}

	public boolean cool() {
		if (pendingBars() == 3) return true;
		SimpleObject dispenser = ctx.objects.populate().filter("Bar dispenser").filter(new WorldPoint(1940, 4963, 0)).next();
		if (bucket && withdrawBucket()) {
			if (dispenser != null && dispenser.validateInteractable()) {
				SimpleItem bucket = ctx.inventory.populate().filter(ItemID.BUCKET_OF_WATER).next();
				if (bucket == null) return true;

				if (ctx.inventory.itemSelectionState() == 0) bucket.click(0);
				if (dispenser.click(0)) {
					ctx.onCondition(() -> pendingBars() == 3);
					return pendingBars() == 3;
				}
			}
		} else if (iceglove) {
			ctx.sleepCondition(() -> pendingBars() >= 2, 5000);
			return equip(ItemID.ICE_GLOVES);
		} else if (teleport) {
			if (!Utils.directTeleport("Blast Furnace") && Tasks.getTeleporter().open())
				Tasks.getTeleporter().teleportStringPath("Minigames", "Blast Furnace");
			return true;
		}
		return false;
	}

	public boolean openBank(boolean preset) {
		if (!preset && ctx.bank.bankOpen()) return true;
		if (preset) ctx.bank.closeBank();

		if (preset) return Tasks.getBanking().usePreset();
		else Tasks.getBanking().open();

		ctx.sleepCondition(() -> preset ? ctx.inventory.inventoryFull() : ctx.bank.bankOpen(), 2000);
		/*
		 * SimpleObject bank =
		 * ctx.objects.populate().filter(26707).nearest().next(); if (bank !=
		 * null && bank.validateInteractable()) { if
		 * (ctx.inventory.itemSelectionState() == 1)
		 * ctx.inventory.populate().next().click(0); bank.click(preset ?
		 * "Last-preset" : "Use"); ctx.sleepCondition(() -> preset ?
		 * ctx.inventory.inventoryFull() : ctx.bank.bankOpen(), 2000); }
		 */
		return ctx.bank.bankOpen();
	}

	WorldPoint banktile = new WorldPoint(1948, 4957, 0);

	public boolean shouldStamina() {
		if (ctx.pathing.distanceTo(banktile) > 3) return false;
		return stamina && !staminaTimer.isRunning() && ctx.pathing.energyLevel() < 30;
	}

	private Timer staminaTimer = new Timer(1);

	private final Pattern STAMINA_POTION = Pattern.compile("Stamina potion(.*)");

	public void drinkStamina() {
		SimpleItem staminaPot = ctx.inventory.populate().filter(STAMINA_POTION).next();
		if (staminaPot == null) {
			if (openBank(false)) {
				ctx.bank.depositInventory();
				Tasks.getBanking().withdrawItem("Stamina potion", 1);
				/*
				 * SimpleItem potion =
				 * ctx.bank.populate().filter(STAMINA_POTION).next(); if (potion
				 * == null) { stamina = false; return; } if
				 * (Tasks.getBanking().withdrawItem(potion.getName(), 1+""))
				 * ctx.sleepCondition(() ->
				 * ctx.inventory.populate().contains(potion), 500);
				 */

			}
		} else {
			if (ctx.bank.bankOpen()) ctx.bank.closeBank();
			while (ctx.pathing.energyLevel() < 80) {
				staminaPot = ctx.inventory.populate().filter(STAMINA_POTION).next();
				if (staminaPot != null) staminaPot.click(0);
				else break;
				ctx.sleep(150, 400);
			}
			staminaTimer = new Timer(TimeUnit.MINUTES.toMillis(1));
		}
	}

	public boolean isFull() {
		if (ore.getSecondaryOre() == ItemID.COAL) return ore.getSecondary() >= 250;
		return ore.getSecondary() >= 50 || (ore == Ore.GOLD_BAR && ore.getBars() >= 50);
	}

	public void retrieve() {
		if (!cool()) return;

		int population = ctx.inventory.populate().filter(ore.getBarName()).population();

		if (population > 0 || ctx.inventory.populate().population() > 1) {
			if (openBank(false)) ctx.bank.depositInventory();
		} else {
			SimpleObject dispenser = ctx.objects.populate().filter(new WorldPoint(1940, 4963, 0)).next();

			if (dispenser != null) { /*
										 * && dispenser.validateInteractable())
										 * {
										 */
				if (dispenser.distanceTo(ctx.players.getLocal()) > 4) {
					ctx.pathing.step(new WorldPoint(1940, 4962, 0));
					return;
				}
				if (ctx.dialogue.dialogueOpen()) {
					String body = ctx.dialogue.getDialogueTitleAndMessage()[1];
					if (body != null && body.contains("ou just recently")) {
						Variables.STATUS = "Still cookin";
						return;
					}
				}

				SimpleWidget BAR = ctx.widgets.getWidget(270, 14);

				if (goldgaunts) equip(ItemID.GOLDSMITH_GAUNTLETS);

				if (Utils.validWidget(BAR)) {
					if (BAR.click(0)) ctx.sleepCondition(() -> !ctx.dialogue.dialogueOpen());
				} else {

					LocalPoint loc = dispenser.getTileObject().getLocalLocation();
					if (MenuActions.invoke("", "", 52, MenuAction.GAME_OBJECT_FIRST_OPTION.getId(), 9092, loc.getSceneY()))
						ctx.onCondition(() -> Utils.validWidget(ctx.widgets.getWidget(270, 14)));
				}
			}
		}
	}

	public boolean equip(int itemId) {
		SimpleItem item = ctx.inventory.populate().filter(itemId).next();
		if (item == null || !ctx.equipment.populate().contains(item)) return true;
		if (item.click(0)) ctx.onCondition(() -> !ctx.equipment.filter(ItemID.GOLDSMITH_GAUNTLETS).isEmpty());
		return true;
	}

	public boolean withdrawBucket() {
		if (!bucket) return true;
		SimpleItem bucket = ctx.inventory.populate().filter(ItemID.BUCKET_OF_WATER, ItemID.BUCKET).next();

		if (bucket == null) {
			Variables.STATUS = "Withdrawing bucket";
			if (!withdraw(ItemID.BUCKET_OF_WATER, 1)) withdraw(ItemID.BUCKET, 1);
		} else if (bucket.getId() == ItemID.BUCKET) {
			fillBucket();
		}
		return bucket.getId() == ItemID.BUCKET_OF_WATER;
	}

	public void fillBucket() {
		ctx.bank.closeBank();
		SimpleObject sink = ctx.objects.populate().filter("Sink").next();

		if (sink != null && sink.validateInteractable() && sink.click("Fill-bucket"))
			ctx.onCondition(() -> !ctx.inventory.populate().filter(ItemID.BUCKET_OF_WATER).isEmpty());
	}

	public int pendingBars() {
		int step = ctx.varpbits.varpbit(Varbits.BAR_DISPENSER);
		if (step == 2 && !ctx.equipment.filter(ItemID.ICE_GLOVES).isEmpty()) return 3;
		return step;
	}

	public int getCoffer() {
		return ctx.varpbits.varpbit(Varbits.BLAST_FURNACE_COFFER);
	}

	public void fillCoffer() {
		if (!coffer) {
			Variables.STATUS = "Coffer is out";
			return;
		}
		Variables.STATUS = "Need to refill coffer";

	}

	public boolean handleCoalBag(boolean empty) {
		SimpleItem bag = ctx.inventory.populate().filter(ItemID.COAL_BAG_12019).next();
		if (bag == null) return true;
		if (!coalbag || ore.getSecondaryOre() != ItemID.COAL) return true;

		if (!empty && COALBAG_FULL) return true;

		if (empty && !COALBAG_FULL) return true;

		if (empty) {
			Variables.STATUS = "Emptying coal bag";
			if (Tasks.getMenuAction().get(bag, "Empty").invoke()) {
				ctx.onCondition(
						() -> ctx.dialogue.dialogueOpen() || ctx.inventory.populate().filter(ItemID.COAL).population() > 0);
				if (ctx.dialogue.dialogueOpen()) COALBAG_FULL = false;

			}

			return !COALBAG_FULL;
		} else {
			ctx.bank.closeBank();
			Variables.STATUS = "Filling coal bag";
			if (ctx.inventory.populate().filter(ore.getSecondaryOre()).isEmpty())
				withdraw(ore.getSecondaryOre(), 27, ore.getSecondaryOre(), ItemID.COAL_BAG_12019);
			else if (Tasks.getMenuAction().get(bag, "Fill").invoke()) {
				ctx.onCondition(() -> ctx.dialogue.dialogueOpen() || !ctx.inventory.inventoryFull());

				if (ctx.dialogue.dialogueOpen()) COALBAG_FULL = true;
			}

			return COALBAG_FULL;
		}
	}

	@Override
	public void paint(Graphics Graphs) {
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.BLACK);
		g.fillRect(5, 5, 200, 75);
		g.setColor(Color.GREEN);
		g.drawRect(5, 5, 200, 75);
		g.setColor(Color.CYAN);
		g.drawString("Total run time: " + Variables.START_TIME.toElapsedString(), 7, 20);
		g.drawString("Status: " + Variables.STATUS, 7, 35);
		g.drawString("Primary: " + ore.getOres(), 7, 50);
		g.drawString("Secondary: " + ore.getSecondary(), 7, 65);
		g.drawString("Bars: " + ore.getBars(), 7, 80);
		g.drawString("Coal Bag: " + COALBAG_FULL, 7, 95);
		g.drawString("STAMINA: " + staminaTimer.getRemaining(), 7, 108);

	}

	final String[] STOP_MESSAGES = { "be found:", "you are dead" };

	@Override
	public void onChatMessage(ChatMessage msg) {
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
			}
		}
	}

	@Override
	public int loopDuration() {
		// TODO Auto-generated method stub
		return 150;
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub

	}

}
