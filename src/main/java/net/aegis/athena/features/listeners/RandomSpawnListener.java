package net.aegis.athena.features.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomSpawnListener implements Listener {

	private static List<Location> spawnpoints = new ArrayList<>() {{
		add(new Location(Bukkit.getWorld("world"), 500, 70, 250));
		add(new Location(Bukkit.getWorld("world"), 200, 85, 100));
		add(new Location(Bukkit.getWorld("world"), 150, 100, 450));
	}};

	private static Random random = new Random();
	public static Location randomLocation(List<Location> list){
		if (list == null || list.size() == 0)
			return null;
		return list.get(random.nextInt(list.size()));
	}

	@EventHandler
	public void onNewPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!(player.hasPlayedBefore())){
			player.teleportAsync(randomLocation((spawnpoints)));
		}
	}
}
