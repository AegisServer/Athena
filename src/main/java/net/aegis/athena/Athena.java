package net.aegis.athena;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import net.aegis.athena.features.commands.DiscordCommand;
import net.aegis.athena.features.commands.ShowItemCommand;
import net.aegis.athena.features.listeners.JoinLeaveListener;
import net.aegis.athena.features.listeners.OlympusBreakListener;
import net.aegis.athena.features.listeners.RandomSpawnListener;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.logging.Level;

public final class Athena extends JavaPlugin {

	private static Athena instance;

	private BukkitAudiences adventure;

	public Athena() {
		if (instance == null) {
			instance = this;
		} else
			Bukkit.getServer().getLogger().info("Athena could not be initialized: Instance is not null but is: " + instance.getClass().getName());
	}

	public static Athena getInstance() {
		if (instance == null)
			Bukkit.getServer().getLogger().info("Athena could not be initialized");
		return instance;
	}

	public @NonNull BukkitAudiences adventure() {
		if(this.adventure == null) {
			throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
		}
		return this.adventure;
	}

	@Override
	public void onEnable() {
		// Plugin startup logic

		//config initialization
		getConfig().options().copyDefaults();
		saveDefaultConfig();
		//end of config initialization

		//mongodb stuff
		String url = getConfig().getString("Url");
		MongoClient mongoClient = MongoClients.create(url);
//		MongoCollection<Document> collection = mongoClient.getDatabase("Aegis").getCollection("users");
		log("Connected to Database");
		//end of mongodb stuff

		//register listener classes
		getServer().getPluginManager().registerEvents(new JoinLeaveListener(), this);
		getServer().getPluginManager().registerEvents(new RandomSpawnListener(), this);
		getServer().getPluginManager().registerEvents(new OlympusBreakListener(), this);
		//end of listener registry

		//register command classes
		getCommand("discord").setExecutor(new DiscordCommand(this));
		getCommand("showitem").setExecutor(new ShowItemCommand());
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

	public static void log(String message) {
		log(Level.INFO, message);
	}

	public static void log(String message, Throwable ex) {
		log(Level.INFO, message, ex);
	}

	public static void warn(String message) {
		log(Level.WARNING, message);
	}

	public static void warn(String message, Throwable ex) {
		log(Level.WARNING, message, ex);
	}

	public static void severe(String message) {
		log(Level.SEVERE, message);
	}

	public static void severe(String message, Throwable ex) {
		log(Level.SEVERE, message, ex);
	}

	public static void log(Level level, String message) {
		log(level, message, null);
	}

	public static void log(Level level, String message, Throwable ex) {
		getInstance().getLogger().log(level, ChatColor.stripColor(message), ex);
	}
}
