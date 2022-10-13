package net.aegis.athena.framework.persistence.mongodb;

import lombok.Builder;
import lombok.Data;
import net.aegis.athena.utils.Env;

/*
	TODO CYN: refactor your database connection to use this instead
 */
@Data
@Builder
public class DatabaseConfig {
	@Builder.Default
	private String host = "localhost";
	@Builder.Default
	private int port = 27017;
	@Builder.Default
	private String username = "root";
	@Builder.Default
	private String password = "password";
	private String prefix;
	private String modelPath;

	public static class DatabaseConfigBuilder {

		public DatabaseConfigBuilder env(final Env env) {
			this.prefix = env == Env.PROD ? null : env.name().toLowerCase();
			return this;
		}

	}
}
