package api.utils;

import java.util.ArrayList;
import java.util.List;

import simple.hooks.wrappers.SimpleItem;
import simple.robot.api.ClientContext;

public class BankCheck {

	public static List<SimpleItem> CHECK_LIST = new ArrayList<SimpleItem>();

	public static boolean bankContains() {
		return !CHECK_LIST.stream().anyMatch(item -> {
			return ClientContext.instance().bank.populate().filter(item.getName()).population(true) < item.getQuantity();
		});
	}

}
