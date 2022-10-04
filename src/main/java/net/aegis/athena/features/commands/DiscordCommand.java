package net.aegis.athena.features.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DiscordCommand extends net.aegis.athena.utils.ChatColor implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

		if (!(sender instanceof Player player))
			return true;
		String aegisRed = "#9F6E75";
		player.sendMessage(translateHexColorCodes(aegisRed) + "https://discord.gg/pUZq9v7uJA");

		return true;
	}

}