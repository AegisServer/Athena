package net.aegis.athena.features.commands;

import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.aegis.athena.Athena;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Aliases;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.ConverterFor;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.annotations.Redirects.Redirect;
import net.aegis.athena.framework.commands.models.annotations.TabCompleterFor;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.framework.exceptions.postconfigured.InvalidInputException;
import net.aegis.athena.models.mode.ModeUser;
import net.aegis.athena.models.mode.ModeUser.FlightMode;
import net.aegis.athena.models.mode.ModeUserService;
import net.aegis.athena.models.nerd.Rank;
import net.aegis.athena.utils.Tasks;
import net.aegis.athena.utils.worldgroup.WorldGroup;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Aliases("gm")
@NoArgsConstructor
@Permission("essentials.gamemode")
@Redirect(from = {"/gms", "/gm0"}, to = "/gm s")
@Redirect(from = {"/gmc", "/gm1"}, to = "/gm c")
@Redirect(from = {"/gma", "/gm2", "/adventure"}, to = "/gm a")
@Redirect(from = {"/gmsp", "/gm3", "/spectator", "/spec"}, to = "/gm sp")
public class GamemodeCommand extends CustomCommand implements Listener {

	private final ModeUserService service = new ModeUserService();
	private ModeUser user;

	public GamemodeCommand(@NonNull CommandEvent event) {
		super(event);
		if (isPlayerCommandEvent())
			user = service.get(player());
	}

	@Path("<gamemode> [player]")
	void run(GameMode gamemode, @Arg("self") Player player) {
		if (!isSelf(player)) {
			checkPermission("essentials.gamemode.others");
			user = service.get(player);
		}

		checkPermission("essentials.gamemode." + gamemode.name().toLowerCase());

		setGameMode(player, gamemode);

		send(player, PREFIX + "Switched to " + Athena.aegisBeige + camelCase(gamemode));
		if (!isSelf(player))
			send(PREFIX + "Switched to " + Athena.aegisBeige + camelCase(gamemode) + Athena.aegisBlue + " for " + Athena.aegisBeige + player.getName());

		if (!(worldGroup() == WorldGroup.UNKNOWN) && user.getRank().isStaff()) {
			user.setGameMode(worldGroup(), gamemode);
			service.save(user);
		}
	}

	public static void setGameMode(Player player, GameMode gamemode) {
		boolean flight = player.getAllowFlight();
		player.setGameMode(gamemode);
		player.setAllowFlight(flight);

		if (gamemode.equals(GameMode.CREATIVE) || gamemode.equals(GameMode.SPECTATOR)) {
			player.setAllowFlight(true);
			player.setFlying(true);
		}
	}

	@ConverterFor(GameMode.class)
	GameMode convertToGameMode(String value) {
		if (value != null)
			if ("3".equals(value) || value.startsWith("sp")) return GameMode.SPECTATOR;
			else if ("2".equals(value) || value.startsWith("a")) return GameMode.ADVENTURE;
			else if ("1".equals(value) || value.startsWith("c")) return GameMode.CREATIVE;
			else if ("0".equals(value) || value.startsWith("s")) return GameMode.SURVIVAL;
		throw new InvalidInputException("Invalid gamemode");
	}

	@TabCompleterFor(GameMode.class)
	List<String> tabCompleteGameMode(String filter) {
		return Arrays.stream(GameMode.values())
				.filter(gamemode -> player().hasPermission("essentials.gamemode." + gamemode.name().toLowerCase()))
				.map(gamemode -> gamemode.name().toLowerCase())
				.filter(name -> name.toLowerCase().startsWith(filter.toLowerCase()))
				.toList();
	}

	@EventHandler
	public void on(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		final WorldGroup newWorldGroup = WorldGroup.of(player);

		final Consumer<Boolean> flying = state -> {
			player.setAllowFlight(state);
			player.setFlying(state);
		};

		if (!Rank.of(player).isStaff()) {
			SpeedCommand.resetSpeed(player);

			if (newWorldGroup == WorldGroup.CREATIVE)
				flying.accept(true);
			else
				flying.accept(false);

			return;
		}

		if (player.getGameMode().equals(GameMode.SPECTATOR)) {
			player.setFallDistance(0);
			flying.accept(true);
			return;
		}

		Tasks.wait(5, () -> {
			ModeUser mode = new ModeUserService().get(player);
			GameMode gameMode = mode.getGamemode(newWorldGroup);
			FlightMode flightMode = mode.getFlightMode(newWorldGroup);

			setGameMode(player, gameMode);

			player.setAllowFlight(flightMode.isAllowFlight());
			player.setFlying(flightMode.isFlying());
		});
	}

}

