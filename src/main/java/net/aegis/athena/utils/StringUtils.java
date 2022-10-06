package net.aegis.athena.utils;

import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.aegis.athena.utils.Nullables.isNullOrEmpty;
import static org.bukkit.ChatColor.stripColor;

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

	@Contract("null -> null; !null -> !null")
	public static String camelCase(String text) {
		if (isNullOrEmpty(text))
			return text;

		return Arrays.stream(text.replaceAll("_", " ").split(" "))
				.map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
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
}
