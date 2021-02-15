package barrow;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import api.Locations;
import api.Tasks;
import api.Variables;
import api.tasks.Supplies.PotionType;
import api.utils.Utils;
import barrow.methods.Brothers;
import barrow.methods.Constants;
import barrow.methods.ScriptUtils;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.MINIGAMES, description = "Barrows", discord = "", name = "Barrows", servers = {
		"Zaros" }, version = "2")
public class Barrows extends Script implements LoopingScript {
	final String[] STOP_MESSAGES = { "be found: prayer potion", "be found: shark", "enough ammo", "you are dead" };

	@Override
	public void onChatMessage(ChatMessage msg) {
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (msg.getMessage().contains("opened the barrows chest")) {
				Variables.COUNT++;
				Brothers.reset();
			}
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
			}
		}
	}

	@Getter
	private ScriptUtils utils;

	@Override
	public void onExecute() {
		Tasks.init(ctx);
		utils = new ScriptUtils(ctx, this);
		Brothers.reset();
		Brothers.update();

		SimpleItem _switch = ctx.inventory.populate().filterHasAction("Wield").next();
		if (_switch != null) Constants.WEAPON_SWITCH = _switch.getName();

		Variables.STARTED = true;
	}

	@Override
	public void onProcess() {
		if (utils.checkWeapon()) {
			Variables.STATUS = "Filling staff";
			utils.fillStaff();
			return;
		}

		ctx.combat.toggleAutoRetaliate(true);
		if (ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			Tasks.getSkill().removeAll();
			if (!Tasks.getBanking().heal()) return;
			if (!Tasks.getBanking().usePreset()) return;
			if (Utils.directTeleport("Barrows")) return;
			if (Tasks.getTeleporter().open()) Tasks.getTeleporter().teleportStringPath("Minigames", "Barrows");

		} else if (ctx.pathing.inArea(Locations.BARROWS_HILLS)) {
			Tasks.getSkill().removeAll();
			Brothers next = Brothers.getNextBrother();
			if (next == null) return;
			if (!ctx.pathing.inArea(next.getDigArea())) {
				Variables.STATUS = "Walking to dig location";
				ctx.pathing.step(next.getDigArea().randomTile());
				ctx.onCondition(() -> ctx.pathing.inArea(next.getDigArea()), 1000, 3);
			} else {
				Variables.STATUS = "Digging";
				utils.dig();
			}
		} else if (ctx.pathing.plane() == 3 || ctx.pathing.inArea(Locations.BARROWS_FINAL_SARCO)) {
			boolean _final = ctx.pathing.inArea(Locations.BARROWS_FINAL_SARCO);
			Brothers current = utils.currentBrother();

			if (_final && Brothers.getTotalKilled() == 0) {
				ctx.magic.castSpellOnce("Home");
				return;
			}
			if (!_final && (current != null && current.isKilled())
					|| (Brothers.getTotalKilled() < 5 && current.equals(Brothers.getTunnelBrother()))) {
				Tasks.getSkill().removeAll();
				utils.exitCrypt();
				return;
			}

			if (ctx.dialogue.dialogueOpen() && Brothers.getTotalKilled() == 5) {
				if (!ctx.dialogue.canContinue()) {
					Variables.STATUS = "Clicking continue again";
					ctx.dialogue.clickDialogueOption(1);
					ctx.sleep(350, 700);
				} else {
					Variables.STATUS = "Clicking continue";
					ctx.dialogue.clickContinue();
					ctx.sleep(150, 300);
				}
				return;
			}
			if (Tasks.getSkill().getPercentage(Skills.PRAYER) < 20) Tasks.getSupplies().drink(PotionType.PRAYER);
			if (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 50) Tasks.getSupplies().eat();

			SimpleNpc npc = utils.aggressiveNPC();
			if (current == null || npc == null) {
				Variables.STATUS = "Opening coffin";
				utils.searchCoffin("Search");
				ctx.sleep(150, 450);
				return;
			}

			if (npc.isDead()) {
				utils.exitCrypt();
				return;
			}

			Tasks.getSkill().enablePrayer(current.getPrayer());
			boolean ahrim = current.equals(Brothers.AHRIM);
			if (ahrim) Tasks.getSupplies().drink(PotionType.RANGED);
			Tasks.getInventory().equip(ahrim ? Constants.WEAPON_SWITCH : "Trident of the seas");

			if (ctx.players.getLocal().getInteracting() == null || ctx.players.getLocal().getInteracting() != npc
					|| !ctx.players.getLocal().inCombat())
				Tasks.getCombat().attack(npc);

		} else {
			Variables.STATUS = "Not sure";
			// Variables.STOP = true;
		}

	}

	@Override
	public int loopDuration() {
		return 60;
	}

	@Override
	public void onTerminate() {
		ctx.log("Shutting down.. Thank you for using the script");
	}

	@Override
	public void paint(Graphics Graphs) {
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.BLACK);
		g.fillRect(5, 5, 200, 60);
		g.setColor(Color.GREEN);
		g.drawRect(5, 5, 200, 60);
		g.setColor(Color.CYAN);
		g.drawString("Total run time: " + Variables.START_TIME.toElapsedString(), 7, 20);
		g.drawString(String.format("%s: %s", "Status", Variables.STATUS), 7, 30);
		g.drawString(String.format("Chests: %s (%s /hr)", Variables.COUNT,
				ctx.paint.valuePerHour((int) Variables.COUNT, Variables.START_TIME.start)), 7, 45);

		g.drawString("In Area: " + ctx.pathing.inArea(Locations.BARROWS_HILLS), 7, 60);
	}

}
