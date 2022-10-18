package net.aegis.athena.features.scoreboard;

import com.gmail.nossr50.events.scoreboard.McMMOScoreboardRevertEvent;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.aegis.athena.features.menus.BookBuilder.WrittenBookMenu;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Aliases;
import net.aegis.athena.framework.commands.models.annotations.Description;
import net.aegis.athena.framework.commands.models.annotations.Disabled;
import net.aegis.athena.framework.commands.models.annotations.HideFromHelp;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.annotations.TabCompleteIgnore;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.models.nickname.Nickname;
import net.aegis.athena.models.scoreboard.ScoreboardService;
import net.aegis.athena.models.scoreboard.ScoreboardUser;
import net.aegis.athena.utils.JsonBuilder;
import net.aegis.athena.utils.PlayerUtils.OnlinePlayers;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.stream.Collectors;

@Disabled // TODO DATABASE: remove line
@NoArgsConstructor
@Aliases({"status", "sidebar", "sb", "featherboard"})
public class ScoreboardCommand extends CustomCommand implements Listener {
	private static final ScoreboardService service = new ScoreboardService();
	private ScoreboardUser user;

//	static {
//		OnlinePlayers.getAll().forEach(player -> {
//			ScoreboardUser user = new ScoreboardService().get(player);
//			if (user.isActive())
//				user.on();
//		});
//	}

	public ScoreboardCommand(@NonNull CommandEvent event) {
		super(event);
		user = service.get(player());
	}

	@Description("Turn the scoreboard on or off")
	@Path("[on/off]")
	void toggle(Boolean enable) {
		user.setActive((enable != null ? enable : !user.isActive()));
		if (!user.isActive())
			user.off();
		else {
			if (user.getLines().isEmpty())
				user.setLines(ScoreboardLine.getDefaultLines(player()));
			user.on();
		}
		service.save(user);
	}

	@Description("Control which lines you want to see")
	@Path("edit")
	void book() {
		WrittenBookMenu builder = new WrittenBookMenu();

		int index = 0;
		JsonBuilder json = new JsonBuilder();
		for (ScoreboardLine line : ScoreboardLine.values()) {
			if (!line.isOptional()) continue;
			if (!line.hasPermission(player()) && !user.getLines().containsKey(line)) continue;
			json.next((user.getLines().containsKey(line) && !user.getLines().get(line)) ? "&a✔" : "&c✗")
					.command("/scoreboard edit toggle " + line.name().toLowerCase())
					.hover("&eClick to toggle")
					.next(" ").group();
			json.next("&3" + camelCase(line))
					.hover(line.render(player()));
			json.newline().group();

			if (++index % 14 == 0) {
				builder.addPage(json);
				json = new JsonBuilder();
			}
		}

		builder.addPage(json).open(player());
	}

	@HideFromHelp
	@TabCompleteIgnore
	@Path("edit toggle <type> [enable]")
	void toggle(ScoreboardLine line, Boolean enable) {
		if (enable == null)
			enable = !user.getLines().containsKey(line) || !user.getLines().get(line);
		user.getLines().put(line, enable);
		if (!enable)
			user.remove(line);
		user.startTasks();
		service.save(user);
		book();
	}

	@Permission(Group.STAFF)
	@Path("list")
	void list() {
		String collect = OnlinePlayers.getAll().stream()
				.map(player -> new ScoreboardService().get(player))
				.filter(ScoreboardUser::isActive)
				.map(user -> Nickname.of(user.getUuid()))
				.collect(Collectors.joining("&3, &e"));
		send(PREFIX + "Active scoreboards: ");
		send("&e" + collect);
	}

	@Permission(Group.STAFF)
	@Path("view <player>")
	void view(OfflinePlayer player) {
		send(service.get(player).toString());
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		ScoreboardService service = new ScoreboardService();
		ScoreboardUser user = service.get(event.getPlayer());
		if (user.isActive())
			user.on();
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		ScoreboardService service = new ScoreboardService();
		ScoreboardUser user = service.get(event.getPlayer());
		user.pause();
	}

	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent event) {
		ScoreboardService service = new ScoreboardService();
		ScoreboardUser user = service.get(event.getPlayer());
		if (user.isActive())
			user.on();
	}

	@EventHandler
	public void onMcMMOScoreboardEnd(McMMOScoreboardRevertEvent event) {
		ScoreboardService service = new ScoreboardService();
		ScoreboardUser user = service.get(event.getTargetPlayer());
		if (user.isActive() && user.isOnline())
			user.on();
	}

}
