package net.aegis.athena.utils;

import de.tr7zw.nbtapi.NBTContainer;
import de.tr7zw.nbtapi.NBTItem;
import lombok.AllArgsConstructor;
import net.aegis.athena.framework.exceptions.postconfigured.InvalidInputException;
import net.aegis.athena.framework.exceptions.postconfigured.PlayerNotFoundException;
import net.aegis.athena.models.nerd.Nerd;
import net.aegis.athena.models.nerd.NerdService;
import net.aegis.athena.models.nerd.Rank;
import net.aegis.athena.models.nickname.Nickname;
import net.aegis.athena.models.nickname.NicknameService;
import net.aegis.athena.utils.worldgroup.WorldGroup;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static net.aegis.athena.utils.Distance.distance;
import static net.aegis.athena.utils.Nullables.isNullOrAir;
import static net.aegis.athena.utils.UUIDUtils.isUuid;

public class PlayerUtils {

	public static List<UUID> uuidsOf(Collection<Player> players) {
		return players.stream().map(Player::getUniqueId).toList();
	}

	public static @NotNull OfflinePlayer getPlayer(UUID uuid) {
		return Bukkit.getOfflinePlayer(uuid);
	}

	public static @NotNull OfflinePlayer getPlayer(Player player) {
		return getPlayer(player.getUniqueId());
	}

	public static @NotNull OfflinePlayer getPlayer(Identity identity) {
		return getPlayer(identity.uuid());
	}

	/**
	 * Searches for a player whose username or nickname fully or partially matches the given partial name.
	 * @param partialName UUID or partial text of a username/nickname
	 * @return an offline player
	 * @throws InvalidInputException input was null or empty
	 * @throws PlayerNotFoundException a player matching that (nick)name could not be found
	 */
	public static @NotNull OfflinePlayer getPlayer(String partialName) throws InvalidInputException, PlayerNotFoundException {
		if (partialName == null || partialName.length() == 0)
			throw new InvalidInputException("No player name given");

		String original = partialName;
		partialName = partialName.toLowerCase().trim();

		if (isUuid(partialName))
			return getPlayer(UUID.fromString(partialName));

		final List<Player> players = OnlinePlayers.getAll();

		for (Player player : players)
			if (player.getName().equalsIgnoreCase(partialName))
				return player;
		for (Player player : players)
			if (Nickname.of(player).equalsIgnoreCase((partialName)))
				return player;

		NicknameService nicknameService = new NicknameService();
		Nickname fromNickname = nicknameService.getFromNickname(partialName);
		if (fromNickname != null)
			return fromNickname.getOfflinePlayer();

		for (Player player : players)
			if (player.getName().toLowerCase().startsWith(partialName))
				return player;
		for (Player player : players)
			if (Nickname.of(player).toLowerCase().startsWith((partialName)))
				return player;

		for (Player player : players)
			if (player.getName().toLowerCase().contains((partialName)))
				return player;
		for (Player player : players)
			if (Nickname.of(player).toLowerCase().contains((partialName)))
				return player;

		NerdService nerdService = new NerdService();

		Nerd fromAlias = nerdService.getFromAlias(partialName);
		if (fromAlias != null)
			return fromAlias.getOfflinePlayer();

		List<Nerd> matches = nerdService.find(partialName);
		if (matches.size() > 0) {
			Nerd nerd = matches.get(0);
			if (nerd != null)
				return nerd.getOfflinePlayer();
		}

		throw new PlayerNotFoundException(original);
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
			if (player != null) {
				send(player, message);
			}
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
		private WorldGroup worldGroup;
		private String region;
		private Location origin;
		private Double radius;
		private Boolean afk;
		private Boolean vanished;
		private Predicate<Rank> rank;
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

		public OnlinePlayers viewer(Player player) {
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

		public OnlinePlayers worldGroup(WorldGroup worldGroup) {
			this.worldGroup = worldGroup;
			return this;
		}

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

		public OnlinePlayers rank(Rank rank) {
			return rank(_rank -> _rank == rank);
		}

		public OnlinePlayers rank(Predicate<Rank> rankPredicate) {
			this.rank = rankPredicate;
			return this;
		}

		public OnlinePlayers hasPermission(String permission) {
			this.permission = permission;
			return this;
		}

		public OnlinePlayers includePlayers(List<Player> players) {
			return include(players.stream().map(Player::getUniqueId).toList());
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

		public OnlinePlayers excludePlayers(List<Player> players) {
			return exclude(players.stream().map(Player::getUniqueId).toList());
		}

		public OnlinePlayers exclude(Player player) {
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
			RANK(
					search -> search.rank != null,
					(search, player) -> search.rank.test(Rank.of(player))),
			PERMISSION(
					search -> search.permission != null,
					(search, player) -> player.hasPermission(search.permission)),
			VIEWER(
					search -> search.viewer != null,
					(search, player) -> canSee(Bukkit.getPlayer(search.viewer), player)),
			WORLD(
					search -> search.world != null,
					(search, player) -> player.getWorld().equals(search.world)),
			WORLDGROUP(
					search -> search.worldGroup != null,
					(search, player) -> WorldGroup.of(player) == search.worldGroup),
			REGION(
					search -> search.world != null && search.region != null,
					(search, player) -> new WorldGuardUtils(search.world).isInRegion(player, search.region)),
			RADIUS(
					search -> search.origin != null && search.radius != null,
					(search, player) -> search.origin.getWorld().equals(player.getWorld()) && distance(player.getLocation(), search.origin).lte(search.radius)),
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

	public static void giveItem(Player player, Material material) {
		giveItem(player, material, 1);
	}

	public static void giveItem(Player player, Material material, String nbt) {
		giveItem(player, material, 1, nbt);
	}

	public static void giveItem(Player player, Material material, int amount) {
		giveItem(player, material, amount, null);
	}

	public static void giveItem(Player player, Material material, int amount, String nbt) {
		if (material == Material.AIR)
			throw new InvalidInputException("Cannot spawn air");

		if (amount > 64) {
			for (int i = 0; i < (amount / 64); i++)
				giveItem(player, new ItemStack(material, 64), nbt);
			giveItem(player, new ItemStack(material, amount % 64), nbt);
		} else {
			giveItem(player, new ItemStack(material, amount), nbt);
		}
	}

	public static void giveItem(Player player, ItemStack item) {
		giveItems(player, item);
	}

	public static void giveItems(Player player, ItemStack item) {
		giveItems(player, Collections.singletonList(item));
	}

	public static void giveItem(Player player, ItemStack item, String nbt) {
		giveItems(player, Collections.singletonList(item), nbt);
	}

	public static void giveItems(Player player, Collection<ItemStack> items) {
		giveItems(player, items, null);
	}

	public static void giveItems(Player player, Collection<ItemStack> items, String nbt) {
		List<ItemStack> finalItems = new ArrayList<>(items);
		finalItems.removeIf(Nullables::isNullOrAir);
		finalItems.removeIf(itemStack -> itemStack.getAmount() == 0);
		if (!Nullables.isNullOrEmpty(nbt)) {
			finalItems.clear();
			NBTContainer nbtContainer = new NBTContainer(nbt);
			for (ItemStack item : new ArrayList<>(items)) {
				NBTItem nbtItem = new NBTItem(item);
				nbtItem.mergeCompound(nbtContainer);
				finalItems.add(nbtItem.getItem());
			}
		}
	}

	public static List<ItemStack> giveItemsAndGetExcess(OfflinePlayer player, ItemStack item) {
		return giveItemsAndGetExcess(player, Collections.singletonList(item));
	}

	public static List<ItemStack> giveItemsAndGetExcess(OfflinePlayer player, List<ItemStack> items) {
		if (!player.isOnline() || player.getPlayer() == null)
			return items;

		return giveItemsAndGetExcess(player.getPlayer().getInventory(), items);
	}

	@NotNull
	public static List<ItemStack> giveItemsAndGetExcess(Inventory inventory, List<ItemStack> items) {
		return new ArrayList<>() {{
			for (ItemStack item : fixMaxStackSize(items))
				if (!isNullOrAir(item))
					addAll(inventory.addItem(item.clone()).values());
		}};
	}

	public static List<ItemStack> fixMaxStackSize(List<ItemStack> items) {
		List<ItemStack> fixed = new ArrayList<>();
		for (ItemStack item : items) {
			if (isNullOrAir(item))
				continue;

			final Material material = item.getType();

			while (item.getAmount() > material.getMaxStackSize()) {
				final ItemStack replacement = item.clone();
				final int moving = Math.min(material.getMaxStackSize(), item.getAmount() - material.getMaxStackSize());
				replacement.setAmount(moving);
				item.setAmount(item.getAmount() - moving);

				fixed.add(replacement);
			}
			fixed.add(item);
		}

		return fixed;
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

	public static void runCommandAsOp(CommandSender sender, String commandNoSlash) {
		boolean deop = !sender.isOp();
		sender.setOp(true);
		runCommand(sender, commandNoSlash);
		if (deop)
			sender.setOp(false);
	}

	public static void runCommandAsConsole(String commandNoSlash) {
		runCommand(Bukkit.getConsoleSender(), commandNoSlash);
	}

	/**
	 * Tests if a player can see a vanished player. Returns false if either player is null.
	 *
	 * @param viewer player who is viewing
	 * @param target target player to check
	 * @return true if the target can be seen by the viewer
	 */
	@Contract("null, _ -> false; _, null -> false")
	public static boolean canSee(@Nullable Player viewer, @Nullable Player target) {
		if (viewer == null || target == null)
			return false;
		if (!viewer.canSee(target))
			return false;

		return viewer.hasPermission("pv.see");
	}

	public static List<String> getOnlineUuids() {
		return OnlinePlayers.getAll().stream()
				.map(player -> player.getUniqueId().toString())
				.collect(toList());
	}

	public static boolean isWorldGuardEditing(Player player) {
		return player.hasPermission("worldguard.region.bypass.*");
	}

}
