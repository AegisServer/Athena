package net.aegis.athena.features.commands;

import net.aegis.athena.utils.*;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShowItemCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

		if (!(sender instanceof Player player))
			return true;
		if (!(player.hasPermission("athena.showitem")))
			return true;

		//get player item in hand and put in variable to use in new itemstack
		ItemStack item = ItemUtils.getTool(player);

		if (Nullables.isNullOrAir(item)){
			PlayerUtils.send(player, "&#9F6E75You are not holding anything");
			return true;
		}

		String itemName = item.getItemMeta().getDisplayName();
		String material = StringUtils.camelCase(item.getType().name());

		if (Nullables.isNullOrEmpty(itemName))
			itemName = material;

		new JsonBuilder()
				.next("&8&l[&r" + itemName + "&8&l]&r")
				.hover(item)
				.send(player);

		return true;
	}
}
