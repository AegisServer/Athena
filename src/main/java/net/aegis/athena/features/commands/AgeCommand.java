package net.aegis.athena.features.commands;

import lombok.Data;
import lombok.Getter;
import net.aegis.athena.Athena;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.models.nerd.Nerd;
import net.aegis.athena.models.nickname.Nickname;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AgeCommand extends CustomCommand {

	public AgeCommand(CommandEvent event) {
		super(event);
	}


	@Path("[player]")
	void player(Nerd nerd) {
		if (arg(1).equalsIgnoreCase("athena") || arg(1).equalsIgnoreCase("aegis") || arg(1).equalsIgnoreCase("server")) {
			server();
			return;
		}

		try {
			int year = nerd.getBirthday().until(LocalDate.now()).getYears();
			send(PREFIX + Nickname.of(nerd) + " is " + Athena.aegisBlue + year + Athena.aegisRed + " years old.");
		} catch (Exception ex) {
			send(PREFIX + "That player does not have a set birthday");
		}
	}

	@Data
	public static class ServerAge {
		@Getter
		public static final LocalDateTime EPOCH = LocalDateTime.now().withMonth(9).withDayOfMonth(18).withHour(10).withMinute(13);
		private final double years, months, weeks, days, hours, minutes, seconds;

		public ServerAge() {
			Duration age = Duration.between(EPOCH, LocalDateTime.now());
			seconds = age.getSeconds();
			minutes = seconds / 60;
			hours = minutes / 60;
			days = hours / 24;
			weeks = days / 7;
			months = days / 30.42;
			years = days / 365;
		}

		private static final DecimalFormat format = new DecimalFormat("###,###,##0.00");

		static {
			format.setRoundingMode(RoundingMode.UP);
		}

		public static String format(double value) {
			return format.format(value);
		}
	}

	@Path()
	void server() {
		ServerAge serverAge = new ServerAge();

		send(Athena.aegisRed + "Aegis was made on" + Athena.aegisBlue + "September 18th, 2022" + Athena.aegisRed + ", at " + Athena.aegisBlue + "10:13 AM ET");
		send(Athena.aegisRed + "That makes it...");
		line();
		send("&e" + ServerAge.format(serverAge.getYears()) + " &3years old");
		send("&e" + ServerAge.format(serverAge.getMonths()) + " &3months old");
		send("&e" + ServerAge.format(serverAge.getWeeks()) + " &3weeks old");
		send("&e" + ServerAge.format(serverAge.getDays()) + " &3days old");
		send("&e" + ServerAge.format(serverAge.getHours()) + " &3hours old");
		send("&e" + ServerAge.format(serverAge.getMinutes()) + " &3minutes old");
		send("&e" + ServerAge.format(serverAge.getSeconds()) + " &3seconds old");
	}
}
