package vorkath.methods;

import api.Locations;
import api.Tasks;
import api.Variables;
import lombok.AllArgsConstructor;
import net.runelite.api.ObjectID;
import net.runelite.api.ProjectileID;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.wrappers.SimpleItem;
import simple.robot.api.ClientContext;
import vorkath.Vorkath;
import vorkath.data.Constants;

@AllArgsConstructor
public class Methods {

	private ClientContext ctx;
	private Vorkath core;

	public boolean shouldRestock() {
		boolean bank = false;
		if (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 20
				|| (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 40 && !Tasks.getSupplies().hasFood()))
			bank = true;
		// if (!Tasks.getSupplies().hasFood()) bank = true;
		if (Variables.FORCE_BANK) bank = true;
		if (bank) Variables.FORCE_BANK = true;
		return bank;
	}

	public void switchBolts() {
		int vorkathHealth = core.getVorkath().getHealth();
		if (vorkathHealth == -1) return;

		SimpleItem rubyBolt = Tasks.getInventory().getItem("Ruby bolts");
		SimpleItem diamondBolt = Tasks.getInventory().getItem("Diamond bolts");

		if (rubyBolt != null && vorkathHealth >= 35) {
			Variables.STATUS = "Switching bolts";
			if (rubyBolt.click(0)) ctx.onCondition(() -> Tasks.getInventory().isWearing(EquipmentSlot.AMMO, rubyBolt.getName()));
		} else if (diamondBolt != null && vorkathHealth < 35) {
			Variables.STATUS = "Switching diamond bolts";
			if (diamondBolt.click(0))
				ctx.onCondition(() -> Tasks.getInventory().isWearing(EquipmentSlot.AMMO, diamondBolt.getName()));
		}
	}

	public boolean zombieActive() {
		return zombieProjectile() || Tasks.getCombat().getNPC(false, "Zombified Spawn") != null;
	}

	public boolean acidActive() {
		return ctx.objects.populate().filter(ObjectID.ACID_POOL_32000).size() > 0 && Constants.CURRENT_ACID_TILES.size() > 0;
	}

	public boolean zombieProjectile() {
		return activeProjectile(ProjectileID.VORKATH_SPAWN_AOE);
	}

	public boolean fireballProjectile() {
		return activeProjectile(ProjectileID.VORKATH_BOMB_AOE);
	}

	public boolean acidProjectile() {
		return activeProjectile(ProjectileID.VORKATH_POISON_POOL_AOE);
	}

	public void teleportHome() {
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			if (ctx.magic.castHomeTeleport()) ctx.onCondition(() -> ctx.pathing.inArea(Locations.EDGEVILLE_AREA));
			Variables.FORCE_BANK = true;
		}
	}

	public boolean activeProjectile(int id) {
		return ctx.projectiles.projectileActive(id);
	}
}
