package org.itsallcode.openfasttrace.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which touches a timestamp file.
 *
 * @deprecated Don't use!
 */
@Deprecated
@Mojo(name = "touch", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MyMojo extends AbstractMojo
{
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    public void execute() throws MojoExecutionException
    {
        final File f = outputDirectory;

        if (!f.exists())
        {
            f.mkdirs();
        }

        final File touch = new File(f, "touch.txt");

        FileWriter w = null;
        try
        {
            w = new FileWriter(touch);

            w.write("touch.txt");
        }
        catch (final IOException e)
        {
            throw new MojoExecutionException("Error creating file " + touch, e);
        }
        finally
        {
            if (w != null)
            {
                try
                {
                    w.close();
                }
                catch (final IOException e)
                {
                    // ignore
                }
            }
        }
    }
}
