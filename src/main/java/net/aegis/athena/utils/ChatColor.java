package net.aegis.athena.utils;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatColor {

	public final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
	public final char COLOR_CHAR = org.bukkit.ChatColor.COLOR_CHAR;

	public String translateHexColorCodes(String message) {
		Matcher matcher = HEX_PATTERN.matcher(message);
		StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
		while (matcher.find()) {
			String group = matcher.group(1);
			matcher.appendReplacement(buffer, COLOR_CHAR + "x"
					+ COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
					+ COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
					+ COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
			);
		}
		return matcher.appendTail(buffer).toString();
	}

}