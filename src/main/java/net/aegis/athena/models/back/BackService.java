package net.aegis.athena.models.back;

import net.aegis.athena.framework.persistence.mongodb.MongoPlayerService;
import net.aegis.athena.framework.persistence.mongodb.annotations.ObjectClass;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.aegis.athena.utils.Nullables.isNullOrEmpty;

@ObjectClass(Back.class)
public class BackService extends MongoPlayerService<Back> {
	private final static Map<UUID, Back> cache = new ConcurrentHashMap<>();

	public Map<UUID, Back> getCache() {
		return cache;
	}

	@Override
	protected boolean deleteIf(Back back) {
		return isNullOrEmpty(back.getLocations());
	}

}
