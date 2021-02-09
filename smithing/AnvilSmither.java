package smithing;

import java.awt.Graphics;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.swing.JFrame;

import net.runelite.api.ChatMessageType;
import net.runelite.api.widgets.WidgetInfo;
import simple.api.Timer;
import simple.api.Utils;
import simple.api.Variables;
import simple.api.panel.Config;
import simple.api.panel.Panel;
import simple.api.panel.Tabs;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;
import smithing.data.Smithable;

@ScriptManifest(author = "KremeSickle", category = Category.SMITHING, description = "<br>This script uses the Varrock smithing anvil<br><br>Start the script at the bank with your last-preset containing a hammer and your bars<br><br>The script will create the item of your choice and rebank when needed<br><br>Please make sure you have the required smithing level to make the item<br><br>Please message me on discord with any questions/concerns", discord = "Datev#0660", name = "KS | Anvil Smither", servers = {
		"Zaros" }, version = "1")
public class AnvilSmither extends Script implements LoopingScript {

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

	private boolean FORCE_BANK = true;
	private Timer LAST_ANIM = new Timer(1);

	public void addConfig() {
		Config.TABS.add(new Tabs(0, "Script Config", "Choose your configuration"));
		Config.CONFIGURATION.add(new Config(0, Smithable.class, "DAGGER", "Smith", "Choose which item to smith", "smithable"));
		Config.CONFIGURATION.add(new Config(0, boolean.class, false, "Logout on finish", "Logout on finish?", "logout"));
		Config.setConfigChanged(true);
	}

	@Override
	public void onProcess() {
		if (Variables.PAUSED) return;
		if (Variables.STOP && ctx.inventory.populate().filter(Pattern.compile("(.*) bar")).isEmpty()) {
			if (Config.getB("logout")) ctx.sendLogout();
			else ctx.stopScript();
			return;
		}
		if (!Variables.STARTED) {
			Variables.STATUS = "Waiting to be started";
			return;
		}

		if (ctx.inventory.populate().filter(Pattern.compile("(.*) bar")).isEmpty() || FORCE_BANK) {
			bank();
		} else {
			if (ctx.players.getLocal().getAnimation() == 898) {
				Variables.STATUS = "Gaining some experience";
				LAST_ANIM = new Timer(1000);
			}
			if (ctx.dialogue.dialogueOpen()) {
				if (!ctx.dialogue.populate().filterContains("ou don't have enough").isEmpty()) {
					Variables.STATUS = "Out of bars";
					FORCE_BANK = true;
				} else if (!ctx.dialogue.populate().filterContains("need a Smithing level").isEmpty()) {
					Variables.STATUS = "Need higher level to smith";
					ctx.log("You need a higher level to smith this item");
					Variables.STOP = true;
				}
				return;
			}

			if (LAST_ANIM.isRunning()) return;

			if (ctx.widgets.getWidget(WidgetInfo.SMITHING_INVENTORY_ITEMS_CONTAINER) != null) {
				Variables.STATUS = "Clicking on widget";
				if (getSmithable().click(0))
					ctx.onCondition(() -> ctx.players.getLocal().getAnimation() == 898 || ctx.dialogue.dialogueOpen());
			} else {
				anvil();
			}
		}
	}

	public void anvil() {
		SimpleObject anvil = ctx.objects.populate().filter("Anvil").nearest().next();
		if (anvil != null) {
			if (anvil.distanceTo(ctx.players.getLocal()) > 5) {
				Variables.STATUS = "Walking to anvil";
				ctx.pathing.step(anvil.getLocation());
			} else if (anvil.validateInteractable()) {
				Variables.STATUS = "Clicking anvil";
				if (anvil.click("Smith"))
					ctx.onCondition(() -> ctx.widgets.getWidget(WidgetInfo.SMITHING_INVENTORY_ITEMS_CONTAINER) != null);
			}
		}
	}

	public void bank() {
		SimpleObject bank = ctx.objects.populate().filter("Bank booth").nearest().next();
		if (bank != null) {
			if (bank.distanceTo(ctx.players.getLocal()) > 5) {
				Variables.STATUS = "Walking path to bank";
				ctx.pathing.step(bank.getLocation());
			} else if (bank.validateInteractable()) {
				Variables.STATUS = "Getting last preset";
				if (bank.click("Last-preset")) {
					FORCE_BANK = false;
					ctx.onCondition(() -> LAST_MESSAGE.contains("our preset is being"), 500);
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

	final String[] STOP_MESSAGES = { "be found:" };
	private String LAST_MESSAGE = "";

	@Override
	public void onChatMessage(ChatMessage msg) {
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			LAST_MESSAGE = msg.getMessage();

			if (msg.getMessage().contains("be found:") && ctx.inventory.populate().filter(Pattern.compile("(.*) bar")).isEmpty())
				Variables.STOP = true;
		}
	}

	public SimpleWidget getSmithable() {
		SimpleWidget inter = ctx.widgets.getWidget(312, 9);
		if (inter != null && inter.visibleOnScreen()) {
			int index = IntStream.range(9, 45).filter(c -> {
				SimpleWidget w = ctx.widgets.getWidget(312, c);
				return w != null && w.visibleOnScreen() && w.getChild(0).getText() != null
						&& w.getChild(0).getText().contains(smith().getFriendlyName());
			}).findFirst().orElse(-1);

			if (index == -1) {
				Variables.STATUS = "REPORT AS BUG - CAN'T FIND ITEM";
				return null;
			}
			return ctx.widgets.getWidget(312, index);
		}
		return null;
	}

	public Smithable smith() {
		return Config.getValue("smithable");
	}

}
