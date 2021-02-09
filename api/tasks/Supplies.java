package api.tasks;

import api.Tasks;
import api.Variables;
import api.simple.KSItem;
import api.utils.Timer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import simple.robot.api.ClientContext;

public class Supplies {

	@AllArgsConstructor
	public enum PotionType {
		ATTACK(new String[] { "attack", "combat" }),
		STRENGTH(new String[] { "strength", "combat" }),
		DEFENCE(new String[] { "defence", "combat" }),
		MAGE(new String[] { "magic" }),
		RANGED(new String[] { "ranging" }),
		ANTIPOISON(new String[] { "antipoison", "antidote" }),
		PRAYER(new String[] { "prayer", "restore" }),
		ANTIFIRE(new String[] { "antifire" }),
		ENERGY(new String[] { "energy", "stamina" }),;

		@Getter
		private String[] value;
	}

	private ClientContext ctx;

	public Supplies(ClientContext ctx) {
		this.ctx = ctx;
	}

	public Timer antiFire = new Timer(1);
	public Timer antiPoison = new Timer(1);

	public boolean drink(PotionType type) {
		if (type.equals(PotionType.ANTIFIRE) && antiFire.isRunning()) return true;

		KSItem potion = new KSItem(Tasks.getInventory().getItem(type.getValue()));
		if (potion.isNull()) return false;
		Variables.STATUS = "Drinking " + type.toString() + " potion";
		if (potion.click("Drink")) {
			if (type.equals(PotionType.ANTIFIRE)) antiFire = new Timer(200000);
			ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != -1);
			ctx.onCondition(() -> ctx.players.getLocal().getAnimation() == -1);
			return true;
		}
		return false;
	}

	public boolean eat() {
		KSItem food = new KSItem(ctx.inventory.populate().filterHasAction("Eat").next());
		if (food.isNull()) return false;

		Variables.STATUS = "Eating food";
		if (food.click("Eat")) {
			ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != -1);
			ctx.onCondition(() -> ctx.players.getLocal().getAnimation() == -1);
			return true;
		}
		return false;
	}

	public boolean eat(String name) {
		KSItem food = new KSItem(Tasks.getInventory().getItem(name));
		if (food.isNull()) return false;

		Variables.STATUS = "Eating food";
		if (food.click("Eat")) {
			ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != -1);
			ctx.onCondition(() -> ctx.players.getLocal().getAnimation() == -1);
			return true;
		}
		return false;
	}

	public boolean hasFood() {
		return ctx.inventory.populate().filterHasAction("Eat").population() > 0;
	}

	public boolean hasPrayer() {
		return Tasks.getInventory().contains("prayer", "restore", "sanfew");
	}

}
