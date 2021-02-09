package combat;

import java.awt.Graphics;

import api.Tasks;
import api.Variables;
import net.runelite.api.ChatMessageType;
import net.runelite.api.NpcID;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.wrappers.SimpleNpc;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.COMBAT, description = "Inferno", discord = "", name = "Inferno", servers = {
		"Zaros" }, version = "2")
public class Inferno extends Script {
	final String[] STOP_MESSAGES = { "be found: prayer potion", "be found: shark", "you are dead" };

	@Override
	public void onChatMessage(ChatMessage msg) {

		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (!Variables.STOP) {
				// Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 ->
				// msg.getMessage().contains(msg1));
			}

		}
	}

	@Override
	public void onExecute() {
		Tasks.init(ctx);
		Variables.STARTED = true;
	}

	@Override
	public void onProcess() {

		if (!ctx.getClient().isInInstancedRegion()) return;

		SimpleNpc ZUK = Tasks.getCombat().getNPC(7706);

		if (ZUK != null) {
			if (activeHealers()) attackHealer();
			else if (!isFightingJad()) Tasks.getCombat().attack(ZUK);

		} else {
			SimpleNpc JAD = Tasks.getCombat().getNPC(7700);
			if (JAD == null) return;
			if (activeHealers()) {
				attackHealer();
			} else if (!isFightingJad()) Tasks.getCombat().attack(JAD);
		}
	}

	private boolean isFightingJad() {
		if (ctx.players.getLocal().getInteracting() != null && ctx.players.getLocal().getInteracting().getName() != null) {
			// ctx.log("name: " +
			// ctx.players.getLocal().getInteracting().getName());
			String name = ctx.players.getLocal().getInteracting().getName().toLowerCase();
			return name.contains("jad") || name.contains("zuk");
		}
		return false;
	}

	private boolean activeHealers() {

		SimpleEntityQuery<SimpleNpc> healers = ctx.npcs.populate().filter(3128, NpcID.JALMEJJAK, NpcID.YTHURKOT_7705);
		for (SimpleNpc healer : healers) {
			if (healer.getInteracting() != null && healer.getInteracting().getName() != null
					&& !healer.getInteracting().getName().equals(ctx.players.getLocal().getName())) {
				return true;
			}
		}
		return false;
	}

	private void attackHealer() {

		SimpleNpc healer = ctx.npcs.populate().filter(3128, NpcID.JALMEJJAK, NpcID.YTHURKOT_7705)
				.filter(h -> h.getInteracting() != null && h.getInteracting().getName() != null
						&& !h.getInteracting().getName().equals(ctx.players.getLocal().getName()))
				.nearest().next();
		if (healer != null) {
			Tasks.getCombat().attack(healer);
		}
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
