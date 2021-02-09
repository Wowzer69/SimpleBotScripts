package aioslayer;

import java.awt.Graphics;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;

import javax.swing.JFrame;

import aioslayer.data.Builder;
import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import aioslayer.data.master.Nieve;
import api.Locations;
import api.Tasks;
import api.Variables;
import api.panel.Config;
import api.panel.Panel;
import api.panel.Tabs;
import api.threads.PrayerObserver;
import api.utils.Utils;
import net.runelite.api.ChatMessageType;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.robot.script.Script;

@ScriptManifest(author = "Trester/Lead-Assistant-scripter-KremeSickle", category = Category.SLAYER, description = "AIO Slayer", discord = "", name = "AIO Slayer", servers = {
		"Zaros" }, version = "1")
public class AIOSlayer extends Script implements LoopingScript {

	private PrayerObserver prayerObserver = null;

	private JFrame frame;
	private Panel panel;

	@Override
	public void onExecute() {
		try {
			Tasks.init(ctx);
			reset();
			Constants.MASTER = new Nieve();
			addConfig();
			String title = Utils.getValue(getClass(), "name") + " v" + Utils.getValue(getClass(), "version");
			panel = new Panel();
			frame = panel.init(title, panel);
			Utils.setZoom(1);

			prayerObserver = new PrayerObserver(ctx, new BooleanSupplier() {
				@Override
				public boolean getAsBoolean() {
					return Variables.USE_PRAYER;
				}
			});
			prayerObserver.start();

		} catch (Exception e) {
			ctx.log(e.getMessage());
			e.printStackTrace();
		}
	}

	public void addConfig() throws IOException {
		Config.TABS.add(new Tabs(0, "Script Config", "Choose your configuration"));
		Config.CONFIGURATION.add(new Config(0, boolean.class, true, "Alch", "Alch items", "doAlch"));

		Config.TABS.add(new Tabs(1, "Monster Config", "Choose your locations"));
		for (Entry<String, MonsterTask> entry : Builder.monsterDict.entrySet()) {
			if (entry.getValue().areas().length > 1) Config.CONFIGURATION.add(new Config(1, entry.getValue().areas(), "",
					entry.getValue().getName(), "Where to kill them at", entry.getKey()));
		}
		Config.load(getStorageDirectory() + ctx.players.getLocal().getName());
		Config.setConfigChanged(true);
	}

	@Override
	public void onProcess() {
		if (!Variables.STARTED) return;

		if (Variables.STOP) {
			Tasks.getSkill().disablePrayers();
			if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
				ctx.magic.castSpellOnce("Home Teleport");
			}
			return;
		}

		if ((!ctx.pathing.inArea(Locations.EDGEVILLE_AREA) && Tasks.getAntiban().staffNearby())
				|| (Tasks.getAntiban().staffUnder() && !ctx.pathing.inArea(Locations.EDGEVILLE_BANK))) {
			ctx.log("Staff found at " + ctx.players.getLocal().getLocation());
			Variables.STOP = true;
			return;
		}

		if (!ctx.combat.autoRetaliate()) ctx.combat.toggleAutoRetaliate(true);

		if (Tasks.getSlayer().shouldBank() && !ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			Tasks.getSkill().disablePrayers();
			ctx.magic.castSpellOnce("Home Teleport");
		} else if (ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			Tasks.getSkill().disablePrayers();
			if (Tasks.getCombat().getMonsterTimer().isRunning()) Tasks.getCombat().getMonsterTimer().end();
			if (!Tasks.getBanking().heal()) return;
			if (!Tasks.getBanking().usePreset(true)) return;
			Variables.FORCE_BANK = false;
		}
		if (Constants.TASK == null) {
			Tasks.getCombat().getMonsterTimer().reset();
			Tasks.getSlayer().handleTask();
		} else if (!Constants.TASK.atLocation()) {
			Constants.TASK.equipGear();
			Constants.TASK.travel();
		} else {
			Tasks.getSkill().enablePrayers();
			Constants.TASK.attack();
		}
	}

	@Override
	public int loopDuration() {
		return 400;
	}

	@Override
	public void onTerminate() {
		if (frame != null) frame.dispose();
		try {
			Config.save(getStorageDirectory() + ctx.players.getLocal().getName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		ctx.log("Shutting down.. Thank you for using the script");
		Tasks.getSkill().disablePrayers();
		if (prayerObserver != null) prayerObserver.interrupt();
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			ctx.magic.castSpellOnce("Home Teleport");
		}
		reset();
	}

	@Override
	public void paint(Graphics Graphs) {
		if (panel != null) panel.update(Variables.STATUS);
	}

	final String[] STOP_MESSAGES = { "you are dead", "no ammo" };

	@Override
	public void onChatMessage(ChatMessage msg) {
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			String message = msg.getMessage();
			Variables.LAST_MESSAGE = message;
			if (message.contains("you're assigned to kill")) {
				String name = msg.getMessage().replaceAll("(.*) kill (.*); (.*)", "$2");

				Constants.SHOULD_SKIP = false;
				MonsterTask result = Builder.getMonsterByString(name);

				if (Tasks.getSlayer().shouldSkip(name)) {
					System.out.println("SKIP");
					Constants.SHOULD_CHECK_TASK = false;
					Constants.SHOULD_SKIP = true;
					Constants.TASK = null;
					return;
				} else if (result == null) {
					ctx.log(name);
					ctx.log("NPC: " + name + " could not be found, stopping script.");
					ctx.stopScript();
					return;
				}

				Constants.TASK = result;
				Constants.SHOULD_CHECK_TASK = true;
				Constants.SHOULD_SKIP = false;
			}

			if (message.contains("wants you to stick to your slayer assignments") || message.contains("something new to")
					|| message.contains(" giving you a total of ")) {
				ctx.log(message);
				ctx.log("Resetting task");
				Constants.TASK = null;
				Variables.FORCE_BANK = true;
				Constants.SHOULD_CHECK_TASK = false;
			}
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> message.contains(msg1));
			}
		}
	}

	public void reset() {
		Variables.reset();
		Config.clear();
		Constants.SHOULD_CHECK_TASK = true;
		Constants.TASK = null;
		Constants.SHOULD_SKIP = false;
		Tasks.getCombat().getMonsterTimer().end();
		Variables.FORCE_BANK = ctx.pathing.inArea(Locations.EDGEVILLE_AREA);
	}
}
