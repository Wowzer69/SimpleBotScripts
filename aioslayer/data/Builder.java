package aioslayer.data;

import java.util.Hashtable;

import aioslayer.data.tasks.AberrantSpectres;
import aioslayer.data.tasks.AbyssalDemon;
import aioslayer.data.tasks.Ankou;
import aioslayer.data.tasks.BlackDemons;
import aioslayer.data.tasks.BlackDragons;
import aioslayer.data.tasks.Bloodvelds;
import aioslayer.data.tasks.BlueDragons;
import aioslayer.data.tasks.CaveHorrors;
import aioslayer.data.tasks.Dagannoth;
import aioslayer.data.tasks.DarkBeasts;
import aioslayer.data.tasks.DustDevils;
import aioslayer.data.tasks.Elves;
import aioslayer.data.tasks.FireGiants;
import aioslayer.data.tasks.Gargoyle;
import aioslayer.data.tasks.GreaterDemon;
import aioslayer.data.tasks.Hellhounds;
import aioslayer.data.tasks.IronDragons;
import aioslayer.data.tasks.Kalphite;
import aioslayer.data.tasks.Kurask;
import aioslayer.data.tasks.Nechryael;
import aioslayer.data.tasks.SmokeDevil;
import aioslayer.data.tasks.SteelDragons;
import aioslayer.data.tasks.Suqah;
import aioslayer.data.tasks.Trolls;
import aioslayer.data.tasks.Turoth;

public class Builder {
	public static Hashtable<String, MonsterTask> monsterDict = new Hashtable<String, MonsterTask>();

	static {
		monsterDict.put("cave horrors", new CaveHorrors());
		monsterDict.put("ankou", new Ankou());
		monsterDict.put("gargoyles", new Gargoyle());
		monsterDict.put("dagannoth", new Dagannoth());
		monsterDict.put("hellhounds", new Hellhounds());
		monsterDict.put("fire giants", new FireGiants());
		monsterDict.put("greater demons", new GreaterDemon());
		monsterDict.put("nechryael", new Nechryael());
		monsterDict.put("abyssal demons", new AbyssalDemon());
		monsterDict.put("black demons", new BlackDemons());
		monsterDict.put("bloodveld", new Bloodvelds());
		monsterDict.put("dark beasts", new DarkBeasts());
		monsterDict.put("dust devils", new DustDevils());
		monsterDict.put("elves", new Elves());
		monsterDict.put("kurask", new Kurask());
		monsterDict.put("smoke devils", new SmokeDevil());
		monsterDict.put("suqah", new Suqah());
		monsterDict.put("trolls", new Trolls());
		monsterDict.put("turoth", new Turoth());
		monsterDict.put("kalphite", new Kalphite());
		monsterDict.put("aberrant spectres", new AberrantSpectres());
		monsterDict.put("black dragons", new BlackDragons());
		monsterDict.put("steel dragons", new SteelDragons());
		monsterDict.put("iron dragons", new IronDragons());
		monsterDict.put("blue dragons", new BlueDragons());
	}

	public static MonsterTask getMonsterByString(String s) {
		s = s.toLowerCase();
		return monsterDict.get(s);
	}

}
