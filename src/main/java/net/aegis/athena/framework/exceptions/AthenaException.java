package net.aegis.athena.framework.exceptions;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.aegis.athena.utils.JsonBuilder;
import net.kyori.adventure.text.ComponentLike;

@Data
@NoArgsConstructor
public class AthenaException extends RuntimeException {
	private JsonBuilder json;

	public AthenaException(JsonBuilder json) {
		super(json.toString());
		this.json = json;
	}

	public AthenaException(ComponentLike component) {
		this(new JsonBuilder(component));
	}

	public AthenaException(String message) {
		this(new JsonBuilder(message));
	}

	public ComponentLike withPrefix(String prefix) {
		return new JsonBuilder(prefix).next(getJson());
	}

}
