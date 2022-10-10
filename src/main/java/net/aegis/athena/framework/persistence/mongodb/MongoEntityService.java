package net.aegis.athena.framework.persistence.mongodb;

import net.aegis.athena.framework.interfaces.EntityOwnedObject;

public abstract class MongoEntityService<T extends EntityOwnedObject> extends MongoBukkitService<T> {

	@Override
	protected String pretty(T object) {
		return object.getUniqueId().toString();
	}

}
