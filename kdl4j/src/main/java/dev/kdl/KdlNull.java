package dev.kdl;

import dev.kdl.parse.context.ParseContext;
import dev.kdl.parse.context.Span;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * The KDL null value.
 *
 * @param type         the type of the value
 * @param typeSpan     the span of the type annotation, if any
 * @param parseContext the context in which the value was parsed, if any
 */
public record KdlNull(
	@Nullable String type,
	@Nullable Span typeSpan,
	@Nullable ParseContext parseContext
) implements KdlValue<Object> {
	/**
	 * Creates a new {@link KdlNull} with no type.
	 */
	public KdlNull() {
		this(null, null, null);
	}

	/**
	 * Creates a new {@link KdlNull} with a type.
	 *
	 * @param type the type of the value
	 */
	public KdlNull(@Nullable String type) {
		this(type, null, null);
	}

	/**
	 * @return whether this value is a KDL null
	 */
	public boolean isNull() {
		return true;
	}

	@Override
	public Object value() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof KdlNull that)) {
			return false;
		}
		return Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type);
	}
}
