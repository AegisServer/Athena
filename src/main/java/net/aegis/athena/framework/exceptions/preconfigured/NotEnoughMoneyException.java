package net.aegis.athena.framework.exceptions.preconfigured;

public class NotEnoughMoneyException extends PreConfiguredException {

	public NotEnoughMoneyException() {
		super("You do not have enough money to complete this transaction");
	}

}
