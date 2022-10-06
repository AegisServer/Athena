package net.aegis.athena.framework.exceptions.preconfigured;

import static net.aegis.athena.utils.Nullables.isNullOrEmpty;

public class NoPermissionException extends PreConfiguredException {

	public NoPermissionException() {
		this(null);
	}

	public NoPermissionException(String extra) {
		super("You don't have permission to do that!" + (isNullOrEmpty(extra) ? "" : " " + extra));
	}

}
