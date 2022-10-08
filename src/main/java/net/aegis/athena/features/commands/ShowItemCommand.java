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

		/**
		 * TODO:
		 * turn .send(player) into a broadcast being sent from the player so that other users can see the /showitem; requires own chat api
		 * let users specify between hand or offhand- armor slots will not be supported
		 */

		if (!(sender instanceof Player player))
			return true;
		if (!(player.hasPermission("athena.showitem")))
			return true;

		//get player item in hand and put in variable to use later
		ItemStack item = ItemUtils.getTool(player);

		if (Nullables.isNullOrAir(item)){
			PlayerUtils.send(player, "&#9F6E75You are not holding anything");
			return true;
			// if player item == null or air, end code and send player error message)
		}

		String itemName = item.getItemMeta().getDisplayName();
		String material = StringUtils.camelCase(item.getType().name());
		// set variable for ItemMeta display name and material name to turn into camel case instead of upper case

		if (Nullables.isNullOrEmpty(itemName))
			itemName = material;
		// if itemname does not exist (vanilla items), set item name to the material

		new JsonBuilder()
				.next("&8&l[&r" + itemName + "&8&l]&r")
				.hover(item)
				.send(player);
		//create jsonbuilder to send player their item in chat with a hover for that item (shows name,lore,etc)

		return true;
	}
}
