package herblore.data.tasks;

import herblore.data.Herbs;
import lombok.Setter;
import simple.api.Utils;
import simple.api.Variables;
import simple.hooks.queries.SimpleItemQuery;
import simple.hooks.wrappers.SimpleItem;
import simple.robot.api.ClientContext;

public class CleanHerb {

	private ClientContext ctx;

	public CleanHerb(ClientContext ctx) {
		this.ctx = ctx;
	}

	private Herbs current = null;
	@Setter
	private boolean stop = false;

	public boolean process(Herbs valid) {
		current = valid;

		if (getHerbs()) {
			cleanHerb();
		}

		return stop;
	}

	public boolean hasItems(boolean both) {
		boolean herbs = ctx.inventory.populate().filter(current.getGrimyId()).population() > 0;

		if (both) {
			if (!herbs) herbs = ctx.bank.populate().filter(current.getGrimyId()).population(true) > 0;
		}
		return herbs;
	}

	public void cleanHerb() {
		if (ctx.bank.bankOpen()) {
			ctx.bank.closeBank();
			ctx.sleepCondition(() -> ctx.bank.bankOpen(), 500);
		}
		Variables.STATUS = "Cleaning herbs";
		Variables.DEBUG = "[CLEAN] " + current.getName();
		SimpleItemQuery<SimpleItem> query = ctx.inventory.populate().filter(current.getGrimyId());

		query.forEach(item -> {
			if (item != null) item.click(0);
			ctx.sleep(250, 350);
		});
	}

	public boolean getHerbs() {
		if (hasItems(false)) return true;
		if (Utils.openBank()) {
			Variables.STATUS = "Withdrawing items";
			ctx.bank.depositAllExcept(current.getGrimyId());
			if (!hasItems(true)) {
				stop = true;
				return false;
			}
			ctx.sleep(150, 250);
			ctx.bank.withdraw(current.getGrimyId(), 28);
			ctx.sleep(450, 750);
		}
		return hasItems(false);
	}

}
