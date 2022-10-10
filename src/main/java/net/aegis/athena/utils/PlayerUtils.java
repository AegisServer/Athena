package net.aegis.athena.utils;

import lombok.AllArgsConstructor;
import net.aegis.athena.framework.interfaces.HasUniqueId;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class PlayerUtils {

	public static @NotNull OfflinePlayer getPlayer(UUID uuid) {
		return Bukkit.getOfflinePlayer(uuid);
	}

	public static @NotNull OfflinePlayer getPlayer(Identity identity) {
		return getPlayer(identity.uuid());
	}

	public static void send(@Nullable Object recipient, @Nullable Object message, @NotNull Object... objects) {
		if (recipient == null || message == null)
			return;

		if (message instanceof String string && objects.length > 0)
			message = String.format(string, objects);

		if (recipient instanceof CommandSender sender) {
			if (!(message instanceof String || message instanceof ComponentLike))
				message = message.toString();

			if (message instanceof String string)
				sender.sendMessage(new JsonBuilder(string));
			else if (message instanceof ComponentLike componentLike)
				sender.sendMessage(componentLike);
		}

		else if (recipient instanceof OfflinePlayer offlinePlayer) {
			Player player = offlinePlayer.getPlayer();
			if (player != null)
				send(player, message);
		}

		else if (recipient instanceof UUID uuid)
			send(getPlayer(uuid), message);

		else if (recipient instanceof Identity identity)
			send(getPlayer(identity), message);

		else if (recipient instanceof Identified identified)
			send(getPlayer(identified.identity()), message);
	}

	public static class OnlinePlayers {
		private UUID viewer;
		private World world;
//		private WorldGroup worldGroup;
		private String region;
		private Location origin;
		private Double radius;
		private Boolean afk;
		private Boolean vanished;
//		private Predicate<Rank> rank;
		private String permission;
		private List<UUID> include;
		private List<UUID> exclude;
		private List<Predicate<Player>> filters = new ArrayList<>();

		public static OnlinePlayers where() {
			return new OnlinePlayers();
		}

		public static List<Player> getAll() {
			return where().get();
		}

		public OnlinePlayers viewer(HasUniqueId player) {
			this.viewer = player.getUniqueId();
			return this;
		}

		public OnlinePlayers world(String world) {
			return world(Objects.requireNonNull(Bukkit.getWorld(world)));
		}

		public OnlinePlayers world(World world) {
			this.world = world;
			return this;
		}

//		public OnlinePlayers worldGroup(WorldGroup worldGroup) {
//			this.worldGroup = worldGroup;
//			return this;
//		}

		public OnlinePlayers region(String region) {
			this.region = region;
			return this;
		}

		public OnlinePlayers radius(double radius) {
			this.radius = radius;
			return this;
		}

		public OnlinePlayers radius(Location origin, double radius) {
			this.origin = origin;
			this.radius = radius;
			return this;
		}

		public OnlinePlayers afk(boolean afk) {
			this.afk = afk;
			return this;
		}

		public OnlinePlayers vanished(boolean vanished) {
			this.vanished = vanished;
			return this;
		}

//		public OnlinePlayers rank(Rank rank) {
//			return rank(_rank -> _rank == rank);
//		}
//
//		public OnlinePlayers rank(Predicate<Rank> rankPredicate) {
//			this.rank = rankPredicate;
//			return this;
//		}

		public OnlinePlayers hasPermission(String permission) {
			this.permission = permission;
			return this;
		}

		public OnlinePlayers includePlayers(List<HasUniqueId> players) {
			return include(players.stream().map(HasUniqueId::getUniqueId).toList());
		}

		public OnlinePlayers include(List<UUID> uuids) {
			if (this.include == null)
				this.include = new ArrayList<>();
			if (uuids == null)
				uuids = new ArrayList<>();

			this.include.addAll(uuids);
			return this;
		}

		public OnlinePlayers excludeSelf() {
			return exclude(viewer);
		}

		public OnlinePlayers excludePlayers(List<HasUniqueId> players) {
			return exclude(players.stream().map(HasUniqueId::getUniqueId).toList());
		}

		public OnlinePlayers exclude(HasUniqueId player) {
			return exclude(List.of(player.getUniqueId()));
		}

		public OnlinePlayers exclude(UUID uuid) {
			return exclude(List.of(uuid));
		}

		public OnlinePlayers exclude(List<UUID> uuids) {
			if (this.exclude == null)
				this.exclude = new ArrayList<>();
			this.exclude.addAll(uuids);
			return this;
		}

		public OnlinePlayers filter(Predicate<Player> filter) {
			this.filters.add(filter);
			return this;
		}

		public List<Player> get() {
			final Supplier<List<UUID>> online = () -> Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(toList());
			final List<UUID> uuids = include == null ? online.get() : include;

			if (uuids.isEmpty())
				return Collections.emptyList();

			Stream<Player> stream = uuids.stream()
					.filter(uuid -> exclude == null || !exclude.contains(uuid))
					.map(Bukkit::getOfflinePlayer)
					.filter(OfflinePlayer::isOnline)
					.map(OfflinePlayer::getPlayer)
					.filter(player -> !CitizensUtils.isNPC(player));

			if (origin == null && this.viewer != null) {
				final Player viewer = Bukkit.getPlayer(this.viewer);
				if (viewer != null)
					origin = viewer.getLocation();
			}

			for (Filter filter : Filter.values())
				stream = filter.filter(this, stream);

			for (Predicate<Player> filter : filters)
				stream = stream.filter(filter);

			return stream.toList();
		}

		public void forEach(Consumer<Player> consumer) {
			get().forEach(consumer);
		}

		@AllArgsConstructor
		private enum Filter {
//			AFK(
//					search -> search.afk != null,
//					(search, player) -> new AFKUserService().get(player).isAfk() == search.afk),
//			VANISHED(
//					search -> search.vanished != null,
//					(search, player) -> Nerd.of(player).isVanished() == search.vanished),
//			RANK(
//					search -> search.rank != null,
//					(search, player) -> search.rank.test(Rank.of(player))),
			PERMISSION(
					search -> search.permission != null,
					(search, player) -> player.hasPermission(search.permission)),
//			VIEWER(
//					search -> search.viewer != null,
//					(search, player) -> canSee(Bukkit.getPlayer(search.viewer), player)),
			WORLD(
					search -> search.world != null,
					(search, player) -> player.getWorld().equals(search.world)),
//			WORLDGROUP(
//					search -> search.worldGroup != null,
//					(search, player) -> WorldGroup.of(player) == search.worldGroup),
			REGION(
					search -> search.world != null && search.region != null,
					(search, player) -> new WorldGuardUtils(search.world).isInRegion(player, search.region)),
//			RADIUS(
//					search -> search.origin != null && search.radius != null,
//					(search, player) -> search.origin.getWorld().equals(player.getWorld()) && distance(player, search.origin).lte(search.radius)),
			;

			private final Predicate<OnlinePlayers> canFilter;
			private final BiPredicate<OnlinePlayers, Player> predicate;

			private Stream<Player> filter(OnlinePlayers search, Stream<Player> stream) {
				if (!canFilter.test(search))
					return stream;

				return stream.filter(player -> predicate.test(search, player));
			}
		}
	}

	public static void runCommand(CommandSender sender, String commandNoSlash) {
		if (sender == null)
			return;

//		if (sender instanceof Player)
//			Utils.callEvent(new PlayerCommandPreprocessEvent((Player) sender, "/" + command));

		Runnable command = () -> Bukkit.dispatchCommand(sender, commandNoSlash);

		if (Bukkit.isPrimaryThread())
			command.run();
		else
			Tasks.sync(command);
	}

	public static void runCommandAsConsole(String commandNoSlash) {
		runCommand(Bukkit.getConsoleSender(), commandNoSlash);
	}

}
