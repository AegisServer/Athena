package net.aegis.athena.utils;

import net.aegis.athena.framework.interfaces.PlayerOwnedObject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Name {
	private static final Map<UUID, String> NAME_CACHE = new HashMap<>();

	public static @Nullable String of(@NotNull UUID uuid) {
		if (!NAME_CACHE.containsKey(uuid)) {
			String name = Bukkit.getOfflinePlayer(uuid).getName();
			if (name != null)
				NAME_CACHE.put(uuid, name);
		}
		return NAME_CACHE.get(uuid);
	}

	public static @Nullable String of(@NotNull OfflinePlayer player) {
		if (player instanceof Player online)
			return of(online);

		UUID uuid = player.getUniqueId();
		if (!NAME_CACHE.containsKey(uuid)) {
			String name = player.getName();
			if (name != null)
				NAME_CACHE.put(uuid, name);
		}
		return NAME_CACHE.get(player.getUniqueId());
	}

	public static @NotNull String of(@NotNull Player player) {
		return put(player.getUniqueId(), player.getName());
	}

	public static @Nullable String of(@NotNull PlayerOwnedObject object) {
		Player player = object.getPlayer();
		if (player != null)
			return of(player);
		else
			return of(object.getUniqueId());
	}

	public static @NotNull String put(@NotNull UUID uuid, @NotNull String name) {
		NAME_CACHE.put(uuid, name);
		return name;
	}
}
