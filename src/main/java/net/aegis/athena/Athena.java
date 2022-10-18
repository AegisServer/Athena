package net.aegis.athena;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import me.lucko.spark.api.Spark;
import net.aegis.athena.features.chat.Chat;
import net.aegis.athena.features.listeners.common.TemporaryListener;
import net.aegis.athena.framework.commands.Commands;
import net.aegis.athena.framework.features.Features;
import net.aegis.athena.framework.persistence.mongodb.MongoService;
import net.aegis.athena.models.nerd.Nerd;
import net.aegis.athena.models.nerd.Rank;
import net.aegis.athena.utils.EnumUtils;
import net.aegis.athena.utils.Env;
import net.aegis.athena.utils.LuckPermsUtils;
import net.aegis.athena.utils.Name;
import net.aegis.athena.utils.PlayerUtils;
import net.aegis.athena.utils.PlayerUtils.OnlinePlayers;
import net.aegis.athena.utils.ReflectionUtils;
import net.aegis.athena.utils.Tasks;
import net.aegis.athena.utils.Timer;
import net.aegis.athena.utils.Utils;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class Athena extends JavaPlugin {

	@Getter
	private Commands commands;
	@Getter
	private Features features;
	private static Athena instance;
	@Getter
	private static Thread thread;
	public static final LocalDateTime EPOCH = LocalDateTime.now();
	private static API api;

	public Athena() {
		if (instance == null) {
			instance = this;
		} else
			Bukkit.getServer().getLogger().info("Athena could not be initialized: Instance is not null but is: " + instance.getClass().getName());

		api = new API();
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

	public final static String aegisBlue = "&#6E759F";
	public final static String aegisRed = "&#9F6E75";
	public final static String aegisBeige = "&#C1BCAB";

	@Override
	public void onEnable() {
		// Plugin startup logic

		//config initialization
		getConfig().options().copyDefaults();
		saveDefaultConfig();
		//end of config initialization

		//mongodb stuff
//		String url = getConfig().getString("Url");
//		MongoClient mongoClient = MongoClients.create(url);
//		MongoCollection<Document> collection = mongoClient.getDatabase("Aegis").getCollection("users");
//		log("Connected to Database");
		//end of mongodb stuff

		//register listener classes
//		getServer().getPluginManager().registerEvents(new JoinLeave(), this);
//		getServer().getPluginManager().registerEvents(new RandomSpawn(), this);
//		getServer().getPluginManager().registerEvents(new OlympusBreak(), this);
		//end of listener registry


		new Timer("Enable", () -> {
			new Timer(" Cache Usernames", () -> OnlinePlayers.getAll().forEach(Name::of));
			new Timer(" Config", this::setupConfig);
			new Timer(" Hooks", this::hooks);
			new Timer(" Databases", this::databases);
			new Timer(" Features", () -> {
				features = new Features(this, "net.aegis.athena.features");
				features.register(Chat.class); // prioritize
				features.registerAll();
			});
			new Timer(" Commands", () -> {
				commands = new Commands(this, "net.aegis.athena.features");
				commands.registerAll();
			});
		});

		//end of command registry
	}

	@Override
	@SuppressWarnings({"Convert2MethodRef", "CodeBlock2Expr"})
	public void onDisable() {
		// Plugin shutdown logic

		List<Runnable> tasks = List.of(
				() -> {broadcastReload();},
				() -> {PlayerUtils.runCommandAsConsole("save-all");},
//				() -> {if (cron.isStarted()) cron.stop();},
				() -> {if (commands != null) commands.unregisterAll();},
				() -> {if (features != null) features.unregisterExcept(Chat.class);},
				() -> {if (features != null) features.unregister(Chat.class);},
				() -> {Bukkit.getServicesManager().unregisterAll(this);},
				() -> {LuckPermsUtils.shutdown();},

				() -> {shutdownDatabases();}, // last
				() -> {if (api != null) api.shutdown();} // last
		);

		for (Runnable task : tasks) {
			try {
				task.run();
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
	}

	public void broadcastReload() {
		if (luckPerms == null)
			return;

		Rank.getOnlineStaff().stream()
				.map(Nerd::getPlayer)
				.forEach(player -> PlayerUtils.send(player, " &c&l ! &c&l! " + aegisBlue + "Reloading Athena &c&l! &c&l!"));
	}

	private void setupConfig() {
		if (!Athena.getInstance().getDataFolder().exists())
			Athena.getInstance().getDataFolder().mkdir();

		FileConfiguration config = getInstance().getConfig();

		addConfigDefault("env", "dev");

		config.options().copyDefaults(true);
		saveConfig();
	}

	public void addConfigDefault(String path, Object value) {
		FileConfiguration config = getInstance().getConfig();
		config.addDefault(path, value);

		config.options().copyDefaults(true);
		saveConfig();
	}

	//	@Getter
	// http://www.sauronsoftware.it/projects/cron4j/manual.php
//	private static final Scheduler cron = new Scheduler();
	@Getter
	private static MultiverseCore multiverseCore;
	@Getter
	private static MultiverseInventories multiverseInventories;
	@Getter
	private static LuckPerms luckPerms = null;
	@Getter
	private static Spark spark = null;

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

	private void databases() {
		new Timer(" MongoDB", () -> {
			// new HomeService();
			Tasks.wait(5, () -> MongoService.loadServices("net.aegis.athena.models"));
		});
	}

	@SneakyThrows
	private void shutdownDatabases() {
		for (Class<? extends MongoService> service : MongoService.getServices())
			if (Utils.canEnable(service)) {
				final MongoService<?> serviceInstance = service.getConstructor().newInstance();
				// TODO Maybe per-service setting to save on shutdown? This will save way too many things
//				serviceInstance.saveCacheSync();
				serviceInstance.clearCache();
			}
	}

	private void hooks() {
//		signMenuFactory = new SignMenuFactory(this);
		multiverseCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
		multiverseInventories = (MultiverseInventories) Bukkit.getPluginManager().getPlugin("Multiverse-Inventories");
//		cron.start();
		RegisteredServiceProvider<LuckPerms> lpProvider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (lpProvider != null)
			luckPerms = lpProvider.getProvider();
		RegisteredServiceProvider<Spark> sparkProvider = Bukkit.getServicesManager().getRegistration(Spark.class);
		if (sparkProvider != null)
			spark = sparkProvider.getProvider();
	}

}
