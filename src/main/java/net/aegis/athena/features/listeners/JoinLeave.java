package net.aegis.athena.features.listeners;

import lombok.NoArgsConstructor;
import net.aegis.athena.Athena;
import net.aegis.athena.utils.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@NoArgsConstructor
public class JoinLeave implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		String joinMessage = StringUtils.colorize(Athena.aegisPrimary + "Welcome to the server, " + Athena.aegisSecondary + player.getName());
		if (player.hasPlayedBefore())
			joinMessage = StringUtils.colorize(Athena.aegisSecondary + player.getName() + Athena.aegisPrimary + " has joined the server");

		event.setJoinMessage(joinMessage);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		String quitMessage = StringUtils.colorize(Athena.aegisSecondary + player.getName() + Athena.aegisPrimary + " has left the server");
		event.setQuitMessage(quitMessage);
	}

}
