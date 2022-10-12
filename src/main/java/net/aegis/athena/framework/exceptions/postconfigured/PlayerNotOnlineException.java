package net.aegis.athena.framework.exceptions.postconfigured;

import net.aegis.athena.models.nickname.Nickname;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class PlayerNotOnlineException extends PostConfiguredException {

	public PlayerNotOnlineException(UUID uuid) {
		super(Nickname.of(uuid) + " is not online");
	}

	public PlayerNotOnlineException(OfflinePlayer player) {
		super(Nickname.of(player.getUniqueId()) + " is not online");
	}

}
