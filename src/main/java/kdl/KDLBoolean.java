package kdl;

import jakarta.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

/**
 * A KDL boolean value.
 */
public class KDLBoolean extends KDLValue<Boolean> {
	/**
	 * Creates a new {@link KDLBoolean}.
	 *
	 * @param type  the type of the value
	 * @param value the value
	 */
	public KDLBoolean(@Nonnull Optional<String> type, boolean value) {
		super(type);
		this.value = value;
	}

	@Nonnull
	@Override
	public Boolean getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (KDLBoolean) o;
		return value == that.value && Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, type);
	}

	private final boolean value;
}
