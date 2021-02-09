package aiohunter;

import java.awt.Button;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

import aiohunter.data.Constants;
import aiohunter.data.enums.Butterfly;
import aiohunter.data.enums.Salamanders;
import aiohunter.data.enums.Trapping;
import aiohunter.methods.Methods;
import aiohunter.methods.Net;
import aiohunter.methods.Sala;
import aiohunter.methods.Traps;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.KeyCode;
import net.runelite.api.Point;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.OverlayUtil;
import simple.api.Utils;
import simple.api.Variables;
import simple.api.listeners.ConfigChangeEvent;
import simple.api.listeners.ConfigChangeListener;
import simple.api.panel.Config;
import simple.api.panel.Panel;
import simple.api.panel.Tabs;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.HUNTER, description = "<br>This script will train your Hunter with various of methods<br><br>Script supports Salamander, Butterflies, Bird Traps & Box Traps<br><br>Start the script in the hunting area with the required hunting items in your inventory<br><br>Choose your preferred Hunting method and then choose which type<br><br>Custom Tiles:<br> Pause the Script VIA the GUI, enable Show Tiles and then hold SHIFT + Left click custom tiles<br><br>Script is currently in beta-testing phase<br><br>Please message me on discord with any questions/concerns", discord = "Datev#0660", name = "KS | AIO Hunter", servers = {
		"Zaros" }, version = "2.1", vip = true)

public class AIOHunter extends Script implements LoopingScript, MouseListener, ConfigChangeListener {

	private JFrame frame;
	private Panel panel;

	public enum TYPES {
		TRAPPING,
		BUTTERFLIES,
		SALAMANDERS
	}

	@Getter
	private Methods methods;
	@Getter
	private Traps traps;
	@Getter
	private Net net;
	@Getter
	private Sala sala;

	@Override
	public void onExecute() {
		try {
			Variables.reset();
			addConfig();
			String title = Utils.getValue(getClass(), "name") + " v" + Utils.getValue(getClass(), "version");
			panel = new Panel();
			frame = panel.init(title, panel);
			setupScript();
			Variables.DISPATCHER.addListener(this);
		} catch (Exception e) {
			ctx.log(e.getMessage());
			e.printStackTrace();
		}
	}

	public void addConfig() {
		Config.clear();

		Config.TABS.add(new Tabs(0, "Script Config", "Choose your configuration"));
		Config.CONFIGURATION
				.add(new Config(0, TYPES.class, "TRAPPING", "Hunting", "Train your skill via this method", "huntType"));
		Config.CONFIGURATION
				.add(new Config(0, Butterfly.class, "RUBY_HARVEST", "Butterfly", "Hunt this type of butterfly", "butterflyType"));
		Config.CONFIGURATION.add(
				new Config(0, Salamanders.class, "SWAMP_LIZARD", "Salamander", "Hunt this type of salamander", "salamanderType"));
		Config.CONFIGURATION.add(new Config(0, Trapping.class, "BIRD_SNARE", "Trapping", "Use this type of hunting", "trapType"));
		Config.CONFIGURATION.add(new Config(0, Button.class, true, "Reset tiles", "Reset current selected tiles", "resetTiles"));
		Config.CONFIGURATION
				.add(new Config(0, boolean.class, true, "Show tiles", "Display tiles that are current / possible", "showTiles"));
		Config.CONFIGURATION.add(new Config(0, Button.class, "", "Update Tiles", "Update auto tiles", "updateTiles"));
		Config.setConfigChanged(true);
	}

	public void setupScript() {
		methods = new Methods(ctx, this);
		traps = new Traps(ctx, this);
		net = new Net(ctx, this);
		sala = new Sala(ctx, this);

		Constants.CURRENT_TILES.clear();
		Constants.POSSIBLE_TILES.clear();
		getMethods().fillTempTiles();
	}

	@Override
	public void onProcess() {
		if (!Variables.STARTED || Variables.PAUSED) return;
		if (getType().equals(TYPES.SALAMANDERS)) sala.handle();
		else if (getType().equals(TYPES.BUTTERFLIES)) net.handle();
		else if (getType().equals(TYPES.TRAPPING)) traps.handle();
	}

	@Override
	public void onTerminate() {
		if (frame != null) frame.dispose();
		ctx.log("Shutting down.. Thank you for using the script");
		Variables.reset();
		Config.clear();
	}

	@Override
	public void paint(Graphics arg0) {
		Graphics2D g = (Graphics2D) arg0;
		if (sala == null || Config.CONFIGURATION.size() == 0) return;

		if (panel != null) panel.update(Variables.STATUS);

		if (Config.getB("showTiles")) {
			if (getType().equals(TYPES.SALAMANDERS)) sala.paint(g);
			else if (getType().equals(TYPES.TRAPPING)) traps.paint(g);
			else if (getType().equals(TYPES.BUTTERFLIES)) net.paint(g);
		}
	}

	@Override
	public int loopDuration() {
		return 150;
	}

	@Override
	public void onChatMessage(ChatMessage e) {
		if (e.getType() == ChatMessageType.GAMEMESSAGE) {
			Variables.LAST_MESSAGE = e.getMessage();
			if (e.getMessage().contains("you can't lay a trap here") || e.getMessage().contains("have high enough hunter level"))
				Constants.CURRENT_TILES.remove(ctx.players.getLocal().getLocation());
		}
	}

	public TYPES getType() {
		return Config.getValue("huntType");
	}

	public Butterfly getButterfly() {
		return Config.getValue("butterflyType");
	}

	public Salamanders getSalamander() {
		return Config.getValue("salamanderType");
	}

	public Trapping getTrap() {
		return Config.getValue("trapType");
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		final boolean hotKeyPressed = ctx.getClient().isKeyPressed(KeyCode.KC_SHIFT);
		if (hotKeyPressed && Variables.PAUSED) {
			final Tile selectedSceneTile = ctx.getClient().getSelectedSceneTile();

			if (selectedSceneTile == null) return;

			final WorldPoint worldPoint = WorldPoint.fromLocalInstance(ctx.getClient(), selectedSceneTile.getLocalLocation());
			getMethods().addTile(worldPoint);
			e.consume();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void onChange(ConfigChangeEvent event) {
		String keyName = event.getNewConfig().getKeyName();

		if (keyName == "huntType" || keyName == "updateTiles" || keyName == "salamanderType") {
			if (getType() != TYPES.BUTTERFLIES) {
				Constants.CURRENT_TILES.clear();
				Constants.POSSIBLE_TILES.clear();
				getMethods().fillTempTiles();
			}
		}

		if (keyName == "resetTiles") {
			Constants.CURRENT_TILES.clear();
			Constants.POSSIBLE_TILES.clear();
		}
	}

	public void draw(Graphics2D graphics, Actor actor, String text, Color color) {
		Polygon poly = actor.getCanvasTilePoly();
		if (poly != null) {
			OverlayUtil.renderPolygon(graphics, poly, color);
		}

		Point textLocation = actor.getCanvasTextLocation(graphics, text, actor.getLogicalHeight());
		if (textLocation != null) {
			OverlayUtil.renderTextLocation(graphics, textLocation, text, color);
		}
	}

}
