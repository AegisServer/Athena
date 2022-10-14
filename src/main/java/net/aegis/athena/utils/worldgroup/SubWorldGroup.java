package net.aegis.athena.utils.worldgroup;

import lombok.Getter;
import net.aegis.athena.utils.LuckPermsUtils;
import net.aegis.athena.utils.StringUtils;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum SubWorldGroup implements IWorldGroup {
	SURVIVAL("survival", "survival_nether", "survival_the_end"),
	WORLD("world", "world_nether", "world_the_end"),
	UNKNOWN;

	@Getter
	private final @NotNull List<String> worldNames;

	SubWorldGroup() {
		this(new String[0]);
	}

	SubWorldGroup(String... worldNames) {
		this.worldNames = Arrays.asList(worldNames);
	}

	@Override
	public String toString() {
		return StringUtils.camelCase(name());
	}

	public static SubWorldGroup of(@Nullable Entity entity) {
		return entity == null ? UNKNOWN : of(entity.getWorld());
	}

	public static SubWorldGroup of(@Nullable Location location) {
		return location == null ? UNKNOWN : of(location.getWorld());
	}

	public static SubWorldGroup of(@Nullable World world) {
		return world == null ? UNKNOWN : of(world.getName());
	}

	private static final Map<String, SubWorldGroup> CACHE = new ConcurrentHashMap<>();

	public static SubWorldGroup of(String world) {
		return CACHE.computeIfAbsent(world, $ -> rawOf(world));
	}

	private static SubWorldGroup rawOf(String world) {
		for (SubWorldGroup group : values())
			if (group.contains(world))
				return group;

		return UNKNOWN;
	}

	static {
		LuckPermsUtils.registerContext(new SubWorldGroupCalculator());
	}

	public static class SubWorldGroupCalculator implements ContextCalculator<Player> {

		@Override
		public void calculate(@NotNull Player target, ContextConsumer contextConsumer) {
			contextConsumer.accept("subworldgroup", SubWorldGroup.of(target).name());
		}

		@Override
		public ContextSet estimatePotentialContexts() {
			ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
			for (SubWorldGroup worldGroup : SubWorldGroup.values())
				builder.add("subworldgroup", worldGroup.name().toLowerCase());
			return builder.build();
		}

	}

}
