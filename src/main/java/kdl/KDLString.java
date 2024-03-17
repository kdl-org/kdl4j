package kdl;

import jakarta.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

/**
 * A KDL string value.
 */
public class KDLString extends KDLValue<String> {
	/**
	 * Creates a new {@link KDLString}.
	 *
	 * @param value the value
	 */
	public KDLString(@Nonnull String value) {
		super(Optional.empty());
		this.value = value;
	}

	/**
	 * Creates a new {@link KDLString}.
	 *
	 * @param type  the type of the value
	 * @param value the value
	 */
	public KDLString(@Nonnull Optional<String> type, @Nonnull String value) {
		super(type);
		this.value = value;
	}

	@Nonnull
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (KDLString) o;
		return Objects.equals(value, that.value) && Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, type);
	}

	@Override
	public String toString() {
		return "String(" + value + ')';
	}

	@Nonnull
	private final String value;
}
