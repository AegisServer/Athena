package net.aegis.athena;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.Getter;
import lombok.Setter;
import net.aegis.athena.features.commands.DiscordCommand;
import net.aegis.athena.features.commands.ShowItemCommand;
import net.aegis.athena.features.listeners.JoinLeaveListener;
import net.aegis.athena.features.listeners.OlympusBreakListener;
import net.aegis.athena.features.listeners.RandomSpawnListener;
import net.aegis.athena.features.listeners.common.TemporaryListener;
import net.aegis.athena.utils.EnumUtils;
import net.aegis.athena.utils.Env;
import net.aegis.athena.utils.ReflectionUtils;
import net.aegis.athena.utils.Utils;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

	public static Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

	public static <T> T singletonOf(Class<T> clazz) {
		return (T) singletons.computeIfAbsent(clazz, $ -> {
			try {
				return clazz.getConstructor().newInstance();
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
				Athena.log(Level.FINE, "Failed to create singleton of " + clazz.getName() + ", falling back to Objenesis", ex);
				try {
					return new ObjenesisStd().newInstance(clazz);
				} catch (Throwable t) {
					throw new IllegalStateException("Failed to create singleton of " + clazz.getName() + " using Objenesis", t);
				}
			}
		});
	}

	@Getter
	private static final List<Listener> listeners = new ArrayList<>();
	@Getter
	private static final List<TemporaryListener> temporaryListeners = new ArrayList<>();
	@Getter
	private static final List<Class<? extends Event>> eventHandlers = new ArrayList<>();

	public static void registerTemporaryListener(TemporaryListener listener) {
		registerListener(listener);
		temporaryListeners.add(listener);
	}

	public static void unregisterTemporaryListener(TemporaryListener listener) {
		listener.unregister();
		unregisterListener(listener);
		temporaryListeners.remove(listener);
	}

	public static void registerListener(Listener listener) {
		if (!Utils.canEnable(listener.getClass()))
			return;

		final boolean isTemporary = listener instanceof TemporaryListener;
		if (listeners.contains(listener) && !isTemporary) {
			Athena.debug("Ignoring duplicate listener registration for class " + listener.getClass().getSimpleName());
			return;
		}

		Athena.debug("Registering listener: " + listener.getClass().getName());
		if (getInstance().isEnabled()) {
			getInstance().getServer().getPluginManager().registerEvents(listener, getInstance());
			listeners.add(listener);
			if (!isTemporary)
				for (Method method : ReflectionUtils.methodsAnnotatedWith(listener.getClass(), EventHandler.class))
					eventHandlers.add((Class<? extends Event>) method.getParameters()[0].getType());
		} else
			log("Could not register listener " + listener.getClass().getName() + "!");
	}

	public static void unregisterListener(Listener listener) {
		try {
			HandlerList.unregisterAll(listener);
			listeners.remove(listener);
		} catch (Exception ex) {
			log("Could not unregister listener " + listener.toString() + "!");
			ex.printStackTrace();
		}
	}

	public static Athena getInstance() {
		if (instance == null)
			Bukkit.getServer().getLogger().info("Athena could not be initialized");
		return instance;
	}

	public static Env getEnv() {
		String env = getInstance().getConfig().getString("env", Env.DEV.name()).toUpperCase();
		try {
			return Env.valueOf(env);
		} catch (IllegalArgumentException ex) {
			Athena.severe("Could not parse environment variable " + env + ", options are: " + EnumUtils.valueNamesPretty(Env.class));
			Athena.severe("Defaulting to " + Env.DEV.name() + " environment");
			return Env.DEV;
		}
	}

	public @NonNull BukkitAudiences adventure() {
		if(this.adventure == null) {
			throw new IllegalStateException("Tried to access Adventure when the plugin was disabled!");
		}
		return this.adventure;
	}

	final static String aegisBlue ="&#6E759F";
	final static String aegisRed = "&#9F6E75";
	final static String aegisBeige = "&#C1BCAB";

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

	@Getter
	private static LuckPerms luckPerms = null;

	@Getter
	@Setter
	private static boolean debug = false;

	public static void debug(String message) {
		if (debug)
			getInstance().getLogger().info("[DEBUG] " + net.md_5.bungee.api.ChatColor.stripColor(message));
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
