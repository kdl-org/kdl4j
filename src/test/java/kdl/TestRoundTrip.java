package kdl;

import kdl.objects.KDLDocument;
import kdl.parse.KDLParseException;
import kdl.parse.KDLParser;
import kdl.print.PrintConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestRoundTrip {
    private static final PrintConfig PRINT_CONFIG = PrintConfig.builder()
            .setEscapeLinespace(true)
            .build();

    @Parameterized.Parameters(name = "{1}")
    public static List<Object[]> getInputs() {
        try {
            return Files.list(new File("src/test/resources/test_cases/input").toPath())
                    .map(path -> new Object[]{path, path.getFileName().toString()})
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Path inputPath;
    private final String fileName;

    public TestRoundTrip(Path inputPath, String fileName) {
        this.inputPath = inputPath;
        this.fileName = fileName;
    }

    @Test
    public void roundTripTest() throws IOException {
        final String inputString = new String(Files.readAllBytes(inputPath));
        final KDLParser parser = new KDLParser();
        final Optional<String> expected = getExpected();

        try {
            final KDLDocument document = parser.parse(inputString);
            final String output = document.toKDLPretty(PRINT_CONFIG);
            if (!expected.isPresent()) {
                throw new RuntimeException(String.format("Expected parse failure, but got:\n%s", output));
            }

            System.out.printf("EXPECTED:%n%s%n---%nGOT:%n%s%n---", expected.get(), output);

            assertThat(output, equalTo(expected.get()));
        } catch (KDLParseException e) {
            if (expected.isPresent()) {
                throw new RuntimeException("Expected no exception!", e);
            }
        }
    }

    private Optional<String> getExpected() throws IOException {
        final String expectedFile = "src/test/resources/test_cases/expected_kdl/" + fileName;
        try {
            return Optional.of(new String(Files.readAllBytes(new File(expectedFile).toPath())));
        } catch (NoSuchFileException e) {
            return Optional.empty();
        }
    }
}
