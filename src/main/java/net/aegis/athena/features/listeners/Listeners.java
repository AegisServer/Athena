package net.aegis.athena.features.listeners;

import net.aegis.athena.framework.features.Feature;

import static net.aegis.athena.utils.Utils.registerListeners;

public class Listeners extends Feature {

	@Override
	public void onStart() {
		registerListeners(getClass().getPackageName());
	}

}
