package net.aegis.athena.models.mode;

import net.aegis.athena.framework.persistence.mongodb.MongoPlayerService;
import net.aegis.athena.framework.persistence.mongodb.annotations.ObjectClass;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(ModeUser.class)
public class ModeUserService extends MongoPlayerService<ModeUser> {
	private final static Map<UUID, ModeUser> cache = new ConcurrentHashMap<>();

	public Map<UUID, ModeUser> getCache() {
		return cache;
	}
}
