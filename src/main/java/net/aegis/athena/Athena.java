package net.aegis.athena;

import net.aegis.athena.features.commands.DiscordCommand;
import net.aegis.athena.features.listeners.JoinLeaveListener;
import net.aegis.athena.features.listeners.OlympusBreakListener;
import net.aegis.athena.features.listeners.RandomSpawnListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class Athena extends JavaPlugin {

	private BukkitAudiences adventure;

	public @NonNull BukkitAudiences adventure() {
		if(this.adventure == null) {
			throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
		}
		return this.adventure;
	}

	@Override
	public void onEnable() {
		// Plugin startup logic

		//register listener classes
		getServer().getPluginManager().registerEvents(new JoinLeaveListener(), this);
		getServer().getPluginManager().registerEvents(new RandomSpawnListener(), this);
		getServer().getPluginManager().registerEvents(new OlympusBreakListener(), this);
		//end of listener registry

		//register command classes
		getCommand("discord").setExecutor(new DiscordCommand(this));
		//end of command registry

		//kyori adventure registry
		this.adventure = BukkitAudiences.create(this);
		//end of kyori adventure registry

	}

	@Override
	public void onDisable() {
		// Plugin shutdown logic

		//kyori adventure shutdown logic

		if(this.adventure != null) {
			this.adventure.close();
			this.adventure = null;
		}

		//end of kyori adventure

	}
}
