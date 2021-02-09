package combat.fastslayer.data;

import lombok.Data;

@Data
public class SlayerTask {
	private String name;
	private int amount;

	public SlayerTask(String name, int amount) {
		this.name = name;
		this.amount = amount;
	}

	public String getName() {
		return replace(this.name);
	}

	public String replace(String name) {
		name = name.replace("wolves", "wolf");
		return name;
	}

}
