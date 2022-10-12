package net.aegis.athena.models.scoreboard;

import com.mongodb.DBObject;
import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.PreLoad;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.aegis.athena.Athena;
import net.aegis.athena.features.scoreboard.ScoreboardLine;
import net.aegis.athena.framework.exceptions.AthenaException;
import net.aegis.athena.framework.interfaces.PlayerOwnedObject;
import net.aegis.athena.framework.persistence.mongodb.models.serializers.UUIDConverter;
import net.aegis.athena.utils.AthenaScoreboard;
import net.aegis.athena.utils.Tasks;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static net.aegis.athena.utils.Nullables.isNullOrEmpty;

@Data
@Entity(value = "scoreboard_user", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@Converters(UUIDConverter.class)
public class ScoreboardUser implements PlayerOwnedObject {
	@Id
	@NonNull
	private UUID uuid;
	private Map<ScoreboardLine, Boolean> lines = new ConcurrentHashMap<>();
	private boolean active = true;

	private transient AthenaScoreboard scoreboard;
	private transient ListOrderedMap<ScoreboardLine, String> rendered = new ListOrderedMap<>();
	private transient int headerTaskId = -1;
	private transient Map<ScoreboardLine, Integer> taskIds = new HashMap<>();

	public static final int HEADER_UPDATE_INTERVAL = 2;
	public static final int UPDATE_INTERVAL = 40;

	public ScoreboardUser(UUID uuid) {
		this.uuid = uuid;
		if (lines.isEmpty())
			lines = ScoreboardLine.getDefaultLines(getOnlinePlayer());
	}

	@PreLoad
	void fixPreLoad(DBObject dbObject) {
		DBObject map = (DBObject) dbObject.get("lines");
		if (map != null && map.containsField("SERVER_TIME"))
			map.removeField("SERVER_TIME");
	}

	public void on() {
		if (UUID.fromString("75d63edb-84cc-4d4a-b761-4f81c91b2b7a").equals(uuid)) {
			try {
				throw new AthenaException("Turning on keyhole's scoreboard");
			} catch (AthenaException ex) {
				Athena.log(ex.getMessage());
				ex.printStackTrace();
			}
		}

		pause();
		if (scoreboard == null)
			scoreboard = new AthenaScoreboard("bnsb-" + uuid.toString().replace("-", ""), "&e> &3Project Eden &e<", getOnlinePlayer());
		else
			scoreboard.subscribe(getOnlinePlayer());
		active = true;
		Tasks.cancel(headerTaskId);
		headerTaskId = Tasks.repeatAsync(0, (long) (ScoreboardLine.getHeaderFrames().size() + 1) * HEADER_UPDATE_INTERVAL, new Header(getOnlinePlayer()));
		startTasks();
	}

	public void off() {
		active = false;
		pause();
	}

	public void pause() {
		if (scoreboard != null) {
			if (isOnline())
				scoreboard.unsubscribe(getOnlinePlayer());
			scoreboard.delete();
			scoreboard = null;
		}
		rendered = new ListOrderedMap<>();
		Tasks.cancel(headerTaskId);
		headerTaskId = -1;
		cancelTasks();
	}

	public void cancelTasks() {
		taskIds.values().forEach(Tasks::cancel);
		taskIds.clear();
	}

	private String getRenderedText(ScoreboardLine line) {
		return rendered.getOrDefault(line, null);
	}

	private int getScore(ScoreboardLine line) {
		List<ScoreboardLine> renderedOrder = new ArrayList<>();
		for (ScoreboardLine toRender : ScoreboardLine.values())
			if (lines.containsKey(toRender) && lines.get(toRender))
				renderedOrder.add(toRender);
		return renderedOrder.size() - renderedOrder.indexOf(line) - 1;
	}

	public void startTasks() {
		cancelTasks();
		Arrays.asList(ScoreboardLine.values()).forEach(line -> {
			if (lines.containsKey(line) && lines.get(line))
				taskIds.put(line, Tasks.repeatAsync(5, line.getInterval(), () -> render(line)));
		});
	}

	public void remove(ScoreboardLine line) {
		removeLine(getRenderedText(line));
		rendered.remove(line);
		if (taskIds.containsKey(line))
			Tasks.cancel(taskIds.get(line));
	}

	public void render(ScoreboardLine line) {
		if (scoreboard == null)
			return;

		if (!isOnline()) {
			pause();
			return;
		}

		String oldText = getRenderedText(line);
		if (lines.containsKey(line) && lines.get(line)) {
			String newText = line.render(getOnlinePlayer());

			if (!isNullOrEmpty(newText)) {
				if (newText.equals(oldText))
					if (scoreboard.getLines().containsKey(oldText) && scoreboard.getLines().get(oldText) == getScore(line))
						return;

				removeLine(oldText);
				rendered.put(line, newText);
				scoreboard.setLine(newText, getScore(line));
			} else {
				removeLine(oldText);
			}
		} else {
			removeLine(oldText);
		}
	}

	private void removeLine(String oldText) {
		try {
			scoreboard.removeLine(oldText);
		} catch (NullPointerException ignore) {}
	}

	public static class Header implements Runnable {
		private final ScoreboardUser user;

		public Header(Player player) {
			user = new ScoreboardService().get((HasUniqueId) player);
		}

		@Override
		public void run() {
			AtomicInteger wait = new AtomicInteger(0);
			ScoreboardLine.getHeaderFrames().iterator().forEachRemaining(header ->
					Tasks.waitAsync(wait.getAndAdd(HEADER_UPDATE_INTERVAL), () -> {
						if (user.isActive() && user.getScoreboard() != null)
							user.getScoreboard().setTitle(header);
					}));
		}
	}
}
