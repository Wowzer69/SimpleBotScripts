package aioslayer.data;

import simple.robot.api.ClientContext;

public interface Masters {

	ClientContext ctx = ClientContext.instance();

	String getName();

	int getId();

	boolean atLocation();

	void travel();
}
