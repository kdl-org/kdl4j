package dev.kdl.parse;

import dev.kdl.parse.context.Span;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ParseContextTest {

	private static final KdlParser V1 = KdlParser.v1();
	private static final KdlParser V2 = KdlParser.v2();

	@Test
	void nodeNameAndTypeSpans() throws KdlParseException, IOException {
		var document = V2.parse("test.kdl", "(nodeType) nodeName");
		var node = document.nodes().get(0);

		assertThat(node.nameSpan()).isEqualTo(Span.of(1, 12, 1, 19));
		assertThat(node.typeSpan()).isEqualTo(Span.of(1, 1, 1, 10));
		assertThat(node.parseContext()).isNotNull();
		assertThat(node.parseContext().span()).isEqualTo(Span.of(1, 1, 1, 19));
		assertThat(node.parseContext().filename()).isEqualTo("test.kdl");
	}

	@Test
	void argumentValueSpans() throws KdlParseException, IOException {
		var document = V2.parse("test.kdl", "(nodeType) nodeName (argType) arg1");
		var node = document.nodes().get(0);
		var argument = node.arguments().get(0);

		assertThat(argument.typeSpan()).isEqualTo(Span.of(1, 21, 1, 29));
		assertThat(argument.parseContext()).isNotNull();
		assertThat(argument.parseContext().span()).isEqualTo(Span.of(1, 21, 1, 34));
	}

	@Test
	void propertySpans() throws KdlParseException, IOException {
		var document = V2.parse("test.kdl", "node propName=(propType) propValue");
		var node = document.nodes().get(0);
		var property = node.properties().properties().get(0);

		assertThat(property.nameSpan()).isEqualTo(Span.of(1, 6, 1, 13));
		assertThat(property.parseContext()).isNotNull();
		assertThat(property.parseContext().span()).isEqualTo(Span.of(1, 6, 1, 34));
		assertThat(property.value().typeSpan()).isEqualTo(Span.of(1, 15, 1, 24));
		assertThat(property.value().parseContext()).isNotNull();
		assertThat(property.value().parseContext().span()).isEqualTo(Span.of(1, 15, 1, 34));
	}

	@Test
	void childNodeSpans() throws KdlParseException, IOException {
		var document = V2.parse("test.kdl","""
			parent {
			  (childType) child
			}""");
		var parent = document.nodes().get(0);
		var child = parent.children().get(0);

		assertThat(child.nameSpan()).isEqualTo(Span.of(2, 15, 2, 19));
		assertThat(child.typeSpan()).isEqualTo(Span.of(2, 3, 2, 13));
		assertThat(child.parseContext()).isNotNull();
		assertThat(child.parseContext().span()).isEqualTo(Span.of(2, 3, 2, 19));
		assertThat(parent.parseContext().sourceLines()).extracting("lineNumber").containsExactly(1, 2, 3);
		assertThat(child.parseContext().sourceLines()).extracting("lineNumber").containsExactly(2);
	}

	@Test
	void kdl1Spans() throws KdlParseException, IOException {
		var document = V1.parse("test.kdl", "(nodeType)nodeName (argType)\"arg1\"");
		var node = document.nodes().get(0);
		var argument = node.arguments().get(0);

		assertThat(node.nameSpan()).isEqualTo(Span.of(1, 11, 1, 18));
		assertThat(node.typeSpan()).isEqualTo(Span.of(1, 1, 1, 10));
		assertThat(node.parseContext().span()).isEqualTo(Span.of(1, 1, 1, 34));
		assertThat(argument.typeSpan()).isEqualTo(Span.of(1, 20, 1, 28));
		assertThat(argument.parseContext().span()).isEqualTo(Span.of(1, 20, 1, 34));
	}

	@Test
	void locationDoesNotAffectEquality() throws KdlParseException, IOException {
		var first = V2.parse("first.kdl", "node 1");
		var second = V2.parse("second.kdl", "node 1");

		assertThat(first).isEqualTo(second);
		assertThat(first).hasSameHashCodeAs(second);
	}
}
