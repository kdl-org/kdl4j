package dev.kdl;

import jakarta.annotation.Nonnull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Kdl2ParserRoundTripTest extends RoundTripTest {
	protected Kdl2ParserRoundTripTest() {
		super(KdlVersion.V2);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("inputs")
	public void roundTripTest(@Nonnull Path filename, @Nonnull Path input, @Nonnull Path expectedOutput, @Nonnull Path expectedReport) throws IOException {
		executeRoundTripTest(input, expectedOutput, expectedReport);
	}

	public static List<Arguments> inputs() throws IOException {
		return getInputs(INPUT_FOLDER, EXPECTED_FOLDER);
	}

	private static final Path INPUT_FOLDER = Paths.get("src/test/resources/test-cases/v2/input");
	private static final Path EXPECTED_FOLDER = Paths.get("src/test/resources/test-cases/v2/expected");
}
