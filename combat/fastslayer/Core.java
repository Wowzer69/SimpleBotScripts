package combat.fastslayer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.stream.Stream;

import api.Tasks;
import api.Variables;
import combat.fastslayer.data.Constants;
import combat.fastslayer.data.SlayerTask;
import combat.fastslayer.methods.Methods;
import discord.Discord;
import discord.DiscordOptions;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.teleporter.Teleporter;

@ScriptManifest(author = "Trester/Steganos", category = Category.SLAYER, description = "Does slayer", name = "Slayer", servers = {
		"Zaros" }, version = "0.1", discord = "")

public class Core extends Discord {
	// You're assigned to kill Ankou; only 1 more to go.
	// Nieve wants you to stick to your Slayer assignments in this area.
	// You've completed one task and received 30 points

	@Getter
	private Teleporter teleport;

	@Getter
	private Methods methods;

	@Override
	public void onExecute() {
		DiscordOptions.CURRENT_SCRIPT = this.getClass();
		Tasks.init(ctx);
		super.onExecute();
		resetVariables();
		teleport = new Teleporter(ctx);
		methods = new Methods(ctx, this);
		resetVariables();

		Constants.SKIP_TASKS.addAll(Arrays.asList("dragon", "gargoyles", "kalphite", "aberrant spectre", "banshee", "wall beast",
				"black demon", "cave horror"));
	}

	public void resetVariables() {
		Constants.SKIP_TASKS.clear();
		Constants.TOTAL_TASKS = -1;
		Constants.TASK = null;
		Constants.SKIP = false;
		Constants.CHECK_TASK = false;
		Variables.STARTED = true;
	}

	@Override
	public void onProcess() {
		if (!super.on()) return;
		if (!Constants.SKIP && getMethods().requiresBank()) {
			getMethods().bank();
		} else if (!Constants.CHECK_TASK) {
			getMethods().checkTask();
		} else if (Constants.TASK == null || Constants.SKIP) {
			if (Constants.TOTAL_TASKS == 20) {
				Variables.STOP = true;
				return;
			}
			boolean ten = Constants.TOTAL_TASKS > 1 && Constants.TOTAL_TASKS % 10 == 9;
			getMethods().getTask(ten, Constants.SKIP);
		} else if (ctx.pathing.inArea(Constants.BURTHORPE_AREA) || ctx.pathing.inArea(Constants.EDGEVILLE_AREA)) {
			getMethods().goToTask();
		} else {
			getMethods().attack();
		}

	}

	@Override
	public void paint(Graphics Graphs) {
		Graphics2D g = (Graphics2D) Graphs;
		g.setColor(Color.BLACK);
		g.fillRect(5, 5, 200, 75);
		g.setColor(Color.GREEN);
		g.drawRect(5, 5, 200, 75);
		g.setColor(Color.CYAN);
		g.drawString("Total run time: " + Variables.START_TIME.toElapsedString(), 7, 20);
		g.drawString("Status: " + Variables.STATUS, 7, 35);
	}

	final String[] STOP_MESSAGES = { "you are dead", "no ammo" };

	// You've completed 5 tasks and received 2 points, giving you a total of 28;
	// return to a Slayer master.
	@Override
	public void onChatMessage(ChatMessage msg) {
		super.onChatMessage(msg);

		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			String message = msg.getMessage();
			if (message.contains("and received")) {
				int totalTask = 0;

				try {
					totalTask = Integer.parseInt(message.replaceAll(".* completed (.*) task .*", "$1"));
				} catch (Exception e) {
					totalTask = 1;
				}
				ctx.log("Total: " + totalTask);

				if (totalTask > 0) Constants.TOTAL_TASKS = totalTask;

				Constants.TASK = null;
				Constants.CHECK_TASK = true;
			}
			if (message.contains("you're assigned to kill")) {
				String name = msg.getMessage().replaceAll("(.*) kill (.*); (.*)", "$2");
				Constants.SKIP = false;
				if (name != "") {
					Constants.TASK = new SlayerTask(name, 1);
					Constants.CHECK_TASK = true;
					System.out.println(Constants.TASK);

					if (getMethods().shouldSkipTask()) {
						Constants.SKIP = true;
						Constants.TASK = null;
					} else Constants.SKIP = false;
				}
				// You're assigned to kill Ankou; only 1 more to go.
			}
			if (message.contains("wants you to stick to your slayer assignments") || message.contains("something new to")
					|| message.contains("completed one task")) {
				Constants.TASK = null;
				Constants.CHECK_TASK = true;
			}
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> message.contains(msg1));
			}
		}
	}

}
