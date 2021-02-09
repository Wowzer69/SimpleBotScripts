package api;

import api.tasks.AntiBan;
import api.tasks.Banking;
import api.tasks.Combat;
import api.tasks.Inventory;
import api.tasks.Looting;
import api.tasks.POHBanking;
import api.tasks.Skill;
import api.tasks.Slayer;
import api.tasks.Supplies;
import api.tasks.Token;
import lombok.Getter;
import simple.hooks.simplebot.teleporter.Teleporter;
import simple.robot.api.ClientContext;

public class Tasks {
	@Getter
	private static POHBanking pohbanking;
	@Getter
	private static Banking banking;
	@Getter
	private static Skill skill;
	@Getter
	private static Inventory inventory;
	@Getter
	private static Supplies supplies;
	@Getter
	private static Combat combat;
	@Getter
	private static Looting loot;
	@Getter
	private static Token token;
	@Getter
	private static Slayer slayer;
	@Getter
	private static AntiBan antiban;
	@Getter
	private static Teleporter teleporter;
	@Getter
	private static MenuActions menuAction;

	public static void init(ClientContext ctx) {
		Variables.reset();
		pohbanking = new POHBanking(ctx);
		banking = new Banking(ctx);
		skill = new Skill(ctx);
		inventory = new Inventory(ctx);
		supplies = new Supplies(ctx);
		combat = new Combat(ctx);
		loot = new Looting(ctx);
		token = new Token(ctx);
		antiban = new AntiBan(ctx);
		slayer = new Slayer(ctx);
		teleporter = new Teleporter(ctx);
		menuAction = new MenuActions(ctx);
	}

}
