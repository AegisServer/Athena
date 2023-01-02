package net.aegis.athena.features.commands;

import lombok.NonNull;
import net.aegis.athena.Athena;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Aliases;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.utils.StringUtils;
import org.bukkit.entity.Player;


@Aliases({"exp", "xp"})
@Permission("experience.use")
public class ExperienceCommand extends CustomCommand {

	public ExperienceCommand(@NonNull CommandEvent event) {
		super(event);
	}

	@Path("<level>")
	void level(@Arg(max = 10, minMaxBypass = Group.SENIOR_STAFF) double amount) {
		set(player(), amount);
	}

	@Path("get <player>")
	@Permission(Group.SENIOR_STAFF)
	void get(Player player) {
		send(PREFIX + player.getName() + " has " + Athena.aegisAccent + getFormattedExp(player));
	}

	private void tellTotalExp(Player player) {
		send(PREFIX + player.getName() + " now has " + Athena.aegisAccent + getFormattedExp(player));
	}

	@Path("set <player> <level>")
	@Permission(Group.SENIOR_STAFF)
	void set(Player player, double amount) {
		int levels = (int) amount;
		float exp = (float) (amount - levels);
		validateAndRun(player, levels, exp);
	}

	@Path("give <player> <amount>")
	@Permission(Group.SENIOR_STAFF)
	void give(Player player, double amount) {
		int levels = (int) amount;
		float exp = (float) (amount - levels);
		if (player.getExp() + exp >= 1) {
			++levels;
			--exp;
		}

		validateAndRun(player, player.getLevel() + levels, player.getExp() + exp);
	}

	@Path("take <player> <amount>")
	@Permission(Group.SENIOR_STAFF)
	void take(Player player, double amount) {
		int levels = (int) amount;
		float exp = (float) (amount - levels);
		if (player.getExp() - exp < 0) {
			++levels;
			--exp;
		}

		validateAndRun(player, player.getLevel() - levels, player.getExp() - exp);
	}

	private void validateAndRun(Player player, int levels, float exp) {
		if (levels < 0)
			error("Level cannot be negative");
		if (levels > Short.MAX_VALUE)
			error("Level cannot be greater than " + Short.MAX_VALUE);
		if (exp < 0)
			error("Exp cannot be negative");
		if (exp >= 1)
			error("Exp cannot be greater or equal to than 1");

		player.setLevel(levels);
		player.setExp(exp);
		tellTotalExp(player);
	}

	private String getFormattedExp(Player player) {
		String totalExp = StringUtils.stripTrailingZeros(StringUtils.pretty(Double.parseDouble(player.getLevel() + StringUtils.trimFirst(String.valueOf(player.getExp())))));
		return totalExp + plural(" level", Double.parseDouble(totalExp));
	}

}
