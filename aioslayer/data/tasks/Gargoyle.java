package aioslayer.data.tasks;

import aioslayer.data.Constants;
import aioslayer.data.MonsterTask;
import api.Tasks;
import api.Variables;
import net.runelite.api.MenuAction;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.robot.api.ClientContext;

public class Gargoyle implements MonsterTask {

	@Override
	public String getName() {
		return "Gargoyles";
	}

	@Override
	public String[] lootList() {
		return new String[] { "Slayer casket", "Granite maul", "Coins" };
	}

	@Override
	public int[] getId() {
		return new int[] { NpcID.GARGOYLE, NpcID.GARGOYLE_1543, NpcID.GARGOYLE_413 };
	}

	@Override
	public void travel() {
		Tasks.getSlayer().useRing("tower");
	}

	@Override
	public void attack() {
		if (!Tasks.getLoot().loot(lootList())) {
			SimpleNpc npc = Tasks.getCombat().getAggressiveNPC(Constants.TASK.getId());
			if (npc != null && npc.getHealthRatio() > -1 && npc.getHealthRatio() <= 3) {
				Variables.STATUS = "Smashing...";
				SimpleItem hammer = Tasks.getInventory().getItem("Rock hammer");
				if (!Tasks.getMenuAction().get(hammer, "Use").invoke()) return;

				if (Tasks.getMenuAction().get(npc, "Attack").setMenuAction(MenuAction.ITEM_USE_ON_NPC).invoke())
					ClientContext.instance().sleep(350);

			} else {
				Tasks.getSlayer().fightNpc();
			}
		}
	}

	@Override
	public boolean atLocation() {
		return Constants.TASK != null && !Tasks.getCombat().isMonsterGoneForAWhile(getId());
	}

	@Override
	public void equipGear() {
		// TODO Auto-generated method stub

	}

	public String[] areas() {
		return new String[] { "Slayer Tower", "Slayer Tower (basement)" };
	}

	@Override
	public Prayers getProtection() {
		return Prayers.PROTECT_FROM_MELEE;
	}
}
