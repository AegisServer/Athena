package net.aegis.athena.framework.interfaces;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface DatabaseObject extends net.aegis.athena.framework.interfaces.HasUniqueId {
	UUID getUuid();

	@Override
	default @NotNull UUID getUniqueId() {
		return getUuid();
	}
}
