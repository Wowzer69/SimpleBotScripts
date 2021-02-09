package api.tasks;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import api.Locations;
import api.Tasks;
import api.Variables;
import net.runelite.api.Friend;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.wrappers.SimplePlayer;
import simple.robot.api.ClientContext;
import simple.robot.utils.ScriptUtils;

public class AntiBan {

	public List<String> STAFF_NAMES = Arrays.asList("david", "hope", "polar", "spooky", "chaflie", "corey", "klem",
			"professor oak", "adreas", "pegasus", "perfection", "raids", "setup", "trilobita", "kenzz", "spirit", "leaned",
			"paine", "h a r r y", "scape", "mamba", "supreme", "isleview", "kenzz", "andy", "fe chaflie", "zaros", "v12",
			"zachery", "vcx", "hc wizard", "immortal fox", "calabria", "listen", "niedermayer", "jake", "julia", "harsh", "hans");

	private ClientContext ctx;

	public AntiBan(ClientContext ctx) {
		this.ctx = ctx;
	}

	public boolean check() {
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA) && Tasks.getAntiban().staffNearby()) return true;
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_BANK) && Tasks.getAntiban().staffUnder()) return true;
		return false;
	}

	public boolean staffNearby() {
		List<SimplePlayer> players = ctx.players.populate().toStream().filter(val -> {
			return STAFF_NAMES.contains(ScriptUtils.stripHtml(val.getName()).toLowerCase());
		}).collect(Collectors.toList());
		players.forEach(player -> ctx.log("[STAFF NEARBY]" + player.getName() + " : " + player.getLocation()));
		return players.size() > 0;
	}

	public boolean staffInChat() {
		List<Friend> players = Arrays.stream(ctx.getClient().getFriendContainer().getMembers())
				.filter(val -> STAFF_NAMES.contains(ScriptUtils.stripHtml(val.getName()).toLowerCase()))
				.collect(Collectors.toList());
		players.forEach(player -> ctx.log("[STAFF IN CHAT]" + player.getName()));
		return players.size() > 0;
	}

	public boolean staffUnder() {
		WorldPoint loc = ctx.players.getLocal().getLocation();
		List<SimplePlayer> players = ctx.players.populate().toStream().filter(val -> {
			return STAFF_NAMES.contains(ScriptUtils.stripHtml(val.getName()).toLowerCase()) && val.getLocation().equals(loc);
		}).collect(Collectors.toList());
		players.forEach(player -> ctx.log("[STAFF UNDER]" + player.getName() + " : " + player.getLocation()));
		return players.size() > 0;
	}

	public void panic() {
		if (!ctx.pathing.inArea(Locations.EDGEVILLE_AREA)) {
			ctx.magic.castSpellOnce("Home Teleport");
		} else if (!ctx.players.getLocal().inCombat()) {
			while (ctx.getClient().getGameState() == GameState.LOGGED_IN) {
				ctx.sendLogout();
			}
		}
	}
}
