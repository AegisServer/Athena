package net.aegis.athena.framework.persistence.mongodb;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Log {

	public static boolean debug;

	public static void debug(String message) {
		if (debug)
			System.out.println("[DEBUG] " + message);
	}

	public static void log(String message) {
		System.out.println("[LOG] " + message);
	}

	public static void warn(String message) {
		System.out.println("[WARN] " + message);
	}

	public static void severe(String message) {
		System.out.println("[SEVERE] " + message);
	}

	public String toString() {
		return "Log()";
	}

}
