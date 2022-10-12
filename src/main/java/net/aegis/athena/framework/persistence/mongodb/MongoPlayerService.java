package net.aegis.athena.framework.persistence.mongodb;

import net.aegis.athena.framework.interfaces.PlayerOwnedObject;
import net.aegis.athena.utils.PlayerUtils.OnlinePlayers;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class MongoPlayerService<T extends PlayerOwnedObject> extends MongoBukkitService<T> {

	@Override
	protected String pretty(T object) {
		return object.getNickname();
	}

	public List<T> getOnline() {
		List<T> online = new ArrayList<>();
		for (Player player : OnlinePlayers.getAll())
			online.add(get((HasUniqueId) player));
		return online;
	}

	public void saveOnline() {
		for (T user : getOnline())
			save(user);
	}

	public void saveOnlineSync() {
		for (T user : getOnline())
			saveSync(user);
	}

}
