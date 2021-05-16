package org.itsallcode.openfasttrace.maven;

/*-
 * #%L
 * OpenFastTrace Maven Plugin
 * %%
 * Copyright (C) 2018 - 2021 itsallcode.org
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

import java.nio.file.Path;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TraceMojoTest extends AbstractTraceMojoTest
{
    @Rule
    public MojoRule rule = new MojoRule();

    private static Path EMPTY_PROJECT = BASE_TEST_DIR.resolve("empty-project");
    private static Path SIMPLE_PROJECT = BASE_TEST_DIR.resolve("simple-project");
    private static Path TRACING_DEFECTS = BASE_TEST_DIR.resolve("project-with-tracing-defects");
    private static Path TRACING_DEFECTS_FAIL_BUILD = BASE_TEST_DIR
            .resolve("project-with-tracing-defects-fail-build");
    private static Path HTML_REPORT_PROJECT = BASE_TEST_DIR
            .resolve("html-report");

    @Test
    public void testEmptyProject() throws Exception
    {
        runTracingMojo(EMPTY_PROJECT);
        assertFileContent(EMPTY_PROJECT.resolve("target/tracing-report.txt"), "ok - 0 total");
    }

    @Test
    public void testTracingSuccessful() throws Exception
    {
        runTracingMojo(SIMPLE_PROJECT);

        assertThat(fileContent(SIMPLE_PROJECT.resolve("target/reports/tracing-report.txt")))
                .isEqualTo("ok - 3 total\n");
    }

    @Test
    public void testTracingFindsDefects() throws Exception
    {
        runTracingMojo(TRACING_DEFECTS);

        assertThat(fileContent(TRACING_DEFECTS.resolve("target/tracing-report.txt")))
                .contains("not ok - 2 total, 1 defect");
    }

    @Test
    public void testTracingFindsDefectsFailBuild() throws Exception
    {
        assertThatThrownBy(() -> runTracingMojo(TRACING_DEFECTS_FAIL_BUILD))
                .isInstanceOf(MojoFailureException.class)
                .hasMessage("Tracing found 1 out of 2 items");

        assertThat(fileContent(TRACING_DEFECTS_FAIL_BUILD.resolve("target/tracing-report.txt")))
                .contains("not ok - 2 total, 1 defect");
    }

    @Test
    public void testHtmlReport() throws Exception
    {
        runTracingMojo(HTML_REPORT_PROJECT);

        assertThat(fileContent(HTML_REPORT_PROJECT.resolve("target/tracing-report.html")))
                .contains("<span class=\"green\">&check;</span> 3 total");
    }

    private void runTracingMojo(Path projectDir) throws Exception
    {
        rule.executeMojo(projectDir.toFile(), "trace");
    }
}
