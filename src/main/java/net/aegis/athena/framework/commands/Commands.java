package net.aegis.athena.framework.commands;



import lombok.Getter;
import net.aegis.athena.Athena;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.ICustomCommand;
import net.aegis.athena.framework.commands.models.annotations.ConverterFor;
import net.aegis.athena.framework.commands.models.annotations.DoubleSlash;
import net.aegis.athena.framework.commands.models.annotations.TabCompleterFor;
import net.aegis.athena.utils.StringUtils;
import net.aegis.athena.utils.Timer;
import net.aegis.athena.utils.Utils;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;


import static net.aegis.athena.utils.ReflectionUtils.methodsAnnotatedWith;
import static net.aegis.athena.utils.ReflectionUtils.subTypesOf;


@SuppressWarnings({"unused", "unchecked"})
public class Commands {
	private final Plugin plugin;
	@Getter
	private final CommandMapUtils mapUtils;
	private final Set<Class<? extends CustomCommand>> commandSet;
	@Getter
	private static final Map<String, CustomCommand> commands = new HashMap<>();
	@Getter
	private static final Map<Class<?>, Method> converters = new HashMap<>();
	@Getter
	private static final Map<Class<?>, Method> tabCompleters = new HashMap<>();
	@Getter
	private static final Map<String, String> redirects = new HashMap<>();
	@Getter
	private static final String pattern = "(\\/){1,2}[\\w\\-]+";

	public Commands(Plugin plugin, String path) {
		this.plugin = plugin;
		this.mapUtils = new CommandMapUtils(plugin);
		this.commandSet = subTypesOf(CustomCommand.class, path);
		registerConvertersAndTabCompleters();
		plugin.getServer().getPluginManager().registerEvents(new CommandListener(), plugin);
	}

	public static CustomCommand get(String alias) {
		return commands.getOrDefault(alias, null);
	}

	public static CustomCommand get(Class<? extends CustomCommand> clazz) {
		return commands.getOrDefault(prettyName(clazz), null);
	}

	public static String prettyName(ICustomCommand customCommand) {
		return prettyName(customCommand.getClass());
	}

	public static String prettyName(Class<? extends ICustomCommand> clazz) {
		return clazz.getSimpleName().replaceAll("Command$", "");
	}

	public static String getPrefix(ICustomCommand customCommand) {
		return getPrefix(customCommand.getClass());
	}

	public static String getPrefix(Class<? extends ICustomCommand> clazz) {
		return StringUtils.getPrefix(prettyName(clazz));
	}

	public void registerAll() {
		Athena.debug(" Registering " + commandSet.size() + " commands");
		commandSet.forEach(this::register);
	}

	public void register(Class<? extends CustomCommand>... customCommands) {
		for (Class<? extends CustomCommand> clazz : customCommands)
			try {
				if (Utils.canEnable(clazz))
					register(Athena.singletonOf(clazz));
			} catch (Throwable ex) {
				plugin.getLogger().info("Error while registering command " + prettyName(clazz));
				ex.printStackTrace();
			}
	}

	public void registerExcept(Class<? extends CustomCommand>... customCommands) {
		List<Class<? extends CustomCommand>> excluded = Arrays.asList(customCommands);
		for (Class<? extends CustomCommand> clazz : commandSet)
			if (!excluded.contains(clazz))
				register(clazz);
	}

	private void register(CustomCommand customCommand) {
		new Timer("  Register command " + customCommand.getName(), () -> {
			try {
				for (String alias : customCommand.getAllAliases()) {
					mapUtils.register(alias, customCommand);

					if (customCommand.getClass().getAnnotation(DoubleSlash.class) != null)
						alias = "/" + alias;

					commands.put(alias.toLowerCase(), customCommand);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			Utils.tryRegisterListener(customCommand);
		});
	}

	public void unregisterAll() {
		for (Class<? extends CustomCommand> clazz : commandSet)
			try {
				unregister(clazz);
			} catch (Throwable ex) {
				plugin.getLogger().info("Error while unregistering command " + prettyName(clazz));
				ex.printStackTrace();
			}
	}

	public void unregister(Class<? extends CustomCommand>... customCommands) {
		for (Class<? extends CustomCommand> clazz : customCommands)
			if (Utils.canEnable(clazz))
				unregister(Athena.singletonOf(clazz));
	}

	public void unregisterExcept(Class<? extends CustomCommand>... customCommands) {
		List<Class<? extends CustomCommand>> excluded = Arrays.asList(customCommands);
		for (Class<? extends CustomCommand> clazz : commandSet)
			if (!excluded.contains(clazz))
				unregister(clazz);
	}

	private void unregister(CustomCommand customCommand) {
		new Timer("  Unregister command " + customCommand.getName(), () -> {
			try {
				mapUtils.unregister(customCommand.getName());
				for (String alias : customCommand.getAllAliases())
					commands.remove(alias);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				customCommand._shutdown();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
	}

	private void registerConvertersAndTabCompleters() {
		commandSet.forEach(this::registerTabCompleters);
		commandSet.forEach(this::registerConverters);
		registerTabCompleters(CustomCommand.class);
		registerConverters(CustomCommand.class);
	}

	private void registerTabCompleters(Class<?> clazz) {
		methodsAnnotatedWith(clazz, TabCompleterFor.class).forEach(method -> {
			for (Class<?> classFor : method.getAnnotation(TabCompleterFor.class).value()) {
				method.setAccessible(true);
				tabCompleters.put(classFor, method);
			}
		});
	}

	private void registerConverters(Class<?> clazz) {
		methodsAnnotatedWith(clazz, ConverterFor.class).forEach(method -> {
			for (Class<?> classFor : method.getAnnotation(ConverterFor.class).value()) {
				method.setAccessible(true);
				converters.put(classFor, method);
			}
		});
	}

}
