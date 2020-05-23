package org.itsallcode.openfasttrace.maven;

import static org.assertj.core.api.Assertions.assertThat;
/*-
 * #%L
 * OpenFastTrace Maven Plugin
 * %%
 * Copyright (C) 2018 - 2019 itsallcode.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
    private static Path SIMPLE_PROJECT = BASE_TEST_DIR.resolve("simple-project");

    @Test
    public void testEmptyProject() throws Exception
    {
        runTracingMojo(EMPTY_PROJECT);
        assertFileContent(EMPTY_PROJECT.resolve("target/tracing-report.txt"), "ok - 0 total");
    }

    @Test
    public void testSimpleProject() throws Exception
    {
        runTracingMojo(SIMPLE_PROJECT);

        assertThat(fileContent(SIMPLE_PROJECT.resolve("target/tracing-report.txt")))
                .isEqualTo("ok - 3 total\n");
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
        assertTrue("File does not exist: " + file, Files.exists(file));
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private void runTracingMojo(Path projectDir) throws Exception
    {
        rule.executeMojo(projectDir.toFile(), "trace");
    }
}
