package api.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import api.panel.options.Range;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.util.ColorUtil;

@Getter
@RequiredArgsConstructor
public class Config {

	public static List<Tabs> TABS = new ArrayList<>();

	public static List<Config> CONFIGURATION = new ArrayList<>();

	@NonNull
	private int section;
	@NonNull
	private Object type;
	@NonNull
	@Setter
	private Object value;
	@NonNull
	private String text, tooltip, keyName;

	private Range range;
	private boolean changeable = true;
	private int cached = -1;

	public Config(int section, Object type, Object defaultValue, String text, String tooltip, String keyName, Range range) {
		this.section = section;
		this.type = type;
		this.keyName = keyName;
		this.text = text;
		this.value = defaultValue;
		this.tooltip = tooltip;
		this.range = range;
	}

	public Config(int section, Object type, Object defaultValue, String text, String tooltip, String keyName,
			boolean changeable) {
		this.section = section;
		this.type = type;
		this.keyName = keyName;
		this.text = text;
		this.value = defaultValue;
		this.tooltip = tooltip;
		this.changeable = changeable;
	}

	public Config(int section, Object type, Object defaultValue, String text, String tooltip, String keyName, int cached) {
		this.section = section;
		this.type = type;
		this.keyName = keyName;
		this.text = text;
		this.value = defaultValue;
		this.tooltip = tooltip;
		this.cached = cached;
	}

	public static void clear() {
		CONFIGURATION.clear();
		TABS.clear();
	}

	public static Config getItem(String keyName) {
		return Config.CONFIGURATION.stream().filter(val -> val.getKeyName().equals(keyName)).findFirst().orElse(null);
	}

	public static <T> T getValue(String keyName) {
		Config value = getItem(keyName);
		try {
			return (T) stringToObject(value.getValue().toString(), (Class<?>) value.getType());
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;
	}

	public static Object stringToObject(String str, Class<?> type) {
		if (type == boolean.class || type == Boolean.class) { return Boolean.parseBoolean(str); }
		if (type == int.class) { return Integer.parseInt(str); }
		if (type == Color.class) { return ColorUtil.fromString(str); }
		if (type.isEnum()) { return Enum.valueOf((Class<? extends Enum>) type, str); }
		if (type == Instant.class) { return Instant.parse(str); }
		if (type == WorldPoint.class) {
			String[] splitStr = str.split(":");
			int x = Integer.parseInt(splitStr[0]);
			int y = Integer.parseInt(splitStr[1]);
			int plane = Integer.parseInt(splitStr[2]);
			return new WorldPoint(x, y, plane);
		}
		return str;
	}

	public static String objectToString(Object object) {
		if (object instanceof Color) { return String.valueOf(((Color) object).getRGB()); }
		if (object instanceof Enum) { return ((Enum) object).name(); }
		if (object instanceof Dimension) {
			Dimension d = (Dimension) object;
			return d.width + "x" + d.height;
		}
		if (object instanceof Point) {
			Point p = (Point) object;
			return p.x + ":" + p.y;
		}
		if (object instanceof Rectangle) {
			Rectangle r = (Rectangle) object;
			return r.x + ":" + r.y + ":" + r.width + ":" + r.height;
		}
		if (object instanceof Instant) { return ((Instant) object).toString(); }
		if (object instanceof WorldPoint) {
			WorldPoint wp = (WorldPoint) object;
			return wp.getX() + ":" + wp.getY() + ":" + wp.getPlane();
		}
		if (object instanceof Duration) { return Long.toString(((Duration) object).toMillis()); }
		if (object instanceof byte[]) { return Base64.getUrlEncoder().encodeToString((byte[]) object); }
		return object == null ? null : object.toString();
	}

	public static boolean getB(String val) {
		return Config.getValue(val);
	}

	public static int getI(String val) {
		return Config.getValue(val);
	}

	public static String getS(String val) {
		return Config.getValue(val);
	}

	@Getter
	@Setter
	public static boolean configChanged;

	public static void save(String string) throws IOException {
		File file = new File(string +".json");
		if (!file.exists()) file.createNewFile();

		Map<String, Object> map = new HashMap<String, Object>();
		CONFIGURATION.forEach(val -> {
			map.put(val.getKeyName(), val.getValue());
		});

		try (Writer writer = new FileWriter(file)) {
		    Gson gson = new GsonBuilder().create();
		    gson.toJson(map, writer);
		    writer.close();
		}
		
	}


	public static void load(String string) throws IOException {
		File file = new File(string +".json");
		if (!file.exists()) {
			save(string);
			return;
		}
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Map<String, Object> map = new Gson().fromJson(reader, HashMap.class);
		for (Entry<String, Object> config : map.entrySet()) {
			Config conf = Config.getItem(config.getKey());
			if (conf != null) conf.setValue(config.getValue());
		}
	}
}
