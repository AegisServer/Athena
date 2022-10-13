package net.aegis.athena.utils;

import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import net.aegis.athena.Athena;
import net.aegis.athena.framework.commands.models.annotations.Disabled;
import net.aegis.athena.framework.commands.models.annotations.Environments;
import net.aegis.athena.framework.exceptions.postconfigured.InvalidInputException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.aegis.athena.utils.ReflectionUtils.methodsAnnotatedWith;
import static net.aegis.athena.utils.ReflectionUtils.subTypesOf;

public class Utils {

	public static void registerListeners(Package packageObject) {
		registerListeners(packageObject.getName());
	}

	public static void registerListeners(String packageName) {
		subTypesOf(Listener.class, packageName).forEach(Utils::tryRegisterListener);
	}

	public static void tryRegisterListener(Class<?> clazz) {
		if (canEnable(clazz))
			tryRegisterListener(Athena.singletonOf(clazz));
	}

	public static void tryRegisterListener(Object object) {
		try {
			final Class<?> clazz = object.getClass();
			if (!canEnable(clazz))
				return;

			boolean hasNoArgsConstructor = Stream.of(clazz.getConstructors()).anyMatch(c -> c.getParameterCount() == 0);
			if (object instanceof Listener listener) {
				if (hasNoArgsConstructor)
					Athena.registerListener(listener);
				else
					Athena.warn("Cannot register listener on " + clazz.getSimpleName() + ", needs @NoArgsConstructor");
			} else if (methodsAnnotatedWith(clazz, EventHandler.class).size() > 0)
				Athena.warn("Found @EventHandlers in " + clazz.getSimpleName() + " which does not implement Listener"
						+ (hasNoArgsConstructor ? "" : " or have a @NoArgsConstructor"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static <K, V> LinkedHashMap<K, V> reverse(LinkedHashMap<K, V> sorted) {
		LinkedHashMap<K, V> reverse = new LinkedHashMap<>();
		List<K> keys = new ArrayList<>(sorted.keySet());
		Collections.reverse(keys);
		keys.forEach(key -> reverse.put(key, sorted.get(key)));
		return reverse;
	}

	public static <T> List<T> reverse(List<T> list) {
		Collections.reverse(list);
		return list;
	}

	public static <T> T getDefaultPrimitiveValue(Class<T> clazz) {
		return (T) Array.get(Array.newInstance(clazz, 1), 0);
	}

	public static boolean isBoolean(Parameter parameter) {
		return parameter.getType() == Boolean.class || parameter.getType() == Boolean.TYPE;
	}

	public static <K extends Comparable<? super K>, V> LinkedHashMap<K, V> sortByKey(Map<K, V> map) {
		return collect(map.entrySet().stream().sorted(Entry.comparingByKey()));
	}

	public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {
		return collect(map.entrySet().stream().sorted(Entry.comparingByValue()));
	}

	public static <K extends Comparable<? super K>, V> LinkedHashMap<K, V> sortByKeyReverse(Map<K, V> map) {
		return reverse(sortByKey(map));
	}

	public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValueReverse(Map<K, V> map) {
		return reverse(sortByValue(map));
	}

	@SneakyThrows
	public static Number getMaxValue(Class<?> type) {
		return (Number) getMinMaxHolder(type).getDeclaredField("MAX_VALUE").get(null);
	}

	@SneakyThrows
	public static Number getMinValue(Class<?> type) {
		return (Number) getMinMaxHolder(type).getDeclaredField("MIN_VALUE").get(null);
	}

	public static Class<?> getMinMaxHolder(Class<?> type) {
		if (Integer.class == type || Integer.TYPE == type) return Integer.class;
		if (Double.class == type || Double.TYPE == type) return Double.class;
		if (Float.class == type || Float.TYPE == type) return Float.class;
		if (Short.class == type || Short.TYPE == type) return Short.class;
		if (Long.class == type || Long.TYPE == type) return Long.class;
		if (Byte.class == type || Byte.TYPE == type) return Byte.class;
		if (BigDecimal.class == type) return Double.class;
		throw new InvalidInputException("No min/max holder defined for " + type.getSimpleName());
	}

	/**
	 * Removes the first element from an iterable that passes the {@code predicate}.
	 * @param predicate the predicate which returns true when an element should be removed
	 * @param from collection to remove an object from
	 * @return the object that was removed or null
	 */
	@Contract(mutates = "param2")
	public static <T> T removeFirstIf(Predicate<T> predicate, Iterable<T> from) {
		Objects.requireNonNull(predicate, "predicate");
		Objects.requireNonNull(from, "from");

		Iterator<T> iterator = from.iterator();
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (predicate.test(item)) {
				iterator.remove();
				return item;
			}
		}
		return null;
	}

	/**
	 * Removes any element from an iterable that passes the {@code predicate}.
	 * @param predicate the predicate which returns true when an element should be removed
	 * @param from collection to remove an object from
	 * @return whether an object was removed
	 */
	@Contract(mutates = "param2")
	public static <T> boolean removeIf(Predicate<T> predicate, Iterable<T> from) {
		Objects.requireNonNull(predicate, "predicate");
		Objects.requireNonNull(from, "from");

		boolean removed = false;
		Iterator<T> iterator = from.iterator();
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (predicate.test(item)) {
				iterator.remove();
				removed = true;
			}
		}
		return removed;
	}

	/**
	 * Removes any element from an iterable that passes the {@code predicate}
	 * and applies it to {@code consumer}.
	 * @param predicate the predicate which returns true when an element should be removed
	 * @param consumer consumer to perform an action on removed elements
	 * @param from collection to remove an object from
	 * @return whether an object was removed
	 */
	@Contract(mutates = "param2")
	public static <T> boolean removeIf(Predicate<T> predicate, Consumer<T> consumer, Iterable<T> from) {
		Objects.requireNonNull(predicate, "predicate");
		Objects.requireNonNull(from, "from");

		boolean removed = false;
		Iterator<T> iterator = from.iterator();
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (predicate.test(item)) {
				consumer.accept(item);
				iterator.remove();
				removed = true;
			}
		}
		return removed;
	}

	/**
	 * Removes any element from an iterable that passes {@link Objects#equals(Object, Object)}.
	 * @param item item to remove from the list
	 * @param from collection to remove the item from
	 * @return whether an object was removed
	 */
	public static <T> boolean removeAll(T item, Iterable<T> from) {
		return removeIf(object -> Objects.equals(object, item), from);
	}

	/**
	 * Removes any element from an iterable that passes {@link Objects#equals(Object, Object)}
	 * and applies it to {@code consumer}.
	 * @param item item to remove from the list
	 * @param consumer consumer to perform an action on removed elements
	 * @param from collection to remove the item from
	 * @return whether an object was removed
	 */
	public static <T> boolean removeAll(T item, Consumer<T> consumer, Iterable<T> from) {
		return removeIf(object -> Objects.equals(object, item), consumer, from);
	}

	@Data
	@AllArgsConstructor
	public static class MinMaxResult<T> {
		private final T object;
		private final Number value;

		public int getInteger() {
			return value.intValue();
		}
		public double getDouble() {
			return value.doubleValue();
		}
		public float getFloat() {
			return value.floatValue();
		}
		public byte getByte() {
			return value.byteValue();
		}
		public short getShort() {
			return value.shortValue();
		}
		public long getLong() {
			return value.longValue();
		}
	}

	@AllArgsConstructor
	public enum ComparisonOperator {
		LESS_THAN((n1, n2) -> n1.doubleValue() < n2.doubleValue()),
		GREATER_THAN((n1, n2) -> n1.doubleValue() > n2.doubleValue()),
		LESS_THAN_OR_EQUAL_TO((n1, n2) -> n1.doubleValue() <= n2.doubleValue()),
		GREATER_THAN_OR_EQUAL_TO((n1, n2) -> n1.doubleValue() >= n2.doubleValue());

		private final BiPredicate<Number, Number> predicate;

		public boolean run(Number number1, Number number2) {
			return predicate.test(number1, number2);
		}
	}

	public static <T> MinMaxResult<T> getMax(Collection<T> things, Function<T, Number> getter) {
		return getMinMax(things, getter, ComparisonOperator.GREATER_THAN);
	}

	public static <T> MinMaxResult<T> getMin(Collection<T> things, Function<T, Number> getter) {
		return getMinMax(things, getter, ComparisonOperator.LESS_THAN);
	}

	private static <T> MinMaxResult<T> getMinMax(Collection<T> things, Function<T, Number> getter, ComparisonOperator operator) {
		Number number = operator == ComparisonOperator.LESS_THAN ? Double.MAX_VALUE : 0;
		T result = null;

		for (T thing : things) {
			Number value = getter.apply(thing);
			if (value == null)
				continue;

			if (operator.run(value.doubleValue(), number.doubleValue())) {
				number = value;
				result = thing;
			}
		}

		return new MinMaxResult<>(result, number);
	}

	public static <K, V> LinkedHashMap<K, V> collect(Stream<Entry<K, V>> stream) {
		return stream.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	public static final String ALPHANUMERICS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static boolean isLong(String text) {
		try {
			Long.parseLong(text);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean isInt(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean isDouble(String text) {
		try {
			Double.parseDouble(text);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static LocalDateTime epochSecond(String timestamp) {
		// try catch for MinecraftServers.Biz giving timestamp instead of epoch second
		try {
			return epochSecond(Long.parseLong(timestamp));
		} catch (NumberFormatException ex) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxxx");
			return LocalDateTime.parse(timestamp, formatter);
		}
	}

	public static LocalDateTime epochSecond(long timestamp) {
		return epochMilli(String.valueOf(timestamp).length() == 13 ? timestamp : timestamp * 1000);
	}

	public static LocalDateTime epochMilli(long timestamp) {
		return Instant.ofEpochMilli(timestamp)
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}

	public static boolean canEnable(Class<?> clazz) {
		if (clazz.getSimpleName().startsWith("_"))
			return false;
		if (Modifier.isAbstract(clazz.getModifiers()))
			return false;
		if (Modifier.isInterface(clazz.getModifiers()))
			return false;
		if (clazz.getAnnotation(Disabled.class) != null)
			return false;
		if (clazz.getAnnotation(Environments.class) != null && !Env.applies(clazz.getAnnotation(Environments.class).value()))
			return false;

		return true;
	}

	public static boolean canEnable(ClassInfo clazz) {
		if (clazz.getSimpleName().startsWith("_"))
			return false;
		if (Modifier.isAbstract(clazz.getModifiers()))
			return false;
		if (Modifier.isInterface(clazz.getModifiers()))
			return false;
		if (clazz.getAnnotationInfo(Disabled.class) != null)
			return false;

		final AnnotationInfo environments = clazz.getAnnotationInfo(Environments.class);
		if (environments != null) {
			final List<Env> envs = Arrays.stream((Object[]) environments.getParameterValues().get("value").getValue())
					.map(obj -> (AnnotationEnumValue) obj)
					.map(value -> Env.valueOf(value.getValueName()))
					.toList();

			if (!Env.applies(envs))
				return false;
		}

		return true;
	}

}
