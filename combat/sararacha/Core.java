package combat.sararacha;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import api.Tasks;
import api.Variables;
import api.utils.Utils;
import combat.sararacha.data.Constants;
import discord.Discord;
import discord.DiscordOptions;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;

@ScriptManifest(author = "KremeSickle", category = Category.UTILITY, description = "Sarachnis", discord = "", name = "Sarachnis", servers = {
		"Zaros" }, version = "2")
public class Core extends Discord implements LoopingScript {

	private static List<Integer> LOOT = new ArrayList<Integer>();
	final String[] STOP_MESSAGES = { "be found:", "you are dead" };

	@Override
	public void onChatMessage(ChatMessage msg) {
		super.onChatMessage(msg);
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
			}
		}
	}

	@Override
	public void onExecute() {
		DiscordOptions.CURRENT_SCRIPT = this.getClass();
		super.onExecute();
		Tasks.init(ctx);

		LOOT.clear();
		LOOT.addAll(Arrays.asList(ItemID.SARACHNIS_CUDGEL, 23495, ItemID.DRAGON_MED_HELM, ItemID.GIANT_EGG_SACFULL,
				ItemID.TORSTOL_SEED, ItemID.DWARF_WEED_SEED, ItemID.COINS_995, ItemID.GRIMY_TORSTOL, ItemID.SNAPDRAGON_SEED,
				ItemID.CRYSTAL_KEY, ItemID.ONYX_BOLT_TIPS, ItemID.YEW_SEED, ItemID.RUNITE_ORE, ItemID.BLOOD_RUNE,
				ItemID.BATTLESTAFF, ItemID.DRAGON_BONES));

		Variables.STARTED = true;

	}

	public void eatFood() {

		while (ctx.skills.level(Skills.HITPOINTS) < 80) {
			SimpleItem food = ctx.inventory.populate().filter("Shark").next();
			if (food == null) break;
			food.click(0);
			ctx.sleep(250);
		}

	}

	public boolean needBank() {
		return ctx.inventory.inventoryFull() || ctx.inventory.populate().filter("Shark").population() < 3
				|| ctx.skills.level(Skills.PRAYER) == 0 || ctx.skills.level(Skills.HITPOINTS) <= 20;
	}

	public void usePotions() {
		SimpleItem attack = ctx.inventory.populate()
				.filter(ItemID.SUPER_ATTACK4, ItemID.SUPER_ATTACK3, ItemID.SUPER_ATTACK2, ItemID.SUPER_ATTACK1).next();
		if (ctx.skills.level(Skills.ATTACK) <= 115 && attack != null) {
			attack.click(0);
			ctx.sleep(250);
		}

		SimpleItem strength = ctx.inventory.populate()
				.filter(ItemID.SUPER_STRENGTH4, ItemID.SUPER_STRENGTH3, ItemID.SUPER_STRENGTH2, ItemID.SUPER_STRENGTH1).next();
		if (ctx.skills.level(Skills.STRENGTH) <= 115 && strength != null) {
			strength.click(0);
			ctx.sleep(250);
		}
	}

	public boolean atHome() {
		return ctx.objects.populate().filter(29237).population() == 1;
	}

	public SimpleGroundItem loot() {
		return ctx.groundItems.populate().filter(item -> item != null && LOOT.contains(item.getId())).nearest().next();
	}

	@Override
	public void onProcess() {
		if (!super.on()) return;

		if (ctx.pathing.inArea(Constants.EDGEVILE_AREA)) {
			Tasks.getSkill().removeAll();
			if (!needBank()) {
				Utils.directTeleport("Sarachnis");
			} else {
				if (!Tasks.getBanking().heal()) Tasks.getBanking().usePreset();
			}

		} else if (ctx.pathing.inArea(Constants.MONSTER_AREA)) {
			if (needBank()) {
				ctx.magic.castSpellOnce("Home Teleport");
				return;
			}

			if (ctx.widgets.getWidget(1002, 0) != null) Utils.directTeleport("Sarachnis");

			Tasks.getSkill().enablePrayer(Prayers.PROTECT_FROM_MISSILES);
			Tasks.getSkill().enablePrayer(Prayers.PIETY);
			usePotions();
			if (ctx.players.getLocal().getLocation().getY() > 9912) {
				SimpleObject gate = ctx.objects.populate().filter(34858).nearest().next();
				if (gate != null) {
					if (gate.click("Quick-Enter")) {
						ctx.sleep(250, 365);
						ctx.sleepCondition(() -> ctx.players.getLocal().getLocation().getX() > 9912, 150);
					}
				}
			} else if (loot() != null) {
				SimpleGroundItem item = loot();
				if (item != null) item.click("Take");

			} else if (ctx.combat.getSpecialAttackPercentage() >= 30) {

				SimpleNpc target = ctx.npcs.populate().filter(8713).nearest().next();
				if (target != null) {

					ctx.combat.toggleSpecialAttack(true);

					if (target.distanceTo(ctx.players.getLocal()) > 5) ctx.pathing.step(target.getLocation());

					if (ctx.players.getLocal().getInteracting() == null) target.click(0);
				}

			} else {
				ctx.magic.castSpellOnce("Teleport to house");
			}
		} else if (atHome()) {
			Tasks.getSkill().removeAll();
			eatFood();
			SimpleObject pool = ctx.objects.populate().filter(29237).next();
			if (pool != null && pool.validateInteractable()) {
				pool.click(0);
				ctx.sleepCondition(() -> ctx.combat.getSpecialAttackPercentage() == 100, 750);
				if (needBank() && ctx.combat.getSpecialAttackPercentage() == 100) {
					ctx.magic.castSpellOnce("Home Teleport");
					return;
				}

				if (ctx.combat.getSpecialAttackPercentage() == 100) Utils.directTeleport("Sarachnis");
			}
		}

	}

	@Override
	public int loopDuration() {
		return 100;
	}

	@Override
	public void paint(Graphics Graphs) {

	}

}
