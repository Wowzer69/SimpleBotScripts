package api.threads;

import java.util.function.BooleanSupplier;

import api.Tasks;
import api.utils.Utils;
import net.runelite.api.GameState;
import simple.robot.api.ClientContext;

public class PrayerObserver extends Thread {
	private BooleanSupplier condition;
	private ClientContext ctx;

	private long currTick = 0;
	private boolean enabled = false;

	public PrayerObserver(ClientContext ctx, BooleanSupplier condition) {
		this.ctx = ctx;
		this.condition = condition;
		this.setUncaughtExceptionHandler(Utils.handler);
	}

	@Override
	public void run() {

		ctx.log("Running");
		while (ctx.getClient().getGameState() != GameState.LOGGED_IN) {
			ctx.log("Not logged in");
			ctx.sleep(500);
		}

		while (true) {
			if (!condition.getAsBoolean()) {
				ctx.sleep(250);
				continue;
			}

			if (ClientContext.instance().getClient().getSpellSelected()) {
				ctx.sleep(10);
				continue;
			}

			if (ctx.getClient().getTickCount() != currTick) {
				Tasks.getSkill().flickPrayer(!enabled);
				if (!enabled) currTick = ctx.getClient().getTickCount();

				enabled = !enabled;
			}
			ctx.sleep(10);
		}
	}
}
