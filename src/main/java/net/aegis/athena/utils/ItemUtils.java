package net.aegis.athena.utils;

import net.aegis.athena.framework.exceptions.postconfigured.InvalidInputException;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static net.aegis.athena.utils.Nullables.isNullOrAir;

public class ItemUtils {

	public static ItemStack getTool(Player player, EquipmentSlot hand) {
		return player.getPlayer().getInventory().getItem(hand);
	}

	public static ItemStack getTool(Player player) {
		return getTool(player, (Material) null);
	}

	public static ItemStack getTool(Player player, Material material) {
		Player _player = player.getPlayer();
		ItemStack mainHand = _player.getInventory().getItemInMainHand();
		ItemStack offHand = _player.getInventory().getItemInOffHand();
		if (!isNullOrAir(mainHand) && (material == null || mainHand.getType() == material))
			return mainHand;
		else if (!isNullOrAir(offHand) && (material == null || offHand.getType() == material))
			return offHand;
		return null;
	}

	public static ItemStack getToolRequired(Player player) {
		ItemStack item = getTool(player);
		if (isNullOrAir(item))
			throw new InvalidInputException("You are not holding anything");
		return item;
	}

}
