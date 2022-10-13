package net.aegis.athena.utils;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.aegis.athena.API;
import net.aegis.athena.framework.exceptions.AthenaException;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.Color;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static net.aegis.athena.utils.Nullables.isNullOrEmpty;

public class StringUtils {
	@Getter
	private static final String colorChar = "§";
	@Getter
	private static final String altColorChar = "&";
	@Getter
	private static final String colorCharsRegex = "[" + colorChar + altColorChar + "]";
	@Getter
	private static final Pattern colorPattern = Pattern.compile(colorCharsRegex + "[\\da-fA-F]");
	@Getter
	private static final Pattern formatPattern = Pattern.compile(colorCharsRegex + "[k-orK-OR]");
	@Getter
	private static final Pattern hexPattern = Pattern.compile(colorCharsRegex + "#[a-fA-F\\d]{6}");
	@Getter
	private static final Pattern hexColorizedPattern = Pattern.compile(colorCharsRegex + "x(" + colorCharsRegex + "[a-fA-F\\d]){6}");
	@Getter
	private static final Pattern colorGroupPattern = Pattern.compile("(" + colorPattern + "|(" + hexPattern + "|" + hexColorizedPattern + "))((" + formatPattern + ")+)?");

	public static final String CHECK = "&a✔";
	public static final String X = "&c✗";
	public static final String COMMA_SPLIT_REGEX = ",(?=[^}]*(?:\\{|$))";
	public static final String NAMESPACED_KEY_REGEX = "[a-z0-9._-]+";

	public static String getPrefix(Class<?> clazz) {
		return getPrefix(clazz.getSimpleName());
	}

	public static String getPrefix(String prefix) {
		return colorize("&8&l[&e" + prefix + "&8&l]&3 ");
	}

	public static String colorize(String input) {
		if (input == null)
			return null;

		while (true) {
			Matcher matcher = hexPattern.matcher(input);
			if (!matcher.find()) break;

			String color = matcher.group();
			input = input.replace(color, ChatColor.of(color.replaceFirst(colorCharsRegex, "")).toString());
		}

		return ChatColor.translateAlternateColorCodes(altColorChar.charAt(0), input);
	}

	// Replaces § with & and unformats hex, does NOT strip colors
	public static String decolorize(String input) {
		if (input == null)
			return null;

		input = colorize(input);

		while (true) {
			Matcher matcher = hexColorizedPattern.matcher(input);
			if (!matcher.find()) break;

			String color = matcher.group();
			input = input.replace(color, color.replace(colorChar + "x", "&#").replaceAll(colorChar, ""));
		}

		return input.replaceAll(colorChar, altColorChar);
	}

	public static String toHex(ChatColor color) {
		return "#" + Integer.toHexString(color.getColor().getRGB()).substring(2);
	}

	public static String toHex(org.bukkit.Color color) {
		String hex = Integer.toHexString(color.asRGB());

		if (hex.length() == 4) // wtf?
			hex = "00" + hex;

		return "#" + hex;
	}

	public static String stripColor(String input) {
		return ChatColor.stripColor(colorize(input));
	}

	public static String stripFormat(String input) {
		return formatPattern.matcher(colorize(input)).replaceAll("");
	}

	private static final int APPROX_LORE_LINE_LENGTH = 40;

	public static List<String> loreize(String string) {
		return loreize(string, APPROX_LORE_LINE_LENGTH);
	}

	public static List<String> loreize(String string, int length) {
		return new ArrayList<>() {{
			final String[] split = string.split(" ");
			StringBuilder line = new StringBuilder();
			for (String word : split) {
				final int oldLength = stripColor(line.toString()).length();
				final int newLength = oldLength + stripColor(word).length();

				boolean append = Math.abs(length - oldLength) >= Math.abs(length - newLength);
				if (!append) {
					String newline = line.toString().trim();
					add(line.toString().trim());
					line = new StringBuilder(getLastColor(newline));
				}

				line.append(word).append(" ");
			}

			add(line.toString().trim());
		}};
	}

	public static String getLastColor(String text) {
		Matcher matcher = colorGroupPattern.matcher(text);
		String last = "";
		while (matcher.find())
			last = matcher.group();
		return last.toLowerCase();
	}

	public static String applyFormattingToAll(String input, boolean bold, boolean strikethrough, boolean underline, boolean italic, boolean magic) {
		if (input == null)
			return null;

		if (bold) input = applyFormattingToAll(input, ChatColor.BOLD);
		if (strikethrough) input = applyFormattingToAll(input, ChatColor.STRIKETHROUGH);
		if (underline) input = applyFormattingToAll(input, ChatColor.UNDERLINE);
		if (italic) input = applyFormattingToAll(input, ChatColor.ITALIC);
		if (magic) input = applyFormattingToAll(input, ChatColor.MAGIC);
		return input;
	}

	public static String applyFormattingToAll(String input, ChatColor formatting) {
		return StringUtils.getColorGroupPattern().matcher(input).replaceAll(result -> result.group() + formatting);
	}

	public static String camelToSnake(String input) {
		return input.replaceAll("(?<!^)([A-Z])", "_$1").toLowerCase();
	}

//	public static String formatPotionData(ItemStack item) {
//		if (!(item.getItemMeta() instanceof PotionMeta))
//			return null;
//
//		return PotionWrapper.of(item).getEffects().stream().map(StringUtils::formatPotionData).collect(joining(", "));
//	}

	public static String formatPotionData(PotionEffect effect) {
		return formatPotionData(effect.getType().getKey().getKey(), effect.getAmplifier(), effect.getDuration());
	}

//	public static String formatPotionData(MobEffectInstance effect) {
//		return formatPotionData(StringUtils.listLast(effect.getDescriptionId(), "."), effect.getAmplifier(), effect.getDuration());
//	}

	public static String formatPotionData(String name, int amplifier, int duration) {
		amplifier = amplifier + 1;
		duration = duration / 20;

		final String amplifierString = amplifier == 1 ? "" : " " + amplifier;
		final String durationString = (duration / 60) + ":" + new DecimalFormat("00").format((duration % 60));

		return camelCase(name) + amplifierString + " (" + durationString + ")";
	}

	public static String pretty(ItemStack item) {
		return pretty(item, 1);
	}

	/**
	 * Get a human-readable description of an item
	 * @param item
	 * @param amount
	 * @return <amount> <description of item>
	 */
	public static String pretty(ItemStack item, int amount) {
//		if (hasModelId(item) && item.getItemMeta().hasDisplayName())
//			return item.getAmount() * amount + " " + item.getItemMeta().getDisplayName();

		String name = camelCase(item.getType().name());

		final Map<Enchantment, Integer> enchants = new HashMap<>();
		if (item.getItemMeta() instanceof EnchantmentStorageMeta meta)
			enchants.putAll(meta.getStoredEnchants());
		else if (item.getItemMeta().hasEnchants())
			enchants.putAll(item.getEnchantments());

		if (!enchants.isEmpty())
			name = enchants.keySet().stream().map(enchantment -> {
				int level = enchants.get(enchantment);
				final String key = camelCase(enchantment.getKey().getKey());
				if (level > 1)
					return key + " " + level;
				else
					return key;
			}).collect(joining(", ")) + " " + name;

//		if (item.getItemMeta() instanceof PotionMeta)
//			name = formatPotionData(item) + " " + name;

		return item.getAmount() * amount + " " + name;
	}

	@NotNull
	public static String pretty(FuzzyItemStack item) {
		return pretty(item, ChatColor.RED, ChatColor.YELLOW);
	}

	@NotNull
	public static String pretty(FuzzyItemStack item, ChatColor color, ChatColor delimiterColor) {
		final String delimiter = " %sor %s".formatted(delimiterColor, color);
		final String materials = item.getMaterials().stream().map(StringUtils::camelCase).collect(joining(delimiter));
		return materials + (item.getAmount() > 1 ? " %sx %s%d".formatted(delimiterColor, color, item.getAmount()) : "");
	}

	public static String bool(boolean b) {
		if (b)
			return "&atrue";
		else
			return "&cfalse";
	}

	public enum ProgressBarStyle {
		NONE,
		COUNT,
		PERCENT
	}

	@NotNull
	public static String an(@NotNull String text) {
		return "a" + (text.matches("(?i)^[AEIOU].*") ? "n" : "") + " " + text;
	}

	public static String progressBar(int progress, int goal) {
		return progressBar(progress, goal, ProgressBarStyle.NONE, 25);
	}

	public static String progressBar(int progress, int goal, ProgressBarStyle style) {
		return progressBar(progress, goal, style, 25);
	}

	public static String progressBar(int progress, int goal, ProgressBarStyle style, int length) {
		double percent = Math.min((double) progress / goal, 1);
		ChatColor color = ChatColor.RED;
		if (percent == 1)
			color = ChatColor.GREEN;
		else if (percent >= 2/3)
			color = ChatColor.YELLOW;
		else if (percent >= 1/3)
			color = ChatColor.GOLD;

		int n = (int) Math.floor(percent * length);

		String bar = String.join("", Collections.nCopies(length, "|"));
		String first = left(bar, n);
		String last = right(bar, length - n);
		String result = color + first + "&8" + last;

		// TODO: Style
		if (style == ProgressBarStyle.COUNT)
			result += " &f" + progress + "/" + goal;
		if (style == ProgressBarStyle.PERCENT)
			result += " &f" + Math.floor(percent * 100) + "%";

		return result;
	}

	private static final String[] compassParts = {"[S]","SW","[W]","NW","[N]","NE","[E]","SE"};

	public static String compass(Player player) {
		return compass(player, 8);
	}

	public static String compass(Player player, int extra) {
		return compass(player, extra, 4);
	}

	public static String compass(Player player, int extra, int separators) {
		String compass = "";
		for (String compassPart : compassParts)
			compass += compassPart + " " + String.join("", Collections.nCopies(separators, "-")) + " ";

		float yaw = Location.normalizeYaw(player.getPlayer().getLocation().getYaw());
		if (yaw < 0) yaw = 360 + yaw;

		int center = (int) Math.round(yaw / (360D / compass.length())) + 1;

		String instance;
		if (center - extra < 0) {
			center += compass.length();
			instance = (compass + compass).substring(center - extra, center + extra + 1);
		} else if (center + extra + 1 > compass.length())
			instance = (compass + compass).substring(center - extra, center + extra + 1);
		else
			instance = compass.substring(center - extra, center + extra + 1);

		instance = instance.replaceAll("\\[", "&2[&f");
		instance = instance.replaceAll("]", "&2]&f");
		return colorize(instance);
	}

	public static String compassTarget(Player hasPlayer, int extra, int separators, Location target) {
		Player player = hasPlayer.getPlayer();
		Vector direction = player.getPlayer().getEyeLocation().toVector().subtract(target.clone().add(0.5, 0.5, 0.5).toVector()).normalize();
		double heading = 180 - Math.toDegrees(Math.atan2(direction.getX(), direction.getZ()));
		char arrow = '▲';

		String compass = "";
		for (String compassPart : compassParts)
			compass += compassPart + " " + String.join("", Collections.nCopies(separators, "-")) + " ";

		float yaw = Location.normalizeYaw(player.getLocation().getYaw());
		if (yaw < 0) yaw = 360 + yaw;

		int center = (int) Math.round(yaw / (360D / compass.length())) + 1;
		int head = (int) Math.round(heading / (360D / compass.length())) + 1;

		StringBuilder compassBuilder = new StringBuilder(compass);
		try {
			compassBuilder.setCharAt(head, arrow);
		} catch (Exception ignored) {
		}
		compass = compassBuilder.toString();

		String instance;
		if (center - extra < 0) {
			center += compass.length();
			instance = (compass + compass).substring(center - extra, center + extra + 1);
		} else if (center + extra + 1 > compass.length()) {
			instance = (compass + compass).substring(center - extra, center + extra + 1);
		} else {
			instance = compass.substring(center - extra, center + extra + 1);
		}

		instance = instance.replaceAll("▲", "&d▲&f");
		instance = instance.replaceAll("\\[", "&2[&f");
		instance = instance.replaceAll("]", "&2]&f");
		return colorize(instance);
	}

	public static String getWorldDisplayName(Location location, String world) {
		if (world.contains("oneblock"))
			world = world.replace("oneblock_world", "one_block");
		else if (world.contains("bskyblock"))
			world = world.replace("bskyblock_world", "skyblock");
		else if ("bearfair21".equals(world))
			return "Bear Fair 21";
		else if ("uhc".equals(world))
			return "UHC";
		else if ("server".equals(world)) {
			if (location != null)
				if (new WorldGuardUtils(location).getRegionNamesAt(location).contains("hub"))
					return "Hub";
		}
		return camelCase(world);
	}

	public static String getWorldDisplayName(Location location, World world) {
		return getWorldDisplayName(location, world.getName());
	}

	public static String getWorldDisplayName(World world) {
		return getWorldDisplayName(null, world.getName());
	}

	public static String getWorldDisplayName(String world) {
		return getWorldDisplayName(null, world);
	}

	public static String getLocationString(Location loc) {
		return "&3World: &e" + loc.getWorld().getName() + " &3x: &e" + df.format(loc.getX()) + " &3y: &e" +
				df.format(loc.getY()) + " &3z: &e" +  df.format(loc.getZ());
	}

	public static String getShortLocationString(Location loc) {
		return (int) loc.getX() + " " + (int) loc.getY() + " " +  (int) loc.getZ() + " " + loc.getWorld().getName();
	}

	public static String getShortishLocationString(Location loc) {
		String coords = (int) loc.getX() + " " + (int) loc.getY() + " " + (int) loc.getZ();
		if (loc.getYaw() != 0 || loc.getPitch() != 0)
			coords += " " + df.format(loc.getYaw()) + " " + df.format(loc.getPitch());
		return coords + " " + loc.getWorld().getName();
	}

	public static String getCoordinateString(Location loc) {
		return (int) loc.getX() + " " + (int) loc.getY() + " " +  (int) loc.getZ();
	}

	public static String getFlooredCoordinateString(Location loc) {
		return (int) Math.floor(loc.getX()) + " " + (int) Math.floor(loc.getY()) + " " + (int) Math.floor(loc.getZ());
	}

//	public static String getFlooredCoordinateString(BlockPos loc) {
//		return (int) Math.floor(loc.getX()) + " " + (int) Math.floor(loc.getY()) + " " + (int) Math.floor(loc.getZ());
//	}


	/**
	 * Sends a message to the player with the input message as a teleportation link.
	 *
	 * @param message  message to display
	 * @param location location to set as a teleport destination on click
	 * @param player   any player handled by {@link PlayerUtils#send(Object, Object, Object...)}
	 */
	public static void sendJsonLocation(String message, Location location, Object player) {
		getJsonLocation(message, location).send(player);
	}

	public static JsonBuilder getJsonLocation(String message, Location location) {
		return new JsonBuilder().next(message).command(getTeleportCommand(location));
	}

	public static String getTeleportCommand(Location location) {
		return "/tppos " + df.format(location.getX()) + " " + df.format(location.getY()) + " " + df.format(location.getZ()) + " " +
			df.format(location.getYaw()) + " " + df.format(location.getPitch()) + " " + location.getWorld().getName();
	}

	public static String getTimeFormat(Duration duration) {
		StringBuilder sb = new StringBuilder();
		if (duration.toHoursPart() > 0)
			sb.append(duration.toHoursPart()).append("h ");

		if (duration.toMinutesPart() > 0)
			sb.append(duration.toMinutesPart()).append("m ");

		sb.append(String.format("%.2fs", duration.toSecondsPart() + (duration.toNanosPart() / 1000000000.0)));

		return sb.toString();
	}

	@RequiredArgsConstructor
	public static class Gradient {
		@NonNull
		private final List<ChatColor> colors;

		public static Gradient of(List<ChatColor> colors) {
			return new Gradient(colors);
		}

		public static Gradient ofTypes(List<ColorType> colorTypes) {
			List<ChatColor> colors = new ArrayList<>();
			for (ColorType colorType : colorTypes) {
				colors.add(colorType.getChatColor());
			}

			return new Gradient(colors);
		}

		public String apply(String text) {
			int count = colors.size();
			if (count == 0)
				return text;
			if (count == 1)
				return colors.get(0) + text;

			int groups = count - 1;
			int groupLength = text.length() / groups;
			List<String> parts = new ArrayList<>();
			for (int i = 0; i < groups; i++) {
				int start = i * groupLength;
				int end = start + groupLength;
				if (i == groups - 1)
					end = text.length();
				parts.add(text.substring(start, end));
			}

			Iterator<ChatColor> iterator = colors.iterator();
			ChatColor color1;
			ChatColor color2 = iterator.next();

			StringBuilder builder = new StringBuilder();

			for (int i = 0; i < groups; i++) {
				color1 = color2;
				color2 = iterator.next();

				String part = parts.get(i);

				int l = part.length();
				for (int j = 0; j < l; j++) {
					builder.append(ChatColor.of(new Color(
							(color1.getColor().getRed() + (j * (1F / l) * (color2.getColor().getRed() - color1.getColor().getRed()))) / 255,
							(color1.getColor().getGreen() + (j * (1F / l) * (color2.getColor().getGreen() - color1.getColor().getGreen()))) / 255,
							(color1.getColor().getBlue() + (j * (1F / l) * (color2.getColor().getBlue() - color1.getColor().getBlue()))) / 255
					)));
					builder.append(part.charAt(j));
				}
			}

			return builder.toString();
		}
	}

	public static class Rainbow {
		public static String apply(String text) {
			StringBuilder builder = new StringBuilder();
			int l = text.length();
			for (int i = 0; i < l; i++) {
				builder.append(ChatColor.of(Color.getHSBColor(((float) i / l) * .75F, .9F, .9F)));
				builder.append(text.charAt(i));
			}
			return builder.toString();
		}
	}

	public enum DefaultFontInfo {
		A('A', 5),
		a('a', 5),
		B('B', 5),
		b('b', 5),
		C('C', 5),
		c('c', 5),
		D('D', 5),
		d('d', 5),
		E('E', 5),
		e('e', 5),
		F('F', 5),
		f('f', 4),
		G('G', 5),
		g('g', 5),
		H('H', 5),
		h('h', 5),
		I('I', 3),
		i('i', 1),
		J('J', 5),
		j('j', 5),
		K('K', 5),
		k('k', 4),
		L('L', 5),
		l('l', 1),
		M('M', 5),
		m('m', 5),
		N('N', 5),
		n('n', 5),
		O('O', 5),
		o('o', 5),
		P('P', 5),
		p('p', 5),
		Q('Q', 5),
		q('q', 5),
		R('R', 5),
		r('r', 5),
		S('S', 5),
		s('s', 5),
		T('T', 5),
		t('t', 4),
		U('U', 5),
		u('u', 5),
		V('V', 5),
		v('v', 5),
		W('W', 5),
		w('w', 5),
		X('X', 5),
		x('x', 5),
		Y('Y', 5),
		y('y', 5),
		Z('Z', 5),
		z('z', 5),
		NUM_1('1', 5),
		NUM_2('2', 5),
		NUM_3('3', 5),
		NUM_4('4', 5),
		NUM_5('5', 5),
		NUM_6('6', 5),
		NUM_7('7', 5),
		NUM_8('8', 5),
		NUM_9('9', 5),
		NUM_0('0', 5),
		EXCLAMATION_POINT('!', 1),
		AT_SYMBOL('@', 6),
		NUM_SIGN('#', 5),
		DOLLAR_SIGN('$', 5),
		PERCENT('%', 5),
		UP_ARROW('^', 5),
		AMPERSAND('&', 5),
		ASTERISK('*', 5),
		LEFT_PARENTHESIS('(', 4),
		RIGHT_PERENTHESIS(')', 4),
		MINUS('-', 5),
		UNDERSCORE('_', 5),
		PLUS_SIGN('+', 5),
		EQUALS_SIGN('=', 5),
		LEFT_CURL_BRACE('{', 4),
		RIGHT_CURL_BRACE('}', 4),
		LEFT_BRACKET('[', 3),
		RIGHT_BRACKET(']', 3),
		COLON(':', 1),
		SEMI_COLON(';', 1),
		DOUBLE_QUOTE('"', 3),
		SINGLE_QUOTE('\'', 1),
		LEFT_ARROW('<', 4),
		RIGHT_ARROW('>', 4),
		QUESTION_MARK('?', 5),
		SLASH('/', 5),
		BACK_SLASH('\\', 5),
		LINE('|', 1),
		TILDE('~', 5),
		TICK('`', 2),
		PERIOD('.', 1),
		COMMA(',', 1),
		SPACE(' ', 3),
		DEFAULT('a', 4);

		private char character;
		private int length;

		DefaultFontInfo(char character, int length) {
			this.character = character;
			this.length = length;
		}

		public char getCharacter() {
			return this.character;
		}

		public int getLength() {
			return this.length;
		}

		public int getBoldLength() {
			if (this == DefaultFontInfo.SPACE)
				return this.getLength();
			return this.length + 1;
		}

		public static DefaultFontInfo getDefaultFontInfo(char c) {
			for (DefaultFontInfo dFI : DefaultFontInfo.values())
				if (dFI.getCharacter() == c)
					return dFI;
			return DefaultFontInfo.DEFAULT;
		}
	}

	public static void sendCenteredMessage(@NonNull Player player, @NonNull String message) {
		if (message.equals(""))
			player.sendMessage("");

		message = colorize(message);

		int CENTER_PX = 154;
		int messagePxSize = 0;
		boolean previousCode = false;
		boolean isBold = false;

		for (char c : message.toCharArray()) {
			if (c == '§') {
				previousCode = true;
				continue;
			} else if (previousCode == true) {
				previousCode = false;
				if (c == 'l' || c == 'L') {
					isBold = true;
					continue;
				} else
					isBold = false;
			} else {
				DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
				messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
				messagePxSize++;
			}
		}

		int halvedMessageSize = messagePxSize / 2;
		int toCompensate = CENTER_PX - halvedMessageSize;
		int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
		int compensated = 0;

		StringBuilder sb = new StringBuilder();
		while (compensated < toCompensate) {
			sb.append(" ");
			compensated += spaceLength;
		}
		player.sendMessage(sb + message);
	}

	private final static TreeMap<Integer, String> romanNumerals = new TreeMap<>();

	static {
		romanNumerals.put(1000, "M");
		romanNumerals.put(900, "CM");
		romanNumerals.put(500, "D");
		romanNumerals.put(400, "CD");
		romanNumerals.put(100, "C");
		romanNumerals.put(90, "XC");
		romanNumerals.put(50, "L");
		romanNumerals.put(40, "XL");
		romanNumerals.put(10, "X");
		romanNumerals.put(9, "IX");
		romanNumerals.put(5, "V");
		romanNumerals.put(4, "IV");
		romanNumerals.put(1, "I");
	}

	public static String toRoman(int number) {
		int l = romanNumerals.floorKey(number);
		if (number == l)
			return romanNumerals.get(number);
		return romanNumerals.get(l) + toRoman(number - l);
	}

	public static int fromRoman(String number) {
		if (number.isEmpty()) return 0;
		if (number.startsWith("M")) return 1000 + fromRoman(number.substring(1));
		if (number.startsWith("CM")) return 900 + fromRoman(number.substring(2));
		if (number.startsWith("D")) return 500 + fromRoman(number.substring(1));
		if (number.startsWith("CD")) return 400 + fromRoman(number.substring(2));
		if (number.startsWith("C")) return 100 + fromRoman(number.substring(1));
		if (number.startsWith("XC")) return 90 + fromRoman(number.substring(2));
		if (number.startsWith("L")) return 50 + fromRoman(number.substring(1));
		if (number.startsWith("XL")) return 40 + fromRoman(number.substring(2));
		if (number.startsWith("X")) return 10 + fromRoman(number.substring(1));
		if (number.startsWith("IX")) return 9 + fromRoman(number.substring(2));
		if (number.startsWith("V")) return 5 + fromRoman(number.substring(1));
		if (number.startsWith("IV")) return 4 + fromRoman(number.substring(2));
		if (number.startsWith("I")) return 1 + fromRoman(number.substring(1));
		throw new IllegalArgumentException(String.format("Could not process roman numeral of '%s'", number));
	}

	public static String getDiscordPrefix(String prefix) {
		return "**[" + prefix + "]** ";
	}

	public static int countUpperCase(String s) {
		return (int) s.codePoints().filter(c -> c >= 'A' && c <= 'Z').count();
	}

	public static int countLowerCase(String s) {
		return (int) s.codePoints().filter(c -> c >= 'a' && c <= 'z').count();
	}

	public static String plural(String label, Number number) {
		return label + (number.doubleValue() == 1 ? "" : "s");
	}

	public static String plural(String labelSingle, String labelPlural, Number number) {
		return number.doubleValue() == 1 ? labelSingle : labelPlural;
	}

	public static String trimFirst(String string) {
		return string.substring(1);
	}

	public static String right(String string, int number) {
		return string.substring(Math.min(string.length() - 1, Math.max(string.length() - number, 0)));
	}

	public static String left(String string, int number) {
		return string.substring(0, Math.min(number, string.length()));
	}

	public static @NotNull String camelCase(Enum<?> _enum) {
		if (_enum == null) return "null";
		return camelCase(_enum.name());
	}

	@Contract("null -> null; !null -> !null")
	public static String camelCase(String text) {
		if (isNullOrEmpty(text))
			return text;

		return Arrays.stream(text.replaceAll("_", " ").split(" "))
				.map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}

	public static String camelCaseWithUnderscores(String text) {
		if (isNullOrEmpty(text))
			return text;

		return Arrays.stream(text.split("_"))
				.map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
				.collect(Collectors.joining("_"));
	}

	public static String asOxfordList(List<String> items, String separator) {
		if (!separator.contains(", "))
			throw new AthenaException("Separator must contain ', '");

		String message = String.join(separator, items);
		int commaIndex = message.lastIndexOf(", ");
		message = new StringBuilder(message).replace(commaIndex, commaIndex + 2, (items.size() > 2 ? "," : "") + " and ").toString();
		return message;
	}

	public static String listFirst(String string, String delimiter) {
		return string.split(delimiter)[0];
	}

	public static String listLast(String string, String delimiter) {
		return string.substring(string.lastIndexOf(delimiter) + 1);
	}

	public static String listGetAt(String string, int index, String delimiter) {
		String[] split = string.split(delimiter);
		return split[index - 1];
	}

	public static String replaceLast(String text, String regex, String replacement) {
		return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

	@Contract("null -> false")
	public static boolean isValidJson(String json) {
		if (json == null)
			return false;

		try {
			new JSONObject(json);
		} catch (JSONException ex) {
			try {
				new JSONArray(json);
			} catch (JSONException ex1) {
				return false;
			}
		}
		return true;
	}

	private static final Gson prettyPrinter = API.get().getPrettyPrinter().create();

	public static String toPrettyString(Object object) {
		if (object == null) return null;
		try {
			return prettyPrinter.toJson(object);
		} catch (Throwable ignored) {
			return object.toString();
		}
	}

	protected static final NumberFormat numberFormat = NumberFormat.getInstance();
	protected static final NumberFormat moneyFormat = NumberFormat.getCurrencyInstance();

	public static String pretty(Number number) {
		String format = numberFormat.format(number);
		if (format.endsWith(".00"))
			format = left(format, format.length() - 3);

		return format;
	}

	public static String prettyMoney(Number number) {
		String format = moneyFormat.format(number);
		if (format.endsWith(".00"))
			format = left(format, format.length() - 3);

		return format;
	}

	public static String stripTrailingZeros(String number) {
		return number.contains(".") ? number.replaceAll("0*$", "").replaceAll("\\.$", "") : number;
	}

	// Attempt to strip symbols and support euro formatting
	public static String asParsableDecimal(String value) {
		if (value == null)
			return "0";

		value = value.replace("$", "");
		if (value.contains(",") && value.contains("."))
			if (value.indexOf(",") < value.indexOf("."))
				value = value.replaceAll(",", "");
			else {
				value = value.replaceAll("\\.", "");
				value = value.replaceAll(",", ".");
			}
		else if (value.contains(",") && value.indexOf(",") == value.lastIndexOf(","))
			if (value.indexOf(",") == value.length() - 3)
				value = value.replace(",", ".");
			else
				value = value.replace(",", "");
		return value;
	}

	public static String ellipsis(String text, int length) {
		if (text.length() > length)
			return text.substring(0, length) + "...";
		else
			return text;
	}

	public static String distanceMetricFormat(int cm) {
		int original = cm;
		int km = cm / 1000 / 100;
		cm -= km * 1000 * 100;
		int meters = cm / 100;
		cm -= meters * 100;

		String result = "";
		if (km > 0)
			result += km + "km ";
		if (meters > 0)
			result += meters + "m ";

		if (result.length() > 0)
			return result.trim();
		else
			return original + "cm";
	}

	public static String getNumberWithSuffix(int number) {
		String text = String.valueOf(number);
		if (text.endsWith("1"))
			if (text.endsWith("11"))
				return number + "th";
			else
				return number + "st";
		else if (text.endsWith("2"))
			if (text.endsWith("12"))
				return number + "th";
			else
				return number + "nd";
		else if (text.endsWith("3"))
			if (text.endsWith("13"))
				return number + "th";
			else
				return number + "rd";
		else
			return number + "th";
	}

	@Getter
	protected static final DecimalFormat df = new DecimalFormat("#.00");

	@Getter
	protected static final DecimalFormat nf = new DecimalFormat("#");

	@Getter
	protected static final DecimalFormat cdf = new DecimalFormat("#,###.00"); // comma decimal formatter

	@Getter
	protected static final DecimalFormat cnf = new DecimalFormat("#,###"); // comma number formatter

	public static DecimalFormat getFormatter(Class<?> type) {
		if (Integer.class == type || Integer.TYPE == type) return nf;
		if (Double.class == type || Double.TYPE == type) return df;
		if (Float.class == type || Float.TYPE == type) return df;
		if (Short.class == type || Short.TYPE == type) return nf;
		if (Long.class == type || Long.TYPE == type) return nf;
		if (Byte.class == type || Byte.TYPE == type) return nf;
		if (BigDecimal.class == type) return df;
		throw new AthenaException("No formatter found for class " + type.getSimpleName());
	}

//	private static final String HASTEBIN = "https://paste.projecteden.gg/";
//
//	@Data
//	private static class PasteResult {
//		private String key;
//
//	}
//
//	@SneakyThrows
//	public static String paste(String content) {
//		Request request = new Request.Builder().url(HASTEBIN + "documents").post(RequestBody.create(MediaType.get("text/plain"), content)).build();
//		try (Response response = new OkHttpClient().newCall(request).execute()) {
//			PasteResult result = new Gson().fromJson(response.body().string(), PasteResult.class);
//			return HASTEBIN + result.getKey();
//		}
//	}
//
//	@NonNull
//	public static String getPaste(String code) {
//		try {
//			Request request = new Request.Builder().url(HASTEBIN + "raw/" + code).get().build();
//			try (Response response = new OkHttpClient().newCall(request).execute()) {
//				return response.body().string();
//			}
//		} catch (Exception ex) {
//			throw new EdenException("An error occurred while retrieving the paste data: " + ex.getMessage());
//		}
//	}

}
