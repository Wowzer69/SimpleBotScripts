package herblore.data.tasks;

import herblore.data.Herbs;
import lombok.Setter;
import net.runelite.api.ItemID;
import simple.api.Timer;
import simple.api.Utils;
import simple.api.Variables;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class CreateUnfPotion {

	private ClientContext ctx;

	public CreateUnfPotion(ClientContext ctx) {
		this.ctx = ctx;
	}

	private Herbs current = null;
	private Timer lastAnimation = new Timer(1);
	@Setter
	private boolean stop = false;

	public boolean process(Herbs valid) {
		current = valid;
		if (ctx.players.getLocal().getAnimation() == 363) lastAnimation = new Timer(2000);
		if (!lastAnimation.isRunning() && getIngredients()) createPotion();
		return stop;
	}

	public boolean hasItems(boolean both) {
		boolean herbs = ctx.inventory.populate().filter(current.getCleanId()).population() > 0;
		boolean vial = ctx.inventory.populate().filter(ItemID.VIAL_OF_WATER).population() > 0;

		if (both) {
			if (!herbs) herbs = ctx.bank.populate().filter(current.getCleanId()).population(true) > 0;
			if (!vial) vial = ctx.bank.populate().filter(ItemID.VIAL_OF_WATER).population(true) > 0;
		}
		return herbs && vial;
	}

	public void createPotion() {
		if (ctx.bank.bankOpen()) {
			ctx.bank.closeBank();
			ctx.sleepCondition(() -> ctx.bank.bankOpen(), 500);
		}
		Variables.STATUS = "Making unfinished " + current.getName();
		Variables.DEBUG = "[UNF] " + current.getName();
		SimpleItem vial = ctx.inventory.populate().filter(ItemID.VIAL_OF_WATER).next();
		SimpleItem herb = ctx.inventory.populate().filter(current.getCleanId()).reverse().next();

		SimpleWidget w = ctx.widgets.getWidget(270, 14);

		if (w != null && !w.isHidden()) {
			if (w.click(0)) {
				ctx.sleep(450, 750);
				lastAnimation = new Timer(2000);
			}

		} else if (herb != null && vial != null && herb.click(0)) {
			vial.click(0);
			ctx.sleep(250, 350);
		}
	}

	public boolean getIngredients() {
		if (hasItems(false)) return true;
		if (Utils.openBank()) {
			ctx.bank.depositAllExcept(current.getCleanId(), ItemID.VIAL_OF_WATER);
			Variables.STATUS = "Withdrawing items";
			if (!hasItems(true)) {
				stop = true;
				return false;
			}
			int amt1 = ctx.inventory.populate().filter(ItemID.VIAL_OF_WATER).population(true);
			int amt2 = ctx.inventory.populate().filter(current.getCleanId()).population(true);
			ctx.sleep(150, 250);
			ctx.bank.withdraw(ItemID.VIAL_OF_WATER, 14 - amt1);
			ctx.bank.withdraw(current.getCleanId(), 14 - amt2);
			ctx.sleep(450, 750);
		}
		return hasItems(false);
	}

}
