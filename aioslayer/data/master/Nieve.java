package aioslayer.data.master;

import aioslayer.data.Masters;
import api.Locations;
import api.MenuActions;
import api.Tasks;
import net.runelite.api.MenuAction;
import simple.robot.api.ClientContext;

public class Nieve implements Masters {

	@Override
	public String getName() {
		return "Nieve";
	}

	@Override
	public int getId() {
		return 6797;
	}

	@Override
	public boolean atLocation() {
		return ctx.pathing.inArea(Locations.EDGEVILLE_AREA) && Tasks.getCombat().getNPC(getId()) != null;
	}

	@Override
	public void travel() {
		MenuActions.invoke("", "", 1, MenuAction.CC_OP.getId(), 1, 14286853);
		ClientContext.instance().sleep(450);
		ClientContext.instance().onCondition(() -> ClientContext.instance().players.getLocal().getAnimation() != -1);
		ClientContext.instance().onCondition(() -> ClientContext.instance().players.getLocal().getAnimation() == -1);
		ClientContext.instance().sleep(1500);
	}
}