package api.panel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Tabs {

	private final int index;
	private final String title, description;

}
