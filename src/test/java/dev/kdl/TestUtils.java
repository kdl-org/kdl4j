package dev.kdl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {
	private TestUtils() {
	}

	@Nonnull
	public static KdlDocument document(@Nonnull KdlNode... nodes) {
		return new KdlDocument(Arrays.asList(nodes));
	}

	@Nonnull
	public static KdlNode node(@Nonnull String name, @Nonnull KdlNode... children) {
		return node(null, name, children);
	}

	@Nonnull
	public static KdlNode node(@Nullable String type, @Nonnull String name, @Nonnull KdlNode... children) {
		return new KdlNode(
			type,
			name,
			Collections.emptyList(),
			KdlProperties.builder().build(),
			Arrays.asList(children)
		);
	}

	@Nonnull
	public static KdlNode node(@Nonnull String name, @Nonnull List<KdlValue<?>> arguments, @Nonnull KdlProperties properties, @Nonnull KdlNode... children) {
		return new KdlNode(
			null,
			name,
			arguments,
			properties,
			Arrays.asList(children)
		);
	}

	@Nonnull
	public static KdlNode node(@Nonnull String name, @Nonnull List<KdlValue<?>> arguments, @Nonnull KdlNode... children) {
		return new KdlNode(
			null,
			name,
			arguments,
			KdlProperties.builder().build(),
			Arrays.asList(children)
		);
	}

	@Nonnull
	public static KdlNode node(@Nonnull String name, @Nonnull KdlProperties properties, @Nonnull KdlNode... children) {
		return new KdlNode(
			null,
			name,
			Collections.emptyList(),
			properties,
			Arrays.asList(children)
		);
	}

	@Nonnull
	public static List<KdlValue<?>> arguments(@Nonnull Object... values) {
		return Arrays.stream(values).map(KdlValue::from).collect(Collectors.toList());
	}

	@Nonnull
	public static KdlNull kdlNull(@Nullable String type) {
		return new KdlNull(type);
	}

	@Nonnull
	public static KdlNull kdlNull() {
		return kdlNull(null);
	}

	@Nonnull
	public static KdlString string(@Nullable String type, @Nonnull String value) {
		return new KdlString(type, value);
	}

	@Nonnull
	public static KdlNumber<?> number(@Nullable String type, @Nonnull Number number) {
		return KdlNumber.from(type, number);
	}

	@Nonnull
	public static KdlBoolean kdlBoolean(@Nullable String type, boolean value) {
		return new KdlBoolean(type, value);
	}

	@Nonnull
	public static KdlProperties properties(@Nonnull String name, @Nullable Object value) {
		return KdlProperties.builder().property(name, KdlValue.from(value)).build();
	}
}
