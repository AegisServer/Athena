package net.aegis.athena.models.nerd;

import com.mongodb.DBObject;
import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.PostLoad;
import dev.morphia.annotations.PreLoad;
import dev.morphia.converters.LocalDateConverter;
import dev.morphia.converters.LocalDateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.aegis.athena.framework.exceptions.postconfigured.InvalidInputException;
import net.aegis.athena.framework.interfaces.Colored;
import net.aegis.athena.framework.interfaces.IsColoredAndNicknamed;
import net.aegis.athena.framework.interfaces.PlayerOwnedObject;
import net.aegis.athena.framework.persistence.mongodb.serializers.UUIDConverter;
import net.aegis.athena.utils.Name;
import net.aegis.athena.utils.PlayerUtils;
import net.aegis.athena.utils.UUIDUtils;
import net.aegis.athena.utils.Utils;
import net.aegis.athena.utils.worldgroup.SubWorldGroup;
import net.aegis.athena.utils.worldgroup.WorldGroup;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.aegis.athena.utils.Nullables.isNullOrEmpty;

@Data
@Entity(value = "nerd", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters({UUIDConverter.class, LocalDateConverter.class, LocalDateTimeConverter.class})
public class Nerd implements PlayerOwnedObject, IsColoredAndNicknamed, Colored {

	@Id
	@NonNull
	protected UUID uuid;
	protected String name;
	protected String preferredName;
	protected String prefix;
	protected boolean checkmark;
	protected LocalDate birthday;
	protected LocalDateTime firstJoin;
	protected LocalDateTime lastJoin;
	protected LocalDateTime lastQuit;
	protected LocalDateTime lastUnvanish;
	protected LocalDateTime lastVanish;
	protected LocalDate promotionDate;
	protected String about;

	protected Set<Pronoun> pronouns = new HashSet<>();
	protected List<String> preferredNames = new ArrayList<>();
	protected Set<String> aliases = new HashSet<>();
	protected Set<String> pastNames = new HashSet<>();

	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Location location;

	private Set<WorldGroup> visitedWorldGroups = new HashSet<>();
	private Set<SubWorldGroup> visitedSubWorldGroups = new HashSet<>();

	// Set both to null after they have moved
	private Location loginLocation;
	private Location teleportLocation;

	private Location teleportOnLogin;

	protected static final LocalDateTime EARLIEST_JOIN = LocalDateTime.of(2022, 9, 15, 0, 0);

	@PreLoad
	void preLoad(DBObject dbObject) {
		List<String> visitedWorldGroups = (List<String>) dbObject.get("visitedWorldGroups");
		if (visitedWorldGroups != null && visitedWorldGroups.remove("ONEBLOCK"))
			visitedWorldGroups.add("SKYBLOCK");
		List<String> visitedSubWorldGroups = (List<String>) dbObject.get("visitedSubWorldGroups");
		if (visitedSubWorldGroups != null && visitedSubWorldGroups.remove("LEGACY"))
			visitedSubWorldGroups.add("LEGACY1");

		List<String> pronouns = (List<String>) dbObject.get("pronouns");
		if (!isNullOrEmpty(pronouns)) {
			List<String> fixed = new ArrayList<>() {{
				for (String pronoun : pronouns) {
					final Pronoun of = Pronoun.of(pronoun);
					if (of != null)
						add(of.name());
				}
			}};

			fixed.removeIf(Objects::isNull);
			dbObject.put("pronouns", fixed);
		}

		List<String> aliases = (List<String>) dbObject.get("aliases");
		if (!isNullOrEmpty(aliases))
			dbObject.put("aliases", aliases.stream().map(String::toLowerCase).toList());
	}

	@PostLoad
	void fix() {
		if (!isNullOrEmpty(preferredName)) {
			preferredNames.add(preferredName);
			preferredName = null;
		}
	}

	public static Nerd of(String name) {
		return of(PlayerUtils.getPlayer(name));
	}

	public static Nerd of(Player player) {
		return of(player.getUniqueId());
	}

	public static Nerd of(PlayerOwnedObject object) {
		return of(object.getUniqueId());
	}

	public static Nerd of(OfflinePlayer player) {
		return of(player.getUniqueId());
	}

	public static Nerd of(UUID uuid) {
		return (Nerd) new NerdService().get(uuid);
	}

	public static List<Nerd> of(Collection<UUID> uuids) {
		return uuids.stream().map(Nerd::of).collect(Collectors.toList());
	}

	public void fromPlayer(OfflinePlayer player) {
		uuid = player.getUniqueId();
		name = Name.of(uuid);
		if (player.getFirstPlayed() > 0) {
			LocalDateTime newFirstJoin = Utils.epochMilli(player.getFirstPlayed());
			if (firstJoin == null || firstJoin.isBefore(EARLIEST_JOIN) || newFirstJoin.isBefore(firstJoin))
				firstJoin = newFirstJoin;
		}
	}


	@Override
	public @NotNull String getName() {
		String name = "api-" + getUuid();
		if (UUIDUtils.isUUID0(uuid))
			name = "Console";

		if (name.length() <= 16) // ignore "api-<uuid>" names
			Name.put(uuid, name);

		return name;
	}

	// this is just here for the ToString.Include
	@ToString.Include
	@NotNull
	@Override
	public Rank getRank() {
		return PlayerOwnedObject.super.getRank();
	}

	/**
	 * Returns the user's name formatted with a color formatting code
	 * @deprecated you're probably looking for {@link Nerd#getColoredName()}
	 */
	@ToString.Include
	@Deprecated
	public String getNameFormat() {
		return getRank().getChatColor() + getName();
	}

	/**
	 * Returns the user's nickname with their rank color prefixed. Formerly known as getNicknameFormat.
	 */
	@Override
	public @NotNull String getColoredName() {
		return getChatColor() + getNickname();
	}

	public @NotNull Color getColor() {
		return getRank().colored().getColor();
	}

	public LocalDateTime getLastJoin(Player viewer) {
		if (isOnline()) {
			if (PlayerUtils.canSee(viewer, (Player) this))
				return getLastJoin();

			return getLastUnvanish();
		}

		return getLastJoin();
	}

	public void setLastJoin(LocalDateTime when) {
		lastJoin = when;
		lastUnvanish = when;
	}

	public LocalDateTime getLastQuit(Player viewer) {
		if (isOnline()) {
			if (PlayerUtils.canSee(viewer, (Player) this))
				return getLastQuit();

			return getLastVanish();
		}

		return getLastQuit();
	}

	public void setLastQuit(LocalDateTime when) {
		lastQuit = when;
		lastVanish = when;
	}

	public @NotNull WorldGroup getWorldGroup() {
		return WorldGroup.of(getLocation());
	}

	public World getWorld() {
		if (isOnline())
			return getOnlinePlayer().getWorld();

		return new NBTPlayer(this.getUniqueId()).getWorld();
	}

	public @NotNull Location getLocation() {
		if (isOnline())
			return getOnlinePlayer().getPlayer().getLocation();

		return getOfflineLocation();
	}

	public Location getOfflineLocation() {
		if (true)
			return new NBTPlayer(this.getUniqueId()).getOfflineLocation();

		// TODO 1.19 Remove if nbt is reliable
		if (location != null)
			return location;

		try {
			location = new NBTPlayer(this.getUniqueId()).getOfflineLocation();
			new NerdService().save(this);
			return location;
		} catch (Exception ex) {
			throw new InvalidInputException("Could not get location of offline player " + name + ": " + ex.getMessage());
		}
	}

	public List<ItemStack> getInventory() {
		if (isOnline())
			return Arrays.asList(getOnlinePlayer().getInventory().getContents());

		return new NBTPlayer(this.getUniqueId()).getOfflineInventory();
	}

	public List<ItemStack> getEnderChest() {
		if (isOnline())
			return Arrays.asList(getOnlinePlayer().getEnderChest().getContents());

		return new NBTPlayer(this.getUniqueId()).getOfflineEnderChest();
	}

	public List<ItemStack> getArmor() {
		if (isOnline())
			return Arrays.asList(getOnlinePlayer().getInventory().getArmorContents());

		return new NBTPlayer(this.getUniqueId()).getOfflineArmor();
	}

	public ItemStack getOffHand() {
		if (isOnline())
			return getOnlinePlayer().getInventory().getItemInOffHand();

		return new NBTPlayer(this.getUniqueId()).getOfflineOffHand();
	}

	@Data
	public static class StaffMember implements PlayerOwnedObject {
		@NonNull
		private UUID uuid;
	}

	public enum Pronoun {
		SHE_HER,
		THEY_THEM,
		HE_HIM,
		XE_XEM,
		ANY,
		;

		@Override
		public String toString() {
			return format(name());
		}

		public static String format(String input) {
			if (input == null) return null;
			return input.replaceAll("_", "/").toLowerCase();
		}

		public static Pronoun of(String input) {
			if (input == null) return null;
			for (Pronoun pronoun : values())
				if (pronoun.toString().contains(format(input)))
					return pronoun;
			return null;
		}
	}

	public List<String> getFilteredPreferredNames() {
		return preferredNames.stream().filter(name -> !name.equalsIgnoreCase(getNickname())).toList();
	}

}