package api.tasks;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import api.MenuActions;
import api.Tasks;
import api.Variables;
import api.simple.KSNPC;
import api.tasks.Supplies.PotionType;
import api.utils.Timer;
import api.utils.Weapons;
import api.utils.Weapons.SPECIAL_WEAPONS;
import lombok.Getter;
import net.runelite.api.NpcID;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class Combat {

	private ClientContext ctx;

	Predicate<SimpleNpc> GET_AGGRESSOR = npc -> !npc.isDead() && ctx.pathing.reachable(npc)
			&& ctx.pathing.distanceTo(npc.getLocation()) < 10
			&& (npc.getInteracting() == null || npc.getInteracting().equals(ctx.players.getLocal().getPlayer())
					|| (npc.getInteracting() != null && !npc.inCombat()));

	public Combat(ClientContext ctx) {
		this.ctx = ctx;
	}

	public SimpleNpc getNPC(String... npcName) {
		return ctx.npcs.populate().filter(npcName).filter(npc -> npc != null && !npc.isDead()).nearest().next();
	}

	public SimpleNpc getNPC(int... npcId) {
		return ctx.npcs.populate().filter(npcId).filter(npc -> npc != null && !npc.isDead()).nearest().next();
	}

	public SimpleNpc getNPC(boolean filter, String... npcName) {
		return ctx.npcs.populate().filter(npcName).filter(GET_AGGRESSOR).nearest().next();
	}

	public SimpleNpc getNPC(boolean filter, int... npcId) {
		SimpleEntityQuery<SimpleNpc> query = ctx.npcs.populate();
		if (filter) query = query.filter(GET_AGGRESSOR);

		return query.nearest().next();
	}

	public SimpleNpc getMultiNpc(int[] ids) {
		SimpleEntityQuery<SimpleNpc> npcs = ctx.npcs.populate().filter(ids).filter(n -> !n.isDead());

		if (ctx.combat.inMultiCombat()) {
			npcs = ctx.npcs.populate().filter(ids).filter(GET_AGGRESSOR);
			if (npcs.size() < 1) npcs = ctx.npcs.populate().filter(ids).filter(n -> n.getHealthRatio() == -1 && !n.isDead());
			if (npcs.size() < 1) npcs = ctx.npcs.populate().filter(ids).filter(n -> !n.isDead());
		}

		if (npcs.population() > 0) return npcs.nearest().next();

		return null;
	}

	public SimpleNpc getMultiNpc(String[] ids) {
		SimpleEntityQuery<SimpleNpc> npcs = ctx.npcs.populate().filter(ids).filter(n -> !n.isDead());

		if (ctx.combat.inMultiCombat()) {
			npcs = ctx.npcs.populate().filter(ids).filter(GET_AGGRESSOR);
			if (npcs.size() < 1) npcs = ctx.npcs.populate().filter(ids).filter(n -> n.getHealthRatio() == -1 && !n.isDead());
			if (npcs.size() < 1) npcs = ctx.npcs.populate().filter(ids).filter(n -> !n.isDead());
		}

		if (npcs.population() > 0) return npcs.nearest().next();

		return null;
	}

	public SimpleNpc getNPC(List<Integer> npcIds) {
		final int[] ids = npcIds.stream().mapToInt(Integer::intValue).toArray();
		return ctx.npcs.populate().filter(ids).filter(npc -> npc != null && !npc.isDead())
				.sort(Comparator.comparingInt(npc -> npcIds.indexOf(npc.getId()))).next();
	}

	public SimpleNpc getNPC(String name) {
		return ctx.npcs.populate().filter(name).filter(GET_AGGRESSOR).nearest().next();
	}

	public void attack(SimpleNpc npc) {
		if (npc == null) {
			Variables.STATUS = "NPC is null";
			return;
		}
		Variables.STATUS = "Attacking NPC";
		try {
			if (new KSNPC(npc).click("Attack")) ctx.onCondition(() -> ctx.players.getLocal().getInteracting() != null, 1000);

		} catch (Exception e) {
			Variables.STATUS = "Failing to attack";
		}
	}

	public void attack(String name) {
		SimpleNpc npc = getAggressiveNPC(name);
		if (npc == null) npc = getNPC(name);
		if (npc == null) return;
		attack(npc);
	}

	public boolean isNpcAggressive(int id) {
		return isNpcAggressive(getNPC(id));
	}

	public SimpleNpc getAggressiveNPC(int... id) {
		return ctx.npcs.populate().filter(id).filter(this::isNpcAggressive).nearest().next();
	}

	public SimpleNpc getAggressiveNPC(String... name) {
		return ctx.npcs.populate().filter(name).filter(this::isNpcAggressive).nearest().next();
	}

	public boolean isNpcAggressive(SimpleNpc npc) {
		return npc != null && npc.getInteracting() != null && npc.getInteracting().getName() != null
				&& npc.getInteracting().getName().contains(ctx.players.getLocal().getName());

	}

	public boolean isRare(SimpleGroundItem loot) {
		return loot.getName().contains("andos") || loot.getName().contains("aradomin") || loot.getName().contains("rmadyl");
	}

	public void checkPots() {
		if (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 50) Tasks.getSupplies().eat();
		if (Tasks.getSkill().getPercentage(Skills.PRAYER) < 40) Tasks.getSupplies().drink(PotionType.PRAYER);
		if (Tasks.getSkill().isPoisonedOrVenomed()) Tasks.getSupplies().drink(PotionType.ANTIPOISON);
		if (Tasks.getSkill().shouldBoost(Skills.RANGED)) Tasks.getSupplies().drink(PotionType.RANGED);
		if (Tasks.getSkill().shouldBoost(Skills.ATTACK)) Tasks.getSupplies().drink(PotionType.ATTACK);
		if (Tasks.getSkill().shouldBoost(Skills.STRENGTH)) Tasks.getSupplies().drink(PotionType.STRENGTH);
		if (Tasks.getSkill().shouldBoost(Skills.DEFENCE)) Tasks.getSupplies().drink(PotionType.DEFENCE);
		// else if (!Tasks.getSupplies().antiFire.isRunning())
		// Tasks.getSupplies().drink(PotionType.ANTIFIRE);

	}

	public void switchSpec() {
		SimpleItem weapon = Weapons.SPECIAL_WEAPON.item();
		if (weapon != null) {
			Variables.STATUS = "Equipping special attack weapon";
			if (Tasks.getMenuAction().get(weapon, "Wear").invoke()) ctx.onCondition(() -> Weapons.SPECIAL_WEAPON.itemEquiped());
		}
	}

	public void useSpecialAttack(SimpleNpc target) {
		if (Weapons.SPECIAL_WEAPON.equals(SPECIAL_WEAPONS.B_GODSWORD)
				&& (target.getId() != NpcID.COMMANDER_ZILYANA || target.getId() != NpcID.GENERAL_GRAARDOR))
			return;
		if (!ctx.combat.specialAttack()) {
			String[] USUAL_ITEMS = ctx.equipment.populate().toStream().map(item -> item.getName()).toArray(String[]::new);
			Variables.STATUS = "Switching weapon";
			switchSpec();
			Variables.STATUS = "Attacking with spec";
			MenuActions.invoke("Use <col=00ff00>Special Attack</col>", "", -1, 57, 1, 38862884);
			Tasks.getCombat().attack(target);
			ctx.onCondition(() -> !ctx.combat.specialAttack(), 250, 4);
			Tasks.getInventory().equipAll(USUAL_ITEMS);
		}
	}

	public boolean specWeaponIsReadyInCombatTab() {
		SimpleWidget w = ctx.widgets.getWidget(593, 1);
		if (w != null && w.getText() != null)
			return Stream.of(Weapons.SPECIAL_WEAPONS.values()).anyMatch(weapon -> w.getText().contains(weapon.getItemName()));
		return false;
	}

	@Getter
	private Timer monsterTimer = new Timer(1);

	public boolean isMonsterGoneForAWhile(int... ids) {
		return !isMonsterPresent(ids) && !monsterTimer.isRunning();
	}

	public boolean isMonsterGoneForAWhile(String... names) {
		return !isMonsterPresent(names) && !monsterTimer.isRunning();
	}

	public boolean isMonsterPresent(String... names) {
		SimpleNpc npc = getNPC(names);
		if (npc != null) {
			monsterTimer.setEndIn(120000);
			return true;
		}
		return false;
	}

	public boolean isMonsterPresent(int... ids) {
		SimpleNpc npc = getNPC(ids);
		if (npc != null) {
			monsterTimer.setEndIn(120000);
			return true;
		}
		return false;
	}

}
