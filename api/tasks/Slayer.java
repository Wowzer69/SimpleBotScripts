package api.tasks;

import aioslayer.data.Constants;
import api.Locations;
import api.MenuActions;
import api.Tasks;
import api.Variables;
import api.panel.Config;
import api.simple.KSItem;
import api.simple.KSNPC;
import api.utils.Utils;
import api.utils.Weapons;
import net.runelite.api.MenuAction;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class Slayer {

	private ClientContext ctx;

	public Slayer(ClientContext ctx) {
		this.ctx = ctx;
	}

	public void handleTask() {
		if (Constants.SHOULD_CHECK_TASK) {
			checkTask();
			return;
		}

		Tasks.getInventory().equipAll("defender", "whip", "scimitar", "ket-xil", "dragonfire shield");

		if (Constants.TASK == null) {
			if (Constants.SHOULD_SKIP && !getSkipScrolls()) return;
			if (!Constants.MASTER.atLocation()) {
				Constants.MASTER.travel();
				return;
			}
			KSNPC master = new KSNPC(ctx.npcs.populate().filter(Constants.MASTER.getId()).nearest().next());

			if (master.isNull()) return;

			ctx.bank.closeBank();
			Variables.STATUS = "Getting slayer task";
			if (ctx.pathing.distanceTo(master.getLocation()) > 3) {
				ctx.pathing.step(master.getLocation());
				ctx.onCondition(() -> ctx.pathing.distanceTo(master.getLocation()) < 4);
				return;
			}
			if (master.click("Assignment")) {
				ctx.onCondition(() -> ctx.dialogue.dialogueOpen());
				Constants.SHOULD_CHECK_TASK = true;
				Constants.SHOULD_SKIP = false;
			}
		}
	}

	public void useRing(String... option) {
		Variables.STATUS = "Attempting to travel";
		SimpleItem ring = Tasks.getInventory().getItem("slayer ring");
		if (ring == null) {
			getRing();
			return;
		}
		ctx.bank.closeBank();
		Variables.STATUS = "Going to slayer task";
		SimpleWidget teleportInterface = ctx.widgets.getWidget(187, 3);

		if (!Utils.validWidget(teleportInterface) && !ctx.dialogue.dialogueOpen()) {
			ctx.bank.closeBank();
			if (Tasks.getMenuAction().get(ring, "Rub").invoke())
				ctx.onCondition(() -> ctx.dialogue.dialogueOpen() || Utils.validWidget(teleportInterface));
		} else {

			String task = Constants.TASK.getName().toLowerCase();
			Config conf = Config.getItem(task);
			if (conf == null) {
				Variables.STATUS = "Unable to find config. " + task;
				return;
			}
			int opt = Tasks.getToken().getOption((String) conf.getValue());
			if (opt == -1) {
				Variables.STATUS = "Invalid location. " + conf.getValue();
				return;
			}
			if (opt == 0) opt++;
			ctx.keyboard.sendKeys(opt + "", false);
			ctx.sleep(450);
			ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() != -1, 450);
			ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() == -1, 550);
			ctx.sleep(650);
		}
	}

	private void checkTask() {
		KSItem ring = new KSItem(Tasks.getInventory().getItem("slayer ring"));
		boolean helm = Tasks.getInventory().isWearing(EquipmentSlot.HELMET);
		if (helm) {
			Variables.STATUS = "Checking slayer task";
			MenuActions.invoke("", "", -1, MenuAction.CC_OP.getId(), 4, 25362446);
			ctx.onCondition(() -> !Constants.SHOULD_CHECK_TASK);
		} else if (ring.isNull()) {
			getRing();
			return;
		}
		ctx.bank.closeBank();
		ctx.shop.closeShop();
		Variables.STATUS = "Checking slayer task";
		if (ring.click("Check")) ctx.onCondition(() -> !Constants.SHOULD_CHECK_TASK);
	}

	private boolean getSkipScrolls() {
		if (Tasks.getInventory().contains("slayer task skip")) return true;
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			Variables.STATUS = "Teleporting to edgeville";
			ctx.magic.castSpellOnce("Home");
			return false;
		}
		Variables.STATUS = "Getting slayer scrolls";
		return Tasks.getBanking().withdrawItem("Slayer task skip", 10);
	}

	private boolean getRing() {
		if (Tasks.getInventory().contains("slayer ring")) return true;
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			Variables.STATUS = "Teleporting to edgeville";
			ctx.magic.castSpellOnce("Home");
			return false;
		}
		Variables.STATUS = "Getting slayer ring";
		return Tasks.getBanking().withdrawItem("slayer ring");
	}

	public void fightNpc() {
		if (Constants.TASK == null) return;
		Tasks.getCombat().checkPots();
		Tasks.getSkill().addPrayer(Constants.TASK.getProtection());

		if (Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "crossbow")) Tasks.getSkill().addPrayer(Prayers.EAGLE_EYE);
		else addMeleePray();

		if (ctx.players.getLocal().getInteracting() != null) {
			Variables.STATUS = "Fighting...";
			return;
		}

		SimpleNpc npc = new KSNPC(Constants.TASK.getId()).inDistance(10).isAggressive(true).get();

		if (npc == null) npc = Tasks.getCombat().getAggressiveNPC(Constants.TASK.getId());
		if (npc == null) npc = Tasks.getCombat().getNPC(true, Constants.TASK.getId());
		if (npc == null) npc = Tasks.getCombat().getMultiNpc(Constants.TASK.getId());
		if (npc != null) {
			Weapons.SPECIAL_WEAPON = Weapons.getSpecialWeapon();
			if (Weapons.canSpecial(false) || Weapons.canSpecial(true)) Tasks.getCombat().useSpecialAttack(npc);
			else Tasks.getCombat().attack(npc);
		}
	}

	private void addMeleePray() {
		int prayerLvl = ctx.skills.realLevel(Skills.PRAYER);
		if (prayerLvl >= 31 && prayerLvl < 70) {
			Tasks.getSkill().addPrayer(Prayers.ULTIMATE_STRENGTH);
			Tasks.getSkill().addPrayer(Prayers.INCREDIBLE_REFLEXES);
		} else {
			Tasks.getSkill().addPrayer(Prayers.PIETY);
		}
	}

	public boolean shouldSkip(String name) {
		boolean slayerHelm = Tasks.getInventory().isWearing(EquipmentSlot.HELMET, "slayer");
		boolean rockHammer = Tasks.getInventory().contains("Rock hammer");
		boolean antiDragonShield = Tasks.getInventory().contains("dragon shield", "dragonfire")
				|| Tasks.getInventory().isWearing(EquipmentSlot.SHIELD, "dragonfire", "dragon shield");

		boolean leafSword = Tasks.getInventory().contains("Leaf-bladed")
				|| Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "Leaf-bladed");

		switch (name.toLowerCase()) {
			case "aberrant spectres":
			case "dust devils":
			case "smoke devils":
				return !slayerHelm;
			case "gargoyles":
				return !rockHammer;
			case "blue dragons":
			case "black dragons":
			case "iron dragons":
			case "red dragons":
			case "steel dragons":
				return !antiDragonShield;
			case "turoth":
			case "kurask":
				return !leafSword;
			case "adamant dragon":
			case "aviansie":
			case "rune dragons":
				// case "brine rat":
			case "cave kraken":
			case "drakes":
			case "fossil island wyverns":
			case "lizardmen":
				// case "minions of scabaras":
			case "mithril dragons":
			case "mutated Zygomites":
			case "rune dragon":
			case "skeletal wyverns":
			case "spiritual creatures":
			case "tzhaar":
			case "vampyre":
			case "wyrms":
			case "kalphites":
			case "mutated zygomites":
				return true;
		}
		return false;
	}

	public boolean shouldBank() {
		return !Tasks.getInventory().contains("antifire") || !Tasks.getInventory().contains("prayer", "restore", "sanfew")
				|| Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 20 || Variables.FORCE_BANK;
	}
}
