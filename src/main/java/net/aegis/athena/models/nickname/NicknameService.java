package net.aegis.athena.models.nickname;

import dev.morphia.query.Query;
import net.aegis.athena.framework.persistence.mongodb.MongoPlayerServices;
import net.aegis.athena.framework.persistence.mongodb.annotations.ObjectClass;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ObjectClass(Nickname.class)
public class NicknameService extends MongoPlayerServices<Nickname> {
	private final static Map<UUID, Nickname> cache = new ConcurrentHashMap<>();

	public Map<UUID, Nickname> getCache() {
		return cache;
	}

	public Nickname getFromNickname(String nickname) {
		Query<Nickname> query = database.createQuery(Nickname.class);
		query.and(query.criteria("nickname").equalIgnoreCase(nickname));
		try (var cursor = query.find()) {
			Nickname data = cursor.tryNext();
			cache(data);
			return data;
		}
	}

	public Nickname getFromQueueId(String queueId) {
		Query<Nickname> query = database.createQuery(Nickname.class);
		query.and(query.criteria("nicknameHistory.nicknameQueueId").equalIgnoreCase(queueId));
		try (var cursor = query.find()) {
			Nickname data = cursor.tryNext();
			cache(data);
			return data;
		}
	}

}
