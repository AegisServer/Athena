package net.aegis.athena.features.commands;

import lombok.NonNull;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.utils.Nullables;
import net.aegis.athena.utils.StringUtils;
import org.bukkit.inventory.ItemStack;

/*
 * TODO:
 * 	- turn .send(player) into a broadcast being sent from the player so that other users can see
 * 		the /showitem; requires own chat api let users specify between hand or offhand- armor slots
 * 		will not be supported
 */

@Permission("athena.showitem")
public class ShowItemCommand extends CustomCommand {

	public ShowItemCommand(@NonNull CommandEvent event) {
		super(event);
	}

	@Path
	void run() {
		//get player item in hand and put in variable to use later
		ItemStack item = getToolRequired();

		// set variable for ItemMeta display name and material name to turn into camel case instead of upper case
		String itemName = item.getItemMeta().getDisplayName();
		String material = StringUtils.camelCase(item.getType().name());

		// if itemname does not exist (vanilla items), set item name to the material
		if (Nullables.isNullOrEmpty(itemName))
			itemName = material;

		//create jsonbuilder to send player their item in chat with a hover for that item (shows name,lore,etc)
		send(json().next("&8&l[&r" + itemName + "&8&l]&r").hover(item));
	}
}
