package net.aegis.athena.models.warps;

import net.aegis.athena.models.warps.Warps.Warp;
import org.bukkit.Location;

import java.util.List;

public enum WarpType {
	NORMAL,
	STAFF,
	QUEST,
	;

	private Warps get() {
		return new WarpsService().get0();
	}

	public List<Warp> getAll() {
		return get().getAll(this);
	}

	public Warp get(String name) {
		return getAll().stream().filter(warp -> warp.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public void add(String name, Location location) {
		get().add(new Warp(name, this, location));
	}

	public void delete(String name) {
		get().delete(this, name);
	}

}
