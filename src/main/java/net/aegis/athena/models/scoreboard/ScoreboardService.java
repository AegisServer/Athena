package net.aegis.athena.models.scoreboard;

import net.aegis.athena.framework.persistence.mongodb.MongoPlayerService;
import net.aegis.athena.framework.persistence.mongodb.MongoPlayerServices;
import net.aegis.athena.framework.persistence.mongodb.annotations.ObjectClass;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(ScoreboardUser.class)
public class ScoreboardService extends MongoPlayerService<ScoreboardUser> {
	private final static Map<UUID, ScoreboardUser> cache = new ConcurrentHashMap<>();

	public Map<UUID, ScoreboardUser> getCache() {
		return cache;
	}

	@Override
	protected void beforeDelete(ScoreboardUser user) {
		if (user.getScoreboard() != null)
			user.getScoreboard().delete();
	}

}
