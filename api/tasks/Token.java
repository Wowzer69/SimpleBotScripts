package api.tasks;

import java.util.Arrays;
import java.util.stream.IntStream;

import api.Locations;
import api.Tasks;
import api.Variables;
import api.simple.KSItem;
import api.utils.Utils;
import joptsimple.internal.Strings;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.VarClientStr;
import simple.hooks.wrappers.SimpleWidget;
import simple.robot.api.ClientContext;

public class Token {

	private ClientContext ctx;

	public Token(ClientContext ctx) {
		this.ctx = ctx;
	}

	public enum INSTANCES {
		KING_BLACK_DRAGON,
		DAGANNOTH_KINGS,
		BYROPHYTA,
		THERMONUCLEAR_SMOKE_DEVIL,
		GENERAL_GRAARDOR,
		KRIL_TSUTSAROTH,
		KREEARA,
		COMMANDER_ZILYANA,
		KALPHITE_QUEEN,
		SARACHNIS,
		CERBERUS,
		CORPOREAL_BEAST
	}

	public void createInstance(INSTANCES option) {
		SimpleWidget widget1 = ctx.widgets.getWidget(1028, 5);
		SimpleWidget widget2 = ctx.widgets.getWidget(1028, option.ordinal());

		if (ctx.dialogue.dialogueOpen()) {
			if (ctx.dialogue.clickDialogueOption(1)) ctx.onCondition(() -> !ctx.pathing.inArea(Locations.EDGEVILLE_AREA));
		} else if (Utils.validWidget(widget1)) {
			if (widget1.getChild(5).click(0)) ctx.sleep(500, 750);
		} else if (Utils.validWidget(widget2)) {
			SimpleWidget child2 = widget2.getChild(0);
			if (child2.getSpriteId() == 697) {
				child2.click(0);
				ctx.sleep(150, 250);
				ctx.sleepCondition(() -> ctx.dialogue.dialogueOpen(), 1000);
			} else {
				SimpleWidget confirm = ctx.widgets.getWidget(1028, 15);
				if (confirm != null && confirm.visibleOnScreen()) confirm.click(0);
			}
		}
	}

	private String getTitle() {
		SimpleWidget w = ctx.widgets.getWidget(219, 1);
		if (!Utils.validWidget(w)) return "";
		return w.getChild(0).getText();
	}

	public void joinInstance(boolean insurance) {
		if (ctx.dialogue.canContinue()) {
			ctx.dialogue.clickContinue();
			ctx.sleep(450, 600);
		} else if (getTitle().length() > 0) {
			if (getTitle().contains("private clan")) {
				SimpleWidget single = getDialogueOptionWidget("Pay one Instance token."),
						two = getDialogueOptionWidget("Pay 2 Instance tokens.");

				if (insurance && Utils.validWidget(single)) ctx.pathing.step(ctx.players.getLocal().getLocation());

				SimpleWidget w = insurance ? two : single;
				if (Utils.validWidget(w)) {
					if (w.click(0)) ctx.sleepCondition(() -> !ctx.pathing.inArea(Locations.EDGEVILLE_AREA), 1500);
				}
			} else if (getTitle().contains("personal insurance")) {
				SimpleWidget w = insurance ? getDialogueOptionWidget("Yes, I want the insurance!")
						: getDialogueOptionWidget("No thanks, I don't need it.");
				if (Utils.validWidget(w)) {
					if (w.click(0)) ctx.sleepCondition(() -> getTitle().contains("private clan"), 2500);
				}
			}
		}
	}

	public int getInterfaceOption(String option) {
		if (option == null) return -1;
		SimpleWidget w = ctx.widgets.getWidget(187, 3);
		if (!Utils.validWidget(w)) return -1;
		return IntStream.range(0, w.getChildren().length).filter(val -> Utils.validWidget(w.getChild(val))).filter(v -> {
			SimpleWidget c = w.getChild(v);
			return Utils.validWidget(c) && c.getText() != null && c.getText().toLowerCase().contains(option.toLowerCase());
		}).map(i -> i + 1).findFirst().orElse(-1);
	}

	public int getOption(String option) {
		if (option == null) return -1;
		SimpleWidget w = ctx.widgets.getWidget(219, 1);
		if (!Utils.validWidget(w)) return getInterfaceOption(option);
		return IntStream.range(0, w.getChildren().length).filter(val -> Utils.validWidget(w.getChild(val))).filter(v -> {
			SimpleWidget c = w.getChild(v);
			return Utils.validWidget(c) && c.getText() != null && c.getText().toLowerCase().contains(option.toLowerCase());
		}).findFirst().orElse(-1);
	}

	public SimpleWidget getDialogueOptionWidget(String option) {
		SimpleWidget w = ctx.widgets.getWidget(219, 1);
		if (!Utils.validWidget(w)) return null;
		return Arrays.stream(w.getChildren()).filter(Utils::validWidget).filter(v -> {
			if (option == null) return true;
			return v.getText().toLowerCase().contains(option.toLowerCase());
		}).findFirst().orElse(null);
	}

	public void handle(String hostName, boolean host, boolean insurance, INSTANCES... option) {
		KSItem token = new KSItem(Tasks.getInventory().getItem("Instance token"));
		if (!token.isNull()) {
			ctx.bank.closeBank();
			if (host) {
				if (ctx.widgets.getWidget(1028, 5) == null) token.click("Create");
				Variables.STATUS = "Creating instance";
				if (option.length == 1) createInstance(option[0]);
			} else {
				if (ctx.dialogue.pendingInput()) {
					Variables.STATUS = "Inputting host name";
					if (Strings.isNullOrEmpty(hostName)) hostName = getHost();

					ctx.log("Host Name: " + hostName);

					if (!Strings.isNullOrEmpty(hostName)) {
						ctx.getClient().setVar(VarClientStr.INPUT_TEXT, hostName);
						ctx.keyboard.sendKeys("", true);
						ctx.sleep(250, 350);
					}
				} else if (ctx.dialogue.dialogueOpen()) {
					Variables.STATUS = "Joining instance";
					joinInstance(insurance);
				} else {
					Variables.STATUS = "Clicking join";
					if (token.click("Join")) {
						ctx.onCondition(() -> ctx.dialogue.pendingInput());
					}
				}
			}
		}
	}

	public String getHost() {
		FriendsChatMember[] members = ctx.getClient().getFriendsChatManager().getMembers();
		String username = Arrays.asList(members).stream().map(player -> player.getName())
				.filter(name -> ctx.players.populate().filter(name).population() == 0).findFirst().orElse(null);
		return username;
	}

}
