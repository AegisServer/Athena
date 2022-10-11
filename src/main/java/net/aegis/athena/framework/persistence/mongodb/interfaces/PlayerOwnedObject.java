package net.aegis.athena.framework.persistence.mongodb.interfaces;

import net.aegis.athena.framework.interfaces.DatabaseObject;
import net.aegis.athena.framework.interfaces.HasUniqueId;
import net.aegis.athena.framework.interfaces.Nicknamed;
import net.aegis.athena.framework.persistence.mongodb.models.nerd.Nerd;
import net.aegis.athena.framework.persistence.mongodb.models.nickname.Nickname;
import net.aegis.athena.framework.persistence.mongodb.models.nickname.NicknameService;
import net.aegis.athena.utils.Nullables;
import net.aegis.athena.utils.StringUtils;
import net.kyori.adventure.identity.Identity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PlayerOwnedObject extends DatabaseObject, Nicknamed, HasUniqueId {

	@NotNull UUID getUuid();

	/**
	 * Gets the unique ID of this object. Alias for {@link #getUuid()}, for compatibility with {@link HasUniqueId}.
	 *
	 * @return this object's unique ID
	 */
	@Override
	@NotNull
	default UUID getUniqueId() {
		return getUuid();
	}

	default @NotNull Nerd getNerd() {
		return Nerd.of(getUuid());
	}

	default @NotNull String getName() {
		return getNerd().getName();
	}

	default @NotNull String getNickname() {
		return Nickname.of(getUuid());
	}

	default Nickname getNicknameData() {
		return new NicknameService().get(getUuid());
	}

	default boolean hasNickname() {
		return !Nullables.isNullOrEmpty(getNicknameData().getNicknameRaw());
	}

	default String toPrettyString() {
		return StringUtils.toPrettyString(this);
	}

/* fuck
	default boolean equals(Object obj) {
		if (!this.getClass().equals(obj.getClass())) return false;
		return getUuid().equals(((PlayerOwnedObject) obj).getUuid());
	}
*/

}
