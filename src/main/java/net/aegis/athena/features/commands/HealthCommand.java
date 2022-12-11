package net.aegis.athena.features.commands;

import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.Description;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.annotations.Redirects.Redirect;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.utils.StringUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import static net.aegis.athena.utils.StringUtils.stripColor;

@Redirect(from = "/entityhealth", to = "/health target")
@Description("View the health of a player or entity")
public class HealthCommand extends CustomCommand {

	public HealthCommand(CommandEvent event) {
		super(event);
	}

	@Path("<player> [number]")
	void health(@Arg("self") Player player, @Arg(permission = Group.STAFF, min = 0.0, max = 20.0) Double health) {
		String healthFormat = getFormattedHealth(player);
		if (health == null)
			send(PREFIX + player.getName() + "'s health is &e" + healthFormat);
		else {
			player.setHealth(health);
			send(PREFIX + stripColor(player.getName()) + "'s health set to &e" + healthFormat);
		}
	}

	@Path("target [number]")
	void target(@Arg(permission = Group.STAFF, min = 0.0, max = 20.0) Double health) {
		LivingEntity target = getTargetLivingEntityRequired();

//		GlowUtils.GlowTask.builder()
//				.duration(10 * 20)
//				.entity(target)
//				.color(GlowColor.RED)
//				.viewers(Collections.singletonList(player()))
//				.start();

		String healthFormat = getFormattedHealth(target);
		if (health == null)
			send(PREFIX + stripColor(target.getName()) + "'s health is &e" + healthFormat);
		else {
			target.setHealth(health);
			send(PREFIX + stripColor(target.getName()) + "'s health set to &e" + healthFormat);
		}
	}

	private String getFormattedHealth(LivingEntity livingEntity) {
		return StringUtils.getDf().format(livingEntity.getHealth());
	}
}
