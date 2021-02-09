package instancedBandos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.stream.Stream;

import api.Locations;
import api.Tasks;
import api.Variables;
import api.simple.KSObject;
import api.tasks.Token.INSTANCES;
import api.threads.PrayerObserver;
import api.utils.Timer;
import instancedBandos.data.Constants;
import instancedBandos.methods.GUI;
import instancedBandos.methods.Methods;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.GameState;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.COMBAT, description = "KS | Bandos Killer", discord = "", name = "KS | Bandos Killer", servers = {
		"Zaros" }, version = "1.1")

public class Core extends Script implements LoopingScript {
	final String[] STOP_MESSAGES = { "be found:", "he owner of the username" };

	private PrayerObserver prayerObserver = null;

	@Override
	public void onChatMessage(ChatMessage msg) {
		if (msg.getType() == ChatMessageType.FRIENDSCHAT) {
			if (msg.getMessage().equals("<<<")) Variables.STOP = true;
		}
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (msg.getMessage().contains("kill count")) Variables.COUNT++;
			if (msg.getMessage().contains("you are dead") && !Constants.USE_INSURANCE) Variables.STOP = true;
			if (!Variables.STOP) Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
		}
	}

	@Getter
	private Methods methods;
	private GUI gui = new GUI();

	@Override
	public void onExecute() {
		Tasks.init(ctx);
		Variables.reset();
		methods = new Methods(ctx);
		Constants.USUAL_ITEMS = ctx.equipment.populate().toStream().map(item -> item.getName()).toArray(String[]::new);

		Variables.LOOTABLES.clear();
		Variables.LOOTABLES
				.addAll(Arrays.asList("Bandos", "Godsword", "Rune platebody", "Coins", "Super restore", "Shark", "potato"));
		System.out.println("Loaded " + Variables.LOOTABLES.size() + " lootable items");

		prayerObserver = new PrayerObserver(ctx, () -> Variables.USE_PRAYER);
		prayerObserver.start();

		gui.setVisible(true);

	}

	@Override
	public void onProcess() {
		if (Variables.STOP) {
			if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
				ctx.magic.castSpellOnce("Home Teleport");
			} else if (!ctx.players.getLocal().inCombat()) {
				while (ctx.getClient().getGameState() == GameState.LOGGED_IN) {
					ctx.sendLogout();
				}
			}
			return;
		}
		if (!Variables.STARTED) return;

		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA) && Tasks.getAntiban().staffNearby()) {
			System.out.println("Staff found at " + ctx.players.getLocal().getLocation());
			Variables.STOP = true;
			return;
		}

		if (ctx.pathing.energyLevel() > 10) ctx.pathing.running(true);
		if (ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			Tasks.getSkill().disablePrayers();
			if (!Tasks.getInventory().contains("token")) Variables.FORCE_BANK = true;
			if (!Tasks.getBanking().heal()) return;
			if (!Tasks.getBanking().usePreset()) return;
			Constants.DOOR_TILE = null;
			Tasks.getToken().handle(Constants.HOST_NAME, Constants.IS_HOST, Constants.USE_INSURANCE, INSTANCES.GENERAL_GRAARDOR);
		} else if (ctx.getClient().isInInstancedRegion()) {
			if (!Tasks.getSupplies().hasFood() || !Tasks.getSupplies().hasPrayer()) {
				ctx.magic.castSpellOnce("Home Teleport");
				return;
			}
			if (ctx.players.getLocal().getLocation().getRegionX() <= 46) {
				KSObject door = new KSObject(ctx.objects.populate().filter(26503).next());
				if (!door.isNull() && door.click("Open")) {
					if (Constants.DOOR_TILE == null) Constants.DOOR_TILE = door.getLocation();
					Variables.STATUS = "Going through door";
					ctx.sleep(500, 600);
					ctx.sleepCondition(() -> ctx.players.getLocal().getLocation().getRegionX() > 46, 1000);
					trip.restart();
					trips++;
				}
			} else {
				methods.handleBandos();
			}
		}
	}

	@Override
	public int loopDuration() {
		return 400;
	}

	@Override
	public void onTerminate() {
		Variables.reset();
		if (gui != null) gui.dispose();
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) ctx.magic.castSpellOnce("Home Teleport");
		ctx.log("Shutting down.. Thank you for using the script");
	}

	private Timer trip = new Timer();
	private int trips = 0;

	@Override
	public void paint(Graphics Graphs) {
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.BLACK);
		g.fillRect(5, 5, 200, 80);
		g.setColor(Color.GREEN);
		g.drawRect(5, 5, 200, 80);
		g.setColor(Color.CYAN);
		g.drawString("Uptime: " + Variables.START_TIME.toElapsedString(), 7, 20);
		g.drawString("Status: " + Variables.STATUS, 7, 35);
		g.drawString(String.format("Kills: %s (%s p/hr)", Variables.COUNT,
				ctx.paint.valuePerHour((int) Variables.COUNT, Variables.START_TIME.getStart())), 7, 48);
		g.drawString("Current trip: " + trip.toElapsedString(), 7, 61);
		g.drawString("Trips: " + trips, 7, 73);

	}

}
