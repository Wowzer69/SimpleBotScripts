package prayer;

import java.awt.Graphics;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.JFrame;

import net.runelite.api.ChatMessageType;
import net.runelite.api.coords.WorldPoint;
import simple.api.Utils;
import simple.api.Variables;
import simple.api.panel.Config;
import simple.api.panel.Panel;
import simple.api.panel.Tabs;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.script.Script;
import simple.robot.utils.WorldArea;

@ScriptManifest(author = "KremeSickle", category = Category.PRAYER, description = "<br>Uses altar in public / own POH w/ lighting option<br><br>Start the script in Edgeville<br><br>Have bones as your last preset (Marrentills + tinderbox if lighting)<br><br>Script will run until no more bones in preset<br><br>Please report all bugs to me on Discord", discord = "Datev#0660", name = "KS | POH Altar for Bones", servers = {
		"Zaros" }, version = "2")
public class AltarPrayer extends Script implements LoopingScript {
	private final WorldArea EDGEVILLE = new WorldArea(new WorldPoint(3074, 3515, 0), new WorldPoint(3105, 3480, 0));

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

	private boolean VALID_HOST = false;

	public void addConfig() {
		Config.TABS.add(new Tabs(0, "Script Config", "Choose your configuration"));

		Config.CONFIGURATION
				.add(new Config(0, boolean.class, true, "Use Own House", "Use your own house instead of public", "ownHouse"));
		Config.CONFIGURATION
				.add(new Config(0, boolean.class, true, "Light Burners", "Light incense burners yourself", "doLight"));

		Config.CONFIGURATION.add(new Config(0, boolean.class, false, "Logout on finish", "Logout on finish?", "logout"));

		Config.setConfigChanged(true);
	}

	public SimpleWidget validHouse() {
		SimpleWidget w = ctx.widgets.getWidget(52, 13);
		int index = IntStream.range(0, w.getChildren().length).filter(i -> "Y".equals(w.getChild(i).getText())).findFirst()
				.orElse(-1);
		return index > -1 ? ctx.widgets.getWidget(52, 19).getChild(index) : null;
	}

	public int getBones() {
		return ctx.inventory.populate().filter(Pattern.compile("(.*) bones")).population();
	}

	@Override
	public void onProcess() {
		if (Variables.PAUSED) return;
		if (Variables.STOP && getBones() == 0) {
			if (Config.getB("logout")) ctx.sendLogout();
			else ctx.stopScript();
			return;
		}
		if (!Variables.STARTED) {
			Variables.STATUS = "Waiting to be started";
			return;
		}
		if (ctx.pathing.inArea(EDGEVILLE)) {
			SimpleObject portal = ctx.objects.populate().filter("Portal").nearest().next();
			SimpleObject post = ctx.objects.populate().filter("House Advertisement").nearest().next();
			if (getBones() == 0) {
				bank();
				return;
			}
			if (ctx.pathing.distanceTo(portal.getLocation()) > 4) {
				Variables.STATUS = "Walking path to portal";
				ctx.pathing.step(Config.getB("ownHouse") ? portal.getLocation() : post.getLocation());
				return;
			}
			if (Config.getB("ownHouse")) {
				Variables.STATUS = "Entering own house";
				if (portal.validateInteractable() && portal.click("Home"))
					ctx.onCondition(() -> ctx.getClient().isInInstancedRegion());
			} else if (VALID_HOST) {
				Variables.STATUS = "Entering last visited house";
				if (post.validateInteractable() && post.click("Visit-Last"))
					ctx.onCondition(() -> ctx.getClient().isInInstancedRegion());
			} else {
				SimpleWidget houses = ctx.widgets.getWidget(52, 8);
				if (houses != null && houses.visibleOnScreen()) {
					SimpleWidget houseList = validHouse();
					if (houseList != null && houseList.visibleOnScreen()) {
						Variables.STATUS = "Entering house";
						if (houseList.click(0)) {
							VALID_HOST = true;
							ctx.onCondition(() -> ctx.getClient().isInInstancedRegion());
						}
					} else {
						Variables.STATUS = "No valid hosts.. refreshing";
						SimpleWidget refresh = ctx.widgets.getWidget(52, 30);
						if (refresh != null && refresh.visibleOnScreen()) {
							if (refresh.click(0)) {
								ctx.sleep(7000, 10000);
							}
						}
					}
				} else {
					if (post != null && post.validateInteractable()) {
						if (post.click("View")) ctx.onCondition(() -> ctx.widgets.getWidget(52, 8) != null);
					}
				}
			}

		} else if (ctx.getClient().isInInstancedRegion()) {
			if (ctx.widgets.getWidget(71, 5) != null) return;
			SimpleObject altar = ctx.objects.populate().filter("Altar").nearest().next();
			SimpleItem bone = ctx.inventory.populate().filter(Pattern.compile("(.*) bones")).next();
			if (altar == null) {
				Variables.STATUS = "Can't find altar";
				VALID_HOST = false;
				if (!Config.getB("ownHouse")) teleportHome();
				return;
			}
			if (bone == null) {
				teleportHome();
				return;
			}
			if (Config.getB("doLight")) {
				SimpleObject unlit = ctx.objects.populate().filter(13212).nearest().next();
				if (unlit != null && ctx.inventory.populate().filter("Marrentill").population() > 0
						&& ctx.inventory.populate().filter("Tinderbox").population() > 0) {
					Variables.STATUS = "Relighting burners";
					if (unlit.click("Light")) ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != -1);
					return;
				}
			}
			if (ctx.players.getLocal().getAnimation() == 896) {
				Variables.STATUS = "Gaining some experience";
				return;
			}

			if (altar.validateInteractable() && bone.click("Use")) {
				Variables.STATUS = "Using bones on altar";
				ctx.onCondition(() -> ctx.inventory.itemSelectionState() == 1);
				if (altar.click(0)) ctx.onCondition(() -> ctx.players.getLocal().getAnimation() == 896);
			}
		}
	}

	@Override
	public int loopDuration() {
		return 200;
	}

	@Override
	public void onTerminate() {
		VALID_HOST = false;

		if (frame != null) frame.dispose();
		ctx.log("Shutting down.. Thank you for using the script");
		Variables.reset();
		Config.clear();
	}

	@Override
	public void paint(Graphics Graphs) {
		if (panel != null) panel.update(Variables.STATUS);
	}

	final String[] STOP_MESSAGES = { "not be found:" };

	@Override
	public void onChatMessage(ChatMessage msg) {
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (msg.getMessage().contains("don't have any recent") || msg.getMessage().contains("owner is not at home")) {
				VALID_HOST = false;
			}
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
			}
		}
	}

	public void teleportHome() {
		Variables.STATUS = "Teleporting home";
		ctx.magic.castSpellOnce("Home");
		ctx.onCondition(() -> ctx.pathing.inArea(EDGEVILLE));
	}

	public void bank() {
		SimpleObject bank = ctx.objects.populate().filter("Bank booth").nearest().next();
		if (bank == null || bank.distanceTo(ctx.players.getLocal()) > 5) {
			Variables.STATUS = "Walking path to bank";
			ctx.pathing.step(bank.getLocation());
		} else if (bank.validateInteractable()) {
			Variables.STATUS = "Getting last preset";
			if (bank.click("Last-preset")) {
				ctx.onCondition(() -> ctx.inventory.populate().population() > 0, 500);
			}
		}
	}

}
