package net.aegis.athena.features.listeners;

import lombok.NoArgsConstructor;
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

		String joinMessage = "&dWelcome to the server, &5" + player.getName();
		if (player.hasPlayedBefore())
			joinMessage = StringUtils.colorize("&5" + player.getName() + " &dhas joined the server");

		event.setJoinMessage(joinMessage);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		String quitMessage = StringUtils.colorize("&5" + player.getName() + " &dhas left the server");
		event.setQuitMessage(quitMessage);
	}

}
