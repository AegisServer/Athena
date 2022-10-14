package net.aegis.athena.features.commands;

import lombok.NonNull;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.commands.models.events.CommandEvent;

public class DiscordCommand extends CustomCommand {

	public DiscordCommand(@NonNull CommandEvent event) {
		super(event);
	}

	@Path
	void run() {
		send(json()
				.next("&#9F6E75Click here to join our Discord!")
				.hover("&#6E759FClick here to open an invite in your browser")
				.url("https://discord.gg/pUZq9v7uJA")
		);
	}

}