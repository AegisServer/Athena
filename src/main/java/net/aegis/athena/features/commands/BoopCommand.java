package net.aegis.athena.features.commands;

import net.aegis.athena.Athena;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Cooldown;
import net.aegis.athena.framework.commands.models.annotations.Description;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.annotations.Switch;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.models.nickname.Nickname;
import net.aegis.athena.utils.JsonBuilder;
import net.aegis.athena.utils.PlayerUtils.OnlinePlayers;
import net.aegis.athena.utils.TimeUtils.TickTime;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

import java.util.List;


@Description("Boop!")
@Cooldown(value = TickTime.SECOND, x = 5, bypass = Group.ADMIN)
public class BoopCommand extends CustomCommand {

	public static final Sound SOUND = Sound.sound(
			org.bukkit.Sound.BLOCK_NOTE_BLOCK_XYLOPHONE,
			Sound.Source.MASTER,
			1f,
			0.1f
	);

	public BoopCommand(CommandEvent event) {
		super(event);
	}

	@Path("all [message...] [--anonymous]")
	@Description("boop all players")
	@Permission(Group.ADMIN)
	void boopAll(String message, @Switch(shorthand = 'a') boolean anonymous) {
		final List<Player> players = OnlinePlayers.where().viewer(player()).get().stream().toList();

		if (players.isEmpty())
			error("No players to boop");

		for (Player player : players) {
			try {
				run(player(), player, message, anonymous);
			} catch (Exception ignore) {
			}
		}
	}

	@Path("<player> [message...] [--anonymous]")
	@Description("boop a player")
	void boop(Player player, String message, @Switch(shorthand = 'a') boolean anonymous) {
		if (message == null)
			message = "";
	}

	public void run(Player booper, Player booped, String message, boolean anonymous) {
		if (isSelf(booped))
			error("You cannot boop yourself!");

		String boopedName = Nickname.of(booped);

		String toBooper = PREFIX;
		String toBooped = PREFIX;
		if (!message.equalsIgnoreCase(""))
			message = Athena.aegisSecondary + " and said " + Athena.aegisPrimary + message;

		if (anonymous) {
			toBooper += Athena.aegisPrimary + "You anonymously booped " + Athena.aegisSecondary + boopedName + message;
			toBooped += Athena.aegisSecondary + "Somebody " + Athena.aegisPrimary + "booped you" + message;
		} else {
			toBooper += Athena.aegisPrimary + "You booped " + Athena.aegisSecondary + message;
			toBooped += Athena.aegisSecondary + nickname() + Athena.aegisPrimary + " booped you" + message;
		}

		send(toBooper);
		JsonBuilder json = new JsonBuilder(toBooped);
		if (!anonymous)
			json.next(Athena.aegisAccent + ". " + Athena.aegisPrimary + "Click to boop back").suggest("/boop " + Nickname.of(booper) + " ");
		// TODO 1.19.2 Chat Validation Kick
		// booped.sendMessage(booper, json);
		booped.sendMessage(json);
		booped.playSound(SOUND);
	}
}
