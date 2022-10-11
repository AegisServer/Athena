package net.aegis.athena.framework.exceptions.postconfigured;

import net.aegis.athena.framework.interfaces.HasUniqueId;
import net.aegis.athena.framework.persistence.mongodb.models.nickname.Nickname;
import net.aegis.athena.utils.PlayerUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerNotOnlineException extends PostConfiguredException {

	public PlayerNotOnlineException(UUID uuid) {
		super(Nickname.of(uuid) + " is not online");
	}

	public PlayerNotOnlineException(HasUniqueId player) {
		super(Nickname.of(player) + " is not online");
	}

}
