package dev.kdl.parse.context;

import jakarta.annotation.Nonnull;

import java.util.List;

public record ParseContext(@Nonnull String filename, @Nonnull List<SourceLine> sourceLines, @Nonnull Span span) {
}
