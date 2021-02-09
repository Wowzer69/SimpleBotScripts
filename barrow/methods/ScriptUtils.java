package barrow.methods;

import java.util.stream.Stream;

import api.Locations;
import api.Tasks;
import api.Variables;
import barrow.Barrows;
import net.runelite.api.NPC;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;

public class ScriptUtils {

	public ClientContext ctx;

	public Barrows main;

	public ScriptUtils(ClientContext ctx, Barrows main) {
		this.ctx = ctx;
		this.main = main;
	}

	public void exitCrypt() {
		SimpleObject stairs = ctx.objects.populate().filter("Staircase").nearest().next();
		if (stairs != null && stairs.validateInteractable()) {
			stairs.click("Climb");
			ctx.sleepCondition(() -> ctx.players.getLocal().getLocation().getPlane() != 3, 500);
		}
	}

	public SimpleNpc aggressiveNPC() {
		NPC target = ctx.getClient().getHintArrowNpc();
		if (target != null) {
			SimpleNpc other = new SimpleNpc(target);
			if (other != null) return other;
		}
		return ctx.npcs.populate().filter(
				n -> n.getInteracting() != null && n.getInteracting().equals(ctx.players.getLocal().getPlayer()) && isBrother(n))
				.nearest().next();

	}

	private boolean isBrother(SimpleNpc n) {
		for (Brothers bro : Brothers.values()) {
			if (n.getName().toLowerCase().contains(bro.name().toLowerCase())) { return true; }
		}
		return false;
	}

	public void searchCoffin(String option) {
		SimpleObject coffin = ctx.objects.populate().filter("Sarcophagus", "Chest").nearest().next();
		if (coffin != null) {
			// ctx.viewport.turnTo(coffin);

			coffin.click(0);
			ctx.sleep(350, 500);
			while (ctx.pathing.inMotion()) {
				ctx.sleep(20, 40);
			}

			if (ctx.dialogue.canContinue() && ctx.dialogue.getDialogueTitleAndMessage()[1].contains("you find a hidden tunnel")) {
				Brothers b = currentBrother();
				if (b != null && Brothers.getTunnelBrother() == null) {
					b.setTunnel(true);
					ctx.log("Found current boss: %s", b.name());
				}

			}
		}
	}

	public void dig() {
		SimpleItem spade = ctx.inventory.populate().filter(952).next();
		if (spade != null) {
			spade.click("Dig");
		}
	}

	public Brothers currentBrother() {
		Brothers currentRoom = null;
		try {
			if (ctx.pathing.inArea(Locations.BARROWS_FINAL_SARCO)) { return Brothers.getTunnelBrother(); }
			currentRoom = Stream.of(Brothers.values()).filter(b -> ctx.pathing.inArea(b.getTunnelArea())).findFirst()
					.orElse(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return currentRoom;
	}

	public boolean checkWeapon() {
		return Tasks.getInventory().contains("Uncharged trident")
				|| Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "Uncharged trident");
	}

	public void fillStaff() {
		if (!checkWeapon()) return;
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			ctx.magic.castSpellOnce("Home");
			return;
		}

		int fireRune = ctx.inventory.populate().filter("Fire Rune").population(true);
		int chaosRune = ctx.inventory.populate().filter("Chaos rune").population(true);
		int deathRune = ctx.inventory.populate().filter("Death Rune").population(true);
		int coins = ctx.inventory.populate().filter("Coins").population(true);

		if (fireRune < 100 || chaosRune < 100 || deathRune < 100 || coins < 1000) {
			Variables.STATUS = "Grabbing supplies from bank";
			if (Tasks.getBanking().open()) {
				ctx.bank.withdraw("Fire Rune", 12495 - fireRune);
				ctx.bank.withdraw("Chaos Rune", 2499 - chaosRune);
				ctx.bank.withdraw("Death Rune", 2499 - deathRune);
				ctx.bank.withdraw("Coins", 24990 - coins);
			}
		} else if (!Tasks.getInventory().contains("Uncharged trident")
				&& Tasks.getInventory().isWearing(EquipmentSlot.WEAPON, "Uncharged trident")) {
					ctx.bank.closeBank();
					Variables.STATUS = "Unequipping staff";
					SimpleItem staff = ctx.equipment.populate().filter("Uncharged trident").next();
					if (staff != null) staff.click(0);
				} else {
					SimpleItem invStaff = Tasks.getInventory().getItem("Uncharged trident");
					SimpleItem coin = Tasks.getInventory().getItem("Coins");
					if (invStaff != null && coin != null) {
						if (ctx.dialogue.pendingInput()) {
							ctx.keyboard.sendKeys("2499", true);
							return;
						}
						coin.click("Use");
						ctx.sleep(150);
						invStaff.click(0);
						ctx.sleepCondition(() -> ctx.dialogue.pendingInput(), 1500);
					} else {
						Variables.STATUS = "NO";
					}
				}

	}

}
