package api.tasks;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import api.Tasks;
import api.simple.KSItem;
import simple.hooks.filters.SimpleEquipment.EquipmentSlot;
import simple.hooks.queries.SimpleItemQuery;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.robot.api.ClientContext;

public class Inventory {
	private ClientContext ctx;

	public Inventory(ClientContext ctx) {
		this.ctx = ctx;
	}

	public boolean predicate(SimpleItem item, String... itemName) {
		Stream<String> array = Stream.of(itemName);
		Predicate<String> filter = arr -> item.getName().toLowerCase().contains(arr.toLowerCase());
		return array.anyMatch(filter);
	}

	public boolean predicate(SimpleGroundItem item, String... itemName) {
		Stream<String> array = Stream.of(itemName);
		Predicate<String> filter = arr -> item.getName().toLowerCase().contains(arr.toLowerCase());
		return array.anyMatch(filter);
	}

	public SimpleItemQuery<SimpleItem> filter(String... itemName) {
		return ctx.inventory.populate().filter(p -> predicate(p, itemName));
	}

	public boolean contains(String... itemName) {
		return filter(itemName).population() > 0;
	}

	public boolean containsAll(int... itemId) {
		List<Integer> inv = ctx.inventory.populate().toStream().map(item -> item.getId()).collect(Collectors.toList());
		return itemId.length == Arrays.stream(itemId).filter(inv::contains).count();
	}

	public boolean containsAll(String... itemName) {
		List<String> inv = ctx.inventory.populate().toStream().map(item -> item.getName()).map(String::toLowerCase)
				.collect(Collectors.toList());
		return itemName.length == Arrays.stream(itemName).map(String::toLowerCase)
				.filter(val -> inv.stream().anyMatch(arr -> arr.contains(val))).count();
	}

	public SimpleItemQuery<SimpleItem> getItems(String... itemName) {
		return filter(itemName);
	}

	public SimpleItem getItem(String... itemName) {
		return getItems(itemName).next();
	}

	public boolean isWearing(EquipmentSlot slot, String... name) {
		String equipped = ctx.equipment.getEquippedItem(slot).getName().toLowerCase();

		return ctx.equipment.getEquippedItem(slot) != null
				&& Arrays.stream(name).anyMatch(item -> equipped.contains(item.toLowerCase()));
	}

	public void equip(String... name) {
		equip(false, name);
	}

	public void equip(boolean validate, String... name) {
		KSItem item = new KSItem(getItem(name));
		if (item.isNull()) return;

		if (item.click("Wear")) {
			if (validate) ctx.onCondition(() -> !ctx.equipment.populate().filter(item.getName()).isEmpty());
		}
	}

	public void equipAll(SimpleItemQuery<SimpleItem> items) {
		items.forEach(item -> {
			KSItem i = new KSItem(item);

			if (!i.isNull()) i.click("Wear");
		});
	}

	public void equipAll(String... name) {
		equipAll(getItems(name));
	}

	public void equipGuthans() {
		if (Tasks.getInventory().contains("guthan")) {
			if (Tasks.getInventory().contains("warspear") && ctx.inventory.inventoryFull()) {
				if (Tasks.getSupplies().eat()) ctx.onCondition(() -> !ctx.inventory.inventoryFull(), 2500);
			} else Tasks.getInventory().equipAll("guthan");
		}
	}
}
