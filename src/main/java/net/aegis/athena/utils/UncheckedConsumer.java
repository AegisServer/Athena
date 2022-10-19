package net.aegis.athena.utils;

@FunctionalInterface
public interface UncheckedConsumer<T> {
	void accept(T var1) throws Exception;
}
