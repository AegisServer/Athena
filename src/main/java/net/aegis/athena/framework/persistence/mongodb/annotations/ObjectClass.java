package net.aegis.athena.framework.persistence.mongodb.annotations;

import net.aegis.athena.framework.interfaces.DatabaseObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectClass {
	Class<? extends DatabaseObject> value();

}
