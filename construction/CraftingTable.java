package construction;

import java.awt.Graphics;
import java.util.stream.Stream;

import javax.swing.JFrame;

import net.runelite.api.ChatMessageType;
import simple.api.Locations;
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
import simple.hooks.simplebot.Game.Tab;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.CONSTRUCTION, description = "<br>This script will build crafting tables using Molten glass<br><br>Have the room built & near portal<br>Have your preset to include everything required.<br><br>If using the Teleport to House option, have the runes/teleport tab in your preset<br><br>Script will shut down once out of materials<br><br>Please message me on discord with any questions/concerns.", discord = "Datev#0660", name = "KS | Molten Crafting Table", servers = {
		"Zaros" }, version = "1")
public class CraftingTable extends Script implements LoopingScript {

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
			Utils.setZoom(5);
		} catch (Exception e) {
			ctx.log(e.getMessage());
			e.printStackTrace();
		}
	}

	public void addConfig() {
		Config.TABS.add(new Tabs(0, "Script Config", "Choose your configuration"));
		Config.CONFIGURATION
				.add(new Config(0, boolean.class, true, "Crafting table 2", "Build the Crafting table 2", "craftTable2"));
		Config.CONFIGURATION
				.add(new Config(0, boolean.class, true, "Crafting table 3", "Build the Crafting table 3", "craftTable3"));
		Config.CONFIGURATION
				.add(new Config(0, boolean.class, false, "Use PoH teleport", "Uses the Teleport to House spell", "pohteleport"));
		Config.CONFIGURATION
				.add(new Config(0, boolean.class, false, "Logout on finish", "Logout when out of ingredients?", "logout"));
		Config.setConfigChanged(true);
	}

	@Override
	public void onProcess() {
		if (Variables.PAUSED) return;
		if (Variables.STOP && getGlass() < 2) {
			if (Config.getB("logout")) ctx.sendLogout();
			else ctx.stopScript();
			return;
		}

		if (!Variables.STARTED) {
			Variables.STATUS = "Waiting to be started";
			return;
		}

		if (ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			if (getGlass() > 2) {
				if (Config.getB("pohteleport")) {
					SimpleItem tab = ctx.inventory.populate().filter("Teleport to house").next();
					if (tab != null) tab.click(0);
					else ctx.magic.castSpellOnce("Teleport to house");
					ctx.onCondition(() -> ctx.getClient().isInInstancedRegion());
				} else {
					SimpleObject portal = ctx.objects.populate().filter("Portal").nearest().next();
					if (ctx.pathing.distanceTo(portal.getLocation()) > 4) {
						Variables.STATUS = "Walking path to portal";
						ctx.pathing.step(portal.getLocation());
						return;
					}
					if (portal.click("Build")) ctx.onCondition(() -> ctx.getClient().isInInstancedRegion());
				}
			} else {
				bank();
			}
		} else if (ctx.getClient().isInInstancedRegion()) {
			if (ctx.widgets.getWidget(71, 5) != null) return;
			if (getGlass() < 2) {
				ctx.magic.castSpellOnce("Home Teleport");
				ctx.onCondition(() -> ctx.pathing.inArea(Locations.EDGEVILLE_AREA));
				return;
			}

			if (ctx.varpbits.varpbit(780) == 0) {
				Variables.STATUS = "Entering build mode";
				SimpleWidget build = ctx.widgets.getWidget(261, 101);
				SimpleWidget check = ctx.widgets.getWidget(370, 5);

				if (Utils.validWidget(check) && check.getSpriteId() != 699) {
					if (check.click(0)) ctx.onCondition(() -> ctx.varpbits.varpbit(780) == 1);
				} else if (Utils.validWidget(build)) {
					if (build.click(0)) ctx.onCondition(() -> Utils.validWidget(ctx.widgets.getWidget(370, 5)));
				} else Utils.openTab(Tab.OPTIONS);
				return;
			}
			SimpleObject object = ctx.objects.populate().filter("Clockmaker's bench", "Clockmaking space").nearest().next();
			SimpleWidget screen = ctx.widgets.getWidget(458, 1);

			if (object == null) {
				Variables.STATUS = "Unable to find object";
				ctx.log("Unable to find object");
				return;
			}
			if (object.getId() == 15441) {
				if (screen == null || !screen.visibleOnScreen()) {
					Variables.STATUS = "Opening construction widget";
					if (object.validateInteractable() && object.click("Build"))
						ctx.onCondition(() -> ctx.widgets.getWidget(458, 1).visibleOnScreen());
				} else {
					int index = getIndex();
					if (index == -1) {
						Variables.STATUS = "Select at least one table";
						return;
					}
					SimpleWidget build = ctx.widgets.getWidget(458, 4 + index);
					Variables.STATUS = "Building object";
					if (build != null && build.visibleOnScreen()) {
						if (build.click(0)) {
							ctx.onCondition(() -> object.getId() != 15441, 1500);
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

	final String[] STOP_MESSAGES = { "item could not be found", "don't have the right", "need a hammer", "need a saw" };

	@Override
	public void onChatMessage(ChatMessage msg) {
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
			}
		}
	}

	public void bank() {
		SimpleObject bank = ctx.objects.populate().filter("Bank booth").nearest().next();
		if (bank == null) {
			Variables.STATUS = "Bank unable to be found";
			return;
		}
		if (bank.distanceTo(ctx.players.getLocal()) > 5) {
			Variables.STATUS = "Walking path to bank";
			ctx.pathing.step(bank.getLocation());
		} else if (bank.validateInteractable()) {
			Variables.STATUS = "Getting last preset";
			if (bank.click("Last-preset")) {
				ctx.onCondition(() -> ctx.inventory.populate().population() > 0, 500);
			}
		}
	}

	public int getGlass() {
		return ctx.inventory.populate().filter("Molten glass").population();
	}

	public int getIndex() {
		int level = ctx.skills.level(Skills.CONSTRUCTION);
		if (Config.getB("craftTable3") && level >= 34) return 2;
		else if (Config.getB("craftTable2") && level >= 25) return 1;
		return -1;
	}
}
