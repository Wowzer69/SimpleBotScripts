package vorkath;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import api.Locations;
import api.Tasks;
import api.Variables;
import api.utils.Timer;
import api.utils.Utils;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Projectile;
import simple.hooks.filters.SimplePrayers.Prayers;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.scripts.listeners.ProjectileMovedEvent;
import simple.hooks.scripts.listeners.ProjectileMovedListener;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Magic.SpellBook;
import simple.robot.script.Script;
import vorkath.data.Constants;
import vorkath.methods.DeathHandler;
import vorkath.methods.Methods;
import vorkath.methods.Pathing;
import vorkath.methods.VorkathHandler;

@ScriptManifest(author = "KremeSickle", category = Category.COMBAT, description = "Does vorkath", name = "KS_ Vorkath", servers = {
		"Zaros" }, version = "0.1", discord = "")

public class Vorkath extends Script implements LoopingScript, ProjectileMovedListener {

	@Override
	public void paint(Graphics Graphs) {

		Graphics2D g = (Graphics2D) Graphs;

		g.setColor(Color.BLACK);
		g.fillRect(0, 230, 170, 75);
		g.setColor(Color.BLACK);
		g.drawRect(0, 230, 170, 75);
		g.setColor(Color.white);

		g.drawString("Private Vorkath v0.1", 7, 245);
		g.drawString("Uptime: " + Variables.START_TIME.toElapsedString(), 7, 257);
		g.drawString("Status: " + Variables.STATUS, 7, 269);

		g.drawString("Vorkath kills: " + Variables.COUNT + " ("
				+ ctx.paint.valuePerHour((int) Variables.COUNT, Variables.START_TIME.getStart()) + ")", 7, 281);

	}

	@Override
	public void onChatMessage(ChatMessage e) {
		if (e.getType() == ChatMessageType.GAMEMESSAGE) {
			Variables.LAST_MESSAGE = e.getMessage();
			if (e.getMessage().contains("antifire potion has expired")) Tasks.getSupplies().antiFire = new Timer(1);

			if (e.getMessage().contains("you are dead")) Constants.RECOLLECT_ITEMS = true;
			if (e.getMessage().contains("kill count")) Variables.COUNT++;
			if (e.getMessage().contains("not be found")) ctx.stopScript();
		}
	}

	@Getter
	private Methods methods;
	@Getter
	private Pathing path;
	@Getter
	private DeathHandler death;
	@Getter
	private VorkathHandler vorkath;

	@Override
	public void onExecute() {
		try {
			Variables.reset();
			Tasks.init(ctx);
			methods = new Methods(ctx, this);
			path = new Pathing(ctx, this);
			death = new DeathHandler(ctx, this);
			vorkath = new VorkathHandler(ctx, this);
			Utils.setZoom(1);
			doChecks();
			Tasks.getSkill().addPrayer(Prayers.PROTECT_FROM_MAGIC);
			Tasks.getSkill().addPrayer(Prayers.EAGLE_EYE);
			Constants.RECOLLECT_ITEMS = false;
			Variables.FORCE_BANK = ctx.pathing.inArea(Locations.EDGEVILLE_AREA);
			Variables.STARTED = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doChecks() {
		boolean stop = false;
		if (ctx.magic.spellBook() != SpellBook.MODERN) {
			ctx.log("Please switch to normal spellbook");
			stop = true;
		}
		if (!Tasks.getInventory().containsAll("Chaos rune", "Earth rune", "Air rune")) {
			ctx.log("Please have required runes in\n inventory:\n%s, %s, %s", "Chaos rune", "Earth rune", "Air rune");
			stop = true;
		}

		if (stop) ctx.stopScript();
	}

	@Override
	public void onProcess() {
		if (!Variables.STARTED || Variables.STOP) return;
		if ((!ctx.pathing.inArea(Locations.EDGEVILLE_AREA) && Tasks.getAntiban().staffNearby())
				|| (Tasks.getAntiban().staffUnder() && !ctx.pathing.inArea(Locations.EDGEVILLE_BANK))) {
			Variables.STOP = true;
			return;
		}

		if (ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			Tasks.getSkill().disablePrayers();
			if (Constants.RECOLLECT_ITEMS) {
				getDeath().handle();
				return;
			}
			if (!Tasks.getBanking().heal()) return;
			if (!Tasks.getBanking().usePreset()) return;
			if (!Utils.directTeleport("Vorkath") && Tasks.getTeleporter().open())
				Tasks.getTeleporter().teleportStringPath("Bossing", "Vorkath");
		} else if (ctx.pathing.inArea(Locations.VORKATH_START_AREA)) {
			Constants.PATH_SET = false;
			Constants.CURRENT_ACID_TILES.clear();
			Tasks.getSkill().disablePrayers();
			if (Constants.RECOLLECT_ITEMS) getDeath().handle();
			else getVorkath().enterInstance();
		} else if (ctx.getClient().isInInstancedRegion()) {
			if (getMethods().shouldRestock()) getMethods().teleportHome();
			else getVorkath().handle();
		}

	}

	@Override
	public void onChange(ProjectileMovedEvent proj) {
		if (proj == null) return;
		Projectile p = proj.getProjectile();
		if (p == null || p.getId() != 1483) return;
		System.out.println(p.getId());
		System.out.println(proj.getPosition());
		if (!Constants.CURRENT_ACID_TILES.contains(proj.getPosition())) Constants.CURRENT_ACID_TILES.add(proj.getPosition());

	}

	@Override
	public void onTerminate() {
		Tasks.getSkill().removeAll();
		Variables.reset();
	}

	@Override
	public int loopDuration() {
		return 150;
	}
}
