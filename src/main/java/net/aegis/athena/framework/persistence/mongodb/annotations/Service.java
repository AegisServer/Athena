package net.aegis.athena.framework.persistence.mongodb.annotations;

import net.aegis.athena.framework.interfaces.DatabaseObject;
import net.aegis.athena.framework.persistence.mongodb.MongoService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
	Class<? extends MongoService<? extends DatabaseObject>> value();

}
