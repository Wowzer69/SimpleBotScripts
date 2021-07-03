package api;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

import net.runelite.api.MenuAction;
import net.runelite.api.VarClientStr;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.MenuOptionClicked;
import simple.hooks.wrappers.SimpleGroundItem;
import simple.hooks.wrappers.SimpleItem;
import simple.hooks.wrappers.SimpleNpc;
import simple.hooks.wrappers.SimpleObject;
import simple.robot.api.ClientContext;

public class MenuActions {

	private static Class<?> _class;
	private static Method action;
	private ClientContext ctx;

	private MenuOptionClicked builder;

	public MenuActions(ClientContext ctx) {
		this.ctx = ctx;
	}

	public boolean invoke() {
		if (builder == null) return false;
		return invoke(builder);
	}

	public MenuActions setAction(int arg0) {
		this.builder.setActionParam(arg0);
		return this;
	}

	public MenuActions setMenuAction(MenuAction arg0) {
		this.builder.setMenuAction(arg0);
		return this;
	}

	public MenuActions setMenuAction(int arg0) {
		this.builder.setMenuAction(MenuAction.of(arg0));
		return this;
	}

	public MenuActions setId(int arg0) {
		this.builder.setId(arg0);
		return this;
	}

	public MenuActions setWidget(int arg0) {
		this.builder.setWidgetId(arg0);
		return this;
	}

	public static boolean invoke(String option, String target, int action, int menuAction, int id, int widgetId) {
		MenuOptionClicked menu = new MenuOptionClicked();
		menu.setActionParam(action);
		menu.setMenuOption(option);
		menu.setMenuTarget(target);
		menu.setMenuAction(MenuAction.of(menuAction));
		menu.setId(id);
		menu.setWidgetId(widgetId);
		return invoke(menu);
	}

	public static boolean invoke(MenuOptionClicked option) {
		try {
			if (_class == null) _class = Class.forName("client");
			if (action == null) {
				action = _class.getDeclaredMethod("a", int.class, int.class, int.class, int.class, java.lang.String.class,
						java.lang.String.class, int.class, int.class, int.class);
			}

			if (option == null) return false;
			action.invoke(_class.newInstance(), option.getActionParam(), option.getWidgetId(), option.getMenuAction().getId(),
					option.getId(), option.getMenuOption(), option.getMenuTarget(), -1, -1, (byte) 89);
			return true;
		} catch (Exception e1) {
			System.out.println(option);
			e1.printStackTrace();
		}
		return false;
	}

	public boolean clickDialogue(int action) {
		return invoke("", "", action, MenuAction.WIDGET_TYPE_6.getId(), 0, 14352385);
	}

	public boolean clickInterface(int action) {
		return invoke("", "", action, MenuAction.WIDGET_TYPE_6.getId(), 0, 12255235);
	}

	public boolean withdraw(SimpleItem item, String action) {
		if (item == null) return false;
		ClientContext ctx = ClientContext.instance();
		int length = ctx.bank.populate().population();
		SimpleItem[] items = ctx.bank.toStream().toArray(SimpleItem[]::new);
		int actionIndex = IntStream.range(0, length).filter(val -> {
			return items[val] != null && items[val].getName().equalsIgnoreCase(item.getName());
		}).findFirst().orElse(-1);

		if (actionIndex == -1) return false;
		int option = 1;
		switch (action) {
			case "1":
				option = 2;
				break;
			case "5":
				option = 3;
				break;
			case "10":
				option = 4;
				break;
			case "all":
				option = 7;
				break;
			default:
				option = 5;
				int amt = Integer.parseInt(action);
				if (ctx.bank.withdrawXAmount() != amt) {
					MenuActions.invoke("", "", -1, MenuAction.CC_OP.getId(), 2, 786465);
					ctx.onCondition(() -> ctx.dialogue.pendingInput());
					ctx.getClient().setVar(VarClientStr.INPUT_TEXT, amt + "");
					ctx.keyboard.sendKeys("", true);
					ctx.onCondition(() -> ctx.bank.withdrawXAmount() == amt);
				}
				break;
		}
		return invoke("", "", actionIndex, MenuAction.CC_OP.getId(), option, 786444);
	}

	public MenuActions get(SimpleItem item, String action) {
		if (item == null) return null;
		MenuAction option = MenuAction.ITEM_FIRST_OPTION;
		switch (action.toLowerCase()) {
			case "wield":
			case "wear":
				option = MenuAction.ITEM_SECOND_OPTION;
				break;
			case "rub":
				option = MenuAction.ITEM_THIRD_OPTION;
				break;
			case "check":
			case "empty":
				option = MenuAction.ITEM_FOURTH_OPTION;
				break;
			case "drop":
				option = MenuAction.ITEM_DROP;
				break;
			case "examine":
				option = MenuAction.EXAMINE_ITEM;
				break;
			case "cancel":
				option = MenuAction.CANCEL;
				break;
			case "use":
				option = MenuAction.ITEM_USE;
				break;
			case "use with":
				option = MenuAction.ITEM_USE_ON_WIDGET;
				break;
		}

		int index = item.getInventoryIndex();
		builder = new MenuOptionClicked();
		builder.setActionParam(index);
		builder.setMenuOption("");
		builder.setMenuTarget("");
		builder.setMenuAction(option);
		builder.setId(item.getId());
		builder.setWidgetId(9764864);
		return this;
	}

	public MenuActions get(SimpleNpc npc, String str) {
		if (npc == null) return null;

		MenuAction option = MenuAction.NPC_FIRST_OPTION;
		String[] actions = npc.getActions();

		int actionIndex = IntStream.range(0, actions.length).filter(val -> {
			String action = actions[val];
			return action != null && action.toLowerCase().contains(str.toLowerCase());
		}).findFirst().orElse(-1);

		if (actionIndex == -1) {
			System.out.println("Action index: " + actionIndex);
			return null;
		}

		switch (actionIndex) {
			case 1:
				option = MenuAction.NPC_SECOND_OPTION;
				break;
			case 2:
				option = MenuAction.NPC_THIRD_OPTION;
				break;
			case 3:
				option = MenuAction.NPC_FOURTH_OPTION;
				break;
			case 4:
				option = MenuAction.NPC_FIFTH_OPTION;
				break;
			case 5:
				option = MenuAction.EXAMINE_NPC;
				break;
		}

		int index = npc.getNpc().getIndex();

		builder = new MenuOptionClicked();
		builder.setActionParam(0);
		builder.setMenuOption("");
		builder.setMenuTarget("");
		builder.setMenuAction(option);
		builder.setId(index);
		builder.setWidgetId(0);
		return this;
	}

	public MenuActions get(SimpleObject object, String str) {
		if (object == null) return null;
		LocalPoint loc = object.getTileObject().getLocalLocation();
		MenuAction option = MenuAction.GAME_OBJECT_FIRST_OPTION;
		String[] actions = object.getActions();

		int actionIndex = IntStream.range(0, actions.length).filter(val -> {
			String action = actions[val];
			return action != null && action.toLowerCase().contains(str.toLowerCase());
		}).findFirst().orElse(-1);

		if (actionIndex == -1) return null;

		switch (actionIndex) {
			case 0:
				option = MenuAction.GAME_OBJECT_FIRST_OPTION;
				break;
			case 1:
				option = MenuAction.GAME_OBJECT_SECOND_OPTION;
				break;
			case 2:
				option = MenuAction.GAME_OBJECT_THIRD_OPTION;
				break;
			case 3:
				option = MenuAction.GAME_OBJECT_FOURTH_OPTION;
				break;
			case 4:
				option = MenuAction.GAME_OBJECT_FIFTH_OPTION;
				break;
		}

		builder = new MenuOptionClicked();
		builder.setActionParam(loc.getSceneX());
		builder.setMenuOption("");
		builder.setMenuTarget("");
		builder.setMenuAction(option);
		builder.setId(object.getId());
		builder.setWidgetId(loc.getSceneY());

		return this;
	}

	public MenuActions itemOnNPC(SimpleItem item, SimpleNpc npc) {
		if (item == null || npc == null) return null;
		if (ClientContext.instance().inventory.itemSelectionState() == 0) get(item, "Use").invoke();

		builder = new MenuOptionClicked();
		builder.setActionParam(0);
		builder.setMenuOption("");
		builder.setMenuTarget("");
		builder.setMenuAction(MenuAction.ITEM_USE_ON_NPC);
		builder.setId(npc.getNpc().getIndex());
		builder.setWidgetId(0);
		return this;
	}

	public MenuActions itemOnObject(SimpleItem item, SimpleObject object) {
		if (item == null || object == null) return null;
		if (ClientContext.instance().inventory.itemSelectionState() == 0) get(item, "Use").invoke();
		LocalPoint loc = object.getTileObject().getLocalLocation();

		builder = new MenuOptionClicked();
		builder.setActionParam(loc.getSceneX());
		builder.setMenuOption("");
		builder.setMenuTarget("");
		builder.setMenuAction(MenuAction.ITEM_USE_ON_GAME_OBJECT);
		builder.setId(object.getId());
		builder.setWidgetId(loc.getSceneY());
		return this;
	}

	public MenuActions get(SimpleGroundItem item, String action) {
		if (item == null) return null;
		MenuAction option = MenuAction.GROUND_ITEM_THIRD_OPTION;
		LocalPoint loc = item.getTile().getLocalLocation();

		switch (action.toLowerCase()) {
			case "examine":
				option = MenuAction.EXAMINE_ITEM_GROUND;
				break;
		}

		builder = new MenuOptionClicked();
		builder.setActionParam(loc.getSceneX());
		builder.setMenuOption("");
		builder.setMenuTarget("");
		builder.setMenuAction(option);
		builder.setId(item.getId());
		builder.setWidgetId(loc.getSceneY());
		return this;
	}

}
