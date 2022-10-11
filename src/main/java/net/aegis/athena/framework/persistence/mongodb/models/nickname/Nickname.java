package net.aegis.athena.framework.persistence.mongodb.models.nickname;

import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.converters.UUIDConverter;
import lombok.*;
import net.aegis.athena.framework.interfaces.HasUniqueId;
import net.aegis.athena.framework.persistence.mongodb.interfaces.PlayerOwnedObject;
import net.aegis.athena.utils.UUIDUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static net.aegis.athena.utils.Nullables.isNullOrEmpty;

@Getter
@Builder
@Entity(value = "nickname", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters(UUIDConverter.class)
public class Nickname implements PlayerOwnedObject {
	@Id
	@NonNull
	protected UUID uuid;

	protected String nickname;

	public static String of(String name) {
		return new NicknameService().get(name).getNickname();
	}

	public static String of(HasUniqueId player) {
		return new NicknameService().get(player).getNickname();
	}

	public static String of(UUID uuid) {
		return new NicknameService().get(uuid).getNickname();
	}

	public @NotNull String getNickname() {
		if (UUIDUtils.isUUID0(uuid))
			return "Console";
		if (isNullOrEmpty(nickname))
			return getName();
		return nickname;
	}

	public String getNicknameRaw() {
		return nickname;
	}

	public boolean hasNickname() {
		return !isNullOrEmpty(nickname);
	}

}
