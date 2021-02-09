package warriorguild;

import java.awt.Graphics;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import lombok.Getter;
import simple.api.Locations;
import simple.api.Utils;
import simple.api.Variables;
import simple.api.panel.Config;
import simple.api.panel.Panel;
import simple.api.panel.Tabs;
import simple.api.panel.options.Range;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.robot.script.Script;
import warriorguild.data.Constants;
import warriorguild.data.Methods;

@ScriptManifest(author = "KremeSickle", category = Category.MINIGAMES, description = "<br>This script will get you tokens and defenders<br><br>Start the script at the warrior guild<br<br>If you're getting tokens, start with (Rune full helm, platebody, platelegs) in inventory (ONLY RUNE IS SUPPORTED)<br><br>If using food or prayer potions, have them in inventory as there is no banking (Uses Melee protection only)<br><br>If using attack cape instead of tokens, have cape equipped<br><br>Script will shut down once Rune defender (or) Dragon defender has been achieved<br><br>Please message me on discord with any questions/concerns", discord = "Datev#0660", name = "KS | Warrior Guild", servers = {
		"Zaros" }, version = "3", vip = true)
public class WarriorGuild extends Script {

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

	@Getter
	private Methods methods;

	public void addConfig() {
		methods = new Methods(ctx, this);
		Config.TABS.add(new Tabs(0, "Script Config", "Choose your configuration"));
		Config.TABS.add(new Tabs(1, "Combat Config", "Choose your combat configuration"));

		Config.CONFIGURATION
				.add(new Config(0, boolean.class, true, "Gather Tokens", "Gather Warrior guild tokens when needed", "getToken"));
		Config.CONFIGURATION
				.add(new Config(0, boolean.class, true, "Gather Defenders", "Gather Bronze - Rune defender", "getDefender"));
		Config.CONFIGURATION.add(
				new Config(0, boolean.class, true, "Gather Dragon defenders", "Gather Dragon defender", "getDragonDefender"));
		Config.CONFIGURATION.add(new Config(0, boolean.class, true, "Stop on defender obtain",
				"Stop the script when selected defenders are obtained", "finish"));
		Config.CONFIGURATION.add(new Config(0, boolean.class, false, "Logout on finish", "Logout on finish?", "logout"));

		Config.CONFIGURATION.add(new Config(1, boolean.class, false, "Eat Food", "Eat any food in inventory", "useFood"));
		Config.CONFIGURATION
				.add(new Config(1, int.class, 40, "Eat At %", "Eat at this % of hitpoints", "eatHP", new Range(1, 100)));
		Config.CONFIGURATION.add(new Config(1, boolean.class, false, "Use Prayer",
				"Drink prayer/super restore potions in inventory", "usePrayer"));
		Config.CONFIGURATION
				.add(new Config(1, int.class, 40, "Drink At %", "Drink at this % of prayer", "drinkPrayer", new Range(1, 100)));

		Config.setConfigChanged(true);
	}

	public void setBooleans() {
		SimpleItem tokens = ctx.inventory.populate().filter(8851).next();
		SimpleItem defender = ctx.inventory.populate().filter("Rune defender").next();
		if (Config.getB("getToken") && !getMethods().hasAttackCape() && (tokens == null || tokens.getQuantity() < 100))
			Constants.CURRENT_TASK = 0;
		else if (Config.getB("getDefender") && defender == null) Constants.CURRENT_TASK = 1;
		else if (Config.getB("getDragonDefender")) Constants.CURRENT_TASK = 2;
	}

	@Override
	public void onProcess() {
		if (Variables.PAUSED) return;
		if (Variables.STOP) {
			if (Config.getB("logout")) ctx.sendLogout();
			else ctx.stopScript();
			return;
		}
		if (!Variables.STARTED) {
			Variables.STATUS = "Waiting to be started";
			return;
		}

		if (Variables.STOP) {
			if (Config.getB("logout")) {
				if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) ctx.magic.castSpellOnce("Home Teleport");
				else ctx.sendLogout();
			} else ctx.stopScript();
			return;
		}

		if (Config.isConfigChanged()) {
			setBooleans();
			Config.setConfigChanged(false);
		}

		if (!getMethods().insideWarriorGuild()) {
			Variables.STATUS = "Please enter the Warrior's guild";
			return;
		}

		if (!ctx.combat.autoRetaliate()) ctx.combat.toggleAutoRetaliate(true);

		if (ctx.skills.level(Skills.HITPOINTS) < Config.getI("eatHP")) {
			SimpleItem food = ctx.inventory.populate().filterHasAction("Eat").next();
			if (food != null && food.click(0)) ctx.sleep(150, 200);
		}

		if (ctx.skills.level(Skills.PRAYER) < Config.getI("drinkPrayer")) {
			SimpleItem potion = ctx.inventory.populate().filter(Pattern.compile("Prayer potion(.*)")).next();
			if (potion == null) ctx.inventory.populate().filter(Pattern.compile("Super restore (.*)")).next();
			if (potion != null && potion.click(0)) ctx.sleep(150, 200);
		}

		if (Constants.CURRENT_TASK == 0) {
			boolean obtain = Config.getB("getToken");

			if (Config.getB("finish") && getMethods().has("Rune defender") && !Config.getB("getDragonDefender")) {
				Variables.STOP = true;
				return;
			}

			if (getMethods().hasAttackCape()) {
				ctx.log("Resetting.. has attack cape");
				Config.getItem("getToken").setValue(false);
				setBooleans();
				return;
			}

			if (!obtain) {
				Variables.STATUS = "Please adjust your settings";
				return;
			}

			if (getMethods().getTokens() > 300) {
				ctx.log("Resetting.. has enough tokens");
				setBooleans();
				return;
			}
			if (!getMethods().loot("Rune platebody", "Rune full helm", "Rune platelegs", "Warrior guild token"))
				getMethods().handleTokens();
		} else if (Constants.CURRENT_TASK == 1) {
			if (getMethods().has("Rune defender")) {
				if (Config.getB("getDragonDefender")) setBooleans();
				else if (Config.getB("finish")) return;
			}
			if (!getMethods().loot("Bronze defender", "Iron defender", "Steel defender", "Black defender", "Mithril defender",
					"Adamant defender", "Rune defender"))
				getMethods().handleDefenders();
		} else if (Constants.CURRENT_TASK == 2) {
			if (Config.getB("finish") && getMethods().has("Dragon defender")) {
				Variables.STOP = true;
				return;
			}
			if (!getMethods().loot("Dragon defender")) getMethods().handleDefenders();
		} else {
			Variables.STOP = true;
		}
	}

	@Override
	public void onTerminate() {
		ctx.prayers.prayer(Prayers.PROTECT_FROM_MELEE, false);
		if (frame != null) frame.dispose();
		ctx.log("Shutting down.. Thank you for using the script");
		Variables.reset();
		Config.clear();
	}

	@Override
	public void paint(Graphics Graphs) {
		if (panel != null) panel.update(Variables.STATUS);
	}

	@Override
	public void onChatMessage(ChatMessage e) {
	}
}
