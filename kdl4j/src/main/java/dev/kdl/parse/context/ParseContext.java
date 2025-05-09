package dev.kdl.parse.context;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

public record ParseContext(@Nullable String filename, @Nonnull List<SourceLine> sourceLines, @Nonnull Span span) {
}
