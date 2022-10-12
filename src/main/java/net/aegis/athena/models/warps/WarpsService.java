package net.aegis.athena.models.warps;

import net.aegis.athena.framework.persistence.mongodb.MongoPlayerService;
import net.aegis.athena.framework.persistence.mongodb.annotations.ObjectClass;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(Warps.class)
public class WarpsService extends MongoPlayerService<Warps> {
	private final static Map<UUID, Warps> cache = new ConcurrentHashMap<>();

	public Map<UUID, Warps> getCache() {
		return cache;
	}

}
