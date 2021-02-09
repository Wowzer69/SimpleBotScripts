package vorkath.methods;

import api.Tasks;
import api.Variables;
import api.simple.KSNPC;
import api.simple.KSObject;
import api.tasks.Supplies.PotionType;
import lombok.RequiredArgsConstructor;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.wrappers.SimpleNpc;
import simple.robot.api.ClientContext;
import simple.robot.utils.Random;
import vorkath.Vorkath;
import vorkath.data.Constants;

@RequiredArgsConstructor
public class VorkathHandler {

	private final ClientContext ctx;
	private final Vorkath core;

	public boolean enterInstance() {
		if (!ctx.getClient().isInInstancedRegion()) {
			Variables.STATUS = "Entering instance";
			KSObject rock = new KSObject(ctx.objects.populate().filter(ObjectID.ICE_CHUNKS_31990).next());
			if (rock != null && rock.validateInteractable() && rock.click("Climb-over"))
				ctx.sleepCondition(() -> ctx.getClient().isInInstancedRegion(), 5500);
		}
		return ctx.getClient().isInInstancedRegion();
	}

	public void handle() {
		if (Tasks.getLoot().loot()) return;
		if (!setup()) return;

		Tasks.getCombat().checkPots();

		Tasks.getSkill().flickPrayer(true);

		if (core.getMethods().fireballProjectile()) handleFireBall();
		if (core.getMethods().zombieActive()) handleZombie();
		if (core.getMethods().acidActive()) handleAcid();

		else {
			if (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 70) Tasks.getSupplies().eat();

			if (!Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "crossbow")) Tasks.getInventory().equip(true, "crossbow");
			// core.getMethods().switchBolts();
			Variables.STATUS = "Killing Vorkath";
			if (ctx.players.getLocal().getInteracting() == null) Tasks.getCombat().attack("Vorkath");
		}
	}

	private void handleZombie() {
		SimpleNpc zombified = ctx.npcs.populate().filter(NpcID.ZOMBIFIED_SPAWN_8063).filter(npc -> !npc.isDead()).next();
		if (zombified == null) return;
		Variables.STATUS = "Zombified active";

		Tasks.getInventory().equip(true, "staff");

		if (Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "staff")) Tasks.getCombat().attack(zombified);
		else ctx.magic.castSpellOnNPC("Crumble Undead", zombified);
	}

	private void handleAcid() {
		Variables.STATUS = "Handling Acid";
		core.getPath().handleAcid();
	}

	private void handleFireBall() {
		if (Constants.LAST_FIRE_BALL.isRunning()) return;

		Variables.STATUS = "Fire ball active";

		WorldPoint tile = core.getPath().getFireBallTile();

		if (tile == null) return;

		while (ctx.pathing.reachable(tile) && !ctx.pathing.onTile(tile))
			ctx.pathing.step(tile);
		Constants.LAST_FIRE_BALL.reset();
		Tasks.getCombat().attack("Vorkath");
	}

	private boolean setup() {
		SimpleNpc vorkath = ctx.npcs.populate().filter(NpcID.VORKATH_8059).next();
		if (vorkath == null) return true;

		if (Tasks.getInventory().contains("bones")) Variables.FORCE_BANK = true;
		else {
			Variables.STATUS = "Waking up vorkath";
			if (new KSNPC(vorkath).click("Poke")) {
				ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != -1, 2000);
				ctx.onCondition(() -> ctx.players.getLocal().getAnimation() == -1);
				Variables.STATUS = "Gaining some distance";

				WorldPoint loc = ctx.players.getLocal().getLocation();
				WorldPoint next = new WorldPoint(loc.getX(), loc.getY() - Random.between(2, 5), loc.getPlane());

				if (ctx.pathing.step(next)) ctx.sleep(400, 600);

				Tasks.getSkill().flickPrayer(true);
				Tasks.getSupplies().drink(PotionType.ANTIFIRE);

			}
		}
		return false;
	}

	public int getHealth() {
		SimpleNpc vorkath = Tasks.getCombat().getNPC(NpcID.VORKATH_8061);
		return vorkath != null ? vorkath.getHealthRatio() : -1;
	}
}
