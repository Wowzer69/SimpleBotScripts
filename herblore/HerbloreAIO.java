package herblore;

import java.awt.Graphics;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;

import herblore.data.Constants;
import herblore.data.Herbs;
import herblore.data.Potions;
import herblore.data.tasks.CleanHerb;
import herblore.data.tasks.CreatePotion;
import herblore.data.tasks.CreateUnfPotion;
import herblore.data.tasks.Tasks;
import herblore.data.tasks.Tasks.Type;
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
import simple.robot.api.ClientContext;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.HERBLORE, description = "<br>This script will creates potions/cleans herbs<br><br>Start the script in Edgeville with the bank open<br><br>There is 4 modes for this script, AIO, Clean Herb, Make Unf Potion, Make Potion<br>~ AIO ~<br>- Cleans all available herbs<br>- Creates all available unfinished potions<br>- Creates all available final potions<br><br>Please PM me on discord with any bugs", discord = "Datev#0660", name = "KS | AIO Herblore", servers = {
		"Zaros" }, version = "2")

public class HerbloreAIO extends Script implements LoopingScript {

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
			ClientContext.instance().log(e.getMessage());
			e.printStackTrace();
		}
	}

	private enum TYPE {
		ALL_IN_ONE,
		CLEAN_HERB,
		UNFINISHED_POTION,
		FULL_POTION
	}

	private CleanHerb cleanHerb;
	private CreateUnfPotion unfPotion;
	private CreatePotion potion;

	public void addConfig() {
		cleanHerb = new CleanHerb(ctx);
		unfPotion = new CreateUnfPotion(ctx);
		potion = new CreatePotion(ctx);

		Config.TABS.add(new Tabs(0, "Script Config", "Choose your configuration"));

		Config.CONFIGURATION.add(new Config(0, TYPE.class, "ALL_IN_ONE", "Mode", "Choose your herblore mode", "mode"));
		Config.CONFIGURATION.add(new Config(0, Herbs.class, "GUAM_LEAF", "Grimy Herb", "Which herb to clean", "herb"));

		Config.CONFIGURATION
				.add(new Config(0, Herbs.class, "GUAM_LEAF", "Unfinished", "Which unfinished potion to make", "unfinish"));

		Config.CONFIGURATION.add(new Config(0, Potions.class, "ATTACK", "Finished", "Which finished potion to make", "finish"));

		Config.CONFIGURATION.add(new Config(0, boolean.class, false, "Logout on finish", "Logout on finish?", "logout"));

		Config.setConfigChanged(true);
	}

	public void check() {
		Constants.tasks.clear();
		if (getType() != TYPE.ALL_IN_ONE) return;

		int level = ctx.skills.realLevel(Skills.HERBLORE);
		if (!ctx.bank.bankOpen()) {
			ctx.log("==== RERUN SCRIPT WITH BANK OPEN ===");
			return;
		}
		List<Herbs> canClean = Herbs.getCleanable(level);
		List<Herbs> canMakeUnf = Herbs.getUnfMakeable(level);
		List<Potions> canMake = Potions.getMakeable(level);
		ctx.log("[Herb clean] Found %s tasks", canClean.size());
		ctx.log("[Unf potion] Found %s tasks", canMakeUnf.size());
		ctx.log("[Fin potion] Found %s tasks", canMake.size());
		if (canClean.size() > 0) Constants.tasks.add(new Tasks(Type.CLEAN_HERB, canClean));
		if (canMakeUnf.size() > 0) Constants.tasks.add(new Tasks(Type.UNFINISHED_POTION, canMakeUnf));
		if (canMake.size() > 0) Constants.tasks.add(new Tasks(Type.FULL_POTION, canMake));
	}

	public boolean check(Tasks current) {
		if (current.getValid().size() == 0) {
			check();
			return true;
		}
		return false;
	}

	public void setTasks() {
		Constants.tasks.clear();
		if (getType().equals(TYPE.ALL_IN_ONE)) check();
		else if (getType().equals(TYPE.CLEAN_HERB)) Constants.tasks.add(new Tasks(Type.CLEAN_HERB, Arrays.asList(getHerb())));
		else if (getType().equals(TYPE.UNFINISHED_POTION))
			Constants.tasks.add(new Tasks(Type.UNFINISHED_POTION, Arrays.asList(getUnfPot())));
		else if (getType().equals(TYPE.FULL_POTION)) Constants.tasks.add(new Tasks(Type.FULL_POTION, Arrays.asList(getPot())));

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

		if (Config.isConfigChanged()) {
			if (Utils.openBank() && ctx.bank.depositInventory()) {
				setTasks();
				Config.setConfigChanged(false);
			}
			return;
		}

		if (Constants.tasks.size() == 0) {
			Variables.STOP = true;
			Variables.STATUS = "No tasks left";
			Variables.DEBUG = "";
			return;
		}

		Tasks currentTask = Constants.tasks.get(0);

		if (check(currentTask)) return;

		switch (currentTask.getType()) {
			case CLEAN_HERB:
				Herbs h = (Herbs) currentTask.getValid().get(0);
				if (h != null && cleanHerb.process(h)) {
					Constants.tasks.get(0).getValid().remove(0);
					cleanHerb.setStop(false);
				}
				break;
			case FULL_POTION:
				Potions p = (Potions) currentTask.getValid().get(0);
				if (p != null && potion.process(p)) {
					Constants.tasks.get(0).getValid().remove(0);
					potion.setStop(false);
				}
				break;
			case UNFINISHED_POTION:
				Herbs u = (Herbs) currentTask.getValid().get(0);
				if (u != null && unfPotion.process(u)) {
					Constants.tasks.get(0).getValid().remove(0);
					unfPotion.setStop(false);
				}
				break;
		}
	}

	@Override
	public int loopDuration() {
		return 600;
	}

	@Override
	public void onTerminate() {
		if (frame != null) frame.dispose();
		ctx.log("Shutting down.. Thank you for using the script");
		Variables.reset();
		Config.clear();
	}

	public void paint(Graphics Graphs) {
		if (panel != null) panel.update(Variables.STATUS);
	}

	@Override
	public void onChatMessage(ChatMessage e) {

	}

	public void resetVariables() {
		Constants.tasks.clear();
		Constants.reverse = false;
		Variables.reset();
	}

	public TYPE getType() {
		return Config.getValue("mode");
	}

	public Herbs getHerb() {
		return Config.getValue("herb");
	}

	public Herbs getUnfPot() {
		return Config.getValue("unfinish");
	}

	public Potions getPot() {
		return Config.getValue("finish");
	}

}
