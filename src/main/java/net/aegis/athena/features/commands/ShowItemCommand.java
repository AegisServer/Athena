package net.aegis.athena.features.commands;

import lombok.NonNull;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.Cooldown;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.utils.Nullables;
import net.aegis.athena.utils.StringUtils;
import net.aegis.athena.utils.TimeUtils.TickTime;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import static net.aegis.athena.utils.Nullables.isNullOrAir;

// TODO: turn into a message being sent from the player, so that other users can see the /showitem

@Permission("athena.showitem")
public class ShowItemCommand extends CustomCommand {

	public ShowItemCommand(@NonNull CommandEvent event) {
		super(event);
	}

	@Path("[hand|offhand]")
	@Cooldown(value = TickTime.SECOND, x = 15, bypass = Group.ADMIN)
	void run(@Arg("hand") String slot) {
		//get player item in hand and put in variable to use later
		ItemStack item = getItem(player(), slot);

		// set variable for ItemMeta display name and material name to turn into camel case instead of upper case
		String itemName = item.getItemMeta().getDisplayName();
		String material = StringUtils.camelCase(item.getType().name());

		// if itemname does not exist (vanilla items), set item name to the material
		if (Nullables.isNullOrEmpty(itemName))
			itemName = material;

		//create jsonbuilder to send player their item in chat with a hover for that item (shows name,lore,etc)
		send(json().next("&8&l[&r" + itemName + "&8&l]&r").hover(item));
	}

	private ItemStack getItem(Player player, String slot) {
		ItemStack item = null;
		PlayerInventory inv = player.getInventory();

		switch (slot.toLowerCase()) {
			case "offhand" -> item = inv.getItemInOffHand();
			case "mainhand", "hand" -> item = inv.getItemInMainHand();
			case "hat", "head", "helm", "helmet" -> item = inv.getHelmet();
			case "chest", "chestplate" -> item = inv.getChestplate();
			case "pants", "legs", "leggings" -> item = inv.getLeggings();
			case "boots", "feet", "shoes" -> item = inv.getBoots();
			default -> error("Unknown slot &e" + slot);
		}

		if (isNullOrAir(item))
			error("Item in " + slot + " not found");

		return item;
	}
}
