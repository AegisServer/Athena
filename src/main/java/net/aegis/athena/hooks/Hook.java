package net.aegis.athena.hooks;

import lombok.Getter;
import lombok.SneakyThrows;
import net.aegis.athena.hooks.citizens.CitizensHook;
import net.aegis.athena.hooks.citizens.CitizensHookImpl;
import net.aegis.athena.utils.Utils;
import org.bukkit.Bukkit;

import static net.aegis.athena.Athena.singletonOf;


@Getter
public class Hook {
	public static final CitizensHook CITIZENS = hook("Citizens", CitizensHook.class, CitizensHookImpl.class);

	@SneakyThrows
	private static <T extends IHook<?>> T hook(String plugin, Class<? extends IHook<T>> defaultImpl, Class<? extends IHook<T>> workingImpl) {
		final IHook<T> hook;

		if (isEnabled(plugin))
			hook = singletonOf(workingImpl);
		else
			hook = singletonOf(defaultImpl);

		Utils.tryRegisterListener(hook);
		return (T) hook;
	}

	public static boolean isEnabled(String plugin) {
		return Bukkit.getServer().getPluginManager().isPluginEnabled(plugin);
	}

}
