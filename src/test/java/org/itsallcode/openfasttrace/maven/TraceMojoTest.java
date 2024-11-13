package org.itsallcode.openfasttrace.maven;

import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.itsallcode.matcher.auto.AutoMatcher;
import org.itsallcode.openfasttrace.api.FilterSettings;
import org.itsallcode.openfasttrace.api.importer.ImportSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TraceMojoTest
{
    @Mock
    ProjectBuilder mavenProjectBuilderMock;
    @Mock
    MavenProject projectMock;
    @TempDir
    Path baseDir;

    TraceMojo testee;

    @BeforeEach
    void setup()
    {
        this.testee = createMojo();
    }

    TraceMojo createMojo()
    {
        when(projectMock.getBasedir()).thenReturn(baseDir.toFile());
        return new TraceMojo(mavenProjectBuilderMock, projectMock);
    }

    @Test
    void createDefaultImportSettings()
    {
        testee.artifactTypes = null;
        assertThat(testee.createImportSettings(), AutoMatcher.equalTo(ImportSettings.builder().build()));
    }

    @Test
    void createImportSettingsAddsSourceRootPaths() throws IOException
    {
        final Path compileSrcPath = baseDir.resolve("src");
        final Path testCompileSrcPath = baseDir.resolve("test");
        Files.createDirectories(compileSrcPath);
        Files.createDirectories(testCompileSrcPath);
        when(projectMock.getCompileSourceRoots()).thenReturn(List.of(compileSrcPath.toString()));
        when(projectMock.getTestCompileSourceRoots()).thenReturn(List.of(testCompileSrcPath.toString()));

        assertImportSettings(ImportSettings.builder().addInputs(compileSrcPath, testCompileSrcPath));
    }

    @Test
    void createImportSettingsAddsResourcesDir() throws IOException
    {
        final Path resourcesPath = baseDir.resolve("res");
        Files.createDirectories(resourcesPath);
        when(projectMock.getResources()).thenReturn(List.of(resource(resourcesPath)));

        assertImportSettings(ImportSettings.builder().addInputs(resourcesPath));
    }

    private static Resource resource(final Path directory)
    {
        final Resource resource = new Resource();
        resource.setDirectory(directory.toString());
        return resource;
    }

    @Test
    void createImportSettingsAddsDocPath() throws IOException
    {
        final Path docDir = baseDir.resolve("doc");
        Files.createDirectories(docDir);

        assertImportSettings(ImportSettings.builder().addInputs(docDir));
    }

    @Test
    void createImportSettingsWithNullArtifactTypes()
    {
        testee.artifactTypes = null;
        assertFilterSettings(FilterSettings.builder());
    }

    @Test
    void createImportSettingsWithEmptyArtifactTypes()
    {
        testee.artifactTypes = emptySet();
        assertFilterSettings(FilterSettings.builder());
    }

    @Test
    void createImportSettingsWithArtifactTypes()
    {
        testee.artifactTypes = Set.of("feat", "req");
        assertFilterSettings(FilterSettings.builder().artifactTypes(Set.of("feat", "req")));
    }

    @Test
    void createImportSettingsWithNullTags()
    {
        testee.tags = null;
        assertFilterSettings(FilterSettings.builder());
    }

    @Test
    void createImportSettingsWithEmptyTags()
    {
        testee.tags = emptySet();
        assertFilterSettings(FilterSettings.builder());
    }

    @Test
    void createImportSettingsWithTags()
    {
        testee.tags = Set.of("tag1", "tag2");
        assertFilterSettings(FilterSettings.builder().tags(Set.of("tag1", "tag2")).withoutTags(false));
    }

    @Test
    void createImportSettingsWithWildcardTag()
    {
        testee.tags = Set.of("_", "tag1", "tag2");
        assertFilterSettings(FilterSettings.builder().tags(Set.of("tag1", "tag2")).withoutTags(true));
    }

    @Test
    void createImportSettingsWithOnlyWildcardTag()
    {
        testee.tags = Set.of("_");
        assertFilterSettings(FilterSettings.builder().tags(emptySet()).withoutTags(true));
    }

    private void assertImportSettings(final ImportSettings.Builder importSettingsBuilder)
    {
        assertThat(testee.createImportSettings(), AutoMatcher.equalTo(importSettingsBuilder.build()));
    }

    private void assertFilterSettings(final FilterSettings.Builder filterSettingsBuilder)
    {
        assertImportSettings(ImportSettings.builder().filter(filterSettingsBuilder.build()));
    }
}
