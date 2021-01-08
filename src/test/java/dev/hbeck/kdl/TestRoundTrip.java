package dev.hbeck.kdl;

import dev.hbeck.kdl.antlr.kdlLexer;
import dev.hbeck.kdl.antlr.kdlParser;
import dev.hbeck.kdl.objects.KDLDocument;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
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
        final kdlLexer lexer = new kdlLexer(CharStreams.fromReader(new FileReader(inputPath.toFile())));
        final kdlParser parser = new kdlParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new BailErrorStrategy());

        final KDLVisitorImpl visitor = new KDLVisitorImpl();
        final KDLDocument document = (KDLDocument) visitor.visit(parser.parse());
        final String output = document.toKDLPretty(4);

        assertThat(output, equalTo(getExpected()));
    }

    private String getExpected() throws IOException {
        final String expectedFile = "src/test/resources/test_cases/expected_kdl/" + fileName;
        return new String(Files.readAllBytes(new File(expectedFile).toPath()));
    }
}
