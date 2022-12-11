package net.aegis.athena.features.commands;

import lombok.NonNull;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.Description;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import org.bukkit.entity.Player;

@Description("Check your connection speed to the server")
public class PingCommand extends CustomCommand {

	public PingCommand(@NonNull CommandEvent event) {
		super(event);
	}

	@Path("[player]")
	void run(@Arg("self") Player player) {
		send(PREFIX + (isSelf(player) ? "Your" : player.getName() + "'s") + " ping is &e" + player.getPing() + "ms");
	}

}

