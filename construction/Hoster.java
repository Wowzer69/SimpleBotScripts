package construction;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.stream.Stream;

import net.runelite.api.ChatMessageType;
import simple.api.Locations;
import simple.api.Variables;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.CONSTRUCTION, description = "", discord = "Datev#0660", name = "KS | PoH Hoster", servers = {
		"Zaros" }, version = "1")
public class Hoster extends Script implements LoopingScript {

	@Override
	public void onExecute() {
		Variables.reset();
		Variables.STARTED = true;
	}

	@Override
	public void onProcess() {
		if (Variables.STOP) {
			ctx.stopScript();
			return;
		}
		if (!Variables.STARTED) return;

		boolean marr = ctx.inventory.populate().filter("Marrentill").population() > 0;
		if (ctx.getClient().isInInstancedRegion()) {
			if (ctx.widgets.getWidget(71, 5) != null) return;
			if (!marr) {
				teleportHome();
				return;
			}
			SimpleObject lit = ctx.objects.populate().filter(13213).nearest().next();
			SimpleObject unlit = ctx.objects.populate().filter(13212).nearest().next();
			if (unlit != null && ctx.pathing.distanceTo(unlit.getLocation()) > 5) ctx.pathing.step(unlit.getLocation());
			if (lit != null && ctx.pathing.distanceTo(lit.getLocation()) > 5) ctx.pathing.step(lit.getLocation());
			if (ctx.players.populate().filter(p -> p.distanceTo(ctx.players.getLocal()) < 6).population() == 1) {
				Variables.STATUS = "Waiting for people";
				return;
			}
			;
			if (unlit != null && marr && ctx.inventory.populate().filter("Tinderbox").population() > 0) {
				Variables.STATUS = "Relighting burners";
				if (unlit.validateInteractable() && unlit.click("Light"))
					ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != -1);
				return;
			}
		} else if (ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			if (!marr) {
				bank();
				return;
			}
			SimpleObject portal = ctx.objects.populate().filter("Portal").nearest().next();
			if (ctx.pathing.distanceTo(portal.getLocation()) > 4) {
				Variables.STATUS = "Walking path to portal";
				ctx.pathing.step(portal.getLocation());
				return;
			}
			if (portal.validateInteractable() && portal.click("Home"))
				ctx.onCondition(() -> ctx.getClient().isInInstancedRegion());
		}
	}

	@Override
	public int loopDuration() {
		return 200;
	}

	@Override
	public void onTerminate() {
		Variables.reset();
		ctx.log("Shutting down.. Thank you for using the script");
	}

	@Override
	public void paint(Graphics Graphs) {
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.BLACK);
		g.fillRect(5, 5, 200, 30);
		g.setColor(Color.GREEN);
		g.drawRect(5, 5, 200, 30);
		g.setColor(Color.CYAN);
		g.drawString("Total run time: " + Variables.START_TIME.toElapsedString(), 7, 20);
		g.drawString("Status: " + Variables.STATUS, 7, 30);
	}

	final String[] STOP_MESSAGES = { "don't have the right", "be found:" };

	@Override
	public void onChatMessage(ChatMessage msg) {
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
			}
		}
	}

	public void teleportHome() {
		Variables.STATUS = "Teleporting home";
		ctx.magic.castSpellOnce("Home");
		ctx.onCondition(() -> ctx.pathing.inArea(Locations.EDGEVILLE_AREA));
	}

	public void bank() {
		SimpleObject bank = ctx.objects.populate().filter("Bank booth").nearest().next();
		if (bank.validateInteractable()) {
			Variables.STATUS = "Getting last preset";
			if (bank.click("Last-preset")) {
				ctx.onCondition(() -> ctx.inventory.populate().population() > 0, 500);
			}
		}
	}

}
