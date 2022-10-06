package net.aegis.athena.framework.exceptions.postconfigured;

public class PlayerNotFoundException extends PostConfiguredException{

	public PlayerNotFoundException(String input) {
		super("Player " + input + " not found");
	}

}
