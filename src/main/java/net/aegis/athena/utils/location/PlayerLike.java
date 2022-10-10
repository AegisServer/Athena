package net.aegis.athena.utils.location;

import net.kyori.adventure.audience.Audience;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface PlayerLike extends HasPlayer, HasLocation, OptionalPlayerLike {

	// reduce nullability checks by re-implementing the methods from OptionalPlayerLike
	// (but without the null checks)

	@Override
	default @NotNull Audience audience() {
		return getPlayer();
	}

	@Override
	default boolean isOnline() {
		return true;
	}

	@Override
	default @NotNull Location getLocation() {
		return getPlayer().getLocation();
	}
}
