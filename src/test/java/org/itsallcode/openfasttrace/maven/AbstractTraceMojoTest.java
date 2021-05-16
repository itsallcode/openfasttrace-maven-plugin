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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class AbstractTraceMojoTest {

    protected static Path BASE_TEST_DIR = Paths.get("src/test/resources").toAbsolutePath();

    protected void assertFileContent(Path file, String... lines) throws IOException
    {
        final String fileContent = fileContent(file);
        for (final String line : lines)
        {
            assertThat(fileContent, containsString(line));
        }
    }

    protected String fileContent(Path file) throws IOException
    {
        assertTrue("File does not exist: " + file, Files.exists(file));
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

}
