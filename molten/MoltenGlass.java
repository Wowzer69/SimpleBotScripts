package molten;

import java.awt.Graphics;
import java.util.stream.Stream;

import javax.swing.JFrame;

import molten.data.Shops;
import net.runelite.api.ChatMessageType;
import net.runelite.api.ItemID;
import simple.api.Utils;
import simple.api.Variables;
import simple.api.panel.Config;
import simple.api.panel.Panel;
import simple.api.panel.Tabs;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.scripts.Category;
import simple.hooks.scripts.LoopingScript;
import simple.hooks.scripts.ScriptManifest;
import simple.hooks.simplebot.ChatMessage;
import simple.hooks.simplebot.Magic.SpellBook;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.script.Script;

@ScriptManifest(author = "KremeSickle", category = Category.CRAFTING, description = "<br>Either purchase the ingredients to create molten glass or use the Superglass Make spell to create it.<br><br>Script will stop once out of coins/supplies.<br><br>To buy supplies, have your last-preset with coins in inventory only and start at selected location<br><br>To cast spell, have your last-preset with astral runes, fire runes, x13 seaweed, x13 bucket of sand & equipped staff of air. Start in Edgeville", discord = "Datev#0660", name = "KS | Molten Glass", servers = {
		"Zaros" }, version = "2", vip = true)
public class MoltenGlass extends Script implements LoopingScript {

	private JFrame frame;
	private Panel panel;

	@Override
	public void onExecute() {
		try {
			Variables.reset();
			addConfig();
			String title = Utils.getValue(getClass(), "name") + " v" + Utils.getValue(getClass(), "version");
			panel = new Panel();
			frame = panel.init(title, panel);
			Utils.setZoom(1);
		} catch (Exception e) {
			ctx.log(e.getMessage());
			e.printStackTrace();
		}
	}

	public enum BUYABLE {
		BUCKET_OF_SAND,
		SEAWEED,
		SODA_ASH
	}

	public void addConfig() {
		Config.TABS.add(new Tabs(0, "Script Config", "Choose your configuration"));
		Config.CONFIGURATION
				.add(new Config(0, boolean.class, false, "Create Molten Glass", "Use the spell to create molten glass", "spell"));
		Config.CONFIGURATION.add(new Config(0, boolean.class, true, "Purchase items", "Purchase Items?", "purchase"));
		Config.CONFIGURATION.add(new Config(0, BUYABLE.class, "BUCKET_OF_SAND", "", "Purchase this item", "itemPurchase"));
		Config.CONFIGURATION.add(new Config(0, Shops.class, "CATHERBY", "", "Location to shop at", "shopLocation"));
		Config.CONFIGURATION.add(new Config(0, boolean.class, false, "Logout on finish", "Logout on finish?", "logout"));

		Config.setConfigChanged(true);
	}

	public void getPreset() {
		SimpleObject bank = ctx.objects.populate().filter(Config.getB("purchase") ? getLocation().getBankId() : 10355).nearest()
				.next();
		if (bank != null && bank.validateInteractable()) {
			Variables.STATUS = "Getting last preset";
			if (bank.click("Last-preset")) {
				ctx.onCondition(() -> ctx.inventory.populate().population() == 1, 500);
			}
		}
	}

	public void bank() {
		SimpleObject bank = ctx.objects.populate().filter(getLocation().getBankId()).nearest().next();
		if (bank == null || bank.distanceTo(ctx.players.getLocal()) > 9) {
			Variables.STATUS = "Walking path to bank";
			if (Config.getB("purchase")) {
				ctx.pathing.walkPath(getLocation().getPath(), true);
				ctx.sleepCondition(() -> ctx.pathing.inMotion(), 1500);
			} else {
				Variables.STATUS = "Teleport to edgeville";
			}
		} else {
			if (!getLocation().isDepositBox()) {
				getPreset();
				return;
			}
			if (!ctx.bank.depositBoxOpen()) {
				Variables.STATUS = "Opening deposit box";
				if (bank.click("Deposit")) ctx.sleepCondition(() -> ctx.bank.depositBoxOpen(), 650);
			} else {
				Variables.STATUS = "Depositing items";
				if (ctx.bank.depositAllExcept(995)) ctx.sleepCondition(() -> ctx.bank.populate().population() == 1, 450);
			}
		}
	}

	public void doBuy() {
		if (ctx.inventory.inventoryFull()) {
			bank();
		} else {
			SimpleNpc npc = ctx.npcs.populate().filter(getLocation().getNpcId()).nearest().next();
			if (npc == null || npc.distanceTo(ctx.players.getLocal()) > 10) {
				Variables.STATUS = "Walking path to npc";
				ctx.pathing.walkPath(getLocation().getPath());
				return;
			}
			if (!ctx.shop.shopOpen()) {
				Variables.STATUS = "Opening shop";
				if (npc.validateInteractable() && npc.click("Trade")) {
					ctx.sleep(75, 150);
					ctx.sleepCondition(() -> ctx.shop.shopOpen());
				}
			} else {
				Variables.STATUS = "Buying ingredients";
				SimpleItem shop = ctx.shop.populate().filter(getItem()).next();
				if (shop.getQuantity() > 0) {
					shop.click("Buy 10");
					ctx.sleep(150, 200);
				}
			}
		}
	}

	public boolean hasItem(int id) {
		return ctx.inventory.populate().filter(id).population() > 0;
	}

	public void doMake() {
		if (hasItem(ItemID.BUCKET_OF_SAND) && (hasItem(ItemID.GIANT_SEAWEED) || hasItem(ItemID.SEAWEED)
				|| hasItem(ItemID.SODA_ASH) || hasItem(ItemID.SWAMP_WEED))) {
			Variables.STATUS = "Casting spell";
			if (ctx.magic.castSpellOnce("Superglass make")) {
				ctx.sleep(350, 600);
				ctx.onCondition(() -> hasItem(ItemID.MOLTEN_GLASS));
			}
		} else {
			getPreset();
		}
	}

	@Override
	public void onProcess() {
		if (Variables.PAUSED) return;
		if (Variables.STOP) {
			if (Config.getB("logout")) ctx.sendLogout();
			else ctx.stopScript();
			return;
		}
		if (!Variables.STARTED) {
			Variables.STATUS = "Waiting to be started";
			return;
		}

		if (Config.getB("purchase")) {
			if (!getLocation().inDistance()) {
				Variables.STATUS = "Teleport near start area";
				return;
			}
			if (!hasCoins()) {
				Variables.STATUS = "Get more coins";
				return;
			}
			doBuy();
		} else if (Config.getB("spell")) {
			if (!hasMagicLevel() || !onLunars() || !hasRunes()) {
				Variables.STATUS = "Get requirements for spell";
				return;
			}
			doMake();
		}
	}

	@Override
	public int loopDuration() {
		return 150;
	}

	@Override
	public void paint(Graphics Graphs) {
		if (panel != null) panel.update(Variables.STATUS);
	}

	final String[] STOP_MESSAGES = { "don't have the required", "not be found:", "enough coins" };

	@Override
	public void onChatMessage(ChatMessage msg) {
		if (msg.getType() == ChatMessageType.GAMEMESSAGE) {
			if (!Variables.STOP) {
				Variables.STOP = Stream.of(STOP_MESSAGES).anyMatch(msg1 -> msg.getMessage().contains(msg1));
			}
		}
	}

	public boolean hasCoins() {
		return ctx.inventory.populate().filter(995).population(true) > 1000;
	}

	public boolean onLunars() {
		return ctx.magic.spellBook() == SpellBook.LUNAR;
	}

	public boolean hasMagicLevel() {
		return ctx.skills.level(Skills.MAGIC) >= 77;
	}

	public boolean hasRunes() {
		return ctx.inventory.populate().filter("Astral rune", "Fire rune").population() == 2;
	}

	@Override
	public void onTerminate() {
		if (frame != null) frame.dispose();
		ctx.log("Shutting down.. Thank you for using the script");
		Variables.reset();
		Config.clear();
	}

	public Shops getLocation() {
		return Config.getValue("shopLocation");
	}

	public String getItem() {
		BUYABLE item = Config.getValue("itemPurchase");
		return Utils.formatString(item.name()).replace("_", " ");
	}

}
