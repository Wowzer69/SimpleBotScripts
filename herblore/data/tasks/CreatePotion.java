package herblore.data.tasks;

import herblore.data.Potions;
import lombok.Setter;
import simple.api.Timer;
import simple.api.Utils;
import simple.api.Variables;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class CreatePotion {

	private ClientContext ctx;

	public CreatePotion(ClientContext ctx) {
		this.ctx = ctx;
	}

	private Potions current = null;
	private Timer lastAnimation = new Timer(1);
	@Setter
	private boolean stop = false;

	public boolean process(Potions valid) {
		current = valid;
		if (ctx.players.getLocal().getAnimation() == 363) lastAnimation = new Timer(2000);
		if (!lastAnimation.isRunning() && getIngredients()) createPotion();
		return stop;
	}

	public boolean hasItems(boolean both) {
		boolean herbs = ctx.inventory.populate().filter(current.getReqHerb().getUnfId()).population() > 0;
		boolean vial = ctx.inventory.populate().filter(current.getIngredientId()).population() > 0;

		if (both) {
			if (!herbs) herbs = ctx.bank.populate().filter(current.getReqHerb().getUnfId()).population(true) > 0;
			if (!vial) vial = ctx.bank.populate().filter(current.getIngredientId()).population(true) > 0;
		}
		return herbs && vial;
	}

	public void createPotion() {
		if (ctx.bank.bankOpen()) {
			ctx.bank.closeBank();
			ctx.sleepCondition(() -> ctx.bank.bankOpen(), 500);
		}
		Variables.STATUS = "Making " + current.getName() + " potion";
		Variables.DEBUG = "[FIN] " + current.getName();
		SimpleItem vial = ctx.inventory.populate().filter(current.getReqHerb().getUnfId()).next();
		SimpleItem herb = ctx.inventory.populate().filter(current.getIngredientId()).reverse().next();

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
			Variables.STATUS = "Withdrawing items";
			ctx.bank.depositAllExcept(current.getReqHerb().getUnfId(), current.getIngredientId());
			if (!hasItems(true)) {
				stop = true;
				return false;
			}
			int amt1 = ctx.inventory.populate().filter(current.getIngredientId()).population(true);
			int amt2 = ctx.inventory.populate().filter(current.getReqHerb().getUnfId()).population(true);

			ctx.sleep(150, 250);
			ctx.bank.withdraw(current.getIngredientId(), 14 - amt1);
			ctx.bank.withdraw(current.getReqHerb().getUnfId(), 14 - amt2);
			ctx.sleep(450, 750);
		}
		return hasItems(false);
	}

}
