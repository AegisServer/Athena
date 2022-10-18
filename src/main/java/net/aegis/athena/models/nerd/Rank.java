package net.aegis.athena.models.nerd;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import net.aegis.athena.framework.interfaces.Colored;
import net.aegis.athena.framework.interfaces.IsColoredAndNamed;
import net.aegis.athena.framework.interfaces.PlayerOwnedObject;
import net.aegis.athena.utils.CompletableFutures;
import net.aegis.athena.utils.EnumUtils;
import net.aegis.athena.utils.LuckPermsUtils;
import net.aegis.athena.utils.PlayerUtils.OnlinePlayers;
import net.aegis.athena.utils.StringUtils;
import net.aegis.athena.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@AllArgsConstructor
public enum Rank implements IsColoredAndNamed {
	DEFAULT(ChatColor.of("#aaaaaa"), ChatColor.GRAY),
	BUILDER(ChatColor.of("#02883e"), ChatColor.DARK_GREEN),
	MODERATOR(ChatColor.of("#4cc9f0"), ChatColor.AQUA),
	ADMIN(ChatColor.of("#3080ff"), ChatColor.BLUE),
	DEVELOPER(ChatColor.of("#07a8a8"), ChatColor.DARK_AQUA),
	OWNER(ChatColor.of("#915bf5"), ChatColor.DARK_PURPLE),
	;

	@Getter
	@NotNull
	private final ChatColor chatColor;
	@Getter
	private final ChatColor similarChatColor;

	public static boolean exists(String key) {
		try {
			Rank.valueOf(key.toUpperCase());
			return true;
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	public boolean hasPrefix() {
		return isStaff();
	}

	public boolean isStaff() {
		return gte(Rank.BUILDER);
	}

	public boolean isBuilder() {
		return gte(BUILDER);
	}

	public boolean isMod() {
		return gte(Rank.MODERATOR);
	}

	public boolean isSeniorStaff() {
		return gte(Rank.ADMIN);
	}

	public boolean isAdmin() {
		return gte(Rank.ADMIN);
	}

	@Override
	public @NotNull Colored colored() {
		return Colored.of(chatColor);
	}

	public String getPrefix() {
		if (hasPrefix())
			return getColoredName();

		return "";
	}

	public @NotNull String getName() {
		return StringUtils.camelCase(name());
	}

	@SneakyThrows
	public CompletableFuture<java.util.List<Nerd>> getNerds() {
		return LuckPermsUtils.getUsersInGroup(this).thenApply(Nerd::of);
	}

	public java.util.List<Nerd> getOnlineNerds() {
		return OnlinePlayers.getAll().stream()
				.filter(player -> Rank.of(player) == this)
				.map(Nerd::of)
				.sorted(Comparator.comparing(Nerd::getNickname))
				.collect(Collectors.toList());
	}

	public static final java.util.List<Rank> STAFF_RANKS = Arrays.stream(Rank.values())
			.filter(Rank::isStaff)
			.sorted(Comparator.reverseOrder())
			.collect(Collectors.toList());

	@NotNull
	public static CompletableFuture<Map<Rank, java.util.List<Nerd>>> getStaffNerds() {
		return CompletableFutures.allOf(new LinkedHashMap<Rank, CompletableFuture<java.util.List<Nerd>>>() {{
			STAFF_RANKS.forEach(rank -> put(rank, rank.getNerds()));
		}});
	}

	public static java.util.List<Nerd> getOnlineStaff() {
		return OnlinePlayers.getAll().stream()
				.filter(player -> Rank.of(player).isStaff())
				.map(Nerd::of)
				.sorted(Comparator.comparing(Nerd::getNickname))
				.collect(Collectors.toList());
	}

	public static java.util.List<Nerd> getOnlineMods() {
		return OnlinePlayers.getAll().stream()
				.filter(player -> Rank.of(player).isMod())
				.map(Nerd::of)
				.sorted(Comparator.comparing(Nerd::getNickname))
				.collect(Collectors.toList());
	}

	public static final List<Rank> REVERSED = Utils.reverse(Arrays.asList(Rank.values()));

	public static final LoadingCache<UUID, Rank> CACHE = CacheBuilder.newBuilder()
			.expireAfterWrite(10, TimeUnit.SECONDS)
			.build(CacheLoader.from(uuid -> {
				for (Rank rank : REVERSED)
					if (LuckPermsUtils.hasGroup(uuid, rank.name().toLowerCase()))
						return rank;

				return DEFAULT;
			}));

	public static Rank of(Player player) {
		return of(player.getUniqueId());
	}

	public static Rank of(PlayerOwnedObject object) {
		return of(object.getUniqueId());
	}

	public static Rank of(OfflinePlayer player) {
		return of(player.getUniqueId());
	}

	public static Rank of(UUID uuid) {
		try {
			return CACHE.get(uuid);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return DEFAULT;
		}
	}

	public static Rank getByString(String input) {
		try {
			return Rank.valueOf(input.toUpperCase());
		} catch (IllegalArgumentException missing) {
			switch (input.toLowerCase()) {
				case "administrator":
					return Rank.ADMIN;
				case "dev":
					return Rank.DEVELOPER;
				case "mod":
					return Rank.MODERATOR;
			}
		}
		return null;
	}

	public enum RankGroup {
		ADMINS,
		SENIOR_STAFF,
		STAFF,
		BUILDERS,
		PLAYERS
	}

	public boolean gt(Rank rank) {
		return ordinal() > rank.ordinal();
	}

	public boolean gte(Rank rank) {
		return ordinal() >= rank.ordinal();
	}

	public boolean lt(Rank rank) {
		return ordinal() < rank.ordinal();
	}

	public boolean lte(Rank rank) {
		return ordinal() <= rank.ordinal();
	}

	public boolean between(Rank lower, Rank upper) {
		if (lower.ordinal() == upper.ordinal())
			return ordinal() == lower.ordinal();
		if (lower.ordinal() > upper.ordinal()) {
			Rank temp = lower;
			lower = upper;
			upper = temp;
		}

		return gte(lower) && lte(upper);
	}

	public Rank next() {
		return EnumUtils.next(Rank.class, this.ordinal());
	}

	public Rank previous() {
		return EnumUtils.previous(Rank.class, this.ordinal());
	}

	public Rank getPromotion() {
		Rank next = next();
		if (next == this)
			return next;

		return next.getPromotion();
	}

	public int enabledOrdinal() {
		return Arrays.stream(Rank.values()).toList().indexOf(this);
	}
}
