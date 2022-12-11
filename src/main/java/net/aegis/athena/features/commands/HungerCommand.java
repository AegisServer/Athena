package net.aegis.athena.features.commands;

import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import org.bukkit.entity.Player;

import static net.aegis.athena.utils.StringUtils.stripColor;

public class HungerCommand extends CustomCommand {

	public HungerCommand(CommandEvent event) {
		super(event);
	}

	@Path("[player] [number]")
	void hunger(@Arg("self") Player player, @Arg(permission = Group.STAFF) Integer hunger) {
		if (hunger == null)
			send(PREFIX + stripColor(player.getName()) + "'s hunger is " + player.getFoodLevel());
		else {
			player.setFoodLevel(hunger);
			send(PREFIX + stripColor(player.getName()) + "'s hunger set to " + player.getFoodLevel());
		}
	}

	@Path("target [number]")
	void target(Integer hunger) {
		hunger(getTargetPlayerRequired(), hunger);
	}
}
