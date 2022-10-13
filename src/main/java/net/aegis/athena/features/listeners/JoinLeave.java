package net.aegis.athena.features.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeave implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		String joinMessage = "&dWelcome to the server, &5" + player.getName();
		if (player.hasPlayedBefore())
			joinMessage = "&5" + player.getName() + " &dhas joined the server";

		event.setJoinMessage(joinMessage);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		String quitMessage = "&5" + player.getName() + " &dhas left the server";
		event.setQuitMessage(quitMessage);
	}

}
