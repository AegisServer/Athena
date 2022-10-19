package net.aegis.athena.features.commands;


import net.aegis.athena.Athena;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Aliases;
import net.aegis.athena.framework.commands.models.annotations.Arg;
import net.aegis.athena.framework.commands.models.annotations.Async;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.annotations.Permission;
import net.aegis.athena.framework.commands.models.annotations.Permission.Group;
import net.aegis.athena.framework.commands.models.events.CommandEvent;
import net.aegis.athena.models.nerd.Nerd;
import net.aegis.athena.models.nerd.NerdService;
import net.aegis.athena.models.nickname.Nickname;
import net.aegis.athena.utils.IOUtils;
import net.aegis.athena.utils.TimeUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static net.aegis.athena.utils.TimeUtils.longDateTimeFormat;

@Aliases("firstjoin")
public class FirstLoginCommand extends CustomCommand {

	public FirstLoginCommand(CommandEvent event) {
		super(event);
	}

	@Path("[player]")
	void firstJoin(@Arg("self") Nerd nerd) {
		send(Athena.aegisBlue + "&l" + Nickname.of(nerd) + Athena.aegisRed + " first joined Aegis on " + Athena.aegisBlue + longDateTimeFormat(nerd.getFirstJoin()) + Athena.aegisRed + " US Eastern Time");
	}

	@Permission(Group.ADMIN)
	@Path("set <player> <date>")
	void set(Nerd nerd, LocalDateTime dateTime) {
		nerd.setFirstJoin(dateTime);
		new NerdService().save(nerd);
		send(PREFIX + "Updated first join of " + Athena.aegisBeige + nerd.getNickname() + Athena.aegisBlue + " to " + Athena.aegisBeige + TimeUtils.shortDateTimeFormat(dateTime));
	}

	@Async
	@Path("stats")
	@Permission(Group.ADMIN)
	void stats() {
		StringBuilder data = new StringBuilder();
		for (Nerd nerd : new NerdService().getAll())
			if (nerd.getFirstJoin() != null)
				data
						.append(nerd.getNickname())
						.append(",")
						.append(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(nerd.getFirstJoin()))
						.append(System.lineSeparator());

		IOUtils.fileAppend("joindates.csv", data.toString());
		send(PREFIX + "Generated joindates.csv");
	}

}
