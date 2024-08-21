package org.itsallcode.openfasttrace.maven;

import static java.util.stream.Collectors.toList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.*;
import org.itsallcode.openfasttrace.api.DetailsSectionDisplay;
import org.itsallcode.openfasttrace.api.FilterSettings;
import org.itsallcode.openfasttrace.api.ReportSettings;
import org.itsallcode.openfasttrace.api.core.*;
import org.itsallcode.openfasttrace.api.importer.ImportSettings;
import org.itsallcode.openfasttrace.api.report.ReportVerbosity;
import org.itsallcode.openfasttrace.core.Oft;
import org.itsallcode.openfasttrace.core.OftRunner;

/**
 * Trace requirements using
 * <a href="https://github.com/itsallcode/openfasttrace">OpenFastTrace</a>.
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
     * Default: {@code true}
     */
    @Parameter(property = "failBuild", defaultValue = "true", required = true)
    private boolean failBuild;

    /**
     * The report output format.
     * <ul>
     * <li>{@code html}: HTML format (default)</li>
     * <li>{@code plain}: Plain text format</li>
     * </ul>
     */
    @Parameter(property = "reportOutputFormat", defaultValue = "html", required = true)
    private String reportOutputFormat;

    /**
     * The report verbosity.
     * <ul>
     * <li>{@code QUIET}</li>
     * <li>{@code MINIMAL}</li>
     * <li>{@code SUMMARY}</li>
     * <li>{@code FAILURES}</li>
     * <li>{@code FAILURE_SUMMARIES}</li>
     * <li>{@code FAILURE_DETAILS} (default)</li>
     * <li>{@code ALL}</li>
     * </ul>
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
     * Determines if the details sections for specification items in the HTML
     * report are hidden or visible.
     * <ul>
     * <li>{@code COLLAPSE}: hide details sections (default)</li>
     * <li>{@code EXPAND}: show details section</li>
     * </ul>
     */
    @Parameter(property = "detailsSectionDisplay", defaultValue = "COLLAPSE", required = true)
    private DetailsSectionDisplay detailsSectionDisplay;

    /**
     * Determines which artifact types should be imported.
     * <ul>
     * <li>If the artifactTypes set is null, no filtering based on artifact type
     * will be applied.</li>
     * <li>If the artifactTypes set is not null, only artifacts with types that
     * match the specified types will be imported.</li>
     */
    @Parameter(property = "artifactTypes")
    private Set<String> artifactTypes;
    
    /**
     * Skip running OFT.
     * <p>
     * Default: <code>false</code>
     */
    @Parameter(property = "openfasttrace.skip", defaultValue = "false")
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
            getLog().warn("Skipping OFT tracing because property 'openfasttrace.skip' was set to 'true'.");
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
                .detailsSectionDisplay(detailsSectionDisplay)
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
                + ", detailsSectionDisplay: " + reportSettings.getDetailsSectionDisplay().name()
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
        catch (final IOException exception)
        {
            throw new UncheckedIOException("Error creating directory '" + path + "': " + exception.getMessage(),
                    exception);
        }
    }

    private ImportSettings createImportSettings()
    {
        final List<Path> sourcePaths = getSourcePaths();
        logSourcePaths(sourcePaths);
        final ImportSettings.Builder settings = ImportSettings.builder()
                .addInputs(sourcePaths);
        final Optional<Path> docPath = getProjectSubPath("doc");
        if (docPath.isPresent())
        {
            getLog().info("Tracing doc directory " + docPath.get());
            settings.addInputs(docPath.get());
        }
        if (artifactTypes != null)
        {
            FilterSettings filterSettings = FilterSettings.builder()
                    .artifactTypes(artifactTypes)
                    .build();
            settings.filter(filterSettings);
        }
        return settings.build();
    }

    private void logSourcePaths(final List<Path> sourcePaths)
    {
        if (!getLog().isInfoEnabled())
        {
            return;
        }
        final Path baseDir = project.getBasedir().toPath();
        final List<Path> relativePaths = sourcePaths.stream().map(baseDir::relativize).collect(toList());
        getLog().info(
                "Tracing " + sourcePaths.size() + " sub-directories of base dir " + baseDir + ": "
                        + relativePaths);
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
        final List<String> compileSourceRoots = mavenProject.getCompileSourceRoots();
        final List<String> testCompileSourceRoots = mavenProject.getTestCompileSourceRoots();
        final List<String> resourceDirs = mavenProject.getResources().stream().map(Resource::getDirectory)
                .collect(toList());
        final List<String> testResourceDirs = mavenProject.getTestResources().stream().map(Resource::getDirectory)
                .collect(toList());
        final Stream<Path> sourcePaths = Stream
                .of(compileSourceRoots, resourceDirs, testCompileSourceRoots, testResourceDirs)
                .flatMap(List::stream)
                .map(Path::of)
                .filter(Files::exists);
        return Stream.concat(sourcePathsOfSubModules, sourcePaths).collect(toList());
    }

    private Optional<Path> getProjectSubPath(final String dir)
    {
        final File file = new File(project.getBasedir(), dir);
        return file.exists() ? Optional.of(file.toPath()) : Optional.empty();
    }
}
