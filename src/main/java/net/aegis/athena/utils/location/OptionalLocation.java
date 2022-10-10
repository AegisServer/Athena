package net.aegis.athena.utils.location;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface OptionalLocation {
	/**
	 * Gets a {@link Location} attached to this object if present
	 *
	 * @return attached location
	 */
	@Nullable Location getLocation();
}
