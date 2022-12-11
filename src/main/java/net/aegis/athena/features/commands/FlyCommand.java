package net.aegis.athena.features.commands;

import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.models.mode.ModeUser;
import net.aegis.athena.models.mode.ModeUserService;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Permission("essentials.fly")
public class FlyCommand extends CustomCommand {
	private final ModeUserService service = new ModeUserService();
	private ModeUser user;

	public FlyCommand(@NotNull CommandEvent event) {
		super(event);
		if (isPlayerCommandEvent())
			user = service.get(player());
	}

	@Path("[enable] [player]")
	void run(Boolean enable, @Arg(value = "self", permission = Group.STAFF) Player player) {
		if (!isSelf(player))
			user = service.get(player);

		if (enable == null)
			enable = !player.getAllowFlight();

		if (!enable && GameMode.SPECTATOR.equals(player.getGameMode()))
			error("You cannot disable fly in spectator mode");

		if (enable)
			on(player);
		else
			off(player);

		send(player, PREFIX + (enable ? "&aEnabled" : "&cDisabled"));
		if (!isSelf(player))
			send(PREFIX + "Fly " + (enable ? "&aenabled" : "&cdisabled") + " &3for &e" + player.getName());

		if (user.getRank().isStaff()) {
			user.setFlightMode(worldGroup(), player.getAllowFlight(), player.isFlying());
			service.save(user);
		}
	}

	public static void off(Player player) {
		player.setFallDistance(0);
		player.setAllowFlight(false);
		player.setFlying(false);
	}

	public static void on(Player player) {
		player.setFallDistance(0);
		player.setAllowFlight(true);
	}
}
