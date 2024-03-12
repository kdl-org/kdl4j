package kdl;

import jakarta.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

/**
 * The KDL null value.
 */
public class KDLNull extends KDLValue<Object> {
	/**
	 * Creates a new {@link KDLNull}.
	 *
	 * @param type the type of the value
	 */
	public KDLNull(@Nonnull Optional<String> type) {
		super(type);
	}

	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		var that = (KDLNull) o;
		return Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type);
	}
}
