package net.aegis.athena.framework.commands.models.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.aegis.athena.Athena;
import net.aegis.athena.framework.commands.Commands;
import net.aegis.athena.framework.commands.models.CustomCommand;
import net.aegis.athena.framework.commands.models.annotations.Description;
import net.aegis.athena.framework.commands.models.annotations.Path;
import net.aegis.athena.framework.exceptions.AthenaException;
import net.aegis.athena.framework.exceptions.preconfigured.MissingArgumentException;
import net.aegis.athena.utils.JsonBuilder;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static net.aegis.athena.utils.Nullables.isNullOrEmpty;

@Data
@RequiredArgsConstructor
public class CommandRunEvent extends CommandEvent {
	private Method method;
	private String usage;

	public CommandRunEvent(CommandSender sender, CustomCommand command, String aliasUsed, List<String> args, List<String> originalArgs) {
		super(sender, command, aliasUsed, args, originalArgs, false);
	}

	public void setUsage(Method method) {
		this.method = method;
		Path path = method.getAnnotation(Path.class);
		if (path != null) {
			this.usage = path.value();
			Description desc = method.getAnnotation(Description.class);
			if (desc != null)
				this.usage += " &7- " + desc.value();
		}
	}

	public String getUsageMessage() {
		return "Correct usage: /" + aliasUsed + " " + usage;
	}

	public void handleException(Throwable ex) {
		if (Athena.isDebug()) {
			Athena.debug("Handling command framework exception for " + getSender().getName());
			ex.printStackTrace();
		}

		String PREFIX = command.getPrefix();
		if (isNullOrEmpty(PREFIX))
			PREFIX = Commands.getPrefix(command);

		if (ex instanceof MissingArgumentException) {
			reply(PREFIX + "&c" + getUsageMessage());
			return;
		}

		if (ex.getCause() != null && ex.getCause() instanceof AthenaException nexusException) {
			reply(new JsonBuilder(PREFIX + "&c").next(nexusException.getJson()));
			return;
		}

		if (ex instanceof AthenaException athenaException) {
			reply(new JsonBuilder(PREFIX + "&c").next(athenaException.getJson()));
			return;
		}

		if (ex.getCause() != null && ex.getCause() instanceof AthenaException edenException) {
			reply(PREFIX + "&c" + edenException.getMessage());
			return;
		}

		if (ex instanceof AthenaException) {
			reply(PREFIX + "&c" + ex.getMessage());
			return;
		}

		if (ex instanceof IllegalArgumentException && ex.getMessage() != null && ex.getMessage().contains("type mismatch")) {
			reply(PREFIX + "&c" + getUsageMessage());
			return;
		}

		reply("&cAn internal error occurred while attempting to execute this command");

		if (ex.getCause() != null && ex instanceof InvocationTargetException)
			ex.getCause().printStackTrace();
		else
			ex.printStackTrace();
	}

}
