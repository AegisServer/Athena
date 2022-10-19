package net.aegis.athena.features.commands;

import lombok.NonNull;
import net.aegis.athena.Athena;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Aliases;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.annotations.Switch;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.framework.exceptions.postconfigured.InvalidInputException;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

@Aliases("ench")
@Permission("enchant.use")
public class EnchantCommand extends CustomCommand {

	public EnchantCommand(@NonNull CommandEvent event) {
		super(event);
	}

	@Path("<enchant> [level]")
	void run(Enchantment enchantment, @Arg(value = "1", max = 127, minMaxBypass = Group.ADMIN) int level, @Switch @Arg("true") boolean unsafe) {
		if (level < 1) {
			remove(enchantment);
			return;
		}

		try {
			final ItemStack tool = getToolRequired();
			final ItemMeta meta = tool.getItemMeta();

			if (meta instanceof EnchantmentStorageMeta storageMeta)
				storageMeta.addStoredEnchant(enchantment, level, unsafe);
			else
				meta.addEnchant(enchantment, level, unsafe);

			tool.setItemMeta(meta);

			send(PREFIX + "Added enchant " + Athena.aegisBeige + camelCase(enchantment.getKey().getKey()) + " " + level);
		} catch (IllegalArgumentException ex) {
			throw new InvalidInputException(ex.getMessage());
		}
	}

	@Path("remove <enchant>")
	void remove(Enchantment enchantment) {
		final ItemStack tool = getToolRequired();
		tool.removeEnchantment(enchantment);

		send(PREFIX + "Removed enchant " + Athena.aegisBeige + camelCase(enchantment.getKey().getKey()));
	}

	@Path("get <enchant>")
	void get(Enchantment enchantment) {
		send(PREFIX + "Level of " + Athena.aegisBeige + camelCase(enchantment.getKey().getKey()) + Athena.aegisBlue + ": " + Athena.aegisBeige + getToolRequired().getEnchantmentLevel(enchantment));
	}

}
