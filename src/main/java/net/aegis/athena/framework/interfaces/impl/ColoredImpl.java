package net.aegis.athena.framework.interfaces.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.aegis.athena.framework.interfaces.Colored;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

@AllArgsConstructor
public class ColoredImpl implements Colored {
	@Getter @Accessors(fluent = true)
	private final int value;

	@Override
	public @NotNull Color getColor() {
		return new Color(value);
	}
}
