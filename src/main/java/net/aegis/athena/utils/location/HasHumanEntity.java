package net.aegis.athena.utils.location;

import org.bukkit.entity.HumanEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an object that has a {@link HumanEntity}
 * @see net.aegis.athena.utils.location.OptionalHumanEntity
 */
@FunctionalInterface
public interface HasHumanEntity extends OptionalHumanEntity {
	/**
	 * Gets a {@link HumanEntity} object that this represents
	 *
	 * @return human entity
	 */
	@Override
	@NotNull HumanEntity getPlayer();
}

