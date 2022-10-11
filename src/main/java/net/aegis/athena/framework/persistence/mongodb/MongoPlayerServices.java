package net.aegis.athena.framework.persistence.mongodb;

import net.aegis.athena.framework.exceptions.postconfigured.PlayerNotFoundException;
import net.aegis.athena.framework.persistence.mongodb.interfaces.PlayerOwnedObject;
import net.aegis.athena.framework.persistence.mongodb.models.nerd.Nerd;
import net.aegis.athena.framework.persistence.mongodb.models.nerd.NerdService;
import net.aegis.athena.utils.UUIDUtils;

import java.util.UUID;
import java.util.function.Function;

public abstract class MongoPlayerServices<T extends PlayerOwnedObject> extends MongoService<T> {

	public T get(String name) {
		Nerd nerd = new NerdService().findExact(name);
		if (nerd == null)
			throw new PlayerNotFoundException(name);
		return get(nerd);
	}

	public void saveSync(T object) {
		if (!isUuidValid(object))
			return;

		super.saveSync(object);
	}

	public void deleteSync(T object) {
		if (!isUuidValid(object))
			return;

		super.deleteSync(object);
	}

	private static final Function<UUID, Boolean> isV4 = UUIDUtils::isV4Uuid;
	private static final Function<UUID, Boolean> is0 = UUIDUtils::isUUID0;
	private static final Function<UUID, Boolean> isApp = UUIDUtils::isAppUuid;

	private boolean isUuidValid(T object) {
		final UUID uuid = object.getUuid();
		return isV4.apply(uuid) || is0.apply(uuid) || isApp.apply(uuid);
	}

}
