package api.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import api.Variables;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.simplebot.Game.Tab;
import simple.hooks.simplebot.Magic.SpellBook;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class Utils {
	private static Pattern regex = Pattern.compile("\\.(\\d+)[A-z]$");

	public static int distance(WorldPoint a) {
		return a.distanceTo(ClientContext.instance().players.getLocal().getLocation());
	}

	public static String formatComma(long start) {
		return new DecimalFormat("#,###,###,###").format(start);
	}

	public static String formatNumber(long num) {
		String[] suffix = new String[] { "K", "M", "B", "T" };
		int size = (num != 0) ? (int) Math.log10(num) : 0;
		if (size >= 3) {
			while (size % 3 != 0) {
				size = size - 1;
			}
		}
		String ret = (size >= 3) ? (+(Math.round((num / Math.pow(10, size)) * 10) / 10d) + suffix[(size / 3) - 1]) : +num + "";

		// if (!ret.endsWith("B") || ret.endsWith("0B"))
		// ret = ret.replaceAll("\\.\\d+", "");

		return ret;

	}

	public static String repeat(char what, int howmany) {
		if (howmany < 1) return new String();
		char[] chars = new char[howmany];
		Arrays.fill(chars, what);
		return new String(chars);
	}

	public static long reverseFormat(String start) {
		start = start.toLowerCase();
		Matcher m = regex.matcher(start);
		int zero = start.endsWith("k") ? 2 : start.endsWith("m") ? 5 : start.endsWith("b") ? 8 : 0;

		if (!start.contains(".")) zero++;
		if (m.find()) {
			int count = m.group(1).length();
			zero = zero - (count - 1);
		}
		return Long.parseLong(start.replace(".", "").replaceAll("k", repeat('0', zero)).replaceAll("m", repeat('0', zero))
				.replaceAll("b", repeat('0', zero)));
	}

	public static WorldPoint getPlayerLocation() {
		return ClientContext.instance().players.getLocal().getLocation();
	}

	public static WorldArea makeArea(int x, int y, int x2, int y2, int z) {
		return new WorldArea(new WorldPoint(x, y, z), new WorldPoint(x2, y2, z));
	}

	public static boolean directTeleport(String teleport) {
		ClientContext ctx = ClientContext.instance();
		ctx.bank.closeBank();
		ctx.shop.closeShop();

		while (!openTab(Tab.MAGIC)) {
		}
		SimpleWidget widget = ctx.widgets.getWidget(218, ctx.magic.spellBook() == SpellBook.MODERN ? 5 : 99);
		Variables.STATUS = "Teleporting to " + teleport;
		String[] actions = widget.getWidget().getActions();
		boolean action = actions.length > 0 && actions[actions.length - 1].contains(teleport);
		if (!action) return false;
		if (widget.click(3)) {
			ctx.sleep(450);
			ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() != -1, 450);
			ctx.sleepCondition(() -> ctx.players.getLocal().getAnimation() == -1, 550);
			ctx.sleep(1500);
		}
		return true;
	}

	public static boolean openTab(Tab tab) {
		if (!isTabOpen(tab)) ClientContext.instance().game.tab(tab);
		return isTabOpen(tab);
	}

	public static boolean isTabOpen(Tab tab) {
		return ClientContext.instance().game.tab().equals(tab);
	}

	public static boolean license(String url) throws Exception {
		URLConnection conn = new URL(url).openConnection();
		List<Integer> ids = new ArrayList<Integer>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
			reader.lines().mapToInt(Integer::parseInt).forEach(ids::add);
		}
		return ids.contains(ClientContext.instance().user.forumsId());
	}

	public static String getValue(Class<?> reflectClass, String val) {
		Annotation[] anno = reflectClass.getAnnotations();
		Class<? extends Annotation> type = anno[0].annotationType();
		return (String) Stream.of(type.getDeclaredMethods()).filter(m -> m.getName().equalsIgnoreCase(val)).map(m -> {
			try {
				return m.invoke(anno[0], (Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}).findFirst().orElse(null);
	}

	public static void setZoom(int zoom) {
		ClientContext ctx = ClientContext.instance();
		ctx.viewport.pitch(100);
		ctx.viewport.angle(0);

		if (!openTab(Tab.OPTIONS)) return;

		SimpleWidget widget = ctx.widgets.getWidget(261, 8 + zoom);
		if (widget != null && widget.visibleOnScreen()) widget.click(0);
	}

	public static String formatString(String... str) {
		return Arrays.asList(str).stream().map(t -> t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}

	final static String tregex = "hint: (.*)";
	final static Pattern pattern = Pattern.compile(tregex);

	public static void doTriva() {
		ClientContext ctx = ClientContext.instance();
		SimpleWidget trivia = ctx.widgets.getWidget(162, 44);
		if (trivia != null && trivia.visibleOnScreen()) {
			String text = trivia.getText();
			ctx.log(text);
			if (text.length() == 0) return;
			Matcher matcher = pattern.matcher(text);
			if (matcher.find()) {
				final String answer = matcher.group(8);
				ctx.keyboard.sendKeys(answer, true);
			}
			return;
		}
	}

	public static boolean validWidget(SimpleWidget w) {
		return w != null && w.visibleOnScreen();
	}

	public static Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread th, Throwable ex) {
			ClientContext.instance().log("Uncaught exception: " + ex);
			StackTraceElement[] stackTrace = ex.getStackTrace();
			for (StackTraceElement st : stackTrace) {
				ClientContext.instance().log("Stacktrace: " + st);
			}
			// ctx.stopScript();
		}
	};

}
