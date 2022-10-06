package net.aegis.athena.framework.exceptions.postconfigured;

import net.aegis.athena.utils.PlayerUtils;

import java.util.UUID;

public class PlayerNotOnlineException extends PostConfiguredException {

	public PlayerNotOnlineException(UUID uuid) {
		super(PlayerUtils.getPlayer(uuid).getName() + " is not online");
	}

}
