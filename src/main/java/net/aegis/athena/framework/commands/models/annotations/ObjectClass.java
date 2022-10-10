package net.aegis.athena.framework.commands.models.annotations;

import net.aegis.athena.framework.commands.models.cooldown.Cooldown;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectClass {
	Class<Cooldown> value();

}
