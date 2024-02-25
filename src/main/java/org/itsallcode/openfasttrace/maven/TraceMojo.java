package org.itsallcode.openfasttrace.maven;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.*;
import org.itsallcode.openfasttrace.api.ReportSettings;
import org.itsallcode.openfasttrace.api.core.*;
import org.itsallcode.openfasttrace.api.importer.ImportSettings;
import org.itsallcode.openfasttrace.api.report.ReportVerbosity;
import org.itsallcode.openfasttrace.core.Oft;
import org.itsallcode.openfasttrace.core.OftRunner;

/**
 * Trace requirements using OpenFastTrace
 */
@Mojo(name = "trace", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class TraceMojo extends AbstractMojo
{
    /**
     * Location of the directory where the reports are generated.
     * <p>
     * Default: <code>${project.build.directory}</code>
     */
    @Parameter(property = "outputDirectory", defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * Let build fail when tracing fails.
     * <p>
     * Default: <code>true</code>
     */
    @Parameter(property = "failBuild", defaultValue = "true", required = true)
    private boolean failBuild;

    /**
     * The report output format, e.g. <code>plain</code> or <code>html</code>.
     * <p>
     * Default: <code>html</code>
     */
    @Parameter(property = "reportOutputFormat", defaultValue = "html", required = true)
    private String reportOutputFormat;

    /**
     * The report verbosity
     * <ul>
     * <li><code>QUIET</code></li>
     * <li><code>MINIMAL</code></li>
     * <li><code>SUMMARY</code></li>
     * <li><code>FAILURES</code></li>
     * <li><code>FAILURE_SUMMARIES</code></li>
     * <li><code>FAILURE_DETAILS</code></li>
     * <li><code>ALL</code></li>
     * </ul>
     * <p>
     * Default: <code>FAILURE_DETAILS</code>
     */
    @Parameter(property = "reportVerbosity", defaultValue = "FAILURE_DETAILS", required = true)
    private ReportVerbosity reportVerbosity;

    /**
     * Show the origin in the tracing report.
     * <p>
     * Default: <code>false</code>
     */
    @Parameter(property = "reportShowOrigin", defaultValue = "false", required = true)
    private boolean reportShowOrigin;

    /**
     * Skip running OFT.
     * <p>
     * Default: <code>false</code>
     */
    @Parameter(property = "openfasttrace.skip", defaultValue = "false", required = false)
    private boolean skip;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Component
    private ProjectBuilder mavenProjectBuilder;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    /**
     * Create a new instance
     */
    public TraceMojo()
    {
        // Added default constructor to fix javadoc warning
    }

    @Override
    public void execute() throws MojoFailureException
    {
        if (skip)
        {
            getLog().warn("Skipping OFT tracing");
            return;
        }
        final Oft oft = new OftRunner();
        getLog().info("Importing spec items...");
        final List<SpecificationItem> items = oft.importItems(createImportSettings());
        getLog().info("Imported " + items.size() + " items.");
        final List<LinkedSpecificationItem> linkedItems = oft.link(items);
        final Trace trace = oft.trace(linkedItems);
        writeTracingReport(oft, trace);
        if (trace.countDefects() == 0)
        {
            getLog().info("Tracing found no defects in " + trace.count() + " items");
            return;
        }
        final String message = "Tracing found " + trace.countDefects() + " defects out of " + trace.count() + " items";
        getLog().warn(message);
        logTracingReport(oft, trace);
        if (failBuild)
        {
            throw new MojoFailureException(message);
        }
    }

    private static void logTracingReport(final Oft oft, final Trace trace)
    {
        final ReportSettings reportSettings = ReportSettings.builder()
                .outputFormat("plain")
                .verbosity(ReportVerbosity.FAILURE_DETAILS)
                .showOrigin(false)
                .build();
        oft.reportToStdOut(trace, reportSettings);
    }

    private void writeTracingReport(final Oft oft, final Trace trace)
    {
        final Path outputPath = getOutputPath();
        final ReportSettings reportSettings = ReportSettings.builder()
                .outputFormat(reportOutputFormat)
                .verbosity(reportVerbosity)
                .showOrigin(reportShowOrigin)
                .build();
        getLog().info("Writing tracing report to " + outputPath + " using settings " + formatSettings(reportSettings));
        oft.reportToPath(trace, outputPath, reportSettings);
    }

    private static String formatSettings(final ReportSettings reportSettings)
    {
        return "[output format: " + reportSettings.getOutputFormat()
                + ", verbosity: " + reportSettings.getReportVerbosity()
                + ", show origin: " + reportSettings.showOrigin()
                + ", newline: " + reportSettings.getNewline().name()
                + "]";
    }

    private Path getOutputPath()
    {
        final String reportSuffix = "html".equals(reportOutputFormat) ? "html" : "txt";
        final Path outputPath = outputDirectory.toPath().resolve("tracing-report." + reportSuffix);
        createDir(outputPath.getParent());
        return outputPath;
    }

    private static void createDir(final Path path)
    {
        if (path.toFile().exists())
        {
            return;
        }
        try
        {
            Files.createDirectories(path);
        }
        catch (final IOException e)
        {
            throw new UncheckedIOException("Error creating directory " + path, e);
        }
    }

    private ImportSettings createImportSettings()
    {
        final ImportSettings.Builder settings = ImportSettings.builder() //
                .addInputs(getSourcePaths());
        final Optional<Path> docPath = getProjectSubPath("doc");
        if (docPath.isPresent())
        {
            settings.addInputs(docPath.get());
        }
        return settings.build();
    }

    private List<Path> getSourcePaths()
    {
        return getSourcePathOfProject(this.project);
    }

    private Path getPomOfSubModule(final String moduleName)
    {
        return this.project.getBasedir().toPath().resolve(moduleName).resolve("pom.xml");
    }

    private MavenProject readProject(final Path pomFile)
    {
        try
        {
            final ProjectBuildingResult build = this.mavenProjectBuilder.build(pomFile.toFile(),
                    this.session.getProjectBuildingRequest());
            return build.getProject();
        }
        catch (final ProjectBuildingException exception)
        {
            throw new IllegalStateException(
                    "Failed to read sub module \"" + pomFile + "\".", exception);
        }
    }

    private List<Path> getSourcePathOfProject(final MavenProject mavenProject)
    {
        final Stream<Path> sourcePathsOfSubModules = mavenProject.getModules().stream()
                .map(moduleName -> readProject(getPomOfSubModule(moduleName)))
                .flatMap(eachProject -> this.getSourcePathOfProject(eachProject).stream());
        final Stream<Path> thisProjectsSourcePaths = Stream
                .concat(mavenProject.getCompileSourceRoots().stream(),
                        mavenProject.getTestCompileSourceRoots().stream())
                .map(Paths::get);
        return Stream.concat(sourcePathsOfSubModules, thisProjectsSourcePaths).collect(Collectors.toList());
    }

    private Optional<Path> getProjectSubPath(final String dir)
    {
        final File file = new File(project.getBasedir(), dir);
        return file.exists() ? Optional.of(file.toPath()) : Optional.empty();
    }
}
