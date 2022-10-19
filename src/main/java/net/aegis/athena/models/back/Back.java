package net.aegis.athena.models.back;

import dev.morphia.annotations.Converters;
import dev.morphia.annotations.Embedded;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.aegis.athena.framework.interfaces.PlayerOwnedObject;
import net.aegis.athena.framework.persistence.mongodb.models.serializers.LocationConverter;
import net.aegis.athena.framework.persistence.mongodb.models.serializers.UUIDConverter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity(value = "back", noClassnameStored = true)
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Converters({UUIDConverter.class, LocationConverter.class})
public class Back implements PlayerOwnedObject {
	@Id
	@NonNull
	private UUID uuid;
	@Embedded
	private List<Location> locations = new ArrayList<>();

	private final static int MAX_LOCATIONS = 10;

	public void add(Location from) {
		locations.removeIf(location -> location.getWorld() == null || !location.isWorldLoaded());
		locations.add(0, from);
		locations = new ArrayList<>(locations.subList(0, Math.min(locations.size(), MAX_LOCATIONS)));
	}

}
