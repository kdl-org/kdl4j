package dev.kdl.parse.context;

import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A context for a parse.
 *
 * @param filename    the name of the file being read
 * @param sourceLines a list of {@link SourceLine}
 * @param span        a span for the part of the source that is described by this context
 */
public record ParseContext(@Nullable String filename, List<SourceLine> sourceLines, Span span) {
}
