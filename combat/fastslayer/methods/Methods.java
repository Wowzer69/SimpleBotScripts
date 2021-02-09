package combat.fastslayer.methods;

import java.util.regex.Pattern;

import api.Tasks;
import api.Variables;
import api.tasks.Supplies.PotionType;
import api.utils.Utils;
import combat.fastslayer.Core;
import combat.fastslayer.data.Constants;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class Methods {

	private final Pattern SLAYER_RING_PATTERN = Pattern.compile("Slayer ring \\(\\d+\\)");
	private final Pattern RANGED_POTION_PATTERN = Pattern.compile("Ranging potion\\(\\d+\\)");

	private ClientContext ctx;
	private Core main;

	public Methods(ClientContext ctx, Core main) {
		this.ctx = ctx;
		this.main = main;
	}

	public void checkTask() {
		SimpleItem ring = Tasks.getInventory().getItem("slayer ring");
		if (ring == null) return;
		ctx.bank.closeBank();
		ctx.shop.closeShop();
		Variables.STATUS = "Checking slayer task";
		ring.click("Check");
		ctx.sleep(150, 200);
		if (Constants.TASK != null) Constants.CHECK_TASK = true;
	}

	public void goToTask() {
		SimpleItem ring = Tasks.getInventory().getItem("slayer ring");
		if (ring == null) return;
		Variables.STATUS = "Going to slayer task";
		SimpleWidget w = ctx.widgets.getWidget(187, 3);
		if ((w == null || !w.visibleOnScreen()) && !ctx.dialogue.dialogueOpen()) {
			ring.click("Rub");
			ctx.sleep(150, 200);
			ctx.sleepCondition(() -> ctx.dialogue.dialogueOpen(), 2000);
		} else {
			int count = ctx.dialogue.dialogueOpen() ? ctx.dialogue.getDialogueOptions().length : 1;
			count = count > 3 ? 2 : 1;
			ctx.keyboard.sendKeys(count + "", false);
			ctx.sleep(450);
			ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() != -1, 450);
			ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() == -1, 550);
			ctx.sleep(650);
		}
	}

	public void getTask(boolean ten, boolean skip) {
		if (skip && Tasks.getInventory().contains("slayer task skip")) {
			getScrolls();
			return;
		}
		if (ten) {
			getEdgeTask();
		} else {
			getBurthorpeTask();
		}
	}

	public void getEdgeTask() {
		if (!ctx.pathing.inArea(Constants.EDGEVILLE_AREA)) {
			Variables.STATUS = "Teleporting to edgeville";
			if (!Utils.directTeleport("Edgeville")) {
				ctx.magic.castSpellOnce("Home");
			}
		} else {

			SimpleNpc master = ctx.npcs.populate().filter(6797).nearest().next();

			if (master == null || ctx.pathing.distanceTo(master.getLocation()) > 5) {
				Variables.STATUS = "Walking to slayer master";
				ctx.pathing.walkPath(Constants.EDGEVILLE_PATH);
				return;
			}

			if (Constants.TASK == null) {
				Variables.STATUS = "Getting slayer task";
				if (master.click("Assignment")) {
					ctx.sleep(250, 500);
					Constants.CHECK_TASK = false;
					Constants.SKIP = false;
				}
			}
		}
	}

	public void getBurthorpeTask() {
		if (!ctx.pathing.inArea(Constants.BURTHORPE_AREA)) {
			Variables.STATUS = "Teleporting to burthorpe";
			if (!Utils.directTeleport("Burthorpe")) {
				if (main.getTeleport().open()) main.getTeleport().teleportStringPath("Cities", "Burthorpe");
			}
		} else {
			SimpleNpc master = ctx.npcs.populate().filter("Turael").nearest().next();

			if (master == null || ctx.pathing.distanceTo(master.getLocation()) > 5) {
				Variables.STATUS = "Walking to slayer master";
				ctx.pathing.walkPath(Constants.BURTHORPE_PATH);
				return;
			}

			if (Constants.TASK == null) {
				Variables.STATUS = "Getting slayer task";
				if (master.click("Assignment")) {
					ctx.sleep(250, 500);
					Constants.CHECK_TASK = false;
					Constants.SKIP = false;
				}
			}
		}
	}

	public void attack() {
		if (Constants.TASK == null) return;
		SimpleGroundItem casket = ctx.groundItems.populate().filter("Slayer casket").next();
		if (casket != null && casket.validateInteractable()) {
			casket.click("Take");
			return;
		}

		if (Tasks.getSkill().shouldBoost(Skills.RANGED)) Tasks.getSupplies().drink(PotionType.RANGED);
		if (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 50) Tasks.getSupplies().eat();

		if (ctx.players.getLocal().getInteracting() != null) return;
		SimpleNpc target = ctx.npcs.populate().filter(npc -> {
			return validNPC(npc) && !npc.isDead() && ctx.pathing.reachable(npc)
					&& (npc.getInteracting() == null || npc.getInteracting().equals(ctx.players.getLocal().getPlayer()));
		}).filterWithin(15).nearest().next();

		Variables.STATUS = "Attacking monsters";

		if (target != null && target.validateInteractable()) {
			target.click("Attack");
			ctx.sleep(250, 350);
		}
	}

	public boolean requiresBank() {
		int food = ctx.inventory.populate().filterHasAction("Eat").population();
		int ring = ctx.inventory.populate().filter(SLAYER_RING_PATTERN).population();
		int pots = ctx.inventory.populate().filter(RANGED_POTION_PATTERN).population();
		// System.out.println (food + " "+ring +" "+ pots);
		return food == 0 && (ring == 0 && ctx.pathing.inArea(Constants.EDGEVILLE_AREA));
	}

	public boolean inMonsterArea() {
		return Constants.TASK != null && ctx.npcs.populate().filter(npc -> {
			return validNPC(npc);
		}).population() > 0;
	}

	public boolean validNPC(SimpleNpc npc) {
		return npc != null && npc.getName() != null && (npc.getName().toLowerCase().contains(Constants.TASK.getName())
				|| Constants.TASK.getName().contains(npc.getName().toLowerCase()));
	}

	public void bank() {
		if (!ctx.pathing.inArea(Constants.EDGEVILLE_AREA)) {
			Variables.STATUS = "Teleporting to edgeville";
			if (!Utils.directTeleport("Edgeville")) {
				ctx.magic.castSpellOnce("Home");
			}
			return;
		}

		if (!Tasks.getBanking().heal()) {
			if (!Tasks.getSupplies().hasFood()) {
				Tasks.getBanking().usePreset();
			} else if (!Tasks.getInventory().contains("slayer ring")) {
				if (withdraw("slayer ring")) ctx.bank.closeBank();
			}

		}
	}

	public boolean withdraw(String name) {
		if (Tasks.getBanking().open()) {
			SimpleItem item = Tasks.getBanking().getItem(name);
			if (item == null) {
				Variables.STOP = true;
				return false;
			}
			return ctx.bank.withdraw(item.getId(), 1);
		}
		return false;
	}

	public boolean shouldSkipTask() {
		return Constants.SKIP_TASKS.stream().anyMatch(line -> Constants.TASK.getName().toLowerCase().contains(line));
	}

	public void getScrolls() {
		if (!ctx.pathing.inArea(Constants.EDGEVILLE_AREA)) {
			Variables.STATUS = "Teleporting to edgeville";
			if (!Utils.directTeleport("Edgeville")) {
				ctx.magic.castSpellOnce("Home");
			}
		} else {
			Variables.STATUS = "Getting slayer scrolls";
			if (Tasks.getBanking().open()) {
				ctx.bank.depositInventory();
				if (!Tasks.getBanking().contains("slayer skip scroll")) {
					Variables.STOP = true;
					return;
				}
				withdraw("slayer skip scroll");
				withdraw("slayer ring");
				ctx.sleep(750, 1000);
				ctx.bank.closeBank();
			}
		}

	}

}
