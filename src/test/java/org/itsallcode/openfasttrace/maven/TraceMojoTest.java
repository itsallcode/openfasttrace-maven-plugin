package org.itsallcode.openfasttrace.maven;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

public class TraceMojoTest
{
    @Rule
    public MojoRule rule = new MojoRule();

    private static Path BASE_TEST_DIR = Paths.get("src/test/resources").toAbsolutePath();
    private static Path EMPTY_PROJECT = BASE_TEST_DIR.resolve("empty-project");

    @Test
    public void testSomething() throws Exception
    {
        runTracingMojo(EMPTY_PROJECT);
        assertFileContent(EMPTY_PROJECT.resolve("target/tracing-report.txt"), "ok - 0 total");
    }

    private void assertFileContent(Path file, String... lines) throws IOException
    {
        final String fileContent = fileContent(file);
        for (final String line : lines)
        {
            assertThat(fileContent, containsString(line));
        }
    }

    private String fileContent(Path file) throws IOException
    {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private void runTracingMojo(Path projectDir) throws Exception
    {
        rule.executeMojo(projectDir.toFile(), "trace");
    }
}
