package net.aegis.athena;

import com.google.gson.GsonBuilder;
import net.aegis.athena.utils.Env;
import net.aegis.athena.utils.SerializationUtils.Json.LocalDateGsonSerializer;
import net.aegis.athena.utils.SerializationUtils.Json.LocalDateTimeGsonSerializer;
import net.aegis.athena.utils.SerializationUtils.Json.LocationGsonSerializer;
import org.bukkit.Location;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class API {

	protected static API instance;

	public API() {
		instance = this;
	}

	public static API get() {
		return instance;
	}

	abstract public Env getEnv();

	public ClassLoader getClassLoader() {
		return null;
	}

	public GsonBuilder getPrettyPrinter() {
		return new GsonBuilder().setPrettyPrinting()
				.registerTypeAdapter(Location.class, new LocationGsonSerializer())
				.registerTypeAdapter(LocalDate.class, new LocalDateGsonSerializer())
				.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeGsonSerializer());
	}

	public String getAppName() {
		return getClass().getSimpleName();
	}

	public UUID getAppUuid() {
		return UUID.nameUUIDFromBytes(getAppName().getBytes());
	}

}
