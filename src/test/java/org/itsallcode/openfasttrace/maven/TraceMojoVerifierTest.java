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

import java.io.File;
import java.nio.file.Path;

import com.exasol.mavenpluginintegrationtesting.MavenIntegrationTestEnvironment;
import org.apache.maven.it.Verifier;
import org.junit.Test;
import org.junit.BeforeClass;

import static org.assertj.core.api.Assertions.assertThat;
import static com.exasol.mavenprojectversiongetter.MavenProjectVersionGetter.getCurrentProjectVersion;

public class TraceMojoVerifierTest extends AbstractTraceMojoTest
{
    private static final String CURRENT_PLUGIN_VERSION = getCurrentProjectVersion();
    private static final File CURRENT_PLUGIN_JAR = Path
            .of("target", "openfasttrace-maven-plugin-" + CURRENT_PLUGIN_VERSION + ".jar")
            .toFile();
    private static final File CURRENT_PLUGIN_POM = Path.of("pom.xml").toFile();
    private static Path PROJECT_WITH_MULTIPLE_LANGUAGES = BASE_TEST_DIR.resolve("project-with-multiple-languages");

    @BeforeClass
    public static void setup()
    {
        final MavenIntegrationTestEnvironment mvnITEnv = new MavenIntegrationTestEnvironment();
        mvnITEnv.installPlugin(CURRENT_PLUGIN_JAR, CURRENT_PLUGIN_POM);
    }

    @Test
    public void testTracingWithMultipleLanguages() throws Exception
    {

        final Verifier verifier = new Verifier(PROJECT_WITH_MULTIPLE_LANGUAGES.toAbsolutePath().toString());
        verifier.executeGoal("verify");
        verifier.verifyErrorFreeLog();
        assertThat(fileContent(PROJECT_WITH_MULTIPLE_LANGUAGES.resolve("target/tracing-report.txt")))
                .isEqualTo("ok - 6 total\n");
    }

}
