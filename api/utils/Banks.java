package api.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import api.Tasks;
import api.Variables;
import api.simple.KSNPC;
import api.simple.KSObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import simple.robot.api.ClientContext;

@AllArgsConstructor
@Getter
public enum Banks {

	CAMELOT("Cities", "Camelot", 10806, false, Type.OBJECT, "Bank booth"),
	CANAFIS("Cities", "Canifis", 13878, false, Type.OBJECT, "Bank Booth"),
	CATHERBY("Cities", "CATHERBY", 11061, true, Type.OBJECT, "Bank booth"),
	DRAYNOR("Cities", "Draynor", 12338, true, Type.OBJECT, "Bank booth"),
	LANDS_END("Cities", "Land's End", 5941, false, Type.OBJECT, "Bank chest"),
	NEITIZNOT("Cities", "Neitiznot", 9275, false, Type.OBJECT, "Bank chest"),
	SHILO_VILLAGE("Cities", "Shilo Village", 11310, false, Type.NPC, "Banker"),
	YANILLE("Cities", "Yanille", 10288, false, Type.NPC, "Banker"),
	PISCATORIS("Cities", "Piscatoris", 9273, false, Type.NPC, "Arnold Lydspor"),
	WINTERDTOTD("Skilling", "Firemaking: Wintertodt", 6461, true, Type.OBJECT, "Bank chest"),
	ANGLERFISH("Skilling", "Fishing: Anglerfish", 7227, false, Type.OBJECT, "Bank booth"),
	MLM("Skilling", "Mining: Motherlode mine", 14936, true, Type.OBJECT, "Bank chest"),
	// VARROCK_ANVIL("Skilling", "Smithing: Varrock Anvils", 12597, false,
	// Type.NPC, "Banker"),
	CASTLE_WARS("Minigames", "Castle Wars", 9776, true, Type.OBJECT, "Bank chest"),
	PEST_CONTROL("Minigames", "Pest Control", 10537, false, Type.OBJECT, "Bank booth"),;

	private String category, name;
	private int region;
	private boolean preset;

	private Type type;
	private String obj;

	public enum Type {
		NPC,
		OBJECT
	}

	public Object get() {
		ClientContext ctx = ClientContext.instance();
		return type.equals(Type.NPC) ? new KSNPC(ctx.npcs.populate().filter(obj).nearest().next())
				: new KSObject(ctx.objects.populate().filter(obj).nearest().next());
	}

	public boolean teleport() {
		ClientContext ctx = ClientContext.instance();

		if (ctx.players.getLocal().getLocation().getRegionID() != region) {
			if (!Utils.directTeleport(name) && Tasks.getTeleporter().open())
				Tasks.getTeleporter().teleportStringPath(category, name);
			ctx.onCondition(() -> ctx.players.getLocal().getLocation().getRegionID() == region, 2500);
		}
		return ctx.players.getLocal().getLocation().getRegionID() == region;
	}

	public boolean bank(boolean open) {
		ClientContext ctx = ClientContext.instance();

		if (!ctx.bank.bankOpen()) {
			if (type.equals(Type.NPC)) {
				KSNPC banker = (KSNPC) get();
				if (banker.isNull()) {
					ctx.log("Banker is null");
					return false;
				}
				if (banker.distanceTo(ctx.players.getLocal()) > 5) {
					Variables.STATUS = "Walking to banker";
					ctx.pathing.step(banker.getLocation());
					ctx.sleep(450, 650);
					return false;
				}
				Variables.STATUS = "Opening bank";
				if (open && banker.validateInteractable() && banker.click("Bank"))
					ctx.onCondition(() -> ctx.bank.bankOpen(), 2500);
				return open ? ctx.bank.bankOpen() : banker.distanceTo(ctx.players.getLocal()) < 5;
			} else {
				KSObject bank = (KSObject) get();
				if (bank.isNull()) {
					ctx.log("Banker is null");
					return false;
				}
				if (bank.distanceTo(ctx.players.getLocal()) > 10) {
					Variables.STATUS = "Walking to bank";
					ctx.pathing.step(bank.getLocation());
					ctx.sleep(450, 650);
					return false;
				}
				Variables.STATUS = "Opening bank";
				if (open && bank.validateInteractable() && bank.click(obj.contains("chest") ? "Use" : "Bank"))
					ctx.onCondition(() -> ctx.bank.bankOpen(), 2500);
				return open ? ctx.bank.bankOpen() : bank.distanceTo(ctx.players.getLocal()) < 10;
			}
		}
		return false;
	}

	public static Banks getRandomBank(boolean preset) {
		Random r = new Random();

		List<Banks> b = Arrays.stream(Banks.values()).filter(p -> preset ? p.isPreset() : p != null).collect(Collectors.toList());
		return b.get(r.nextInt(b.size()));
	}

}