package construction;

import java.awt.Graphics;
import java.util.stream.Stream;

import javax.swing.JFrame;

import net.runelite.api.ChatMessageType;
import simple.api.Utils;
import simple.api.Variables;
import simple.api.panel.Config;
import simple.api.panel.Panel;
import simple.api.panel.Tabs;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.CONSTRUCTION, description = "<br>This script builds Magic balance's in your POH.<br><br>Start near the Elemental Balance Space in your house in build mode<br><br>Have the required runes in your inventory (Fire,Air,Earth,Water), saw & hammer<br><br>Script will advance onto the highest balance once level is reached.<br><br>Script will shut down after runes run out.", discord = "Datev#0660", name = "KS | Magic Balance", servers = {
		"Zaros" }, version = "2")
public class MagicalBalance extends Script implements LoopingScript {

	private JFrame frame;
	private Panel panel;

	@Override
	public void onExecute() {
		try {
			Variables.reset();
			addConfig();
			String title = Utils.getValue(getClass(), "name") + " v" + Utils.getValue(getClass(), "version");
			panel = new Panel();
			frame = panel.init(title, panel);
			Utils.setZoom(1);
		} catch (Exception e) {
			ctx.log(e.getMessage());
			e.printStackTrace();
		}
	}

	public void addConfig() {
		Config.TABS.add(new Tabs(0, "Script Config", "Choose your configuration"));
		Config.CONFIGURATION.add(
				new Config(0, boolean.class, true, "Lesser magical balance", "Build the Lesser magical balance", "buildLesser"));
		Config.CONFIGURATION.add(
				new Config(0, boolean.class, true, "Medium magical balance", "Build the Medium magical balance", "buildMedium"));
		Config.CONFIGURATION.add(new Config(0, boolean.class, true, "Greater magical balance",
				"Build the Greater magical balance", "buildGreator"));
		Config.CONFIGURATION
				.add(new Config(0, boolean.class, false, "Logout on finish", "Logout when out of ingredients?", "logout"));
		Config.setConfigChanged(true);
	}

	@Override
	public void onProcess() {
		if (Variables.PAUSED) return;
		if (Variables.STOP) {
			Variables.STATUS = "Shutting down";
			if (Config.getB("logout")) ctx.sendLogout();
			else ctx.stopScript();
			return;
		}
		if (!Variables.STARTED) {
			Variables.STATUS = "Waiting to be started";
			return;
		}
		SimpleObject object = ctx.objects.populate().filter("Elemental balance", "Elemental balance space").nearest().next();
		SimpleWidget screen = ctx.widgets.getWidget(458, 1);
		if (object == null) {
			ctx.log("Unable to find object");
			Variables.STOP = true;
			return;
		}
		if (object.getId() == 15345) {
			if (screen == null || !screen.visibleOnScreen()) {
				Variables.STATUS = "Opening construction widget";
				if (object.validateInteractable() && object.click("Build"))
					ctx.onCondition(() -> ctx.widgets.getWidget(458, 1).visibleOnScreen());
			} else {
				int index = getIndex();
				if (index == -1) {
					Variables.STATUS = "Select at least one balance";
					return;
				}
				SimpleWidget build = ctx.widgets.getWidget(458, 4 + index);
				Variables.STATUS = "Building object";
				if (build != null && build.visibleOnScreen()) {
					if (build.click(0)) {
						ctx.onCondition(() -> object.getId() != 15345, 1500);
						ctx.sleep(300, 450);
					}
				}
			}
		} else {
			Variables.STATUS = "Removing object";
			if (ctx.dialogue.dialogueOpen()) {
				ctx.dialogue.clickDialogueOption(1);
				ctx.onCondition(() -> !ctx.dialogue.dialogueOpen());
			}
			if (object.validateInteractable() && object.click("Remove")) {
				ctx.onCondition(() -> ctx.dialogue.dialogueOpen());
			}
		}
	}

	@Override
	public int loopDuration() {
		return 200;
	}

	@Override
	public void onTerminate() {
		if (frame != null) frame.dispose();
		ctx.log("Shutting down.. Thank you for using the script");
		Variables.reset();
		Config.clear();
	}

	@Override
	public void paint(Graphics Graphs) {
		if (panel != null) panel.update(Variables.STATUS);
	}

	final String[] STOP_MESSAGES = { "don't have the right", "need a hammer", "need a saw" };

	@Override
	public void onChatMessage(ChatMessage msg) {
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
			}
		}
	}

	public int getIndex() {
		int level = ctx.skills.level(Skills.CONSTRUCTION);
		if (Config.getB("buildGreator") && level >= 77) return 2;
		else if (Config.getB("buildMedium") && level >= 57) return 1;
		else if (Config.getB("buildLesser")) return 0;
		return -1;
	}

}
