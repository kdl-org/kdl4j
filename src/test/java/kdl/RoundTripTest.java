package kdl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import kdl.parse.KDLParseException;
import kdl.parse.KDLParser;
import kdl.print.PrintConfig;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RoundTripTest {

	@ParameterizedTest(name = "{0}")
	@MethodSource("inputs")
	void roundTripTest(String input, Optional<Path> expected) throws IOException {
		try {
			var document = parser.parse(Files.newInputStream(getInputPath(input)));
			var output = document.toKDLPretty(PRINT_CONFIG);

			if (expected.isEmpty()) {
				fail("Parse exception expected but got:\n%s", output);
			} else {
				assertThat(output).isEqualTo(new String(Files.readAllBytes(expected.get())));
			}
		} catch (KDLParseException e) {
			if (expected.isPresent()) {
				fail("Unexpected exception", e);
			}
		}
	}

	private Path getInputPath(String filename) {
		return INPUT_FOLDER.resolve(filename);
	}

	static List<Arguments> inputs() throws IOException {
		try (var inputs = Files.list(INPUT_FOLDER)) {
			return inputs
				.map(Path::getFileName)
				.map(Path::toString)
				.map(input -> {
					var expected = EXPECTED_FOLDER.resolve(input);
					if (Files.exists(expected)) {
						return Arguments.of(input, Optional.of(expected));
					} else {
						return Arguments.of(input, Optional.empty());
					}
				}).collect(Collectors.toList());
		}
	}

	private final KDLParser parser = new KDLParser();

	private static final Path INPUT_FOLDER = Paths.get("src/test/resources/test_cases/input");
	private static final Path EXPECTED_FOLDER = Paths.get("src/test/resources/test_cases/expected_kdl");
	private static final PrintConfig PRINT_CONFIG = PrintConfig.builder()
		.setEscapeLinespace(true)
		.setRespectRadix(false)
		.setPrintEmptyChildren(false)
		.build();

}
