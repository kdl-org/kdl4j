package dev.kdl.parse.context;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * A context for a parse.
 *
 * @param filename    the name of the file being read
 * @param sourceLines a list of {@link SourceLine}
 * @param span        a span for the part of the source that is described by this context
 */
public record ParseContext(@Nullable String filename, @Nonnull List<SourceLine> sourceLines, @Nonnull Span span) {
}
