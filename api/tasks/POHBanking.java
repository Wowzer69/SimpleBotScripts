package api.tasks;

import api.Tasks;
import api.Variables;
import api.simple.KSItem;
import api.simple.KSObject;
import api.utils.Banks;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.ObjectID;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;

public class POHBanking {

	private ClientContext ctx;

	public POHBanking(ClientContext ctx) {
		this.ctx = ctx;
	}

	@Getter
	@Setter
	private Banks currentBank = null;

	public boolean openBank() {
		if (currentBank == null) currentBank = Banks.getRandomBank(false);

		return currentBank.teleport() && currentBank.bank(true);
	}

	public boolean usePreset(boolean slayer) {
		if (currentBank == null) currentBank = Banks.getRandomBank(true);

		if (currentBank.teleport() && currentBank.bank(false)) {
			boolean slay = slayer && !Tasks.getInventory().contains("Slayer casket");
			boolean other = !slayer && (Tasks.getSupplies().hasFood() && Tasks.getSupplies().hasPrayer());

			KSObject bank = (KSObject) currentBank.get();

			if (bank.isNull()) {
				ctx.log("BANK is null");
				return false;
			}

			if ((slay || other) && !Variables.FORCE_BANK) return true;
			Variables.STATUS = "Getting last preset";
			if (ctx.inventory.itemSelectionState() == 1) ctx.inventory.populate().next().click(0);
			if (bank.click("Last-preset")) {
				ctx.onCondition(() -> Variables.LAST_MESSAGE.contains("preset"), 6000);
				Variables.FORCE_BANK = false;
				return true;
			}
		}
		return false;
	}

	public SimpleObject getPool() {
		return ctx.objects
				.populate().filter(ObjectID.FANCY_REJUVENATION_POOL, ObjectID.POOL_OF_REVITALISATION,
						ObjectID.POOL_OF_RESTORATION, ObjectID.ORNATE_REJUVENATION_POOL, ObjectID.POOL_OF_REJUVENATION)
				.nearest().next();
	}

	public boolean teleportHome() {
		if (!ctx.getClient().isInInstancedRegion()) {
			KSItem tab = new KSItem(Tasks.getInventory().getItem("Teleport to house"));
			if (tab.isNull()) {
				ctx.log("Out of teleports");
				ctx.sleep(15000);
				return false;
			}
			if (tab.click("Break")) ctx.onCondition(() -> ctx.getClient().isInInstancedRegion(), 5000);
		}
		return ctx.getClient().isInInstancedRegion();
	}

	public boolean usePool() {
		if (Tasks.getBanking().isFull()) return true;

		if (!teleportHome()) return false;
		SimpleObject box = getPool();
		if (box == null) return false;
		if (box.distanceTo(ctx.players.getLocal()) > 5) {
			Variables.STATUS = "Walking to pool";
			ctx.pathing.step(box.getLocation());
			ctx.sleep(450, 650);
			return false;
		}
		Variables.STATUS = "Refilling hitpoints";
		if (box.validateInteractable() && box.click("Drink")) ctx.onCondition(() -> Tasks.getBanking().isFull());
		return false;
	}
}
