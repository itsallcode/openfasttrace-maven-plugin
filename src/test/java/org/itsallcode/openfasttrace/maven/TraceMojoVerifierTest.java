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
import static org.itsallcode.openfasttrace.maven.TestHelper.BASE_TEST_DIR;
import static org.itsallcode.openfasttrace.maven.TestHelper.fileContent;

import java.io.File;
import java.nio.file.Path;

import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.exasol.mavenpluginintegrationtesting.MavenIntegrationTestEnvironment;

class TraceMojoVerifierTest
{
    private static final String CURRENT_PLUGIN_VERSION = getCurrentProjectVersion();
    private static final File CURRENT_PLUGIN_JAR = Path
            .of("target", "openfasttrace-maven-plugin-" + CURRENT_PLUGIN_VERSION + ".jar")
            .toFile();
    private static final File CURRENT_PLUGIN_POM = Path.of("pom.xml").toFile();
    private static final Path PROJECT_WITH_MULTIPLE_LANGUAGES = BASE_TEST_DIR
            .resolve("project-with-multiple-languages");
    private static final Path PROJECT_WITH_SUB_MODULE = BASE_TEST_DIR.resolve("project-with-sub-module");
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
        System.out.println(verifier.getLogFileName());
        assertThat(fileContent(PROJECT_WITH_SUB_MODULE.resolve("target/tracing-report.txt")))
                .isEqualTo("ok - 3 total\n");
    }
}
