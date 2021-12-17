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

import static com.exasol.mavenprojectversiongetter.MavenProjectVersionGetter.getCurrentProjectVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;

import com.exasol.mavenpluginintegrationtesting.MavenIntegrationTestEnvironment;

class TraceMojoVerifierTest
{
    static Path BASE_TEST_DIR = Paths.get("src/test/resources").toAbsolutePath();
    private static final String CURRENT_PLUGIN_VERSION = getCurrentProjectVersion();
    private static final File CURRENT_PLUGIN_JAR = Path
            .of("target", "openfasttrace-maven-plugin-" + CURRENT_PLUGIN_VERSION + ".jar")
            .toFile();
    private static final File CURRENT_PLUGIN_POM = Path.of("pom.xml").toFile();
    private static final Path PROJECT_WITH_MULTIPLE_LANGUAGES = BASE_TEST_DIR
            .resolve("project-with-multiple-languages");
    private static final Path PROJECT_WITH_SUB_MODULE = BASE_TEST_DIR.resolve("project-with-sub-module");
    private static final Path PROJECT_WITH_NESTED_SUB_MODULE = BASE_TEST_DIR.resolve("project-with-nested-sub-module");
    private static final Path EMPTY_PROJECT = BASE_TEST_DIR.resolve("empty-project");
    private static final Path SIMPLE_PROJECT = BASE_TEST_DIR.resolve("simple-project");
    private static final Path TRACING_DEFECTS = BASE_TEST_DIR.resolve("project-with-tracing-defects");
    private static final Path TRACING_DEFECTS_FAIL_BUILD = BASE_TEST_DIR
            .resolve("project-with-tracing-defects-fail-build");
    private static final Path HTML_REPORT_PROJECT = BASE_TEST_DIR
            .resolve("html-report");
    private static MavenIntegrationTestEnvironment mvnITEnv;

    @BeforeAll
    static void beforeAll()
    {
        mvnITEnv = new MavenIntegrationTestEnvironment();
        mvnITEnv.installPlugin(CURRENT_PLUGIN_JAR, CURRENT_PLUGIN_POM);
    }

    @Test
    void testTracingWithMultipleLanguages() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_MULTIPLE_LANGUAGES);
        verifier.executeGoal("verify");
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_MULTIPLE_LANGUAGES.resolve("target/tracing-report.txt")))
                .isEqualTo("ok - 6 total\n");
    }

    @Test
    void testTracingWithSubModule() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_SUB_MODULE);
        verifier.executeGoal("verify");
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_SUB_MODULE.resolve("target/tracing-report.txt")))
                .isEqualTo("ok - 3 total\n");
    }

    @Test
    void testTracingWithNestedSubModule() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_NESTED_SUB_MODULE);
        verifier.executeGoal("verify");
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_NESTED_SUB_MODULE.resolve("target/tracing-report.txt")))
                .isEqualTo("ok - 3 total\n");
    }

    static void assertFileContent(Path file, String... lines) throws IOException
    {
        final String fileContent = fileContent(file);
        for (final String line : lines)
        {
            MatcherAssert.assertThat(fileContent, containsString(line));
        }
    }

    static String fileContent(Path file) throws IOException
    {
        Assertions.assertTrue(Files.exists(file), "File does not exist: " + file);
        return Files.readString(file);
    }

    @Test
    void testEmptyProject() throws Exception
    {
        runTracingMojo(EMPTY_PROJECT);
        assertFileContent(EMPTY_PROJECT.resolve("target/tracing-report.txt"), "ok - 0 total");
    }

    @Test
    void testTracingSuccessful() throws Exception
    {
        runTracingMojo(SIMPLE_PROJECT);

        assertThat(fileContent(SIMPLE_PROJECT.resolve("target/reports/tracing-report.txt")))
                .isEqualTo("ok - 3 total\n");
    }

    @Test
    void testTracingFindsDefects() throws Exception
    {
        runTracingMojo(TRACING_DEFECTS);

        assertThat(fileContent(TRACING_DEFECTS.resolve("target/tracing-report.txt")))
                .contains("not ok - 2 total, 1 defect");
    }

    @Test
    void testTracingFindsDefectsFailBuild() throws Exception
    {
        assertThatThrownBy(() -> runTracingMojo(TRACING_DEFECTS_FAIL_BUILD))
                .isInstanceOf(VerificationException.class)
                .hasMessageContaining("Tracing found 1 defects out of 2 items");

        assertThat(fileContent(TRACING_DEFECTS_FAIL_BUILD.resolve("target/tracing-report.txt")))
                .contains("not ok - 2 total, 1 defect");
    }

    @Test
    void testHtmlReport() throws Exception
    {
        runTracingMojo(HTML_REPORT_PROJECT);

        assertThat(fileContent(HTML_REPORT_PROJECT.resolve("target/tracing-report.html")))
                .contains("<span class=\"green\">&check;</span> 3 total");
    }

    private void runTracingMojo(Path projectDir) throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(projectDir);
        verifier.executeGoal("openfasttrace:trace");
        verifier.verifyErrorFreeLog();
    }
}
