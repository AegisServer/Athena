package net.aegis.athena.utils;

import net.aegis.athena.API;

import java.util.Arrays;
import java.util.List;

public enum Env {
	DEV,
	TEST,
	PROD;

	public static boolean applies(Env... envs) {
		return applies(Arrays.asList(envs));
	}

	public static boolean applies(List<Env> envs) {
		return envs.contains(API.get().getEnv());
	}
}
