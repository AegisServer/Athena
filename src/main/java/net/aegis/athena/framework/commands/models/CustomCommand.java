package net.aegis.athena.framework.commands.models;

import com.google.common.base.Strings;
import lombok.*;
import net.aegis.athena.framework.commands.models.annotations.*;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.framework.commands.models.events.CommandRunEvent;
import net.aegis.athena.framework.exceptions.postconfigured.InvalidInputException;
import net.aegis.athena.framework.exceptions.postconfigured.PlayerNotFoundException;
import net.aegis.athena.framework.exceptions.postconfigured.PlayerNotOnlineException;
import net.aegis.athena.framework.exceptions.preconfigured.MustBeCommandBlockException;
import net.aegis.athena.framework.exceptions.preconfigured.MustBeConsoleException;
import net.aegis.athena.framework.exceptions.preconfigured.MustBeIngameException;
import net.aegis.athena.framework.exceptions.preconfigured.NoPermissionException;
import net.aegis.athena.framework.interfaces.HasUniqueId;
import net.aegis.athena.framework.interfaces.PlayerOwnedObject;
import net.aegis.athena.framework.persistence.mongodb.MongoPlayerService;
import net.aegis.athena.framework.persistence.mongodb.models.nerd.Nerd;
import net.aegis.athena.framework.persistence.mongodb.models.nerd.NerdService;
import net.aegis.athena.framework.persistence.mongodb.models.nickname.Nickname;
import net.aegis.athena.utils.*;
import net.aegis.athena.utils.PlayerUtils.OnlinePlayers;
import net.aegis.athena.utils.SerializationUtils.Json;
import net.aegis.athena.utils.TimeUtils.Timespan;
import net.aegis.athena.utils.location.HasLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static net.aegis.athena.utils.Distance.distance;
import static net.aegis.athena.utils.Nullables.isNullOrAir;
import static net.aegis.athena.utils.Nullables.isNullOrEmpty;
import static net.aegis.athena.utils.StringUtils.an;
import static net.aegis.athena.utils.StringUtils.trimFirst;
import static net.aegis.athena.utils.TimeUtils.parseDate;
import static net.aegis.athena.utils.TimeUtils.parseDateTime;

@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@SuppressWarnings({"SameParameterValue", "unused", "WeakerAccess", "UnusedReturnValue"})
public abstract class CustomCommand extends ICustomCommand {
	@NonNull
	@Getter
	protected CommandEvent event;
	public String PREFIX = StringUtils.getPrefix(getName());
	public String DISCORD_PREFIX = StringUtils.getDiscordPrefix(getName());

	public String getPrefix() {
		return PREFIX;
	}

	public String getAliasUsed() {
		return event.getAliasUsed();
	}

	public void _shutdown() {}

	protected boolean isCommandEvent() {
		return event instanceof CommandRunEvent;
	}

	protected boolean isPlayerCommandEvent() {
		return event instanceof CommandRunEvent && isPlayer();
	}

	protected String camelCase(Enum<?> _enum) {
		if (_enum == null) return "null";
		return camelCase(_enum.name());
	}

	protected String camelCase(String string) {
		return StringUtils.camelCase(string);
	}

	protected String plural(String label, Number number) {
		return StringUtils.plural(label, number);
	}

	protected String plural(String labelSingle, String labelPlural, Number number) {
		return StringUtils.plural(labelSingle, labelPlural, number);
	}

	protected ItemStack getTool() {
		return ItemUtils.getTool(player());
	}

	protected ItemStack getToolRequired() {
		return ItemUtils.getToolRequired(player());
	}

	protected ItemStack getToolRequired(Material material) {
		final ItemStack tool = getToolRequired();
		if (tool.getType() != material)
			error("You must be holding a " + camelCase(material));
		return tool;
	}

	protected EquipmentSlot getHandWithTool() {
		return ItemUtils.getHandWithTool(player());
	}

	protected EquipmentSlot getHandWithToolRequired() {
		return ItemUtils.getHandWithToolRequired(player());
	}

	protected Block getTargetBlock() {
		return player().getTargetBlockExact(500);
	}

	protected Block getTargetBlockRequired() {
		Block targetBlock = getTargetBlock();
		if (isNullOrAir(targetBlock))
			error("You must be looking at a block");
		return targetBlock;
	}

	protected Sign getTargetSignRequired() {
		Block targetBlock = getTargetBlock();
		Material material = targetBlock.getType();
		if (isNullOrAir(material) || !MaterialTag.SIGNS.isTagged(material))
			error("You must be looking at a sign");
		return (Sign) targetBlock.getState();
	}

	// Ignores entities in creative or spectator mode
	protected Entity getTargetEntity() {
		return player().getTargetEntity(120);
	}

	protected Entity getTargetEntityRequired() {
		Entity targetEntity = getTargetEntity();
		if (targetEntity == null)
			error("You must be looking at an entity");
		return targetEntity;
	}

	protected Entity getTargetEntityRequired(EntityType entityType) {
		Entity targetEntity = getTargetEntity();
		if (targetEntity == null || targetEntity.getType() != entityType)
			error("You must be looking at " + an(camelCase(entityType)));
		return targetEntity;
	}

	protected LivingEntity getTargetLivingEntityRequired() {
		Entity targetEntity = getTargetEntity();
		if (!(targetEntity instanceof LivingEntity))
			error("You must be looking at a living entity");
		return (LivingEntity) targetEntity;
	}

	protected Player getTargetPlayerRequired() {
		Entity targetEntity = getTargetEntity();
		if (!(targetEntity instanceof Player))
			error("You must be looking at a player");
		return (Player) targetEntity;
	}

	protected Location location() {
		if (isCommandBlock())
			return commandBlock().getBlock().getLocation();
		return player().getLocation();
	}

	protected Block block() {
		return location().getBlock();
	}

	protected World world() {
		return location().getWorld();
	}

	protected PlayerInventory inventory() {
		return player().getInventory();
	}

	protected boolean isSameWorld(HasLocation location) {
		return world().equals(location.getLocation().getWorld());
	}

	protected boolean isSameWorld(World world) {
		return world().equals(world);
	}

	protected boolean isDifferentWorld(HasLocation location) {
		return !isSameWorld(location.getLocation().getWorld());
	}

	protected boolean isDifferentWorld(World world) {
		return !isSameWorld(world);
	}

	protected WorldGuardUtils worldguard() {
		return new WorldGuardUtils(world());
	}

//	public void giveItem(ItemStack item) {
//		PlayerUtils.giveItems(player(), Collections.singletonList(item));
//	}

//	public void giveItems(ItemStack item, int amount) {
//		giveItems(Collections.nCopies(amount, item));
//	}

//	public void giveItems(Collection<ItemStack> items) {
//		PlayerUtils.giveItems(player(), items, null);
//	}

	protected void send(CommandSender sender, String message, Object... objects) {
		send(sender, json(String.format(message, objects)));
	}

	protected void send(CommandSender sender, BaseComponent... baseComponents) {
		sender.sendMessage(baseComponents);
	}

	protected void send(String uuid, String message) {
		send(UUID.fromString(uuid), message);
	}

	protected void send(Object object) {
		if (object instanceof String)
			send(sender(), object);
		else if (object instanceof JsonBuilder)
			send(sender(), object);
		else if (object instanceof Component)
			send(sender(), object);
		else
			throw new InvalidInputException("Cannot send object: " + object.getClass().getSimpleName());
	}

	protected void send() {
		send(sender(), "");
	}

	protected void send(String message) {
		send(sender(), json(message));
	}

	protected void send(ComponentLike component) {
		send(sender(), component);
	}

	protected void send(Object sender, Object message) {
		PlayerUtils.send(sender, message);
	}

	protected void line() {
		line(1);
	}

	protected void line(CommandSender player) {
		line(player, 1);
	}

	protected void line(int count) {
		line(sender(), count);
	}

	protected void line(CommandSender player, int count) {
		for (int i = 0; i < count; i++)
			send(player, "");
	}

	protected JsonBuilder json() {
		return json("");
	}

	protected JsonBuilder json(String message) {
		return new JsonBuilder(message);
	}

	protected String toPrettyString(Object object) {
		return StringUtils.toPrettyString(object);
	}

	@Contract("_ -> fail")
	public void error(String error) throws InvalidInputException {
		throw new InvalidInputException(error);
	}

	@Contract("_ -> fail")
	public void error(JsonBuilder error) throws InvalidInputException {
		throw new InvalidInputException(error);
	}

	@Contract("_ -> fail")
	public void error(ComponentLike error) throws InvalidInputException {
		throw new InvalidInputException(error);
	}

	@Deprecated
	public void error(Player player, String error) {
		player.sendMessage(new JsonBuilder(NamedTextColor.RED).next(error));
	}

	public void rethrow(Throwable ex) {
		throw new RuntimeException(ex);
	}

	public void permissionError() {
		throw new NoPermissionException();
	}

	public boolean hasPermission(String permission) {
		if (isCommandBlock() || isConsole())
			return true;
		else
			return player().hasPermission(permission);
	}

	public void showUsage() {
		error(((CommandRunEvent) event).getUsageMessage());
	}

	protected CommandSender sender() {
		return event.getSender();
	}

	protected Player player() {
		if (!isPlayer())
			throw new MustBeIngameException();

		return (Player) event.getSender();
	}

	protected PlayerOwnedObject playerOwnedObject() {
		return this::uuid;
	}

	protected Nerd nerd() {
		return Nerd.of((HasUniqueId) player());
	}

	protected @NotNull UUID uuid() {
		if (isPlayer())
			return player().getUniqueId();
		return UUIDUtils.UUID0;
	}

	protected String name() {
		if (!isPlayer())
			return camelCase(sender().getName());
		else
			return nerd().getName();
	}

	protected String nickname() {
		if (!isPlayer())
			return sender().getName();
		else
			return Nickname.of((HasUniqueId) player());
	}

	protected String nickname(OfflinePlayer player) {
		return Nickname.of((HasUniqueId) player);
	}

	protected String nickname(PlayerOwnedObject player) {
		return Nickname.of(player);
	}

	protected ConsoleCommandSender console() {
		if (!isConsole())
			throw new MustBeConsoleException();

		return (ConsoleCommandSender) event.getSender();
	}

	protected BlockCommandSender commandBlock() {
		if (!isCommandBlock())
			throw new MustBeCommandBlockException();

		return (BlockCommandSender) event.getSender();
	}

	protected boolean isPlayer() {
		return isPlayer(sender());
	}

	private boolean isPlayer(Object object) {
		return object instanceof Player;
	}

	protected boolean isOfflinePlayer() {
		return isOfflinePlayer(sender());
	}

	private boolean isOfflinePlayer(Object object) {
		return object instanceof OfflinePlayer;
	}

	protected boolean isConsole() {
		return isConsole(sender());
	}

	private boolean isConsole(Object object) {
		return object instanceof ConsoleCommandSender;
	}

	protected boolean isCommandBlock() {
		return isCommandBlock(sender());
	}

	private boolean isCommandBlock(Object object) {
		return object instanceof BlockCommandSender;
	}

	protected boolean isSelf(PlayerOwnedObject object) {
		return isPlayer() && isSelf(PlayerUtils.getPlayer(object.getUuid()));
	}

	protected boolean isSelf(OfflinePlayer player) {
		return isPlayer() && isSelf(player(), player);
	}

	protected boolean isSelf(Player player) {
		return isPlayer() && isSelf(player(), player);
	}

	protected boolean isSelf(OfflinePlayer self, OfflinePlayer player) {
		return isPlayer() && self.getUniqueId().equals(player.getUniqueId());
	}

	protected void runCommand(String commandNoSlash) {
		runCommand(sender(), commandNoSlash);
	}

	protected void runCommand(CommandSender sender, String commandNoSlash) {
		PlayerUtils.runCommand(sender, commandNoSlash);
	}

	protected void runCommandAsOp(String commandNoSlash) {
		runCommandAsOp(sender(), commandNoSlash);
	}

	protected void runCommandAsOp(CommandSender sender, String commandNoSlash) {
		PlayerUtils.runCommandAsOp(sender, commandNoSlash);
	}

	protected void runCommandAsConsole(String commandNoSlash) {
		PlayerUtils.runCommandAsConsole(commandNoSlash);
	}

	protected void checkPermission(String permission) {
		if (isPlayer())
			if (!sender().hasPermission(permission))
				throw new NoPermissionException();
	}

	protected String argsString() {
		return argsString(args());
	}

	protected String argsString(List<String> args) {
		if (args() == null || args().size() == 0) return "";
		return String.join(" ", args());
	}

	protected List<String> args() {
		return event.getArgs();
	}

	protected void setArgs(List<String> args) {
		event.setArgs(args);
	}

	protected String arg(int i) {
		return arg(i, false);
	}

	protected String arg(int i, boolean rest) {
		if (event.getArgs().size() < i) return null;
		if (rest)
			return String.join(" ", event.getArgs().subList(i - 1, event.getArgs().size()));

		String result = event.getArgs().get(i - 1);
		if (isNullOrEmpty(result)) return null;
		return result;
	}

	protected boolean isIntArg(int i) {
		if (event.getArgs().size() < i) return false;
		return isInt(arg(i));
	}

	protected boolean isInt(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	protected int asInt(String input) {
		try {
			return Integer.parseInt(input);
		} catch (NumberFormatException ex) {
			throw new InvalidInputException("Argument &e" + input + " &cis not a valid integer");
		}
	}

	protected Integer intArg(int i) {
		if (event.getArgs().size() < i) return null;
		try {
			return Integer.parseInt(arg(i));
		} catch (NumberFormatException ex) {
			throw new InvalidInputException("Argument &e#" + i + " &cis not a valid integer");
		}
	}

	protected boolean isDoubleArg(int i) {
		if (event.getArgs().size() < i) return false;
		return isDouble(arg(i));
	}

	protected boolean isDouble(String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	protected double asDouble(String input) {
		try {
			return Double.parseDouble(input);
		} catch (NumberFormatException ex) {
			throw new InvalidInputException("Argument &e" + input + " &cis not a valid double");
		}
	}

	protected Double doubleArg(int i) {
		if (event.getArgs().size() < i) return null;
		try {
			return Double.parseDouble(arg(i));
		} catch (NumberFormatException ex) {
			throw new InvalidInputException("Argument &e#" + i + " &cis not a valid double");
		}
	}

	protected boolean isFloatArg(int i) {
		if (event.getArgs().size() < i) return false;
		return isFloat(arg(i));
	}

	protected boolean isFloat(String input) {
		try {
			Float.parseFloat(input);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	protected float asFloat(String input) {
		try {
			return Float.parseFloat(input);
		} catch (NumberFormatException ex) {
			throw new InvalidInputException("Argument &e" + input + " &cis not a valid float");
		}
	}

	protected Float floatArg(int i) {
		if (event.getArgs().size() < i) return null;
		try {
			return Float.parseFloat(arg(i));
		} catch (NumberFormatException ex) {
			throw new InvalidInputException("Argument &e#" + i + " &cis not a valid float");
		}
	}

	protected Boolean booleanArg(int i) {
		if (event.getArgs().size() < i) return null;
		String value = arg(i);
		if (Arrays.asList("enable", "on", "yes", "1").contains(value.toLowerCase())) value = "true";
		return Boolean.parseBoolean(value);
	}

	protected void fallback() {
		Fallback fallback = getClass().getAnnotation(Fallback.class);
		if (fallback != null)
			PlayerUtils.runCommand(sender(), fallback.value() + ":" + event.getAliasUsed() + " " + event.getArgsString());
		else
			throw new InvalidInputException("Nothing to fallback to");
	}

	@SneakyThrows
	protected PlayerOwnedObject convertToPlayerOwnedObject(String value, Class<? extends PlayerOwnedObject> type) {
		Class<? extends MongoPlayerService> service = (Class<? extends MongoPlayerService>) MongoPlayerService.ofObject(type);
		if (service != null) {
			final OfflinePlayer player = convertToOfflinePlayer(value);
			if (player != null)
				return (PlayerOwnedObject) service.newInstance().get((HasUniqueId) player);
		}
		return null;
	}

	@ConverterFor(OfflinePlayer.class)
	public OfflinePlayer convertToOfflinePlayer(String value) {
		if (value == null) return null;
		if ("null".equalsIgnoreCase(value)) return null;
		if ("self".equalsIgnoreCase(value)) value = uuid().toString();
		return PlayerUtils.getPlayer(UUID.fromString(value.replaceFirst("[pP]:", "")));
	}

	public Player convertToPlayer(OfflinePlayer offlinePlayer) {
		if (!offlinePlayer.isOnline() || offlinePlayer.getPlayer() == null)
			throw new PlayerNotOnlineException((HasUniqueId) offlinePlayer);
		if (isPlayer() && !PlayerUtils.canSee(player(), offlinePlayer.getPlayer()))
			throw new PlayerNotOnlineException((HasUniqueId) offlinePlayer);

		return offlinePlayer.getPlayer();
	}

	@ConverterFor(Location.class)
	Location convertToLocation(String value) {
		if ("current".equalsIgnoreCase(value))
			return location();
		if ("target".equalsIgnoreCase(value))
			return getTargetBlockRequired().getLocation();

		return Json.deserializeLocation(value);
	}

	@ConverterFor(World.class)
	World convertToWorld(String value) {
		if ("current".equalsIgnoreCase(value))
			return world();
		if ("null".equalsIgnoreCase(value))
			return null;
		World world = Bukkit.getWorld(value);
		if (world == null)
			throw new InvalidInputException("World from " + value + " not found");
		return world;
	}

	@TabCompleterFor(World.class)
	List<String> tabCompleteWorld(String filter) {
		return Bukkit.getWorlds().stream()
				.map(World::getName)
				.filter(name -> name.toLowerCase().startsWith(filter.toLowerCase()))
				.collect(toList());
	}

	@TabCompleterFor(Boolean.class)
	List<String> tabCompleteBoolean(String filter) {
		return Stream.of("true", "false")
				.filter(bool -> bool.toLowerCase().startsWith(filter.toLowerCase()))
				.collect(toList());
	}

	@ConverterFor(Material.class)
	Material convertToMaterial(String value) {
		if (isInt(value))
			return Material.values()[Integer.parseInt(value)];

		Material material = Material.matchMaterial(value);
		if (material == null)
			material = Material.matchMaterial("WHITE_" + value);

		if (material == null)
			material = Material.matchMaterial("OAK_" + value);

		if (material == null)
			throw new InvalidInputException("Material from " + value + " not found");

		if (material == Material.COMMAND_BLOCK_MINECART)
			material = Material.MINECART;

		return material;
	}

	@TabCompleterFor(Material.class)
	List<String> tabCompleteMaterial(String value) {
		List<String> results = tabCompleteEnum(value, Material.class);
		results.remove(Material.COMMAND_BLOCK_MINECART.name().toLowerCase());
		return results;
	}

	@ConverterFor(ChatColor.class)
	ChatColor convertToChatColor(String value) {
		if (StringUtils.getHexPattern().matcher(value).matches())
			return ChatColor.of(value.replaceFirst("&", ""));

		try {
			return ChatColor.of(value.toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new InvalidInputException("Color &e" + value + "&c not found");
		}
	}

	@TabCompleterFor(ChatColor.class)
	List<String> tabCompleteChatColor(String filter) {
		return Arrays.stream(ChatColor.values())
				.filter(color -> color.getColor() != null)
				.map(ChatColor::getName)
				.filter(name -> name.startsWith(filter.toLowerCase()))
				.collect(toList());
	}

	@ConverterFor(ColorType.class)
	ColorType convertToColorType(String value) {
		ColorType color = ColorType.of(value.replace('_', ' ').toLowerCase());
		if (color == null)
			throw new InvalidInputException("Color &e" + value + "&c not found");
		return color;
	}

	@TabCompleterFor(ColorType.class)
	List<String> tabCompleteColorType(String filter) {
		return Arrays.stream(ColorType.values())
				.map(ColorType::getName)
				.filter(name -> name.replace(' ', '_').startsWith(filter.toLowerCase()))
				.collect(toList());
	}

	@ConverterFor(LocalDate.class)
	public LocalDate convertToLocalDate(String value) {
		if (value.startsWith("+"))
			return Timespan.of(value.replaceFirst("\\+", "")).fromNow().toLocalDate();
		if (value.startsWith("-"))
			return Timespan.of(value.replaceFirst("-", "")).sinceNow().toLocalDate();

		return parseDate(value);
	}

	@ConverterFor(LocalDateTime.class)
	public LocalDateTime convertToLocalDateTime(String value) {
		if (value.startsWith("+"))
			return Timespan.of(value.replaceFirst("\\+", "")).fromNow();
		if (value.startsWith("-"))
			return Timespan.of(value.replaceFirst("-", "")).sinceNow();

		return parseDateTime(value);
	}

	@ConverterFor(Timespan.class)
	Timespan convertToTimespan(String input) {
		return Timespan.of(input);
	}

	@ConverterFor(Enchantment.class)
	Enchantment convertToEnchantment(String value) {
		Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(value));
		if (enchantment == null) {
			enchantment = Enchantment.getByName(value);
			if (enchantment == null)
				throw new InvalidInputException("Enchantment from &e" + value + "&c not found");
		}
		return enchantment;
	}

	@TabCompleterFor(Enchantment.class)
	List<String> applicableEnchantmentsTabCompleter(String filter) {
		List<String> results = new ArrayList<>();
		try {
			List<String> enchants = ItemUtils.getApplicableEnchantments(getToolRequired()).stream()
					.map(enchantment -> enchantment.getKey().getKey())
					.filter(enchantment -> enchantment.toLowerCase().startsWith(filter.toLowerCase()))
					.collect(toList());
			if (enchants.size() == 0)
				return getAllEnchants(filter);
			else
				return enchants;
		} catch (InvalidInputException ignore) {
			return getAllEnchants(filter);
		}
	}

	public List<String> getAllEnchants(String filter) {
		return Arrays.stream(Enchantment.values())
				.filter(enchantment -> enchantment.getKey().getKey().toLowerCase().startsWith(filter.toLowerCase()))
				.map(enchantment -> enchantment.getKey().getKey())
				.collect(toList());
	}

	@TabCompleterFor(LivingEntity.class)
	protected List<String> tabCompleteLivingEntity(String value) {
		return new ArrayList<>() {{
			for (EntityType entityType : EntityType.values()) {
				Class<? extends Entity> entityClass = entityType.getEntityClass();
				if (entityClass != null && LivingEntity.class.isAssignableFrom(entityClass))
					if (entityType.name().toLowerCase().startsWith(value.toLowerCase()))
						add(entityType.name().toLowerCase());
			}
		}};
	}

	protected <T> void paginate(Collection<T> values, BiFunction<T, String, JsonBuilder> formatter, String command, int page) {
		paginate(values, formatter, command, page, 10);
	}

	protected <T> void paginate(Collection<T> values, BiFunction<T, String, JsonBuilder> formatter, String command, int page, int amount) {
		if (page < 1)
			error("Page number must be 1 or greater");

		int start = (page - 1) * amount;
		if (values.size() < start)
			error("No results on page " + page);

		int end = Math.min(values.size(), start + amount);

		line();
		AtomicInteger index = new AtomicInteger(start);
		new LinkedList<>(values).subList(start, end).forEach(t -> send(formatter.apply(t, getLeadingZeroIndex(index))));
		line();

		boolean first = page == 1;
		boolean last = end == values.size();

		if (first && last)
			return;

		JsonBuilder buttons = json();
		if (first)
			buttons.next("&7 « Previous  &3");
		else
			buttons.next("&e « Previous  &3").command(command + " " + (page - 1));

		buttons.group().next("&3|&3|").group();

		if (last)
			buttons.next("  &7Next »");
		else
			buttons.next("  &eNext »").command(command + " " + (page + 1));

		send(buttons.group());
	}

	@NotNull
	private String getLeadingZeroIndex(AtomicInteger index) {
		int nextIndex = index.incrementAndGet();
		int nextMagnitude = Integer.parseInt("1" + Strings.repeat("0", String.valueOf(nextIndex).length()));
		int needsLeading0 = nextMagnitude - 10;

		String string = "&3" + nextIndex;
		if (nextIndex > 1 && nextIndex == nextMagnitude / 10)
			return string;

		if (nextIndex > needsLeading0)
			string = "&30" + string;
		return string;
	}

	@Path("help")
	protected void help() {
		List<String> aliases = getAllAliases();
		if (aliases.size() > 1)
			send(PREFIX + "Aliases: " + String.join("&e, &3", aliases));

		List<JsonBuilder> lines = new ArrayList<>();
		final List<Method> methods = getPathMethodsForDisplay(event)
				.stream()
				.filter(method -> {
					Path path = method.getAnnotation(Path.class);
					HideFromHelp hide = method.getAnnotation(HideFromHelp.class);

					if (hide != null)
						return false;

					if ("help".equals(path.value()) || "?".equals(path.value()))
						return false;

					return true;
				}).toList();

		methods.forEach(method -> {
			Path path = method.getAnnotation(Path.class);
			Description desc = method.getAnnotation(Description.class);

			if (methods.size() == 1 && desc == null)
				desc = this.getClass().getAnnotation(Description.class);

			String usage = "/" + getAliasUsed().toLowerCase() + " " + (isNullOrEmpty(path.value()) ? "" : path.value());
			String description = (desc == null ? "" : " &7- " + desc.value());
			StringBuilder suggestion = new StringBuilder();
			for (String word : usage.split(" ")) {
				if (word.startsWith("[") || word.startsWith("<"))
					break;
				if (word.startsWith("("))
					suggestion.append(trimFirst(word.split("\\|")[0]));
				else
					suggestion.append(word).append(" ");
			}

			lines.add(json("&c" + usage + description).suggest(suggestion.toString()));
		});

		if (lines.size() == 0)
			error("No usage available");

		send(PREFIX + "Usage:");
		lines.forEach(this::send);
	}

	@AllArgsConstructor
	public class WhoFormatter {
		private final OfflinePlayer self, target;
		private final WhoType whoType;

		public String format() {
			boolean self = isSelf(this.self, target);
			String name = Objects.requireNonNullElse(Nickname.of((HasUniqueId) target), "Unknown");

			if (whoType == WhoType.POSSESSIVE_UPPER || whoType == WhoType.POSSESSIVE_LOWER)
				return self ? (whoType == WhoType.POSSESSIVE_UPPER ? "Y" : "y") + "our" : name + "'" + (name.endsWith("s") ? "" : "s");
			else if (whoType == WhoType.ACTIONARY_UPPER || whoType == WhoType.ACTIONARY_LOWER)
				return self ? (whoType == WhoType.ACTIONARY_UPPER ? "Y" : "y") + "ou do" : name + " does";

			throw new InvalidInputException("Unknown format action");
		}
	}

	public String formatWho(PlayerOwnedObject target, WhoType whoType) {
		return formatWho(player(), target, whoType);
	}

	public String formatWho(OfflinePlayer target, WhoType whoType) {
		return formatWho(player(), target, whoType);
	}

	public String formatWho(Player self, PlayerOwnedObject target, WhoType whoType) {
		return formatWho(PlayerUtils.getPlayer(target.getUuid()), whoType);
	}

	public String formatWho(Player self, OfflinePlayer target, WhoType whoType) {
		return new WhoFormatter(self, target, whoType).format();
	}

	public enum WhoType {
		POSSESSIVE_UPPER,
		POSSESSIVE_LOWER,
		ACTIONARY_UPPER,
		ACTIONARY_LOWER
	}

	public static Pattern getSwitchPattern(Parameter parameter) {
		Switch annotation = parameter.getDeclaredAnnotation(Switch.class);
		String regex = "(?i)^(--" + parameter.getName();
		char shorthand = annotation.shorthand();
		if (shorthand != '-')
			regex += "|-" + shorthand;

		return Pattern.compile(regex + ")(=[^\\s]+)?$");
	}

}