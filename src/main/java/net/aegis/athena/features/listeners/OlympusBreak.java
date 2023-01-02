package net.aegis.athena.features.listeners;

import lombok.NoArgsConstructor;
import net.aegis.athena.Athena;
import net.aegis.athena.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@NoArgsConstructor
public class OlympusBreak implements Listener {


	public static World getWorld() {
		return Bukkit.getWorld("mountaindoodle");
	}

	public static Location locationOf(double x, double y, double z) {
		return locationOf(x, y, z, 0, 0);
	}

	public static Location locationOf(double x, double y, double z, float yaw, float pitch) {
		return new Location(getWorld(), x, y, z, yaw, pitch);
	}

	public static boolean isNotAtOlympus(Block block) {
		return isNotAtOlympus(block.getLocation());
	}

	public static boolean isNotAtOlympus(Player player) {
		return isNotAtOlympus(player.getLocation());
	}

	public static boolean isNotAtOlympus(Location location) {
		return !location.getWorld().equals(getWorld());
	}

	public static boolean isNotAtOlympus(PlayerInteractEvent event) {
		return isNotAtOlympus(event.getHand(), event.getPlayer());
	}

	public static boolean isNotAtOlympus(PlayerInteractEntityEvent event) {
		return isNotAtOlympus(event.getHand(), event.getPlayer());
	}

	private static boolean isNotAtOlympus(EquipmentSlot slot, Player player) {
		if (!EquipmentSlot.HAND.equals(slot)) return true;

		return OlympusBreak.isNotAtOlympus(player);
	}

	public static ItemStack getItem(Block block) {
		return (ItemStack) block.getDrops().toArray()[0];
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();

		if (OlympusBreak.isNotAtOlympus(block))
			return;
		if (PlayerUtils.isWorldGuardEditing(player))
			return;

		event.setCancelled(true);

		if (event.getBlock().getType() != Material.WHEAT) {
			PlayerUtils.send(player, Athena.aegisPrimary + "You cannot break that here");
			return;
		}

		Ageable ageable = (Ageable) block.getBlockData();
		ageable.setAge(0);
		block.setBlockData(ageable);
		getWorld().dropItemNaturally(event.getBlock().getLocation(), getItem(event.getBlock()));
	}

}
