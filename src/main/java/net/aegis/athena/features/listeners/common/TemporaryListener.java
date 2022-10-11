package net.aegis.athena.features.listeners.common;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public interface TemporaryListener extends Listener, Player {

	Player getPlayer();

	default void unregister() {}

}
