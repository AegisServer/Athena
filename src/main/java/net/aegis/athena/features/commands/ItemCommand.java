package net.aegis.athena.features.commands;

import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Aliases;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.utils.PlayerUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

@Aliases("i")
@Permission("essentials.item")
public class ItemCommand extends CustomCommand {

	public ItemCommand(CommandEvent event) {
		super(event);
	}

	@Path("<item> [amount] [nbt...]")
	void run(ItemStack item, @Arg(min = 1, max = 2304, minMaxBypass = Group.STAFF) Integer amount, @Arg(permission = Group.STAFF) String nbt) {
		item.setAmount(amount == null ? item.getType().getMaxStackSize() : amount);
		PlayerUtils.giveItem(player(), item, nbt);
	}

	//TODO For resourcepack stuff
//	@Permission(Group.STAFF)
//	@Path("rp <material> <id>")
//	void rp(Material material, int id) {
//		PlayerUtils.giveItem(player(), new ItemBuilder(material).modelId(id).build());
//	}

	@Path("tag <tag> [amount]")
	void tag(Tag<?> tag, @Arg("1") int amount) {
		tag.getValues().forEach(tagged -> {
			if (tagged instanceof Material material)
				run(new ItemStack(material), amount, null);
				// for custom blocks
//			else if (tagged instanceof CustomBlock customBlock)
//				run(customBlock.get().getItemStack(), amount, null);
			else
				error("Unsupported tag type");
		});
	}

	//TODO For custom recipes
//	@Permission(Group.SENIOR_STAFF)
//	@Path("ingredients <item> [amount] [--index]")
//	void ingredients(ItemStack itemStack, @Arg("1") int amount, @Switch int index) {
//		final List<List<ItemStack>> recipes = RecipeUtils.uncraft(itemStack);
//		if (recipes.isEmpty())
//			error("No recipes found for &e" + camelCase(arg(2)));
//
//		if (index >= recipes.size())
//			error(camelCase(arg(2)) + " only has &e" + recipes.size() + plural(" recipe", recipes.size()));
//
//		for (int i = 0; i < amount; i++)
//			recipes.get(index).forEach(this::giveItem);
//	}

}
