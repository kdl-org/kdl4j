import org.jspecify.annotations.NullMarked;

/**
 * Module for Kdl4j.
 */
@NullMarked
module dev.kdl4j {
	requires org.jspecify;
	exports dev.kdl;
	exports dev.kdl.parse;
	exports dev.kdl.parse.context;
	exports dev.kdl.print;
}
