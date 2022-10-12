package net.aegis.athena.features.scoreboard;

import lombok.Getter;
import lombok.SneakyThrows;
import me.lucko.spark.api.statistic.StatisticWindow.MillisPerTick;
import net.aegis.athena.Athena;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.models.hours.Hours;
import net.aegis.athena.models.hours.HoursService;
import net.aegis.athena.models.nerd.Rank;
import net.aegis.athena.models.scoreboard.ScoreboardUser;
import net.aegis.athena.models.ticket.Tickets;
import net.aegis.athena.models.ticket.TicketsService;
import net.aegis.athena.utils.LocationUtils;
import net.aegis.athena.utils.PlayerUtils.OnlinePlayers;
import net.aegis.athena.utils.StringUtils;
import net.aegis.athena.utils.TimeUtils.Timespan.TimespanBuilder;
import net.aegis.athena.utils.location.HasPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.annotation.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static net.aegis.athena.utils.StringUtils.camelCase;

public enum ScoreboardLine {
	ONLINE {
		@Override
		public String render(Player player) {
			return "&3Online Nerds: &e" + OnlinePlayers.where().viewer(player).get().size();
		}
	},

	@Permission(Group.MODERATOR)
	TICKETS {
		@Override
		public String render(Player player) {
			final TicketsService service = new TicketsService();
			final Tickets tickets = service.get0();
			int open = tickets.getAllOpen().size();
			int all = tickets.getAll().size();
			return "&3Tickets: &" + (open == 0 ? "e" : "c") + open + " &3/ " + all;
		}
	},

	TPS {
		@Override
		public String render(Player player) {
			double tps1m = Bukkit.getTPS()[0];
			return "&3TPS: &" + (tps1m >= 19 ? "a" : tps1m >= 16 ? "6" : "c") + new DecimalFormat("0.00").format(tps1m);
		}
	},

	MSPT {
		@Override
		public String render(Player player) {
			final var mspt = Athena.getSpark().mspt();
			if (mspt == null)
				return "&3MSPT: &cnull";

			final var recent = mspt.poll(MillisPerTick.MINUTES_1);
			Function<Double, String> formatter = value -> {
				if (value < 40)
					return "&a" + Math.round(value);
				else if (value > 50)
					return "&c" + Math.round(value);
				else
					return "&6" + Math.round(value);
			};
			return "&3MSPT: %s&7/%s&7/%s&7/%s".formatted(
					formatter.apply(recent.min()),
					formatter.apply(recent.mean()),
					formatter.apply(recent.percentile95th()),
					formatter.apply(recent.max())
			);
		}
	},

	@Permission(Group.MODERATOR)
	RAM {
		@Override
		public String render(Player player) {
			long total = Runtime.getRuntime().totalMemory();
			long used = total - Runtime.getRuntime().freeMemory();
			double gb = Math.pow(1024, 3);
			return "&3RAM: &e" + new DecimalFormat("0.00").format(used / gb)
					+ "&3/" + new DecimalFormat("#.##").format(total / gb) + "gb";
		}
	},

	PING {
		@Override
		public String render(Player player) {
			Function<Integer, String> formatter = value -> {
				if (value < 200)
					return "&a" + Math.round(value);
				else if (value > 500)
					return "&c" + Math.round(value);
				else
					return "&6" + Math.round(value);
			};
			return "&3Ping: &e" + formatter.apply(player.getPing()) + "ms";
		}
	},

//	CHANNEL {
//		@Override
//		public String render(Player player) {
//			String line = "&3Channel: &e";
//			Chatter chatter = new ChatterService().get(player);
//			if (chatter == null)
//				return line + "&eNone";
//			Channel activeChannel = chatter.getActiveChannel();
//			if (activeChannel == null)
//				return line + "&eNone";
//			if (activeChannel instanceof PrivateChannel)
//				return line + "&b" + String.join(",", ((PrivateChannel) activeChannel).getOthersNames(chatter));
//			if (activeChannel instanceof PublicChannel channel) {
//				return line + channel.getColor() + channel.getName();
//			}
//			return line + "Unknown";
//		}
//	},

	@Permission("essentials.gamemode")
	GAMEMODE {
		@Override
		public String render(Player player) {
			return "&3Mode: &e" + camelCase(player.getGameMode().name());
		}
	},

	WORLD {
		@Override
		public String render(Player player) {
			return "&3World: &e" + StringUtils.getWorldDisplayName(player.getLocation(), player.getWorld());
		}
	},

	BIOME {
		@Override
		public String render(Player player) {
			Location location = player.getLocation();
			return "&3Biome: &e" + camelCase(location.getWorld().getBiome(location.getBlockX(), location.getBlockY(), location.getBlockZ()).name());
		}
	},

	LIGHT_LEVEL {
		@Override
		public String render(Player player) {
			return "&3Light Level: &e" + player.getLocation().getBlock().getLightLevel();
		}
	},

	@Interval(2)
	COMPASS {
		@Override
		public String render(Player player) {
			return StringUtils.compass(player, 8);
		}
	},

	@Interval(2)
	FACING {
		@Override
		public String render(Player player) {
			return "&3Facing: &e" + camelCase(player.getFacing()) + " (" + LocationUtils.getShortFacingDirection((HasPlayer) player) + ")";
		}
	},

	@Interval(3)
	COORDINATES {
		@Override
		public String render(Player player) {
			Location location = player.getLocation();
			return "&e" + (int) location.getX() + " " + (int) location.getY() + " " + (int) location.getZ();
		}
	},

	@Interval(20)
	HOURS {
		@Override
		public String render(Player player) {
			Hours hours = new HoursService().get(player.getUniqueId());
			return "&3Hours: &e" + TimespanBuilder.ofSeconds(hours.getTotal()).noneDisplay(true).format();
		}
	},

	HELP {
		@Override
		public String render(Player player) {
			return "&c/sb help";
		}
	},
	;

	public abstract String render(Player player);

	@SneakyThrows
	public <T extends Annotation> T getAnnotation(Class<? extends Annotation> clazz) {
		return (T) getClass().getField(name()).getAnnotation(clazz);
	}

	public Permission getPermission() {
		return getAnnotation(Permission.class);
	}

	public boolean isOptional() {
		return getAnnotation(Required.class) == null;
	}

	public int getInterval() {
		Interval annotation = getAnnotation(Interval.class);
		return annotation == null ? ScoreboardUser.UPDATE_INTERVAL : annotation.value();
	}

	public boolean hasPermission(Player player) {
		Permission annotation = getPermission();
		return annotation == null || player.hasPermission(annotation.value());
	}

	public static Map<ScoreboardLine, Boolean> getDefaultLines(Player player) {
		final Rank rank = Rank.of(player);
		final boolean isStaff = rank.isStaff();
		return new HashMap<>() {{
			if (ScoreboardLine.ONLINE.hasPermission(player)) put(ScoreboardLine.ONLINE, true);
			if (ScoreboardLine.TICKETS.hasPermission(player)) put(ScoreboardLine.TICKETS, true);
			if (ScoreboardLine.TPS.hasPermission(player)) put(ScoreboardLine.TPS, true);
			if (ScoreboardLine.PING.hasPermission(player)) put(ScoreboardLine.PING, true);
//			if (ScoreboardLine.CHANNEL.hasPermission(player)) put(ScoreboardLine.CHANNEL, true);
//			if (ScoreboardLine.VANISHED.hasPermission(player)) put(ScoreboardLine.VANISHED, true);
			if (ScoreboardLine.GAMEMODE.hasPermission(player)) put(ScoreboardLine.GAMEMODE, true);
			if (ScoreboardLine.WORLD.hasPermission(player)) put(ScoreboardLine.WORLD, true);
			if (ScoreboardLine.BIOME.hasPermission(player)) put(ScoreboardLine.BIOME, false);
			if (ScoreboardLine.LIGHT_LEVEL.hasPermission(player)) put(ScoreboardLine.LIGHT_LEVEL, false);
			if (ScoreboardLine.COMPASS.hasPermission(player)) put(ScoreboardLine.COMPASS, true);
			if (ScoreboardLine.COORDINATES.hasPermission(player)) put(ScoreboardLine.COORDINATES, true);
			if (ScoreboardLine.HOURS.hasPermission(player)) put(ScoreboardLine.HOURS, true);
			if (ScoreboardLine.HELP.hasPermission(player)) put(ScoreboardLine.HELP, !isStaff);
		}};
	}

	@Target({ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	protected @interface Permission {
		String value();
	}

	@Target({ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	protected @interface Required {
	}

	@Target({ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	protected @interface Interval {
		int value();
	}

	@Getter
	private static final List<String> headerFrames = Arrays.asList(
			"&a⚘ &3Project Eden &a⚘",
			"&a⚘ &3Project Eden &a⚘",
			"&a⚘ &3Project Eden &a⚘",
			"&a⚘ &bProject Eden &a⚘",
			"&a⚘ &3Project Eden &a⚘",
			"&a⚘ &bProject Eden &a⚘",
			"&a⚘ &3Project Eden &a⚘",
			"&a⚘ &3Project Eden &a⚘",
			"&a⚘ &3Project Eden &a⚘",

			"&a⚘ &3&bP&3roject Eden &a⚘",
			"&a⚘ &3P&br&3oject Eden &a⚘",
			"&a⚘ &3Pr&bo&3ject Eden &a⚘",
			"&a⚘ &3Pro&bj&3ect Eden &a⚘",
			"&a⚘ &3Proj&be&3ct Eden &a⚘",
			"&a⚘ &3Proje&bc&3t Eden &a⚘",
			"&a⚘ &3Projec&bt&3 Eden &a⚘",
			"&a⚘ &3Project&b &3Eden &a⚘",
			"&a⚘ &3Project &bE&3den &a⚘",
			"&a⚘ &3Project E&bd&3en &a⚘",
			"&a⚘ &3Project Ed&be&3n &a⚘",
			"&a⚘ &3Project Ede&bn&3 &a⚘",
			"&a⚘ &3Project Ed&be&3n &a⚘",
			"&a⚘ &3Project E&bd&3en &a⚘",
			"&a⚘ &3Project &bE&3den &a⚘",
			"&a⚘ &3Project&b &3Eden &a⚘",
			"&a⚘ &3Projec&bt&3 Eden &a⚘",
			"&a⚘ &3Proje&bc&3t Eden &a⚘",
			"&a⚘ &3Proj&be&3ct Eden &a⚘",
			"&a⚘ &3Pro&bj&3ect Eden &a⚘",
			"&a⚘ &3Pr&bo&3ject Eden &a⚘",
			"&a⚘ &3P&br&3oject Eden &a⚘",

			"&a⚘ &3&bP&3roject Eden &a⚘",
			"&a⚘ &3P&br&3oject Eden &a⚘",
			"&a⚘ &3Pr&bo&3ject Eden &a⚘",
			"&a⚘ &3Pro&bj&3ect Eden &a⚘",
			"&a⚘ &3Proj&be&3ct Eden &a⚘",
			"&a⚘ &3Proje&bc&3t Eden &a⚘",
			"&a⚘ &3Projec&bt&3 Eden &a⚘",
			"&a⚘ &3Project&b &3Eden &a⚘",
			"&a⚘ &3Project &bE&3den &a⚘",
			"&a⚘ &3Project E&bd&3en &a⚘",
			"&a⚘ &3Project Ed&be&3n &a⚘",
			"&a⚘ &3Project Ede&bn&3 &a⚘",
			"&a⚘ &3Project Ed&be&3n &a⚘",
			"&a⚘ &3Project E&bd&3en &a⚘",
			"&a⚘ &3Project &bE&3den &a⚘",
			"&a⚘ &3Project&b &3Eden &a⚘",
			"&a⚘ &3Projec&bt&3 Eden &a⚘",
			"&a⚘ &3Proje&bc&3t Eden &a⚘",
			"&a⚘ &3Proj&be&3ct Eden &a⚘",
			"&a⚘ &3Pro&bj&3ect Eden &a⚘",
			"&a⚘ &3Pr&bo&3ject Eden &a⚘",
			"&a⚘ &3P&br&3oject Eden &a⚘",
			"&a⚘ &3&bP&3roject Eden &a⚘"
	);
}
