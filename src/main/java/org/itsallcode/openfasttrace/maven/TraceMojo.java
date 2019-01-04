package org.itsallcode.openfasttrace.maven;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.itsallcode.openfasttrace.ImportSettings;
import org.itsallcode.openfasttrace.ImportSettings.Builder;
import org.itsallcode.openfasttrace.Oft;
import org.itsallcode.openfasttrace.core.LinkedSpecificationItem;
import org.itsallcode.openfasttrace.core.OftRunner;
import org.itsallcode.openfasttrace.core.SpecificationItem;
import org.itsallcode.openfasttrace.core.Trace;

/**
 * Trace requirements using OpenFastTrace
 */
@Mojo(name = "trace", defaultPhase = LifecyclePhase.NONE)
public class TraceMojo extends AbstractMojo
{
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Component
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException
    {
        final Oft oft = new OftRunner();
        getLog().info("Importing spec items...");
        final List<SpecificationItem> items = oft.importItems(createImportSettings());
        getLog().info("Imported " + items.size() + " items.");
        final List<LinkedSpecificationItem> linkedItems = oft.link(items);
        final Trace trace = oft.trace(linkedItems);
        final Path outputPath = getOutputPath();
        if (trace.countDefects() > 0)
        {
            getLog().warn("Tracing found " + trace.countDefects() + " out of " + trace.count()
                    + " items");
        }
        else
        {
            getLog().info("Tracing found no defects in " + trace.count() + " items");
        }
        getLog().info("Writing tracing report to " + outputPath);
        oft.reportToPath(trace, outputPath);
    }

    private Path getOutputPath()
    {
        final Path outputPath = outputDirectory.toPath().resolve("tracing-report.txt");
        createDir(outputPath.getParent());
        return outputPath;
    }

    private void createDir(final Path path)
    {
        if (Files.exists(path))
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
        final Builder settings = ImportSettings.builder() //
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
        return Stream
                .concat(project.getCompileSourceRoots().stream(),
                        project.getTestCompileSourceRoots().stream())
                .map(Paths::get) //
                .collect(toList());
    }

    private Optional<Path> getProjectSubPath(String dir)
    {
        final File file = new File(project.getBasedir(), dir);
        return file.exists() ? Optional.of(file.toPath()) : Optional.empty();
    }
}
