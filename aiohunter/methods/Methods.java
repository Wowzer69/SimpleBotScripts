package aiohunter.methods;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import aiohunter.AIOHunter;
import aiohunter.AIOHunter.TYPES;
import aiohunter.data.Constants;
import aiohunter.data.enums.Trapping;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.runelite.api.ObjectID;
import net.runelite.api.coords.WorldPoint;
import simple.api.Variables;
import simple.hooks.filters.SimpleSkills.Skills;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;
import simple.robot.utils.Random;
import simple.robot.utils.WorldArea;

@RequiredArgsConstructor
public class Methods {

	@NonNull
	private ClientContext ctx;
	@NonNull
	private AIOHunter core;

	public WorldPoint getNextTile() {
		return Constants.CURRENT_TILES.stream().unordered().filter(this::validTile).findAny().orElse(null);
	}

	public boolean validTile(WorldPoint tile) {
		SimpleEntityQuery<SimpleObject> q = ctx.objects.populate().filter(tile);
		if (!q.filter(core.getTrap().getActiveTrap()).isEmpty()) return false;

		boolean v = q.filter(merge(Trapping.getFailedTraps(), Trapping.getCaughtTraps())).isEmpty();
		if (!v) return true;
		return true;
	}

	public void delay(BooleanSupplier arg0) {
		delay(arg0, 500);
	}

	public void delay(BooleanSupplier arg0, int delay) {
		if (Variables.LAST_MESSAGE.contains("you can't lay a trap here")) return;
		ctx.onCondition(() -> ctx.players.getLocal().getAnimation() != -1);
		ctx.onCondition(() -> ctx.players.getLocal().getAnimation() == -1);
		ctx.onCondition(arg0, delay);
	}

	public int[] merge(int[]... val) {
		return Arrays.stream(val).flatMapToInt(i -> Arrays.stream(i)).toArray();
	}

	public void addTrap() {
		if (Constants.POSSIBLE_TILES.size() == 0) return;
		if (Constants.CURRENT_TILES.size() < getTrapAmount()) {
			System.out.println("Tiles left: " + Constants.POSSIBLE_TILES.size());
			ctx.log("Adding another trap");
			int random = Random.between(0, Constants.POSSIBLE_TILES.size() - 1);
			Constants.CURRENT_TILES.add(Constants.POSSIBLE_TILES.get(random));
			Constants.POSSIBLE_TILES.remove(random);
		}
	}

	public int getTrapAmount() {
		int level = ctx.skills.level(Skills.HUNTER);
		return level >= 80 ? 5 : level >= 60 ? 4 : level >= 40 ? 3 : level >= 20 ? 2 : 1;
	}

	public CopyOnWriteArrayList<WorldPoint> getAutoTiles(int range) {
		CopyOnWriteArrayList<WorldPoint> tempArr = new CopyOnWriteArrayList<WorldPoint>();
		if (core.getType().equals(TYPES.SALAMANDERS)) {
			ctx.objects.populate().filter(core.getSalamander().getUnused()).filterWithin(10)
					.forEach(tree -> tempArr.add(tree.getLocation()));
		} else {
			int[] INVALID_TILES = merge(Trapping.getActiveTraps(), Trapping.getCaughtTraps(), Trapping.getFailedTraps(),
					new int[] { ObjectID.FERN_19839, ObjectID.FERN_19840 });

			final WorldArea area = new WorldArea(
					new WorldPoint(Constants.LAST_LOCATION.getX() - range, Constants.LAST_LOCATION.getY() + range,
							Constants.LAST_LOCATION.getPlane()),
					new WorldPoint(Constants.LAST_LOCATION.getX() + range, Constants.LAST_LOCATION.getY() - range,
							Constants.LAST_LOCATION.getPlane()));

			tempArr.addAll(Arrays.asList(area.getWorldPoints()));

			tempArr.removeIf(
					tile -> !ctx.pathing.reachable(tile) || !ctx.objects.populate().filter(tile).filter(INVALID_TILES).isEmpty());
		}
		System.out.println("Found " + tempArr.size() + " valid tiles");

		return tempArr;
	}

	public void addTile(WorldPoint tile) {
		if (Constants.CURRENT_TILES.contains(tile)) Constants.CURRENT_TILES.remove(tile);
		if (Constants.POSSIBLE_TILES.contains(tile)) Constants.POSSIBLE_TILES.remove(tile);
		else Constants.POSSIBLE_TILES.add(tile);
	}

	public void fillTempTiles() {
		if (Constants.LAST_LOCATION == null || !Constants.LAST_LOCATION.equals(ctx.players.getLocal().getLocation())) {
			Constants.LAST_LOCATION = ctx.players.getLocal().getLocation();
		}
		if (!Constants.POSSIBLE_TILES.isEmpty()) return;
		Constants.POSSIBLE_TILES = getAutoTiles(3);
	}

	public boolean hasItems(String... itemName) {
		List<String> inv = ctx.inventory.populate().toStream().map(item -> item.getName()).map(String::toLowerCase)
				.collect(Collectors.toList());
		return itemName.length == Arrays.stream(itemName).map(String::toLowerCase)
				.filter(val -> inv.stream().anyMatch(arr -> arr.contains(val))).count();
	}

}
