package org.itsallcode.openfasttrace.maven;

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

    @Test
    void createDefaultImportSettings()
    {
        final TraceMojo testee = testee();
        testee.artifactTypes = null;
        assertThat(testee.createImportSettings(), AutoMatcher.equalTo(ImportSettings.builder().build()));
    }

    @Test
    void createImportSettingsAddsSourceRootPaths() throws IOException
    {
        final TraceMojo testee = testee();
        final Path compileSrcPath = baseDir.resolve("src");
        final Path testCompileSrcPath = baseDir.resolve("test");
        Files.createDirectories(compileSrcPath);
        Files.createDirectories(testCompileSrcPath);
        when(projectMock.getCompileSourceRoots()).thenReturn(List.of(compileSrcPath.toString()));
        when(projectMock.getTestCompileSourceRoots()).thenReturn(List.of(testCompileSrcPath.toString()));
        assertThat(testee.createImportSettings(),
                AutoMatcher.equalTo(ImportSettings.builder().addInputs(compileSrcPath, testCompileSrcPath).build()));
    }

    @Test
    void createImportSettingsAddsResourcesDir() throws IOException
    {
        final TraceMojo testee = testee();
        final Path resourcesPath = baseDir.resolve("res");
        Files.createDirectories(resourcesPath);
        when(projectMock.getResources()).thenReturn(List.of(resource(resourcesPath)));
        assertThat(testee.createImportSettings(),
                AutoMatcher.equalTo(ImportSettings.builder().addInputs(resourcesPath).build()));
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
        final TraceMojo testee = testee();
        final Path docDir = baseDir.resolve("doc");
        Files.createDirectories(docDir);
        assertThat(testee.createImportSettings(),
                AutoMatcher.equalTo(ImportSettings.builder().addInputs(docDir).build()));
    }

    @Test
    void createImportSettingsWithArtifactTypes()
    {
        final TraceMojo testee = testee();
        testee.artifactTypes = Set.of("feat", "req");
        assertThat(testee.createImportSettings(), AutoMatcher.equalTo(
                ImportSettings.builder().filter(FilterSettings.builder().artifactTypes(Set.of("feat", "req")).build())
                        .build()));
    }

    TraceMojo testee()
    {
        when(projectMock.getBasedir()).thenReturn(baseDir.toFile());
        return new TraceMojo(mavenProjectBuilderMock, projectMock);
    }
}
