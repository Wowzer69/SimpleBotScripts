package api.simple;

import api.Tasks;
import api.Variables;
import net.runelite.api.coords.WorldPoint;
import simple.hooks.queries.SimpleEntityQuery;
import simple.hooks.wrappers.SimpleLocalPlayer;
import simple.hooks.wrappers.SimpleNpc;
import simple.robot.api.ClientContext;

public class KSNPC {

	private ClientContext ctx;

	public SimpleNpc npc;

	public KSNPC(SimpleNpc npc) {
		this.npc = npc;
	}

	private boolean inCombat, isDead, inDistance, isAggressive;

	private int distance;

	private Object val;

	public KSNPC(Object val) {
		this.val = val;
		this.ctx = ClientContext.instance();

		inCombat = isAggressive = isDead = false;
		inDistance = true;

		distance = 15;
	}

	public KSNPC isAggressive(boolean val) {
		this.isAggressive = val;
		return this;
	}

	public KSNPC inCombat(boolean val) {
		this.inCombat = val;
		return this;
	}

	public KSNPC isDead(boolean val) {
		this.isDead = val;
		return this;
	}

	public KSNPC inDistance(int val) {
		this.distance = val;
		return this;
	}

	public SimpleNpc get() {
		return query().next();
	}

	public SimpleEntityQuery<SimpleNpc> query() {
		SimpleEntityQuery<SimpleNpc> query = ctx.npcs.populate();
		if (query.size() == 0) return query;

		if (val instanceof String) query = query.filter((String) val);
		else if (val instanceof Number) query = query.filter((int) val);
		else if (val instanceof String[]) query = query.filter((String) val);
		else if (val instanceof int[]) query = query.filter((int[]) val);

		if (inDistance) query = query.filterWithin(distance);

		if (isAggressive) query = query.filter(npc -> {
			if (npc.getInteracting() != null && npc.getInteracting().equals(ctx.players.getLocal().getPlayer())) return true;
			if (npc.getInteracting() != null && !npc.inCombat()) return true;
			return false;

		});
		query = query.filter(npc -> npc.inCombat() == inCombat && npc.isDead() == isDead);
		return query;
	}

	public synchronized boolean click(String arg0) {
		if (Variables.USE_PACKETS) return Tasks.getMenuAction().get(npc, arg0).invoke();
		return npc.click(arg0);
	}

	public synchronized boolean validateInteractable() {
		if (Variables.USE_PACKETS) return true;
		return npc.validateInteractable();
	}

	public boolean isNull() {
		return npc == null;
	}

	public int distanceTo(SimpleLocalPlayer local) {
		return npc.distanceTo(local);
	}

	public WorldPoint getLocation() {
		return npc.getLocation();
	}

}
