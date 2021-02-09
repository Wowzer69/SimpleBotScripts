package instancedBandos.methods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import api.Tasks;
import api.Variables;
import api.utils.Weapons;
import api.utils.Weapons.SPECIAL_WEAPONS;
import instancedBandos.data.Constants;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.wrappers.SimpleNpc;
import simple.robot.api.ClientContext;
import simple.robot.util.Random;

public class Methods {

	private ClientContext ctx;

	public Methods(ClientContext ctx) {
		this.ctx = ctx;
	}

	private final List<Integer> TARGETS = new ArrayList<Integer>(Arrays.asList(NpcID.GENERAL_GRAARDOR, NpcID.SERGEANT_STEELWILL,
			NpcID.SERGEANT_GRIMSPIKE, NpcID.SERGEANT_STRONGSTACK));

	public void handleBandos() {
		Variables.USE_PRAYER = true;
		SimpleNpc target = Tasks.getCombat().getNPC(TARGETS);
		int heal = Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "Guthan's warspear") ? 40 : 70;
		Weapons.SPECIAL_WEAPON = Weapons.getSpecialWeapon();
		if (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < heal) Tasks.getSupplies().eat();
		Tasks.getCombat().checkPots();
		if (target != null) {
			if (Constants.STAND_TILE != null) Constants.STAND_TILE = null;
			if (target.getId() == NpcID.GENERAL_GRAARDOR) {
				if (target.getInteracting() != null && target.getInteracting().equals(ctx.players.getLocal().getPlayer()))
					Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MELEE);
				else if (target.getInteracting() != null) Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MISSILES);
				Tasks.getSkill().addPrayer(Prayers.PIETY);
				Tasks.getInventory().equipAll(Constants.USUAL_ITEMS);
			} else {
				if (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 90) Tasks.getInventory().equipGuthans();
				else Tasks.getInventory().equipAll(Constants.USUAL_ITEMS);

				if (Tasks.getCombat().isNpcAggressive(NpcID.SERGEANT_STEELWILL))
					Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MAGIC);
				else if (Tasks.getCombat().isNpcAggressive(NpcID.SERGEANT_GRIMSPIKE))
					Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MISSILES);
				else if (Tasks.getCombat().isNpcAggressive(NpcID.SERGEANT_STRONGSTACK))
					Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MELEE);
				else Tasks.getSkill().removeAllBut(Prayers.PIETY);
			}

			if (ctx.players.getLocal().getInteracting() == null
					|| !ctx.players.getLocal().getInteracting().getName().equals(target.getName())) {
				if ((Weapons.SPECIAL_WEAPON == SPECIAL_WEAPONS.S_GODSWORD && ctx.skills.level(Skills.HITPOINTS) < 70)
						&& (Weapons.canSpecial(false) || Weapons.canSpecial(true)))
					Tasks.getCombat().useSpecialAttack(target);
				else Tasks.getCombat().attack(target);
			}
		} else {
			Tasks.getInventory().equipAll(Constants.USUAL_ITEMS);
			Variables.STATUS = "Waiting for respawn";
			Tasks.getSkill().disablePrayers();
			Tasks.getBanking().prayAtAltar("Bandos altar");
			if (!Tasks.getLoot().loot(Variables.LOOTABLES)) walkSafeSpot();
		}
	}

	private void walkSafeSpot() {
		if (!Constants.USE_SAFESPOT) return;

		if (Constants.STAND_TILE == null) {
			int x = Constants.TANKING ? Constants.DOOR_TILE.getX() + 6 : Constants.DOOR_TILE.getX() + 1;
			int y = Constants.TANKING ? Constants.DOOR_TILE.getY() + 7
					: Random.between(Constants.DOOR_TILE.getY() + 2, Constants.DOOR_TILE.getY() + 15);
			Constants.STAND_TILE = new WorldPoint(x, y, 2);
		}
		if (!ctx.pathing.onTile(Constants.STAND_TILE)) {
			Variables.STATUS = "Walking to safe spot";
			ctx.pathing.step(Constants.STAND_TILE);
		}
	}
}
