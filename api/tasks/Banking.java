package api.tasks;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import api.Tasks;
import api.Variables;
import api.simple.KSObject;
import api.utils.Timer;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;

public class Banking {

	private ClientContext ctx;

	public Banking(ClientContext ctx) {
		this.ctx = ctx;
	}

	public SimpleObject getBank() {
		return ctx.objects.populate().filter(10355, 26707).nearest().next();
	}

	public boolean open() {
		if (ctx.bank.bankOpen()) return true;

		KSObject bank = new KSObject(getBank());
		if (bank.isNull()) return false;
		if (bank.distanceTo(ctx.players.getLocal()) > 5) {
			Variables.STATUS = "Walking to bank";
			ctx.pathing.step(bank.getLocation());
			ctx.onCondition(() -> ctx.pathing.inMotion(), 250);
			return false;
		}

		Variables.STATUS = "Opening bank";
		String action = bank.getName().contains("chest") ? "Use" : "Bank";

		if (bank.click(action)) ctx.sleepCondition(() -> ctx.bank.bankOpen());
		return false;
	}

	public boolean usePreset() {
		return usePreset(false);
	}

	public boolean usePreset(boolean slayer) {
		KSObject bank = new KSObject(getBank());
		if (bank.isNull()) return true;

		boolean slay = slayer && !Tasks.getInventory().contains("Slayer casket");
		boolean other = !slayer && (Tasks.getSupplies().hasFood() && Tasks.getSupplies().hasPrayer());

		if ((slay || other) && !Variables.FORCE_BANK) return true;
		if (ctx.pathing.distanceTo(bank.getLocation()) > 5) {
			Variables.STATUS = "Walking to bank";
			ctx.pathing.step(bank.getLocation());
			ctx.onCondition(() -> ctx.pathing.distanceTo(bank.getLocation()) < 4, 150);
			return false;
		}
		Variables.STATUS = "Getting last preset";
		if (ctx.inventory.itemSelectionState() == 1) ctx.inventory.populate().next().click(0);
		if (bank.click("Last-preset")) {
			ctx.onCondition(() -> Variables.LAST_MESSAGE.contains("preset"), 6000);
			Variables.FORCE_BANK = false;
		}
		return true;
	}

	public boolean isFull() {
		return ctx.skills.level(Skills.HITPOINTS) == ctx.skills.realLevel(Skills.HITPOINTS)
				&& ctx.skills.level(Skills.PRAYER) == ctx.skills.realLevel(Skills.PRAYER);
	}

	public boolean heal() {
		if (isFull()) return true;

		KSObject box = new KSObject(ctx.objects.populate().filter(60003).nearest().next());
		if (box.isNull()) return false;
		if (box.distanceTo(ctx.players.getLocal()) > 5) {
			Variables.STATUS = "Walking to heal chest";
			ctx.pathing.step(box.getLocation());
			ctx.sleep(450, 650);
			return false;
		}
		Variables.STATUS = "Refilling hitpoints";
		if (box.click("Heal")) ctx.onCondition(() -> !ctx.pathing.inMotion());
		return false;
	}

	public boolean withdrawItem(String name) {
		return withdrawItem(name, 1);
	}

	public boolean withdrawItem(String name, int amount) {
		if (open()) {
			SimpleItem item = Tasks.getBanking().getItem(name);
			if (item == null) {
				ctx.log("Unable to find: " + name);
				Variables.STOP = true;
				ctx.bank.closeBank();
				return false;
			}
			if (!Variables.USE_PACKETS) return ctx.bank.withdraw(name, amount);
			return Tasks.getMenuAction().withdraw(item, amount + "");
		}
		return false;
	}

	public boolean containsAll(int... itemId) {
		List<Integer> inv = ctx.bank.populate().toStream().map(item -> item.getId()).collect(Collectors.toList());
		return itemId.length == Arrays.stream(itemId).filter(inv::contains).count();
	}

	public boolean containsAll(String... itemName) {
		List<String> inv = ctx.bank.populate().toStream().map(item -> item.getName()).map(String::toLowerCase)
				.collect(Collectors.toList());
		return itemName.length == Arrays.stream(itemName).map(String::toLowerCase)
				.filter(val -> inv.stream().anyMatch(arr -> arr.contains(val))).count();
	}

	public boolean contains(String... itemName) {
		return !ctx.bank.populate().filter(p -> Tasks.getInventory().predicate(p, itemName)).isEmpty();
	}

	public SimpleItem getItem(String... itemName) {
		return ctx.bank.populate().filter(p -> Tasks.getInventory().predicate(p, itemName)).next();
	}

	private Timer altarTimer = new Timer(1);

	public void prayAtAltar(String name) {
		if (Tasks.getSkill().getPercentage(Skills.PRAYER) > 60 || altarTimer.isRunning()) return;
		KSObject altar = new KSObject(ctx.objects.populate().filter(name).next());
		if (altar.isNull()) return;
		if (altar.click("Pray")) {
			ctx.onCondition(() -> Tasks.getSkill().getPercentage(Skills.PRAYER) >= 95, 5000);
			altarTimer.setEndIn(600000);
		}
	}

}
