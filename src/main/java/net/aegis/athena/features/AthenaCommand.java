package net.aegis.athena.features;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.aegis.athena.Athena;
import net.aegis.athena.features.listeners.common.TemporaryListener;
import net.aegis.athena.framework.commands.CommandMapUtils;
import net.aegis.athena.framework.commands.Commands;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.annotations.Switch;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.framework.exceptions.AthenaException;
import net.aegis.athena.framework.exceptions.postconfigured.CommandCooldownException;
import net.aegis.athena.framework.exceptions.postconfigured.InvalidInputException;
import net.aegis.athena.framework.features.Features;
import net.aegis.athena.framework.persistence.mongodb.MongoPlayerService;
import net.aegis.athena.models.cooldown.CooldownService;
import net.aegis.athena.models.nickname.Nickname;
import net.aegis.athena.utils.JsonBuilder;
import net.aegis.athena.utils.PlayerUtils;
import net.aegis.athena.utils.Tasks;
import net.aegis.athena.utils.TimeUtils.TickTime;
import net.aegis.athena.utils.TimeUtils.Timespan;
import net.aegis.athena.utils.Utils;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipFile;

import static net.aegis.athena.utils.Nullables.isNullOrEmpty;
import static net.aegis.athena.utils.UUIDUtils.UUID0;

@NoArgsConstructor
@Permission(Group.ADMIN)
public class AthenaCommand extends CustomCommand {

	public AthenaCommand(CommandEvent event) {
		super(event);
	}

	@Path("uptime")
	void uptime() {
		send(PREFIX + "Up for" + Athena.aegisBeige + " " + Timespan.of(Athena.EPOCH).format());
	}

	@Path("gc")
	void gc() {
		send("Collecting garbage...");
		System.gc();
		send("Garbage collected");
	}

	@Path("getEnv")
	void getEnv() {
		send(Athena.getEnv().name());
	}

	@Path("version")
	void getPluginYMLVersion() {
		send(getVersion());
	}

	@Path("stats")
	void stats() {
		send("Features: " + Features.getRegistered().size());
		send("Commands: " + new HashSet<>(Commands.getCommands().values()).size());
		send("Listeners: " + Athena.getListeners().size());
		send("Temporary Listeners: " + Athena.getTemporaryListeners().size());
		send("Event Handlers: " + Athena.getEventHandlers().size());
		send("Services: " + MongoPlayerService.getServices().size());
	}

	@Path("stats commands [page]")
	void statsCommands(@Arg("1") int page) {
		CommandMapUtils mapUtils = Athena.getInstance().getCommands().getMapUtils();

		Map<Plugin, Integer> commands = new HashMap<>();
		Set<String> keys = new HashSet<>();

		for (Command value : mapUtils.getKnownCommandMap().values()) {
			if (!(value instanceof PluginCommand command))
				continue;

			Plugin plugin = command.getPlugin();

			String commandName = command.getName();
			if (commandName.contains(":"))
				commandName = commandName.split(":")[1];

			String key = plugin.getName() + "-" + commandName;
			if (keys.contains(key))
				continue;

			keys.add(key);
			commands.put(plugin, commands.getOrDefault(plugin, 0) + 1);
		}

		send(PREFIX + "Commands by plugin");
		paginate(Utils.sortByValueReverse(commands).keySet(), (plugin, index) ->
				json(index + " &e" + plugin.getName() + " &7- " + commands.get(plugin)), "/athena stats commands", page);
	}

	@Path("closeInventory [player]")
	void closeInventory(@Arg("self") Player player) {
		player.closeInventory();
	}

	@Path("reload cancel")
	void cancelReload() {
		reloader = null;
		excludedConditions = null;
		send(PREFIX + "Reload cancelled");
	}

	@Path("reload [--excludedConditions]")
	void reload(@Switch @Arg(type = ReloadCondition.class) List<ReloadCondition> excludedConditions) {
		AthenaCommand.excludedConditions = excludedConditions;

		try {
			ReloadCondition.tryReload(excludedConditions);
		} catch (Exception ex) {
			reloader = uuid();
			JsonBuilder json = new JsonBuilder(ex.getMessage(), NamedTextColor.RED);
			if (ex instanceof AthenaException nex)
				json = new JsonBuilder(nex.getJson());

			error(json.next(", reload queued ").group().next("&e⟳").hover("&eClick to retry manually").command("/athena reload"));
		}

		// TODO DATABASE: uncomment
		CooldownService cooldownService = new CooldownService();
		if (!cooldownService.check(UUID0, "reload", TickTime.SECOND.x(15)))
			throw new CommandCooldownException(UUID0, "reload");

		runCommand("plugman reload Athena");
	}

	private static UUID reloader;
	private static List<ReloadCondition> excludedConditions;

	static {
		Tasks.repeat(TickTime.SECOND.x(5), TickTime.SECOND.x(5), AthenaCommand::tryReload);
	}

	private static void tryReload() {
		if (reloader == null)
			return;

		if (!ReloadCondition.canReload(excludedConditions))
			return;

		OfflinePlayer player = Bukkit.getPlayer(reloader);
		if (player == null)
			return;

		PlayerUtils.runCommand(player.getPlayer(), "athena reload");
	}

	@Getter
	@AllArgsConstructor
	public enum ReloadCondition {
		FILE_NOT_FOUND(() -> {
			File file = Paths.get("plugins/Athena-" + getVersion() + ".jar").toFile();
			if (!file.exists())
				throw new InvalidInputException("Athena.jar doesn't exist");
		}),
		FILE_NOT_COMPLETE(() -> {
			File file = Paths.get("plugins/Athena-" + getVersion() + ".jar").toFile();
			try {
				new ZipFile(file).entries();
			} catch (IOException ex) {
				throw new InvalidInputException("Athena.jar is not complete");
			}
		}),
		TEMP_LISTENERS(() -> {
			if (!Athena.getTemporaryListeners().isEmpty())
				throw new InvalidInputException(new JsonBuilder("There are " + Athena.getTemporaryListeners().size() + " temporary listeners registered").command("/athena temporaryListeners").hover("&eClick to view"));
		}),
//		SMARTINVS(() -> {
//			long count = OnlinePlayers.getAll().stream().filter(player -> {
//				boolean open = SmartInvsPlugin.manager().getInventory(player).isPresent();
//
//				if (open && AFK.get(player).hasBeenAfkFor(TickTime.MINUTE.x(15))) {
//					player.closeInventory();
//					open = false;
//				}
//
//				return open;
//			}).count();
//
//			if (count > 0)
//				throw new InvalidInputException(new JsonBuilder("There are " + count + " SmartInvs menus open").command("/athena smartInvs").hover("&eClick to view"));
//		}),
//		SIGN_MENUS(() -> {
//			if (!Nexus.getSignMenuFactory().getInputReceivers().isEmpty())
//				throw new InvalidInputException("There are " + Nexus.getSignMenuFactory().getInputReceivers().size() + " sign menus open");
//		}),
//		QUEST_DIALOG(() -> {
//			for (Quester quester : new QuesterService().getOnline())
//				if (quester.getDialog() != null)
//					if (quester.getDialog().getTaskId().get() > 0)
//						throw new InvalidInputException("Someone is in a quest dialog");
//		}),
//		RESOURCE_PACK(() -> {
//			if (ResourcePack.isReloading())
//				throw new InvalidInputException("Resource pack is reloading!");
//		}),
		;

		public static boolean canReload() {
			return canReload(null);
		}

		public static boolean canReload(List<ReloadCondition> excludedConditions) {
			try {
				tryReload(excludedConditions);
			} catch (Exception ex) {
				return false;
			}

			return true;
		}

		public static void tryReload() {
			tryReload(null);
		}

		public static void tryReload(List<ReloadCondition> excludedConditions) {
			for (ReloadCondition condition : ReloadCondition.values())
				if (isNullOrEmpty(excludedConditions) || !excludedConditions.contains(condition))
					condition.run();
		}

		public void run() {
			runnable.run();
		}

		private final Runnable runnable;
	}

	@Path("temporaryListeners")
	void temporaryListeners() {
		if (Athena.getTemporaryListeners().isEmpty())
			error("No temporary listeners registered");

		send(PREFIX + "Temporary listeners");

		for (TemporaryListener temporaryListener : Athena.getTemporaryListeners()) {
			final Player player = temporaryListener.getPlayer();

			send((player.isOnline() ? "&e" : "&c") + Nickname.of(player) + " &7- " + temporaryListener.getClass().getSimpleName());
		}
	}

	private static String getVersion() {
		return Athena.getInstance().getDescription().getVersion();
	}
}
