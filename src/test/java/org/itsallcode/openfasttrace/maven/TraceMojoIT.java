package org.itsallcode.openfasttrace.maven;

import static com.exasol.mavenprojectversiongetter.MavenProjectVersionGetter.getCurrentProjectVersion;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.io.FileMatchers.anExistingFile;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.logging.Logger;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.exasol.mavenpluginintegrationtesting.MavenIntegrationTestEnvironment;

class TraceMojoIT
{
    private static final Logger LOG = Logger.getLogger(TraceMojoIT.class.getName());
    private static final Path BASE_TEST_DIR = Paths.get("src/test/resources").toAbsolutePath();
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
    private static final Path PROJECT_WITH_TAGS = BASE_TEST_DIR.resolve("project-with-tags");
    private static final Path TRACING_DEFECTS = BASE_TEST_DIR.resolve("project-with-tracing-defects");
    private static final Path TRACING_DEFECTS_FAIL_BUILD = BASE_TEST_DIR
            .resolve("project-with-tracing-defects-fail-build");
    private static final Path HTML_REPORT_PROJECT = BASE_TEST_DIR
            .resolve("html-report");
    public static final Path PARTIAL_ARTIFACT_COVERAGE_PROJECT = BASE_TEST_DIR
            .resolve("project-with-partial-artifact-coverage");
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
        assertThat(fileContent(PROJECT_WITH_MULTIPLE_LANGUAGES.resolve("target/tracing-report.txt")),
                equalTo("ok - 8 total\n"));
    }

    @Test
    void testTracingWithSubModule() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_SUB_MODULE);
        verifier.setCliOptions(List.of("-pl ."));
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_SUB_MODULE.resolve("target/tracing-report.txt")),
                equalTo("ok - 3 total\n"));
    }

    @Test
    void testTracingWithNestedSubModule() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_NESTED_SUB_MODULE);
        verifier.setCliOptions(List.of("-pl .")); // only check root project
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_NESTED_SUB_MODULE.resolve("target/tracing-report.txt")),
                equalTo("ok - 3 total\n"));
    }

    @Test
    void testTracingParallel() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_NESTED_SUB_MODULE);
        verifier.setCliOptions(List.of("-pl .", "-T 2"));
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_NESTED_SUB_MODULE.resolve("target/tracing-report.txt")),
                equalTo("ok - 3 total\n"));
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

        assertThat(fileContent(SIMPLE_PROJECT.resolve("target/reports/tracing-report.txt")),
                equalTo("ok - 3 total\n"));
    }

    @Test
    void testTracingWithPlugins() throws Exception
    {
        runTracingMojo(PROJECT_WITH_PLUGINS);

        assertThat(fileContent(PROJECT_WITH_PLUGINS.resolve("target/reports/tracing-report.txt")),
                equalTo("ok - 6 total\n"));
    }

    @Test
    void testTracingFindsDefects() throws Exception
    {
        runTracingMojo(TRACING_DEFECTS);

        assertThat(fileContent(TRACING_DEFECTS.resolve("target/tracing-report.txt")),
                containsString("not ok - 2 total, 1 defect"));
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
                () -> assertThat(TRACING_DEFECTS.resolve("target/tracing-report.txt").toFile(),
                        not(anExistingFile())));
    }

    @Test
    void testTracingFindsDefectsFailBuild()
    {
        final VerificationException exception = assertThrows(VerificationException.class,
                () -> runTracingMojo(TRACING_DEFECTS_FAIL_BUILD));
        assertAll(() -> assertThat(exception.getMessage(), containsString("Tracing found 1 defects out of 2 items")),
                () -> assertThat(fileContent(TRACING_DEFECTS_FAIL_BUILD.resolve("target/tracing-report.txt")),
                        containsString("not ok - 2 total, 1 defect")));
    }

    @Test
    void testHtmlReport() throws Exception
    {
        runTracingMojo(HTML_REPORT_PROJECT);

        final String content = fileContent(HTML_REPORT_PROJECT.resolve("target/tracing-report.html"));
        assertAll(() -> assertThat(content, containsString("<span class=\"green\">&check;</span> 3 total")),
                () -> assertThat(content, containsString("<details>")));
    }

    @Test
    void testHtmlReportWithExpandedDetails() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(HTML_REPORT_PROJECT);
        verifier.addCliOption("-DdetailsSectionDisplay=EXPAND");
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();

        assertThat(fileContent(HTML_REPORT_PROJECT.resolve("target/tracing-report.html")),
                containsString("<details open>"));
    }

    @Test
    void testTracingSelectedArtifactTypes() throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PARTIAL_ARTIFACT_COVERAGE_PROJECT);
        verifier.addCliOption("-DartifactTypes=one,two");
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PARTIAL_ARTIFACT_COVERAGE_PROJECT.resolve("target/tracing-report.txt")),
                equalTo("ok - 2 total\n"));
    }

    @ParameterizedTest(name = "wanted tags {0} finds {1} items")
    @CsvSource(delimiter = ';', nullValues = "NULL", value =
    { "NULL; 3", "tagA; 1", "tagB; 1", "tagA,tagB; 2", "tagA,tagB,_; 3", "tagC; 0", "_,tagA; 2",
            // This should find 1 item but finds 3 due to a bug in OFT:
            // https://github.com/itsallcode/openfasttrace/issues/432
            "_; 3" })
    void testTracingSelectedTags(final String tags, final int expectedItemCount) throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(PROJECT_WITH_TAGS);
        verifier.addCliOption("-DfailBuild=false");
        if (tags != null)
        {
            verifier.addCliOption("-Dtags=" + tags);
        }
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();
        final String expectedResult;
        if (expectedItemCount > 0)
        {
            expectedResult = "not ok - %1$d total, %1$d defect".formatted(expectedItemCount);
        }
        else
        {
            expectedResult = "ok - 0 total";
        }
        assertThat(fileContent(PROJECT_WITH_TAGS.resolve("target/tracing-report.txt")),
                containsString(expectedResult));
    }

    private static void runTracingMojo(final Path projectDir) throws Exception
    {
        final Verifier verifier = mvnITEnv.getVerifier(projectDir);
        verifier.executeGoal(OFT_GOAL);
        verifier.verifyErrorFreeLog();
    }
}
