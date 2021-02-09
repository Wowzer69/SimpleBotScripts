package aioslayer.data.master;

import aioslayer.data.Masters;

public class Example implements Masters {

	@Override
	public String getName() {
		return "Example";
	}

	@Override
	public int getId() {
		return -1;
	}

	@Override
	public void travel() {

	}

	@Override
	public boolean atLocation() {
		return false;

	}
}
