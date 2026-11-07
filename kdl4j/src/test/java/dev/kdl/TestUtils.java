package dev.kdl;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {
	private TestUtils() {
	}

	public static KdlDocument document(KdlNode... nodes) {
		return new KdlDocument(Arrays.asList(nodes));
	}

	public static KdlNode node(String name, KdlNode... children) {
		return node(null, name, children);
	}

	public static KdlNode node(@Nullable String type, String name, KdlNode... children) {
		return new KdlNode(
			type,
			name,
			Collections.emptyList(),
			KdlProperties.builder().build(),
			Arrays.asList(children)
		);
	}

	public static KdlNode node(String name, List<KdlValue<?>> arguments, KdlProperties properties, KdlNode... children) {
		return new KdlNode(
			null,
			name,
			arguments,
			properties,
			Arrays.asList(children)
		);
	}

	public static KdlNode node(String name, List<KdlValue<?>> arguments, KdlNode... children) {
		return new KdlNode(
			null,
			name,
			arguments,
			KdlProperties.builder().build(),
			Arrays.asList(children)
		);
	}

	public static KdlNode node(String name, KdlProperties properties, KdlNode... children) {
		return new KdlNode(
			null,
			name,
			Collections.emptyList(),
			properties,
			Arrays.asList(children)
		);
	}

	public static List<KdlValue<?>> arguments(Object... values) {
		return Arrays.stream(values).map(KdlValue::from).collect(Collectors.toList());
	}

	public static KdlNull kdlNull(@Nullable String type) {
		return new KdlNull(type);
	}

	public static KdlNull kdlNull() {
		return kdlNull(null);
	}

	public static KdlString string(@Nullable String type, String value) {
		return new KdlString(type, value);
	}

	public static KdlNumber<?> number(@Nullable String type, Number number) {
		return KdlNumber.from(type, number);
	}

	public static KdlBoolean kdlBoolean(@Nullable String type, boolean value) {
		return new KdlBoolean(type, value);
	}

	public static KdlProperties properties(String name, @Nullable Object value) {
		return KdlProperties.builder().property(name, KdlValue.from(value)).build();
	}
}
