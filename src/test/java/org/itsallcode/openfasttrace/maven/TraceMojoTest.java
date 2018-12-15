package org.itsallcode.openfasttrace.maven;

import java.io.File;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TraceMojoTest
{
    @Rule
    public MojoRule rule = new MojoRule();

    @Before
    public void setUp()
    {
    }

    @Test
    public void testSomething() throws Exception
    {
        rule.executeMojo(new File("src/test/resources/empty-project"), "trace");
    }
}
