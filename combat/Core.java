package combat;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import api.Locations;
import api.Tasks;
import api.Variables;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Combat.Style;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.COMBAT, description = "Combat", discord = "", name = "Combat", servers = {
		"Zaros" }, version = "2")
public class Core extends Script implements LoopingScript {
	final String[] STOP_MESSAGES = { "be found: prayer potion", "be found: shark", "you are dead" };

	@Getter
	public class Task {
		private Skills skill;
		private int level;
		private Style style;

		public Task(Skills skill, int level) {
			this.skill = skill;
			this.level = level;
			this.style = skill == Skills.ATTACK ? Style.ACCURATE : skill == Skills.DEFENCE ? Style.DEFENSIVE : Style.AGGRESSIVE;
		}
	}

	private List<Task> tasks = new ArrayList<Task>();

	@Override
	public void onChatMessage(ChatMessage msg) {

		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
			}

			if (msg.getMessage().contains("rune runes to cast")) {
				tasks.removeIf(task -> task.getSkill() == Skills.MAGIC);
			}

			if (msg.getMessage().contains("enough ammo")) {
				tasks.removeIf(task -> task.getSkill() == Skills.RANGED);
			}
		}
	}

	@Override
	public void onExecute() {
		Tasks.init(ctx);
		tasks.clear();

		setTasks();

		tasks = tasks.stream().filter(task -> ctx.skills.level(task.getSkill()) < task.getLevel()).collect(Collectors.toList());

		Variables.STARTED = true;
	}

	public void setTasks() {
		tasks.add(new Task(Skills.STRENGTH, 20));
		tasks.add(new Task(Skills.ATTACK, 40));
		tasks.add(new Task(Skills.DEFENCE, 55));
		tasks.add(new Task(Skills.STRENGTH, 70));
		tasks.add(new Task(Skills.ATTACK, 70));
		tasks.add(new Task(Skills.DEFENCE, 70));
		tasks.add(new Task(Skills.STRENGTH, 95));
		tasks.add(new Task(Skills.ATTACK, 95));
		tasks.add(new Task(Skills.DEFENCE, 90));
		if (ranging) tasks.add(new Task(Skills.RANGED, 70));

		if (maging) tasks.add(new Task(Skills.MAGIC, 99));

		tasks.add(new Task(Skills.STRENGTH, 99));
		tasks.add(new Task(Skills.ATTACK, 99));
		tasks.add(new Task(Skills.DEFENCE, 99));
		tasks.add(new Task(Skills.RANGED, 99));

	}

	boolean ranging = true;
	boolean maging = true;

	@Override
	public void onProcess() {

		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA) && Tasks.getAntiban().staffNearby()) {
			System.out.println("Staff found at " + ctx.players.getLocal().getLocation());
			Variables.STOP = true;
			return;
		}

		if (tasks.size() == 0) {
			Variables.STOP = true;
			return;
		}

		Task task = tasks.get(0);

		if (ctx.skills.level(task.getSkill()) >= task.getLevel()) {
			tasks.remove(0);
			return;
		}
		if (task.getSkill() != Skills.MAGIC) ctx.combat.style(task.getStyle());

		if (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 20) Tasks.getSupplies().eat();

		if (!equipGear(task)) return;

		SimpleNpc yak = Tasks.getCombat().getNPC("Yak");
		if (yak != null) {
			if (task.getSkill() == Skills.MAGIC) useSpell(yak);
			else if (ctx.players.getLocal().getInteracting() == null) Tasks.getCombat().attack(yak);
		}
	}

	public boolean equipGear(Task task) {
		int attackLvl = ctx.skills.level(Skills.ATTACK);
		int strLvl = ctx.skills.level(Skills.STRENGTH);
		int defLvl = ctx.skills.level(Skills.DEFENCE);
		int ranged = ctx.skills.level(Skills.RANGED);
		int magic = ctx.skills.level(Skills.MAGIC);

		if (task.getSkill() == Skills.RANGED) {
			int weapon = ranged >= 50 ? ItemID.MAGIC_SHORTBOW : ItemID.SHORTBOW;
			if (ctx.equipment.populate().filter(weapon).population() == 0) {
				SimpleItem scim = ctx.inventory.populate().filter(weapon).next();
				if (scim != null) {
					scim.click(0);
					ctx.sleep(250, 450);
					return false;
				}
			}
			if (ranged >= 70) {
				SimpleItem hide = ctx.inventory.populate().filter(item -> item.getName().contains("lack d'")).next();
				if (hide != null) {
					hide.click(0);
					ctx.sleep(250, 450);
					return false;
				}
			}
		} else if (task.getSkill() == Skills.MAGIC) {
			SimpleItem item = Tasks.getInventory().getItem("full helm", "platebody", "plateleg", "staff", "vamb", "kiteshield");
			if (item != null) {
				item.click(0);
				ctx.sleep(250, 450);
				return false;
			}
			SimpleItem amulet = ctx.equipment.populate().filter("Amulet of glory").next();
			if (amulet != null) {
				amulet.click(0);
				ctx.sleep(250, 450);
				return false;
			}
		} else {
			int weapon = /*
							 * attackLvl >= 70 ? ItemID.ABYSSAL_WHIP :
							 */ attackLvl >= 60 ? ItemID.DRAGON_SCIMITAR
					: attackLvl >= 40 ? ItemID.RUNE_SCIMITAR : ItemID.IRON_SCIMITAR;

			if (ctx.equipment.populate().filter(weapon).population() == 0) {
				SimpleItem scim = ctx.inventory.populate().filter(weapon).next();
				if (scim != null) {
					scim.click(0);
					ctx.sleep(250, 450);
					return false;
				}
			}
		}
		return true;
	}

	public void useSpell(SimpleNpc npc) {
		int lvl = ctx.skills.level(Skills.MAGIC);
		if (lvl < 21) {
			ctx.magic.castSpellOnNPC("Wind strike", npc);
			ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != -1);
		} else if (lvl < 66) {
			SimpleItem fire = Tasks.getInventory().getItem("Mind rune");
			if (fire != null) ctx.magic.castSpellOnItem(lvl < 55 ? "Low Level Alchemy" : "High Level Alchemy", fire.getId());
			ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != -1);
		} else {
			String spell = lvl >= 80 ? "Stun" : lvl >= 73 ? "Enfeeble" : "Vulnerability";
			ctx.magic.castSpellOnNPC(spell, npc);
			ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != -1);
		}
	}

	@Override
	public int loopDuration() {
		return 150;
	}

	@Override
	public void onTerminate() {
		Variables.reset();
		ctx.log("Shutting down.. Thank you for using the script");
	}

	@Override
	public void paint(Graphics Graphs) {

	}

}
