package net.aegis.athena.models.ticket;

import net.aegis.athena.framework.persistence.mongodb.MongoPlayerService;
import net.aegis.athena.framework.persistence.mongodb.MongoPlayerServices;
import net.aegis.athena.framework.persistence.mongodb.annotations.ObjectClass;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(Tickets.class)
public class TicketsService extends MongoPlayerServices<Tickets> {
	private final static Map<UUID, Tickets> cache = new ConcurrentHashMap<>();

	public Map<UUID, Tickets> getCache() {
		return cache;
	}

}
