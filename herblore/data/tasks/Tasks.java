package herblore.data.tasks;

import java.util.LinkedList;
import java.util.List;

import lombok.Data;

@Data
public class Tasks {

	public enum Type {
		CLEAN_HERB,
		UNFINISHED_POTION,
		FULL_POTION
	}

	private Type type;
	private List<?> valid;

	public Tasks(Type type, List<?> valid) {
		this.type = type;
		this.valid = new LinkedList<>(valid);
	}

}
