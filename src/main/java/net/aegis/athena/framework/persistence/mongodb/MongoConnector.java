package net.aegis.athena.framework.persistence.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.annotations.Entity;
import dev.morphia.converters.TypeConverter;
import dev.morphia.mapping.Mapper;
import dev.morphia.mapping.MapperOptions;
import dev.morphia.mapping.MapperOptions.Builder;
import lombok.Getter;
import net.aegis.athena.API;
import net.aegis.athena.utils.Nullables;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static net.aegis.athena.utils.ReflectionUtils.typesAnnotatedWith;

public class MongoConnector {
	protected static final Morphia morphia = new Morphia();
	@Getter
	private static Datastore datastore;

	public static Datastore connect() {
		if (datastore != null)
			return datastore;

		API.getOptional().ifPresent(api -> {
			// Properly merge deleted hashmaps and null vars
			Builder options = MapperOptions.builder().storeEmpties(true).storeNulls(true);
			if (api.getClassLoader() != null)
				options.classLoader(api.getClassLoader());

			morphia.getMapper().setOptions(options.build());

			DatabaseConfig config = api.getDatabaseConfig();
			// Load classes into memory once
			if (!Nullables.isNullOrEmpty(config.getModelPath()))
				typesAnnotatedWith(Entity.class, config.getModelPath());

			ConnectionString connectionString = new ConnectionString(config.getLink());
			MongoClientSettings settings = MongoClientSettings.builder()
					.applyConnectionString(connectionString)
					.build();
			MongoClient mongoClient = (MongoClient) MongoClients.create(settings);

			String database = (config.getPrefix() == null ? "" : config.getPrefix() + "_") + "aegisserver";
			datastore = morphia.createDatastore(mongoClient, database);
			datastore.ensureIndexes();

			List<Class<? extends TypeConverter>> classes = new ArrayList<>();
			classes.addAll(api.getDefaultMongoConverters());
			classes.addAll(api.getMongoConverters());

			for (Class<? extends TypeConverter> clazz : classes) {
				try {
					Constructor<? extends TypeConverter> constructor = clazz.getDeclaredConstructor(Mapper.class);
					TypeConverter instance = constructor.newInstance(morphia.getMapper());
					morphia.getMapper().getConverters().addConverter(instance);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		return datastore;
	}

	public static void shutdown() {
		try {
			if (datastore != null) {
				datastore.getMongo().close();
				datastore = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
