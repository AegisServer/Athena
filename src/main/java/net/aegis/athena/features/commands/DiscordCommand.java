package net.aegis.athena.features.commands;

import net.aegis.athena.Athena;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DiscordCommand extends net.aegis.athena.utils.ChatColor implements CommandExecutor {

	private final Athena athena;

	public DiscordCommand(Athena athena) {
		this.athena = athena;
	}


	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

		if (!(sender instanceof Player player))
			return true;
		String aegisRed = "#9F6E75";
		String aegisBlue = "#6E759F";

		TextComponent component = Component.text("Click here to join our Discord!", TextColor.fromHexString(aegisRed))
				.hoverEvent(HoverEvent.showText(Component.text("Click here to open an invite in your browser", TextColor.fromHexString(aegisBlue))))
				.clickEvent(ClickEvent.openUrl("https://discord.gg/pUZq9v7uJA"));
		athena.adventure().player((Player) sender).sendMessage(component);

		return true;
	}

}