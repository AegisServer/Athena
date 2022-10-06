package net.aegis.athena.features.commands;

import net.aegis.athena.Athena;
import net.aegis.athena.utils.JsonBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DiscordCommand implements CommandExecutor {

	private final Athena athena;

	public DiscordCommand(Athena athena) {
		this.athena = athena;
	}


	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

		if (!(sender instanceof Player player))
			return true;

//		TextComponent component = Component.text("Click here to join our Discord!", TextColor.fromHexString(aegisRed))
//				.hoverEvent(HoverEvent.showText(Component.text("Click here to open an invite in your browser", TextColor.fromHexString(aegisBlue))))
//				.clickEvent(ClickEvent.openUrl("https://discord.gg/pUZq9v7uJA"));
//		athena.adventure().player((Player) sender).sendMessage(component);

		new JsonBuilder()
				.next("&#9F6E75Click here to join our Discord!")
				.hover("&#6E759FClick here to open an invite in your browser")
				.url("https://discord.gg/pUZq9v7uJA")
				.send(player);
		return true;
	}

}