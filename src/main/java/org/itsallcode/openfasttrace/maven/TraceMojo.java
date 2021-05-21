package org.itsallcode.openfasttrace.maven;

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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.itsallcode.openfasttrace.api.ReportSettings;
import org.itsallcode.openfasttrace.api.core.LinkedSpecificationItem;
import org.itsallcode.openfasttrace.api.core.SpecificationItem;
import org.itsallcode.openfasttrace.api.core.Trace;
import org.itsallcode.openfasttrace.api.importer.ImportSettings;
import org.itsallcode.openfasttrace.api.report.ReportVerbosity;
import org.itsallcode.openfasttrace.core.Oft;
import org.itsallcode.openfasttrace.core.OftRunner;

/**
 * Trace requirements using OpenFastTrace
 */
@Mojo(name = "trace", defaultPhase = LifecyclePhase.VERIFY)
public class TraceMojo extends AbstractMojo
{
    /**
     * Location of the directory where the reports are generated.
     * <p>
     * Default: <code>${project.build.directory}</code>
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDirectory", required = true)
    private File outputDirectory;

    /**
     * Let build fail when tracing fails.
     * <p>
     * Default: <code>true</code>
     */
    @Parameter(defaultValue = "true", property = "failBuild", required = true)
    private boolean failBuild;

    /**
     * The report output format, e.g. <code>plain</code> or <code>html</code>.
     * <p>
     * Default: <code>html</code>
     */
    @Parameter(defaultValue = "html", property = "reportOutputFormat", required = true)
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
    @Parameter(defaultValue = "FAILURE_DETAILS", property = "reportVerbosity", required = true)
    private ReportVerbosity reportVerbosity;

    /**
     * Show the origin in the tracing report.
     * <p>
     * Default: <code>false</code>
     */
    @Parameter(defaultValue = "false", property = "reportShowOrigin", required = true)
    private boolean reportShowOrigin;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoFailureException
    {
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
        final String message = "Tracing found " + trace.countDefects() + " out of " + trace.count()
                + " items";
        getLog().warn(message);
        if (failBuild)
        {
            throw new MojoFailureException(message);
        }
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

    private String formatSettings(ReportSettings reportSettings)
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

    private void createDir(final Path path)
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
