package dev.kdl;

import dev.kdl.parse.KdlParseException;
import dev.kdl.parse.KdlParser;
import dev.kdl.parse.Reporter;
import dev.kdl.parse.lexer.reader.KdlReadException;
import dev.kdl.print.KdlPrinter;
import dev.kdl.print.KdlPrinterConfiguration;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static dev.kdl.print.KdlPrinterConfiguration.PropertiesOrder.NAME_ASCENDING;
import static dev.kdl.print.KdlPrinterConfiguration.Whitespace.SPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public abstract class RoundTripTest {
	protected RoundTripTest(@Nonnull KdlVersion version) {
		this.parser = KdlParser.fromVersion(version);
		this.printer = new KdlPrinter(
			KdlPrinterConfiguration.builder()
				.version(version)
				.indentation(List.of(SPACE, SPACE, SPACE, SPACE))
				.printDuplicateProperties(false)
				.propertiesOrder(NAME_ASCENDING)
				.build()
		);
	}

	protected void executeRoundTripTest(@Nonnull Path input, @Nonnull Path expectedOutput, @Nonnull Path expectedReport) throws IOException {
		var shouldSucceed = Files.exists(expectedOutput);
		try {
			var document = parser.parse(input);
			var output = printer.printToString(document);

			if (!shouldSucceed) {
				fail("Parse exception expected but got:\n%s", output);
			} else {
				assertThat(output).isEqualTo(Files.readString(expectedOutput, StandardCharsets.UTF_8));
			}
		} catch (KdlParseException | KdlReadException e) {
			if (shouldSucceed) {
				fail("Unexpected exception", e);
			}
			if (e instanceof KdlParseException kdlParseException) {
				if (Files.exists(expectedReport)) {
					var report = Reporter.getReport(kdlParseException);
					assertThat(report).isEqualToIgnoringWhitespace(Files.readString(expectedReport, StandardCharsets.UTF_8));
				}
			}
		}
	}

	static protected List<Arguments> getInputs(@Nonnull Path inputFolder, @Nonnull Path expectedFolder) throws IOException {
		try (var inputs = Files.list(inputFolder)) {
			return inputs
				.map(input -> {
					var expectedOutput = expectedFolder.resolve(input.getFileName());
					var expectedReport = expectedFolder.resolve(input.getFileName() + ".error");
					return Arguments.of(input.getFileName(), input, expectedOutput, expectedReport);
				})
				.collect(Collectors.toList());
		}
	}

	@Nonnull
	private final KdlParser parser;
	@Nonnull
	private final KdlPrinter printer;
}
