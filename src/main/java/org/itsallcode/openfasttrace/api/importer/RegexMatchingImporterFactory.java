package org.itsallcode.openfasttrace.api.importer;

import java.util.Collection;

/**
 * Compatibility shim for RegexMatchingImporterFactory which was renamed to
 * AbstractRegexMatchingImporterFactory in OpenFastTrace 4.5.0.
 * <p>
 * Shim can be removed when the following issue is fixed:
 * <a href="https://github.com/itsallcode/openfasttrace-asciidoc-plugin/issues/27>
 * itsallcode/openffasttrace-asciidoc-plugin # 27
 * </a>
 * </p>
 * 
 * @deprecated use {@link AbstractRegexMatchingImporterFactory} instead.
 */
@Deprecated(since = "4.5.0")
@SuppressWarnings("java:S118") // Shim class. Ignore name convention.
public abstract class RegexMatchingImporterFactory extends AbstractRegexMatchingImporterFactory
{
    protected RegexMatchingImporterFactory(final String... extensions)
    {
        super(extensions);
    }

    protected RegexMatchingImporterFactory(final Collection<String> extensions)
    {
        super(extensions);
    }
}
