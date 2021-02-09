package api.tasks;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import api.MenuActions;
import api.Variables;
import net.runelite.api.MenuAction;
import net.runelite.api.VarPlayer;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.simplebot.Game.Tab;
import simple.robot.api.ClientContext;

public class Skill {
	private ClientContext ctx;
	Method action;
	Class<?> _class;

	public Skill(ClientContext ctx) {
		this.ctx = ctx;
	}

	public int getPercentage(Skills skill) {
		float perc = ((float) ctx.skills.level(skill) / ctx.skills.realLevel(skill));
		float perc1 = (perc * 100);
		return (int) perc1;
	}

	public boolean shouldBoost(Skills s) {
		int lvl = ctx.skills.realLevel(s);
		int lvl1 = ctx.skills.level(s);
		int diff = lvl1 - lvl;
		int amt = 3;
		if (s == Skills.ATTACK || s == Skills.STRENGTH || s == Skills.DEFENCE) amt = 12;
		else if (s == Skills.RANGED) amt = 8;
		else if (s == Skills.MAGIC) amt = 2;
		else if (s == Skills.PRAYER) amt = 30;
		return diff == 0 || diff <= amt;
	}

	public void openTab(Tab tab) {
		if (!isTabOpen(tab)) ClientContext.instance().game.tab(tab);
	}

	public boolean isTabOpen(Tab tab) {
		return ClientContext.instance().game.tab().equals(tab);
	}

	public void flickPrayer(boolean enable) {
		try {
			synchronized (Variables.ACTIVE_PRAYERS) {
				Variables.ACTIVE_PRAYERS.forEach(enable ? this::enablePrayer : this::disablePrayer);
			}

		} catch (Exception e) {
			ctx.log("Exception");
		}
	}

	public void enablePrayer(Prayers prayer) {
		if (ctx.skills.level(Skills.PRAYER) == 0) return;
		if (!ctx.prayers.prayerActive(prayer)) MenuActions.invoke("Activate",
				"<col=ff9040>" + prayer.name().toString().replaceAll(" ", "_").toLowerCase() + "</col>", -1,
				MenuAction.CC_OP.getId(), 1, prayer.getWidgetInfo().getId());

	}

	public void disablePrayer(Prayers prayer) {
		if (ctx.prayers.prayerActive(prayer)) MenuActions.invoke("Deactivate",
				"<col=ff9040>" + prayer.name().toString().replaceAll(" ", "_").toLowerCase() + "</col>", -1,
				MenuAction.CC_OP.getId(), 1, prayer.getWidgetInfo().getId());
	}

	public void addPrayer(Prayers prayer) {
		if (Variables.ACTIVE_PRAYERS.contains(prayer)) return;
		if (prayer.name().toLowerCase().contains("protect"))
			Variables.ACTIVE_PRAYERS.removeIf(n -> n.name().toLowerCase().contains("protect"));
		Variables.ACTIVE_PRAYERS.add(prayer);
	}

	public void removePrayer(Prayers prayer) {
		if (Variables.ACTIVE_PRAYERS.contains(prayer)) {
			Variables.ACTIVE_PRAYERS.remove(prayer);
		}
		disablePrayer(prayer);
	}

	public void removeAll() {
		Stream.of(Prayers.values()).forEach(this::removePrayer);
	}

	public void removeAllBut(Prayers prayer) {
		Stream.of(Prayers.values()).filter(p -> p != prayer).forEach(this::removePrayer);
	}

	public void disablePrayers() {
		flickPrayer(false);
		Variables.USE_PRAYER = false;
	}

	public void enablePrayers() {
		flickPrayer(true);
		Variables.USE_PRAYER = true;
	}

	public boolean isPoisonedOrVenomed() {
		return ctx.getClient().getVar(VarPlayer.IS_POISONED) >= 30;
	}

}
