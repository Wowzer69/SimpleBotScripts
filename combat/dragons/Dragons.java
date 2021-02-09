package combat.dragons;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.stream.Stream;

import api.Locations;
import api.Tasks;
import api.Variables;
import api.simple.KSObject;
import api.tasks.Supplies.PotionType;
import api.threads.PrayerObserver;
import api.utils.Timer;
import api.utils.Utils;
import combat.dragons.data.Constants;
import combat.dragons.data.Constants.TYPES;
import discord.DiscordOptions;
import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

@ScriptManifest(author = "KremeSickle", category = Category.COMBAT, description = "Rune Dragons", discord = "", name = "KS | Rune Dragons", servers = {
		"Zaros" }, version = "2")
public class Dragons extends Script implements LoopingScript {
	final String[] STOP_MESSAGES = { "be found:", "enough ammo", "you are dead" };

	private PrayerObserver prayerObserver = null;

	@Override
	public void onChatMessage(ChatMessage msg) {
		Variables.LAST_MESSAGE = msg.getMessage();
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (msg.getMessage().contains("antifire potion has expired")) {
				Tasks.getSupplies().antiFire = new Timer(1);
			}
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
			}
		}
	}

	@Override
	public void onExecute() {
		Tasks.init(ctx);

		Constants.TYPE = TYPES.RUNE_DRAGON;

		Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MAGIC);
		Tasks.getSkill().addPrayer(Prayers.PIETY);

		Variables.LOOTABLES.clear();
		Variables.LOOTABLES.addAll(Arrays.asList("Runite", "platebody", "longsword", "scimitar", "platelegs", "plateskirt",
				"Dragon bolts", "Runite bolts", "Rune javelin", "Brimstone", "limbs", "lump", "Draconic"));
		System.out.println("Loaded " + Variables.LOOTABLES.size() + " lootable items");

		prayerObserver = new PrayerObserver(ctx, () -> Variables.USE_PRAYER);
		prayerObserver.start();

		Variables.STARTED = true;
	}

	@Override
	public void onProcess() {

		if (Variables.STOP) {
			Tasks.getAntiban().panic();
			return;
		}

		if (Tasks.getAntiban().check()) {
			Variables.STOP = true;
			return;
		}

		if (ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			Tasks.getSkill().disablePrayers();
			if (!Tasks.getBanking().heal()) return;
			if (!Tasks.getBanking().usePreset()) return;
			if (!Utils.directTeleport("Adamant & Rune Dragons") && Tasks.getTeleporter().open())
				Tasks.getTeleporter().teleportStringPath("Monsters", "Adamant & Rune Dragons");

		} else if (ctx.pathing.inArea(getLocation())) {
			Tasks.getSkill().enablePrayers();
			if (Tasks.getLoot().loot(Variables.LOOTABLES)) return;
			if (!Tasks.getSupplies().hasFood() || !Tasks.getSupplies().hasPrayer()) {

				ctx.magic.castSpellOnce("Home Teleport");
				return;
			}

			Tasks.getCombat().checkPots();

			if (!Tasks.getSupplies().antiFire.isRunning()) Tasks.getSupplies().drink(PotionType.ANTIFIRE);

			SimpleItem bones = Tasks.getInventory().getItem("dragon bones");
			if (bones != null) bones.click(0);

			SimpleNpc aggr = Tasks.getCombat().getAggressiveNPC(getMonster());
			if (aggr != null && aggr.distanceTo(ctx.players.getLocal()) < 6 && false) {
				Variables.STATUS = "Moving away from dragon";
				WorldPoint npcLocation = aggr.getLocation();

				WorldPoint north = new WorldPoint(npcLocation.getX() + 7, npcLocation.getY(), 0);
				WorldPoint south = new WorldPoint(npcLocation.getX() - 7, npcLocation.getY(), 0);
				WorldPoint east = new WorldPoint(npcLocation.getX(), npcLocation.getY() + 7, 0);
				WorldPoint west = new WorldPoint(npcLocation.getX(), npcLocation.getY() - 7, 0);

				if (ctx.pathing.reachable(north)) ctx.pathing.step(north);
				else if (ctx.pathing.reachable(south)) ctx.pathing.step(south);
				else if (ctx.pathing.reachable(east)) ctx.pathing.step(east);
				else if (ctx.pathing.reachable(west)) ctx.pathing.step(west);
			}
			if (ctx.players.getLocal().getInteracting() == null) Tasks.getCombat().attack(getMonster());
			else Variables.STATUS = "In combat";
		} else if (ctx.pathing.inArea(Constants.DRAGON_LOBBY_AREA)) {
			KSObject gate = new KSObject(ctx.objects.populate().filter(getGate()).nearest().next());
			if (gate.isNull()) return;
			if (gate.distanceTo(ctx.players.getLocal()) > 2) {
				Variables.STATUS = "Walking to gate";
				if (ctx.pathing.step(gate.getLocation())) ctx.onCondition(() -> !ctx.pathing.inMotion());
			} else if (gate.validateInteractable()) {
				Variables.STATUS = "Going through gate";
				if (gate.click(Variables.USE_PACKETS ? "Pass" : null)) ctx.onCondition(() -> ctx.pathing.inArea(getLocation()));
			}
		}

	}

	public String getMonster() {
		return Constants.TYPE.equals(TYPES.ADAMANT_DRAGON) ? "Adamant dragon" : "Rune dragon";
	}

	public WorldArea getLocation() {
		return Constants.TYPE.equals(TYPES.ADAMANT_DRAGON) ? Constants.ADAMANT_DRAGON_AREA : Constants.RUNE_DRAGON_AREA;
	}

	public WorldPoint getGate() {
		return Constants.TYPE.equals(TYPES.ADAMANT_DRAGON) ? Constants.ADAMANT_GATE_LOCATION : Constants.RUNE_GATE_LOCATION;
	}

	@Override
	public int loopDuration() {
		return 150;
	}

	@Override
	public void onTerminate() {
		ctx.log("Shutting down.. Thank you for using the script");
		Variables.reset();
	}

	@Override
	public void paint(Graphics Graphs) {
		if (!Variables.STARTED) return;
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.BLACK);
		g.fillRect(5, 5, 200, 40);
		g.setColor(Color.GREEN);
		g.drawRect(5, 5, 200, 40);
		g.setColor(Color.CYAN);
		g.drawString("Uptime: " + Variables.START_TIME.toElapsedString(), 7, 20);
		g.drawString("Status: " + Variables.STATUS, 7, 35);
	}

}
