package net.aegis.athena.framework.exceptions.preconfigured;

public class NegativeBalanceException extends PreConfiguredException {

	public NegativeBalanceException() {
		super("Balances cannot be negative");
	}

}
