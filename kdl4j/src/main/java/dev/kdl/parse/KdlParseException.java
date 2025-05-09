package dev.kdl.parse;

import dev.kdl.parse.context.ParseContext;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class KdlParseException extends Exception {
	public KdlParseException(
		@Nonnull String errorMessage,
		@Nullable ParseContext context,
		@Nullable String label,
		@Nullable String help
	) {
		super(computeMessage(errorMessage, context));
		this.errorMessage = errorMessage;
		this.context = context;
		this.label = label;
		this.help = help;
	}

	public KdlParseException(@Nonnull String errorMessage) {
		this(errorMessage, null, null, null);
	}

	public KdlParseException(
		@Nonnull String errorMessage,
		@Nullable ParseContext context
	) {
		this(errorMessage, context, null, null);
	}

	public KdlParseException(
		@Nonnull String errorMessage,
		@Nullable String help
	) {
		this(errorMessage, null, null, help);
	}

	public KdlParseException(
		@Nonnull String errorMessage,
		@Nullable ParseContext context,
		@Nullable String label
	) {
		this(errorMessage, context, label, null);
	}

	@Nonnull
	public String getErrorMessage() {
		return errorMessage;
	}

	@Nullable
	public String getLabel() {
		return label;
	}

	@Nullable
	public ParseContext getContext() {
		return context;
	}

	@Nullable
	public String getHelp() {
		return help;
	}

	@Nonnull
	private final String errorMessage;
	@Nullable
	private final ParseContext context;
	@Nullable
	private final String label;
	@Nullable
	private final String help;

	private static String computeMessage(@Nonnull String errorMessage, @Nullable ParseContext context) {
		return context == null
			? errorMessage
			: errorMessage + " at " + context.span().start().line() + ':' + context.span().start().column();
	}
}
