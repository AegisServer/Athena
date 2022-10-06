package net.aegis.athena.framework.exceptions.preconfigured;

import net.aegis.athena.framework.exceptions.AthenaException;
import net.md_5.bungee.api.ChatColor;

public class PreConfiguredException extends AthenaException {

	public PreConfiguredException(String message) {
		super(ChatColor.RED + message);
	}
}
