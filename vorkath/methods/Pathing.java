package vorkath.methods;

import java.util.Arrays;

import api.Tasks;
import api.utils.Utils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;
import vorkath.Vorkath;
import vorkath.data.Constants;

@RequiredArgsConstructor
public class Pathing {

	@NonNull
	private ClientContext ctx;
	@NonNull
	private Vorkath core;

	public WorldArea getInstancedArea() {
		SimpleObject chunk = ctx.objects.populate().filter(ObjectID.ICE_CHUNKS_31990).next();
		if (chunk == null) return null;
		int x = chunk.getLocation().getX();
		int y = chunk.getLocation().getY();
		return Utils.makeArea(x - 10, y + 1, x + 10, y + 24, 0);
	}

	public WorldPoint getFireBallTile() {
		WorldPoint loc = ctx.players.getLocal().getPlayer().getWorldLocation();
		WorldPoint east = new WorldPoint(loc.getX() + 3, loc.getY(), loc.getPlane());
		WorldPoint west = new WorldPoint(loc.getX() - 3, loc.getY(), loc.getPlane());
		return ctx.pathing.reachable(east) ? east : west;
	}

	public WorldPoint start, west, east;

	public void handleAcid() {
		while (ctx.getClient().isInInstancedRegion() && core.getMethods().acidActive()) {
			if (!Constants.PATH_SET) {
				ctx.log("Path is not set");
				start = west = east = null;
				ctx.log(Constants.CURRENT_ACID_TILES.size() + "");
				int distance = 4;
				start = Arrays.stream(getInstancedArea().getWorldPoints()).filter(tile -> ctx.pathing.distanceTo(tile) < 10)
						.filter(tile -> {
							return Arrays.stream(getTiles(tile, distance))
									.noneMatch(point -> Constants.CURRENT_ACID_TILES.contains(point));
						}).sorted((a, b) -> (int) ctx.pathing.distanceTo(a) - (int) ctx.pathing.distanceTo(b)).findFirst()
						.orElse(null);
				if (start != null) {
					Constants.PATH_SET = true;
					ctx.log("Set path");
					west = new WorldPoint(start.getX() - distance, start.getY(), start.getPlane());
					east = new WorldPoint(start.getX() + distance, start.getY(), start.getPlane());
					System.out.println(west);
				} else {
					ctx.log("NONE");
				}
			} else {
				if (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 20) core.getMethods().teleportHome();
				if (ctx.players.getLocal().getLocation().getX() != start.getX()) ctx.pathing.step(start);
				else if (ctx.pathing.distanceTo(east) <= 1) ctx.pathing.step(start);
				else if (ctx.pathing.distanceTo(start) >= 1) ctx.pathing.step(east);
				if (Tasks.getSkill().getPercentage(Skills.HITPOINTS) < 40) Tasks.getSupplies().eat();
			}
		}
		Constants.PATH_SET = false;
	}

	public WorldPoint[] getTiles(WorldPoint loc, int range) {
		WorldPoint[] locs = new WorldPoint[range * 2 + 1];
		for (int i = -range; i <= range; i++)
			locs[i + range] = new WorldPoint(loc.getX() + i, loc.getY(), loc.getPlane());
		return locs;
	}

}
