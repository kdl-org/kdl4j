package dev.hbeck.kdl.kq

import dev.hbeck.kdl.kq.QueryCharClasses.Companion.isNumericPredicateStart
import dev.hbeck.kdl.objects.KDLBoolean
import dev.hbeck.kdl.objects.KDLDocument
import dev.hbeck.kdl.objects.KDLNull
import dev.hbeck.kdl.objects.KDLObject
import dev.hbeck.kdl.objects.KDLProperty
import dev.hbeck.kdl.objects.KDLValue
import dev.hbeck.kdl.parse.CharClasses.isUnicodeWhitespace
import dev.hbeck.kdl.parse.CharClasses.isValidBareIdChar
import dev.hbeck.kdl.parse.CharClasses.isValidBareIdStart
import dev.hbeck.kdl.parse.CharClasses.isValidNumericStart
import dev.hbeck.kdl.parse.KDLInternalException
import dev.hbeck.kdl.parse.KDLParseContext
import dev.hbeck.kdl.parse.KDLParser.EOF
import dev.hbeck.kdl.parse.KDLParserFacade
import dev.hbeck.kdl.search.Operation
import dev.hbeck.kdl.search.Search
import dev.hbeck.kdl.search.mutation.AddMutation
import dev.hbeck.kdl.search.mutation.Mutation
import dev.hbeck.kdl.search.mutation.SetMutation
import dev.hbeck.kdl.search.mutation.SubtractMutation
import dev.hbeck.kdl.search.predicates.ArgPredicate
import dev.hbeck.kdl.search.predicates.NodeContentPredicate
import dev.hbeck.kdl.search.predicates.PropPredicate
import java.io.StringReader
import java.lang.StringBuilder
import java.util.*
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException
import kotlin.collections.LinkedHashMap

/**
 * Parses Operation objects, using the following grammar:
 *
// * operation :=  search ws* mutation?
// * search := general-search | pathed-search | root
// * root := '{}'
// * general-search := '*' node-predicate?
// * pathed-search := '.' (node-predicate pathed-search?)?
// *
// * node-predicate := identifier-predicate? prop-and-arg-predicates? child-predicate?
// * identifier-predicate := bare-identifier | string | regex
// * prop-and-arg-predicates := '[' (prop-or-arg-predicate* | '*' ']'
// * prop-or-arg-predicate-list := (ws* prop-or-arg-predicate? ws*) | (prop-or-arg-predicate ws prop-or-arg-predicate-list)
// * prop-or-arg-predicate := value | regex | numeric-predicate | property-predicate
// * child-predicate := '{' (general-search | pathed-search) '}'
// * numeric-predicate := ('=' | '<' | '>') number
// * property-predicate := identifier-predicate ( '=' value | '~' regex | numeric-predicate | '=*')
// *
// * regex := escaped-regex | raw-regex
// * escaped-regex := '/' character* '/'
// * raw-regex := 'r' raw-regex-hash
// * raw-regex-hash := ('#' raw-regex-hash '#') | raw-regex-slashes
// * raw-regex-slashes := '/' .* '/'
// *
// * mutation := add-mutation | sub-mutation | set-mutation
// * add-mutation := ws* '+' ws* (add-list | node-children)
// * add-list := (value | prop) (ws+ add-list)?
// * sub-mutation := ws* '-' ws* (sub-list | ('{' '*'? '}') | '[*]' | '.')
// * sub-list := (value | regex | numeric-predicate | property-predicate) (ws+ sub-list)?
// * set-mutation := ws* '=' ws* (identifier-predicate '=' value) | '=' identifier
 */
class OperationParser {
    private val kdlParser = KDLParserFacade()

    fun parse(raw: String): Search = parse(KDLParseContext(StringReader(raw)))

    // * operation :=  search ws* mutation?
    fun parse(context: KDLParseContext): Search {

    }

    // * search := general-search | pathed-search | root
    // * root := '{}'
    // * general-search := '*' node-predicate?
    fun parseSearch(context: KDLParseContext) {

    }

    // * pathed-search := '.' (node-predicate pathed-search?)?
    fun parsePathedSearch(context: KDLParseContext) {

    }

    // node-predicate := identifier-predicate? prop-and-arg-predicates? child-predicate?
    fun parseNodePredicate(context: KDLParseContext) {
        val c = context.read()
        if (c != '.'.toInt()) {
            throw KDLInternalException("Expected '.' at start of node predicate, but found ")
        }
    }


    // * identifier-predicate := bare-indentifier | string | regex
    fun parseIdentifierPredicate(context: KDLParseContext) {

    }

    // * prop-and-arg-predicates := '[' (prop-or-arg-predicate* | '*' ']'
    // * prop-or-arg-predicate-list := (ws* prop-or-arg-predicate? ws*) | (prop-or-arg-predicate ws prop-or-arg-predicate-list)
    fun parsePropAndArgPredicates(context: KDLParseContext) {

    }

    // * child-predicate := '{' (general-search | pathed-search) '}'
    fun parseChildPredicate(context: KDLParseContext) {

    }

    // prop-or-arg-predicate := value | regex | numeric-predicate | property-predicate
    // property-predicate := identifier-predicate ( '=' value | '~' regex | numeric-predicate | '=*')
    fun parsePropOrArgPredicate(context: KDLParseContext): NodeContentPredicate {
        var c = context.peek()
        val strPredicate: Predicate<String> = when {
            c == '"'.toInt() -> {
                val str = kdlParser.parseEscapedString(context)
                Predicate.isEqual(str)
            }
            c == '/'.toInt() -> parseEscapedRegex(context)
            isValidNumericStart(c) -> {
                val number = kdlParser.parseNumber(context)
                return ArgPredicate { value: KDLValue -> value.isNumber && value == number }
            }
            isValidBareIdStart(c) -> {
                if (c == 'r'.toInt()) {
                    context.read();
                    c = context.peek()
                    if (c == '#'.toInt() || c == '/'.toInt() || c == '"'.toInt()) {
                        parseRawRegexOrString(context)
                    } else {
                        // Starts with 'r', can't be a literal
                        Predicate.isEqual(parseBareIdentifierOrLiteral(context))
                    }
                } else {
                    when (val bareIdentifierOrLiteral = parseBareIdentifierOrLiteral(context)) {
                        "null" -> return ArgPredicate { value -> value == KDLNull.INSTANCE }
                        "true" -> return ArgPredicate { value -> value == KDLBoolean.TRUE }
                        "false" -> return ArgPredicate { value -> value == KDLBoolean.FALSE }
                        else -> Predicate.isEqual(bareIdentifierOrLiteral)
                    }
                }

            }
            isNumericPredicateStart(c) -> return ArgPredicate(parseNumericPredicate(context))
            c == EOF -> throw QueryParseException("Reached end of input while attempting to parse ")
            else -> throw QueryParseException("Error parsing arg or prop predicate. Unexpected character: \\u{$c}")
        }

        c = context.peek()
        val valuePredicate = when (c) {
            '='.toInt() -> {
                context.read()
                c = context.peek()
                if (c == '*'.toInt()) {
                    Predicate { true }
                } else {
                    val value = kdlParser.parseValue(context)
                    Predicate { other -> value == other }
                }
            }
            '>'.toInt(), '<'.toInt() -> parseNumericPredicate(context)
            '~'.toInt() -> {
                context.read()
                c = context.peek()
                val regex = when (c) {
                    'r'.toInt() -> parseRawRegexOrString(context)
                    '/'.toInt() -> parseEscapedRegex(context)
                    else -> throw QueryParseException("Expected 'r' or '/' at start of regex, found \\u{$c}")
                }

                Predicate { other -> other.isString && regex.test(other.asString.value) }
            }
            else -> return ArgPredicate { other -> other.isString && strPredicate.test(other.asString.value) }
        }

        return PropPredicate(strPredicate, valuePredicate)
    }

    // numeric-predicate := ('=' | '<' | '>') number
    fun parseNumericPredicate(context: KDLParseContext): Predicate<KDLValue> {
        val c = context.read()
        val number = kdlParser.parseNumber(context).asBigDecimal
        return when (c) {
            '='.toInt() -> Predicate { value: KDLValue -> value.isNumber && number.equals(value.asNumber.get().asBigDecimal) }
            '>'.toInt() -> Predicate { value: KDLValue -> value.isNumber && number.compareTo(value.asNumber.get().asBigDecimal) < 0 }
            '<'.toInt() -> Predicate { value: KDLValue -> value.isNumber && number.compareTo(value.asNumber.get().asBigDecimal) > 0 }
            else -> throw KDLInternalException("Expected '=', '>', or '<' but got \\u{$c}")
        }
    }

    // regex := escaped-regex | raw-regex
    // escaped-regex := '/' character* '/'
    fun parseEscapedRegex(context: KDLParseContext): Predicate<String> {
        var c = context.read()
        if (c != '/'.toInt()) {
            throw KDLInternalException("Expected '/' but got \\u{$c}")
        }

        var inEscape = false
        val stringBuilder = StringBuilder()
        while (true) {
            c = context.read()
            when (c) {
                '/'.toInt() -> {
                    if (inEscape) {
                        stringBuilder.append("/")
                    } else {
                        try {
                            return Pattern.compile(stringBuilder.toString()).asPredicate()
                        } catch (e: PatternSyntaxException) {
                            throw QueryParseException("Couldn't parse regex: " + e.message)
                        }
                    }
                }
                '\\'.toInt() -> {
                    if (inEscape) {
                        stringBuilder.append('\\')
                        inEscape = false;
                    } else {
                        inEscape = true
                    }
                }
                EOF -> throw QueryParseException("Ran out of input while parsing regex")
                else -> stringBuilder.appendCodePoint(c)
            }
        }
    }

    // regex := escaped-regex | raw-regex
    // raw-regex := 'r' raw-regex-hash
    // raw-regex-hash := ('#' raw-regex-hash '#') | raw-regex-slashes
    // raw-regex-slashes := '/' .* '/'
    fun parseRawRegexOrString(context: KDLParseContext): Predicate<String> {
        var c = context.read()
        if (c != 'r'.toInt()) {
            throw KDLInternalException("Raw regex should start with 'r' but found \\u{$c}")
        }

        var hashDepth = 0
        c = context.read()
        while (c == '#'.toInt()) {
            hashDepth++
            c = context.read()
        }

        return when (c) {
            '/'.toInt() -> {
                val regexStr = parseRaw(context, c, hashDepth)
                try {
                    Pattern.compile(regexStr).asPredicate()
                } catch (e: PatternSyntaxException) {
                    throw QueryParseException("Failed to parse regex '$regexStr': ${e.localizedMessage}")
                }
            }
            '"'.toInt() -> Predicate.isEqual(parseRaw(context, c, hashDepth))
            EOF -> throw QueryParseException("Input exhausted when parsing raw regex or string")
            else -> throw QueryParseException("Malformed raw regex or string, expected '/' or '\"' but found '\\u{$c}'")
        }
    }

    fun parseRaw(context: KDLParseContext, terminator: Int, hashDepth: Int): String {
        val stringBuilder = StringBuilder()
        while (true) {
            var c = context.read()
            when (c) {
                terminator -> {
                    val subStringBuilder = StringBuilder()
                    subStringBuilder.append(terminator)
                    var hashDepthHere = 0
                    while (true) {
                        c = context.peek()
                        if (c == '#'.toInt()) {
                            context.read()
                            hashDepthHere++
                            subStringBuilder.append('#')
                        } else {
                            break
                        }
                    }

                    when {
                        hashDepthHere < hashDepth -> stringBuilder.append(subStringBuilder)
                        hashDepthHere == hashDepth -> return stringBuilder.toString()
                        else -> throw QueryParseException("Too many # characters when closing raw regex or string")
                    }
                }
                EOF -> throw QueryParseException("Exhausted input while reading raw regex or string")
                else -> stringBuilder.appendCodePoint(c)
            }
        }
    }

    fun parseBareIdentifierOrLiteral(context: KDLParseContext): String {
        var c = context.read()
        if (!isValidBareIdStart(c)) {
            throw QueryParseException("Illegal character at start of bare identifier or literal")
        } else if (c == EOF) {
            throw KDLInternalException("EOF when a bare identifer or literal expected")
        }

        val stringBuilder = StringBuilder()
        stringBuilder.appendCodePoint(c)
        c = context.peek()
        while (isValidBareIdChar(c) && c != EOF) {
            stringBuilder.appendCodePoint(context.read())
            c = context.peek()
        }
        return stringBuilder.toString()
    }

    // mutation := add-mutation | sub-mutation | set-mutation
    fun parseMutation(context: KDLParseContext): Mutation {
        consumeWhitespace(context)
        return when (val c =context.peek()) {
            '+'.toInt() -> parseAddMutation(context)
            '-'.toInt() -> parseSubtractionMutation(context)
            '='.toInt() -> parseSetMutation(context)
            else -> throw QueryParseException("Unknown operator. Expected one of '+', '-', or '=' but got ${runeToStr(c)}")
        }
    }

    // add-mutation := ws* '+' ws* (add-list | node-children)
    // add-list := (value | prop) (ws+ add-list)?
    fun parseAddMutation(context: KDLParseContext): Mutation {
        var c = context.read()
        if (c != '+'.toInt()) {
            throw KDLInternalException("Expected '+' at start of add mutation, but got '${runeToStr(c)}'")
        }

        val props: Map<String, KDLValue> = mutableMapOf()
        val args: List<KDLValue> = mutableListOf()
        var child: Optional<KDLDocument> = Optional.empty()
        var readChild = false

        consumeWhitespace(context)
        c = context.peek()
        while (c != EOF) {
            when (c) {
                '{'.toInt() -> {
                    if (readChild) {
                        throw QueryParseException("")
                    }

                    readChild = true
                    child = Optional.of(kdlParser.parseDocument(context, false))
                }
                else -> {
                    when(val argOrProp = kdlParser.parseArgOrProp(context)) {
                        is KDLProperty -> props.plus(argOrProp.key to argOrProp.value)
                        is KDLValue -> args.plus(argOrProp)
                        else -> throw QueryParseException("")
                    }
                }
            }

            consumeWhitespace(context)
            c = context.peek()
        }

        return AddMutation(args, props, child)
    }

    // sub-mutation := ws* '-' ws* subtraction sub-list
    // subtraction := (value | regex | numeric-predicate | property-predicate | '{' '*'? '}' |  '[*]')
    // sub-list := (ws+ subtraction sub-list)?
    fun parseSubtractionMutation(context: KDLParseContext): Mutation {
        var c = context.read()
        if (c != '-'.toInt()) {
            throw KDLInternalException("Expected '-' at start of subtract mutation, but got '${runeToStr(c)}'")
        }

        val argPredicates: List<Predicate<KDLValue>> = mutableListOf()
        val propPredicates: List<Predicate<KDLProperty>> = mutableListOf()
        var emptyChild = false
        var deleteChild = false
        var foundChildSubtraction = false
        var foundSplatArgPropDeletion = false

        consumeWhitespace(context)
        c = context.peek()
        while (c != EOF) {
            when (c) {
                '{'.toInt() -> {
                    if (foundChildSubtraction) {
                        throw QueryParseException("Only one child clause is allowed in subtraction mutation");
                    }

                    val (emptyChildRet, deleteChildRet) = parseChildSubtraction(context)
                    emptyChild = emptyChildRet
                    deleteChild = deleteChildRet
                    foundChildSubtraction = true
                }
                '['.toInt() -> {
                    if (argPredicates.isNotEmpty() || propPredicates.isNotEmpty()) {
                        throw QueryParseException("")
                    }

                    context.read()
                    if (context.read() != '*'.toInt() && context.read() != ']'.toInt()) {
                        throw QueryParseException("")
                    }

                    argPredicates.plus(Predicate { true })
                    propPredicates.plus(Predicate { true })

                    foundSplatArgPropDeletion = true
                }
                else -> {
                    val pred = parsePropOrArgPredicate(context)
                    if (foundSplatArgPropDeletion) {
                        throw QueryParseException("")
                    }

                    when (pred) {
                        is ArgPredicate -> argPredicates.plus(pred.predicate)
                        is PropPredicate ->
                            propPredicates.plus(
                                    Predicate { prop -> pred.keyPredicate.test(prop.key) && pred.valuePredicate.test(prop.value) }
                            )
                        else -> throw QueryParseException("Expected prop predicate or arg predicate but found ${pred.javaClass.simpleName}")
                    }
                }
            }

            consumeWhitespace(context)
            c = context.peek()
        }

        return SubtractMutation(argPredicates, propPredicates, emptyChild, deleteChild)
    }

    // childSubtraction
    fun parseChildSubtraction(context: KDLParseContext): Pair<Boolean, Boolean> {
        var c = context.read()
        if (c != '{'.toInt()) {
            throw KDLInternalException("Expected '{', but found '${runeToStr(c)}'")
        }

        var emptyChild = false
        var deleteChild = false
        c = context.read()
        when (c) {
            '*'.toInt() -> {
                if (context.peek() != '}'.toInt()) {
                    throw QueryParseException("Expected '}', but found '${runeToStr(c)}'")
                }
                emptyChild = true
            };
            '}'.toInt() -> deleteChild = true
        }

        return emptyChild to deleteChild
    }

    // set-mutation := ws* '=' ws* set-item set-list?
    // set-item := value | identifier-predicate '=' value | '=' identifier | node-children
    // set-list :=  set-item (ws+ set-list)?
    fun parseSetMutation(context: KDLParseContext): Mutation {
        var c = context.read()
        if (c != '='.toInt()) {
            throw KDLInternalException("Expected '=', but found '${runeToStr(c)}'")
        }

        val builder = SetMutation.builder()
        var foundChild = false
        var foundId = false

        consumeWhitespace(context)
        c = context.peek()
        while (c != EOF) {
            when {
                c == '='.toInt() -> {
                    if (foundId) {
                        throw QueryParseException("Only one identifier specification is allowed in a set mutation")
                    }

                    context.read()
                    builder.setIdentifier(Optional.of(kdlParser.parseIdentifier(context)))
                }
                c == '{'.toInt() -> {
                    if (foundChild) {
                        throw QueryParseException("Only one child specification is allowed in a set mutation")
                    }

                    context.read()
                    builder.setChild(Optional.of(kdlParser.parseDocument(context, false)))
                    foundChild = true
                }
                isValidNumericStart(c) -> {
                    builder.addArg(kdlParser.parseNumber(context)))
                }
                else -> parseSetMutationItem(context, builder)
            }

            consumeWhitespace(context)
            c = context.peek()
        }

        return builder.build()
    }

    // This is a mess. We could be parsing any of the following:
    // str
    // raw_str
    // literal
    // bare_id = value
    // str = value
    // raw_str = value
    // regex = value
    // raw_regex = value
    fun parseSetMutationItem(context: KDLParseContext, builder: SetMutation.Builder) {

    }

    private fun consumeWhitespace(context: KDLParseContext) {
        var c = context.peek()
        while (isUnicodeWhitespace(c)) {
            context.read()
            c = context.peek()
        }
    }

    private fun runeToStr(c: Int): String =
            if (c <= Char.MAX_VALUE.toInt()) {
                "${c.toChar()}"
            } else if (c == EOF) {
                "EOF"
            } else {
                "\\u{${Integer.toHexString(c)}}"
            }
}