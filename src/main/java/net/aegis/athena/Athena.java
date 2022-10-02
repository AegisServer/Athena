package net.aegis.athena;

import net.aegis.athena.listeners.JoinLeaveMessages;
import net.aegis.athena.listeners.RandomSpawn;
import org.bukkit.plugin.java.JavaPlugin;

public final class Athena extends JavaPlugin {

	@Override
	public void onEnable() {
		// Plugin startup logic

		//register listener classes

		getServer().getPluginManager().registerEvents(new JoinLeaveMessages(), this);
		getServer().getPluginManager().registerEvents(new RandomSpawn(), this);

		//end of listener registry

	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
