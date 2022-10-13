package net.aegis.athena.framework.commands.models.annotations;

import net.aegis.athena.utils.TimeUtils.TickTime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Deprecated // TODO CYN: setup cooldown service
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cooldown {
	TickTime value();

	double x() default 1;

	boolean global() default false;

	String bypass() default "";

}
