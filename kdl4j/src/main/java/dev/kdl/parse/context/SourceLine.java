package dev.kdl.parse.context;

import jakarta.annotation.Nonnull;

public record SourceLine(int lineNumber, @Nonnull String line) {
}
