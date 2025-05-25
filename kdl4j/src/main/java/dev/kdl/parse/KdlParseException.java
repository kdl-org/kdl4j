package dev.kdl.parse;

import dev.kdl.parse.context.ParseContext;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * An exception thrown when a KDL document is invalid. The error can be printed for users using a {@link Reporter}.
 */
public class KdlParseException extends Exception {

	/**
	 * Creates a new {@link KdlParseException}.
	 *
	 * @param errorMessage the main error message
	 * @param context      a context of the error
	 * @param label        a label for the highlighted section of the context
	 * @param help         a help message for the user
	 */
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

	/**
	 * Creates a new {@link KdlParseException}.
	 *
	 * @param errorMessage the main error message
	 */
	public KdlParseException(@Nonnull String errorMessage) {
		this(errorMessage, null, null, null);
	}

	/**
	 * Creates a new {@link KdlParseException}.
	 *
	 * @param errorMessage the main error message
	 * @param context      the context of the error
	 */
	public KdlParseException(
		@Nonnull String errorMessage,
		@Nullable ParseContext context
	) {
		this(errorMessage, context, null, null);
	}

	/**
	 * Creates a new {@link KdlParseException}.
	 *
	 * @param errorMessage the main error message
	 * @param help         a help message for the user
	 */
	public KdlParseException(
		@Nonnull String errorMessage,
		@Nullable String help
	) {
		this(errorMessage, null, null, help);
	}

	/**
	 * Creates a new {@link KdlParseException}.
	 *
	 * @param errorMessage the main error message
	 * @param context      the context of the error
	 * @param label        the label for the highlighted section of the context
	 */
	public KdlParseException(
		@Nonnull String errorMessage,
		@Nullable ParseContext context,
		@Nullable String label
	) {
		this(errorMessage, context, label, null);
	}

	/**
	 * @return the main error message
	 */
	@Nonnull
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @return the label for the highlighted section of the context
	 */
	@Nullable
	public String getLabel() {
		return label;
	}

	/**
	 * @return the context of the error
	 */
	@Nullable
	public ParseContext getContext() {
		return context;
	}

	/**
	 * @return the help message for the user
	 */
	@Nullable
	public String getHelp() {
		return help;
	}

	/**
	 * The main error message.
	 */
	@Nonnull
	private final String errorMessage;
	/**
	 * A context of the error
	 */
	@Nullable
	private final ParseContext context;
	/**
	 * A label for the highlighted section of the context
	 */
	@Nullable
	private final String label;
	/**
	 * A help message for the user
	 */
	@Nullable
	private final String help;

	private static String computeMessage(@Nonnull String errorMessage, @Nullable ParseContext context) {
		return context == null
			? errorMessage
			: errorMessage + " at " + context.span().start().line() + ':' + context.span().start().column();
	}
}
