package net.aegis.athena.utils;

import lombok.Getter;
import net.aegis.athena.Athena;

public class Timer {
	private static final int IGNORE = 1000;

	@Getter
	private final long duration;

	public Timer(String id, Runnable runnable) {
		long startTime = System.currentTimeMillis();

		runnable.run();

		duration = System.currentTimeMillis() - startTime;
		if (Athena.isDebug() || duration > IGNORE)
			Athena.log("[Timer] " + id + " took " + duration + "ms");
	}

}
