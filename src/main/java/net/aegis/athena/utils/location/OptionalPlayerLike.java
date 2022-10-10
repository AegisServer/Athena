package net.aegis.athena.utils.location;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface OptionalPlayerLike extends OptionalPlayer, net.aegis.athena.framework.interfaces.HasUniqueId, HasOfflinePlayer, OptionalLocation, Identified, ForwardingAudience.Single {
	/**
	 * Gets the identity associated with this object
	 *
	 * @return associated identity
	 */
	@Override
	default @NotNull Identity identity() {
		return Identity.identity(getUniqueId());
	}

	/**
	 * Returns if the {@link Player} associated with this object is online.
	 *
	 * @return if the player is online
	 */
	default boolean isOnline() {
		return getPlayer() != null;
	}

	@Override
	default @NotNull Audience audience() {
		return Objects.requireNonNullElse(getPlayer(), Audience.empty());
	}

	@Override
	default @Nullable Location getLocation() {
		final Player player = getPlayer();
		return player == null ? null : player.getLocation();
	}
}
