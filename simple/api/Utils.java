package simple.api;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import net.runelite.api.coords.WorldPoint;
import simple.hooks.simplebot.Game.Tab;
import simple.hooks.simplebot.Magic.SpellBook;
import simple.hooks.wrappers.SimpleObject;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;
import simple.robot.utils.WorldArea;

public class Utils {

	public static boolean openBank() {
		if (!ClientContext.instance().bank.bankOpen()) {
			if (ClientContext.instance().inventory.itemSelectionState() == 1) {
				ClientContext.instance().pathing.running(!ClientContext.instance().pathing.running());
				return false;
			}
			SimpleObject bank = ClientContext.instance().objects.populate().filter("Bank booth", "Bank Chest").nearest().next();
			if (bank != null && bank.validateInteractable() && (bank.click("Bank") || bank.click("Use")))
				ClientContext.instance().onCondition(() -> ClientContext.instance().bank.bankOpen());
		}
		return ClientContext.instance().bank.bankOpen();
	}

	public static String formatString(String... str) {
		return Arrays.asList(str).stream().map(t -> t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}

	public static boolean directTeleport(String teleport) {
		ClientContext ctx = ClientContext.instance();
		ctx.bank.closeBank();
		ctx.shop.closeShop();

		openTab(Tab.MAGIC);
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

	public static void openTab(Tab tab) {
		if (!isTabOpen(tab)) ClientContext.instance().game.tab(tab);
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

	public static WorldArea makeArea(int x, int y, int x2, int y2, int z) {
		return new WorldArea(new WorldPoint(x, y, z), new WorldPoint(x2, y2, z));
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
		if (!ret.endsWith("B") || ret.endsWith("0B")) ret = ret.replaceAll("\\.\\d+", "");
		return ret;

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

		openTab(Tab.OPTIONS);

		SimpleWidget widget = ctx.widgets.getWidget(261, 8 + zoom);
		if (widget != null && widget.visibleOnScreen()) widget.click(0);
	}

	public static BufferedImage getImage(String path, String url) {
		try {
			String[] split = url.split("/");
			return ImageIO.read(new File(path + split[split.length - 1]));
		} catch (IOException e) {
			return downloadImage(path, url);
		}
	}

	public static BufferedImage downloadImage(String path, String str) {
		BufferedImage image = null;
		try {
			final URL url = new URL(str);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
			image = ImageIO.read(connection.getInputStream());
			if (image != null) {
				String[] split = str.split("/");
				ImageIO.write(image, "png", new File(path + "/" + split[split.length - 1]));
			}
			return image;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean validWidget(SimpleWidget w) {
		return w != null && w.visibleOnScreen();
	}

}
