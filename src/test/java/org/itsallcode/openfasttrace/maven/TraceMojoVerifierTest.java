package org.itsallcode.openfasttrace.maven;

import static com.exasol.mavenprojectversiongetter.MavenProjectVersionGetter.getCurrentProjectVersion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.logging.Logger;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;

import com.exasol.mavenpluginintegrationtesting.MavenIntegrationTestEnvironment;

class TraceMojoVerifierTest
{
    private static final Logger LOG = Logger.getLogger(TraceMojoVerifierTest.class.getName());
    private static Path BASE_TEST_DIR = Paths.get("src/test/resources").toAbsolutePath();
    private static final String CURRENT_PLUGIN_VERSION = getCurrentProjectVersion();
    private static final String OFT_GOAL = "org.itsallcode:openfasttrace-maven-plugin:" + CURRENT_PLUGIN_VERSION
            + ":trace";
    private static final Path CURRENT_PLUGIN_JAR = Path
            .of("target", "openfasttrace-maven-plugin-" + CURRENT_PLUGIN_VERSION + ".jar")
            .toAbsolutePath();
    private static final Path CURRENT_PLUGIN_POM = Path.of("pom.xml").toAbsolutePath();
    private static final Path PROJECT_WITH_MULTIPLE_LANGUAGES = BASE_TEST_DIR
            .resolve("project-with-multiple-languages");
    private static final Path PROJECT_WITH_SUB_MODULE = BASE_TEST_DIR.resolve("project-with-sub-module");
    private static final Path PROJECT_WITH_NESTED_SUB_MODULE = BASE_TEST_DIR.resolve("project-with-nested-sub-module");
    private static final Path EMPTY_PROJECT = BASE_TEST_DIR.resolve("empty-project");
    private static final Path SIMPLE_PROJECT = BASE_TEST_DIR.resolve("simple-project");
    private static final Path PROJECT_WITH_PLUGINS = BASE_TEST_DIR.resolve("project-with-plugins");
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
        mvnITEnv.installPlugin(CURRENT_PLUGIN_JAR.toFile(), CURRENT_PLUGIN_POM.toFile());
    }

    @BeforeEach
    void logTestName(final TestInfo testInfo)
    {
        LOG.info(() -> "Running test " + testInfo.getDisplayName() + "...");
    }

    @Test
    void testTracingWithMultipleLanguages() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_MULTIPLE_LANGUAGES);
        verifier.setCliOptions(List.of("-pl ."));
        verifier.executeGoals(List.of("generate-sources", "generate-test-sources", OFT_GOAL));
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_MULTIPLE_LANGUAGES.resolve("target/tracing-report.txt")))
                .isEqualTo("ok - 8 total\n");
    }

    @Test
    void testTracingWithSubModule() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_SUB_MODULE);
        verifier.setCliOptions(List.of("-pl ."));
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_SUB_MODULE.resolve("target/tracing-report.txt")))
                .isEqualTo("ok - 3 total\n");
    }

    @Test
    void testTracingWithNestedSubModule() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_NESTED_SUB_MODULE);
        verifier.setCliOptions(List.of("-pl .")); // only check root project
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_NESTED_SUB_MODULE.resolve("target/tracing-report.txt")))
                .isEqualTo("ok - 3 total\n");
    }

    @Test
    void testTracingParallel() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_NESTED_SUB_MODULE);
        verifier.setCliOptions(List.of("-pl .", "-T 2"));
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_NESTED_SUB_MODULE.resolve("target/tracing-report.txt")))
                .isEqualTo("ok - 3 total\n");
    }

    static void assertFileContent(final Path file, final String... lines) throws IOException
    {
        final String fileContent = fileContent(file);
        for (final String line : lines)
        {
            MatcherAssert.assertThat(fileContent, containsString(line));
        }
    }

    static String fileContent(final Path file) throws IOException
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
    void testTracingWithPlugins() throws Exception
    {
        installAsciiDocImporter();
        runTracingMojo(PROJECT_WITH_PLUGINS);

        assertThat(fileContent(PROJECT_WITH_PLUGINS.resolve("target/reports/tracing-report.txt")))
                .isEqualTo("ok - 6 total\n");
    }

    private static void installAsciiDocImporter()
    {
        final Path pluginPath = Path.of("../openfasttrace-asciidoc-plugin").toAbsolutePath();
        final Path pluginJar = pluginPath.resolve("target/openfasttrace-asciidoc-plugin-0.1.0.jar");
        assertTrue(Files.exists(pluginJar), "AsciiDoc plugin exists at " + pluginJar);
        mvnITEnv.installPlugin(pluginJar.toFile(),
                pluginPath.resolve("pom.xml").toFile());
    }

    @Test
    void testTracingFindsDefects() throws Exception
    {
        runTracingMojo(TRACING_DEFECTS);

        assertThat(fileContent(TRACING_DEFECTS.resolve("target/tracing-report.txt")))
                .contains("not ok - 2 total, 1 defect");
    }

    @Test
    void testTracingSkipped() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(TRACING_DEFECTS);
        verifier.addCliOption("-Dopenfasttrace.skip=true");
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();

        assertAll(
                () -> verifier.verifyTextInLog(
                        "Skipping OFT tracing because property 'openfasttrace.skip' was set to 'true'."),
                () -> assertThat(TRACING_DEFECTS.resolve("target/tracing-report.txt")).doesNotExist());
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
                .contains("<span class=\"green\">&check;</span> 3 total") //
                .contains("<details>");
    }

    @Test
    void testHtmlReportWithExpandedDetails() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(HTML_REPORT_PROJECT);
        verifier.addCliOption("-DdetailsSectionDisplay=EXPAND");
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();

        assertThat(fileContent(HTML_REPORT_PROJECT.resolve("target/tracing-report.html")))
                .contains("<details open>");
    }

    private static void runTracingMojo(final Path projectDir) throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(projectDir);
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();
    }
}
