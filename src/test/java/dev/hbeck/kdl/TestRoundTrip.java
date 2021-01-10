package dev.hbeck.kdl;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.parse.KDLParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestRoundTrip {

    @Parameterized.Parameters(name = "{1}")
    public static List<Object[]> getInputs() {
        System.out.println(System.getProperty("user.dir"));
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
        final FileReader reader = new FileReader(inputPath.toFile());
        final KDLParser parserV2 = new KDLParser();
        final KDLDocument document = parserV2.parse(reader);
        final String output = document.toKDLPretty(4);
        assertThat(output, equalTo(getExpected()));
    }

    private String getExpected() throws IOException {
        final String expectedFile = "src/test/resources/test_cases/expected_kdl/" + fileName;
        return new String(Files.readAllBytes(new File(expectedFile).toPath()));
    }
}
