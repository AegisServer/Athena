package net.aegis.athena;

import net.aegis.athena.features.commands.DiscordCommand;
import net.aegis.athena.features.listeners.JoinLeaveMessages;
import net.aegis.athena.features.listeners.RandomSpawn;
import org.bukkit.plugin.java.JavaPlugin;

public final class Athena extends JavaPlugin {

	@Override
	public void onEnable() {
		// Plugin startup logic

		//register listener classes

		getServer().getPluginManager().registerEvents(new JoinLeaveMessages(), this);
		getServer().getPluginManager().registerEvents(new RandomSpawn(), this);

		//end of listener registry

		//register command classes

		getCommand("discord").setExecutor((new DiscordCommand()));

	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic
	}
}
