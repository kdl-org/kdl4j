package dev.hbeck.kdl.kq

import dev.hbeck.kdl.kq.OperationParser.ExprType.*
import dev.hbeck.kdl.kq.QueryCharClasses.Companion.isNumericPredicateStart
import dev.hbeck.kdl.kq.QueryCharClasses.Companion.isValidQueryBareIdChar
import dev.hbeck.kdl.kq.QueryCharClasses.Companion.isValidQueryBareIdStart
import dev.hbeck.kdl.objects.KDLBoolean
import dev.hbeck.kdl.objects.KDLNull
import dev.hbeck.kdl.objects.KDLNumber
import dev.hbeck.kdl.objects.KDLProperty
import dev.hbeck.kdl.objects.KDLString
import dev.hbeck.kdl.objects.KDLValue
import dev.hbeck.kdl.parse.CharClasses
import dev.hbeck.kdl.parse.CharClasses.isUnicodeWhitespace
import dev.hbeck.kdl.parse.CharClasses.isValidBareIdChar
import dev.hbeck.kdl.parse.CharClasses.isValidBareIdStart
import dev.hbeck.kdl.parse.CharClasses.isValidNumericStart
import dev.hbeck.kdl.parse.KDLInternalException
import dev.hbeck.kdl.parse.KDLParseContext
import dev.hbeck.kdl.parse.KDLParseException
import dev.hbeck.kdl.parse.KDLParser
import dev.hbeck.kdl.parse.KDLParser.EOF
import dev.hbeck.kdl.parse.KDLParserFacade
import dev.hbeck.kdl.search.GeneralSearch
import dev.hbeck.kdl.search.PathedSearch
import dev.hbeck.kdl.search.RootSearch
import dev.hbeck.kdl.search.Search
import dev.hbeck.kdl.search.mutation.AddMutation
import dev.hbeck.kdl.search.mutation.Mutation
import dev.hbeck.kdl.search.mutation.SetMutation
import dev.hbeck.kdl.search.mutation.SubtractMutation
import dev.hbeck.kdl.search.predicates.AnyContentPredicate
import dev.hbeck.kdl.search.predicates.ArgPredicate
import dev.hbeck.kdl.search.predicates.ChildPredicate
import dev.hbeck.kdl.search.predicates.ConjunctionPredicate
import dev.hbeck.kdl.search.predicates.DisjunctionPredicate
import dev.hbeck.kdl.search.predicates.EmptyContentPredicate
import dev.hbeck.kdl.search.predicates.NegatedPredicate
import dev.hbeck.kdl.search.predicates.NodeContentPredicate
import dev.hbeck.kdl.search.predicates.NodePredicate
import dev.hbeck.kdl.search.predicates.PositionalArgPredicate
import dev.hbeck.kdl.search.predicates.PropPredicate
import java.io.IOException
import java.io.StringReader
import java.lang.ArithmeticException
import java.lang.StringBuilder
import java.math.BigDecimal
import java.util.*
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class OperationParser {
    private val kdlParser = KDLParserFacade()

    fun parse(raw: String): Pair<Search, Mutation?> = parse(KDLParseContext(StringReader(raw)))

    // operation :=  search ws* mutation?
    fun parse(context: KDLParseContext): Pair<Search, Mutation?> =
            try {
                consumeWhitespace(context)
                val search = parseSearch(context)
                val mutation = parseMutation(context)

                mutation?.let {
                    if (search is RootSearch) {
                        if (it !is AddMutation) {
                            throw QueryParseException("If a root search '{}' is used, only addition is supported, and only a child will have any effect")
                        }
                    }
                }

                search to mutation
            } catch (e: QueryParseException) {
                val message = String.format("%s\n%s", e.message, context.errorLocationAndInvalidateContext)
                throw KDLParseException(message, e)
            } catch (e: IOException) {
                throw IOException(context.errorLocationAndInvalidateContext, e)
            } catch (e: KDLInternalException) {
                throw KDLInternalException(context.errorLocationAndInvalidateContext, e)
            } catch (t: Throwable) {
                throw KDLInternalException(String.format("Unexpected exception:\n%s", context.errorLocationAndInvalidateContext), t)
            }

    // search := general-search | pathed-search | root
    // root := '{}'
    fun parseSearch(context: KDLParseContext): Search {
        consumeWhitespace(context)
        var c = context.peek()
        return when (c) {
            '.'.toInt() -> parsePathedSearch(context, false)
            '*'.toInt() -> parseGeneralSearch(context, false)
            '{'.toInt() -> {
                context.read()
                c = context.read()
                if (c == '}'.toInt()) {
                    RootSearch()
                } else {
                    throw QueryParseException("Unknown search type specified. Expected one of '.', '{}', or '*' but found '${runeToStr(c)}'")
                }
            }
            EOF -> throw QueryParseException("No query specified")
            else -> throw QueryParseException("Unknown search type specified. Expected one of '.', '{}', or '*' but found '${runeToStr(c)}'")
        }
    }

    // general-search := '*' depth? node-predicate?
    fun parseGeneralSearch(context: KDLParseContext, inSubSearch: Boolean): GeneralSearch {
        var c = context.read()
        if (c != '*'.toInt()) {
            throw KDLInternalException("Expected general search to start with '*', but found '${runeToStr(c)}'")
        }

        val builder = GeneralSearch.builder()

        c = context.peek()
        if (c == '{'.toInt()) {
            try {
                parseDepthRange(context, builder)
            } catch (e: ArithmeticException) {
                throw QueryParseException("MinDepth/MaxDepth must be positive integers")
            }
        }

        builder.setPredicate(parseNodePredicate(context, false, inSubSearch))

        return builder.build()
    }

    // depth := '{' number? ',' number? '}'
    fun parseDepthRange(context: KDLParseContext, builder: GeneralSearch.Builder) {
        var c = context.read()
        if (c != '{'.toInt()) {
            throw KDLInternalException("Expected depth range to start with '{', but found '${runeToStr(c)}'")
        }

        consumeWhitespace(context)
        c = context.peek()
        when {
            c == ','.toInt() -> {
                context.read()
            }
            isValidNumericStart(c) -> {
                builder.setMinDepth(kdlParser.parseNumber(context).asBigDecimal.intValueExact())
                consumeWhitespace(context)
                c = context.read()
                if (c != ','.toInt()) {
                    throw QueryParseException("Expected ',' in depth range, but found '${runeToStr(c)}'")
                }
            }
            c == EOF -> throw QueryParseException("Incomplete query, ran out of input parsing depth range")
            else -> throw QueryParseException("Expected number or ',' in depth range, but found '${runeToStr(c)}")
        }

        consumeWhitespace(context)
        c = context.peek()
        when {
            c == '}'.toInt() -> {
                context.read()
            }
            isValidNumericStart(c) -> {
                builder.setMaxDepth(kdlParser.parseNumber(context).asBigDecimal.intValueExact())
                consumeWhitespace(context)
                c = context.read()
                if (c != '}'.toInt()) {
                    throw QueryParseException("Expected '}' at end of depth range, but found '${runeToStr(c)}'")
                }
            }
            c == EOF -> throw QueryParseException("Incomplete query, ran out of input parsing depth range")
            else -> throw QueryParseException("Expected number or '}' in depth range, but found '${runeToStr(c)}")
        }
    }

    // pathed-search := '.' node-predicate pathed-search?
    fun parsePathedSearch(context: KDLParseContext, inSubSearch: Boolean): PathedSearch {
        var c = context.peek()
        if (c != '.'.toInt()) {
            throw KDLInternalException("Expected pathed search to start with '.', but found '${runeToStr(c)}'")
        }

        val builder = PathedSearch.builder()
        consumeWhitespace(context)
        while (true) {
            when (c) {
                '-'.toInt(), '+'.toInt(), '='.toInt(), EOF -> return builder.build()
                '.'.toInt() -> {
                    context.read()
                    builder.addLevel(parseNodePredicate(context, true, inSubSearch))
                }
                '}'.toInt() -> {
                    if (inSubSearch) {
                        return builder.build()
                    } else {
                        throw QueryParseException("Expected a predicate or the end of the query, but found '}'")
                    }
                }
                else -> throw QueryParseException("Expected a predicate or the end of the query, but found '${runeToStr(c)}'")
            }

            consumeWhitespace(context)
            c = context.peek()
        }
    }

    // node-predicate := identifier-predicate? predicates?
    fun parseNodePredicate(context: KDLParseContext, pathed: Boolean, inSubSearch: Boolean): NodePredicate {
        consumeWhitespace(context)
        var c = context.peek()
        when (c) {
            '.'.toInt() -> {
                if (!pathed) {
                    throw QueryParseException("Found '.' in non-pathed query")
                } else {
                    return NodePredicate.any()
                }
            }
            EOF -> return NodePredicate.any()
            '['.toInt() -> {
                return NodePredicate({ true }, parseContentPredicates(context))
            }
            '}'.toInt() -> {
                if (inSubSearch) {
                    return NodePredicate.any()
                } else {
                    throw QueryParseException("Expected '.', '[' or and identifier while parsing node predicate, but found '}'")
                }
            }
            '+'.toInt(), '-'.toInt(), '='.toInt() -> {
                if (inSubSearch) {
                    throw QueryParseException("Mutations are not allowed in sub-searches")
                } else {
                    return NodePredicate.any()
                }
            }
            else -> {
                val idPredicate = parseIdentifierPredicate(context)
                consumeWhitespace(context)
                c = context.peek()
                return when (c) {
                    '.'.toInt(), EOF -> NodePredicate(idPredicate, NodeContentPredicate.any())
                    '['.toInt() -> NodePredicate(idPredicate, parseContentPredicates(context))
                    '}'.toInt() -> {
                        if (inSubSearch) {
                            NodePredicate(idPredicate, NodeContentPredicate.any())
                        } else {
                            throw QueryParseException("Expected '.', '[', or the end of input following node predicate, but found '}'")
                        }
                    }
                    '+'.toInt(), '-'.toInt(), '='.toInt() -> {
                        if (inSubSearch) {
                            throw QueryParseException("Mutations are not allowed in sub-searches")
                        } else {
                            return NodePredicate(idPredicate, NodeContentPredicate.any())
                        }
                    }
                    else -> throw QueryParseException("Expected '.', '[', or the end of input following node predicate, but found '${runeToStr(c)}'")
                }
            }
        }
    }

    // identifier-predicate := bare-identifier | string | regex
    fun parseIdentifierPredicate(context: KDLParseContext): Predicate<String> {
        var c = context.peek()
        return when {
            c == '/'.toInt() -> {
                parseEscapedRegex(context)
            }
            c == '"'.toInt() -> {
                val expected = kdlParser.parseEscapedString(context)
                Predicate { it == expected }
            }
            isValidBareIdStart(c) -> {
                if (c == 'r'.toInt()) {
                    context.read()
                    c = context.peek()
                    context.unread('r'.toInt())
                    when (c) {
                        '#'.toInt(), '"'.toInt(), '/'.toInt() -> parseRawRegexOrString(context)
                        else -> {
                            val expected = parseBareIdentifier(context)
                            Predicate { it == expected }
                        }
                    }

                } else {
                    val expected = parseBareIdentifier(context)
                    Predicate { it == expected }
                }
            }
            c == EOF -> throw KDLInternalException("Expected regex, but found the end of input")
            else -> throw QueryParseException("Expected an identifier or regex at start of node predicate, but found '${runeToStr(c)}'")
        }
    }

    fun parseBareIdentifier(context: KDLParseContext): String {
        var c = context.read()
        if (!isValidQueryBareIdStart(c)) {
            throw KDLParseException("Illegal character at start of bare identifier")
        } else if (c == EOF) {
            throw KDLInternalException("EOF when a bare identifer expected")
        }
        val stringBuilder = StringBuilder()
        stringBuilder.appendCodePoint(c)
        c = context.peek()
        while (isValidQueryBareIdChar(c) && c != EOF) {
            stringBuilder.appendCodePoint(context.read())
            c = context.peek()
        }
        return stringBuilder.toString()
    }

    // content-predicates := '[' content-predicate ']'
    // content-predicate := ('(' compound-expr ')') | atom --- May omit parens around outermost
    // compound-expr := and-expr | or-expr
    // and-expr := content-predicate '&' content-predicate ('&' content-predicate)*
    // or-expr := content-predicate '|' content-predicate ('|' content-predicate)*
    // atom := child-predicate | prop-or-arg-predicate
    fun parseContentPredicates(context: KDLParseContext): NodeContentPredicate {
        var c = context.read()
        if (c != '['.toInt()) {
            throw KDLInternalException("Expected '[' at start of node content predicate, but found '${runeToStr(c)}'")
        }

        consumeWhitespace(context)
        c = context.peek()
        return when (c) {
            '*'.toInt() -> {
                context.read()
                consumeWhitespace(context)
                c = context.read()
                if (c != ']'.toInt()) {
                    throw QueryParseException("Expected ']' at end of match-any-content predicate, but found '${runeToStr(c)}'")
                }
                AnyContentPredicate()
            }
            ']'.toInt() -> {
                context.read()
                EmptyContentPredicate()
            }
            EOF -> throw QueryParseException("Ran out of input while parsing node content predicate")
            else -> {
                val predicates = parseContentPredicates(context, UNKNOWN)
                consumeWhitespace(context)
                c = context.read()
                if (c != ']'.toInt()) {
                    throw QueryParseException("Expected ']' at end of node content predicates, but found '${runeToStr(c)}'")
                }

                predicates
            }
        }
    }

    enum class ExprType {
        AND, OR, UNKNOWN
    }

    fun parseContentPredicates(context: KDLParseContext, exprType: ExprType): NodeContentPredicate {
        consumeWhitespace(context)
        var c = context.peek()
        var expr = if (c == '('.toInt()) {
            context.read()
            consumeWhitespace(context)
            val predicates = parseContentPredicates(context, UNKNOWN)
            consumeWhitespace(context)

            c = context.read()
            if (c != ')'.toInt()) {
                throw QueryParseException("Expected ')' but found '${runeToStr(c)}'")
            }

            predicates
        } else {
            parseChildArgOrPropPredicate(context)
        }

        consumeWhitespace(context)
        c = context.peek()
        when (c) {
            ']'.toInt(), ')'.toInt() -> {
                return expr
            }
            '('.toInt() -> {
                context.read()
                consumeWhitespace(context)
                val subPredicate = parseContentPredicates(context, UNKNOWN)
                consumeWhitespace(context)

                c = context.read()
                if (c != ')'.toInt()) {
                    throw QueryParseException("Expected ')' but found '${runeToStr(c)}'")
                }

                expr = when (exprType) {
                    AND -> ConjunctionPredicate(expr, subPredicate)
                    OR -> DisjunctionPredicate(expr, subPredicate)
                    UNKNOWN -> throw KDLInternalException("Found '(' but current expression type is unknown")
                }
            }
            '&'.toInt() -> {
                context.read()
                when (exprType) {
                    OR -> throw QueryParseException("Found '&' when only '|' allowed, use parenthesis to clarify node content predicate")
                    UNKNOWN, AND -> expr = ConjunctionPredicate(expr, parseContentPredicates(context, AND))
                }
            }
            '|'.toInt() -> {
                context.read()
                when (exprType) {
                    AND -> throw QueryParseException("Found '|' when only '&' allowed, use parenthesis to clarify node content predicate")
                    UNKNOWN, OR -> expr = DisjunctionPredicate(expr, parseContentPredicates(context, OR))
                }
            }
            EOF -> throw QueryParseException("Ran out of input while parsing node content predicate")
            else -> throw QueryParseException("Unexpected character in node content predicate: '${runeToStr(c)}'")
        }

        consumeWhitespace(context)
        return expr
    }

    fun parseChildArgOrPropPredicate(context: KDLParseContext): NodeContentPredicate {
        return when (context.peek()) {
            '{'.toInt() -> parseChildPredicate(context)
            '!'.toInt() -> {
                context.read()
                NegatedPredicate(parseChildArgOrPropPredicate(context))
            }
            EOF -> throw QueryParseException("Ran out of input while parsing node content predicate")
            else -> return parsePropOrArgPredicate(context)
        }
    }

    // child-predicate := '{' (general-search | pathed-search) '}'
    fun parseChildPredicate(context: KDLParseContext): ChildPredicate {
        var c = context.read()
        if (c != '{'.toInt()) {
            throw KDLInternalException("Expected '{' at start of child predicate, but found '${runeToStr(c)}'")
        }

        consumeWhitespace(context)
        c = context.peek()
        val predicate = when (c) {
            '.'.toInt() -> {
                ChildPredicate(Optional.of(parsePathedSearch(context, true)))
            }
            '*'.toInt() -> {
                ChildPredicate(Optional.of(parseGeneralSearch(context, true)))
            }
            '}'.toInt() -> {
                ChildPredicate.empty()
            }
            else -> throw QueryParseException("Couldn't parse child predicate. Expected one of '.', '*', or '}', but found '${runeToStr(c)}'")
        }

        consumeWhitespace(context)
        c = context.read()
        if (c != '}'.toInt()) {
            throw QueryParseException("Expected '}' at end of child predicate, but found '${runeToStr(c)}'")
        }

        return predicate
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
            c == '.'.toInt() -> return parsePositionalArgPredicate(context)
            isValidNumericStart(c) -> {
                val number = kdlParser.parseNumber(context)
                return ArgPredicate { value: KDLValue -> value.isNumber && value == number }
            }
            isValidQueryBareIdStart(c) -> {
                if (c == 'r'.toInt()) {
                    context.read()
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
            else -> throw QueryParseException("Error parsing arg or prop predicate. Unexpected character: '${runeToStr(c)}'")
        }

        c = context.peek()
        val valuePredicate = when (c) {
            '='.toInt() -> {
                context.read()
                c = context.peek()
                if (c == '*'.toInt()) {
                    context.read()
                    Predicate { it != KDLNull.INSTANCE }
                } else {
                    when (val value = kdlParser.parseValue(context)) {
                        is KDLNumber -> Predicate { other -> other.isNumber && value.asBigDecimal == other.asNumber.get().asBigDecimal }
                        else -> Predicate { other -> value == other }
                    }
                }
            }
            '>'.toInt(), '<'.toInt() -> parseNumericPredicate(context)
            '~'.toInt() -> {
                context.read()
                c = context.peek()
                val regex = when (c) {
                    'r'.toInt() -> parseRawRegexOrString(context)
                    '/'.toInt() -> parseEscapedRegex(context)
                    else -> throw QueryParseException("Expected 'r' or '/' at start of regex, found '${runeToStr(c)}'")
                }

                Predicate { other -> other.isString && regex.test(other.asString.value) }
            }
            else -> return ArgPredicate { other -> other.isString && strPredicate.test(other.asString.value) }
        }

        return PropPredicate(strPredicate, valuePredicate)
    }

    fun parsePositionalArgPredicate(context: KDLParseContext): NodeContentPredicate {
        val index = parsePositionalArgPosition(context)

        var c = context.peek()
        val valuePredicate = when (c) {
            '='.toInt() -> {
                context.read()
                c = context.peek()
                if (c == '*'.toInt()) {
                    Predicate { it != KDLNull.INSTANCE }
                } else {
                    when (val value = kdlParser.parseValue(context)) {
                        is KDLNumber -> Predicate { other -> other.isNumber && value.asBigDecimal == other.asNumber.get().asBigDecimal }
                        else -> Predicate { other -> value == other }
                    }
                }
            }
            '>'.toInt(), '<'.toInt() -> parseNumericPredicate(context)
            '~'.toInt() -> {
                context.read()
                c = context.peek()
                val regex = when (c) {
                    'r'.toInt() -> parseRawRegexOrString(context)
                    '/'.toInt() -> parseEscapedRegex(context)
                    else -> throw QueryParseException("Expected 'r' or '/' at start of regex, found '${runeToStr(c)}'")
                }

                Predicate { other -> other.isString && regex.test(other.asString.value) }
            }
            else -> throw QueryParseException("Expected one of '=', '>', or '~' in positional argument predicate, but found '${runeToStr(c)}'")
        }

        return PositionalArgPredicate(index, valuePredicate)
    }

    fun parsePositionalArgPosition(context: KDLParseContext): Int {
        val c = context.read()
        if (c != '.'.toInt()) {
            throw KDLInternalException("Expected '.' at start of positional arg, but found '${runeToStr(c)}'")
        }

        if (context.read() != '['.toInt()) {
            throw QueryParseException("Expected '[' in positional arg position, but found '${runeToStr(c)}'")
        }

        consumeWhitespace(context)
        val indexUnchecked = kdlParser.parseNumber(context)
        val index = try {
            if (indexUnchecked.asBigDecimal < BigDecimal.ZERO) {
                throw QueryParseException("Argument positions must be greater than or equal to 0")
            }
            indexUnchecked.asBigDecimal.intValueExact()
        } catch (e: ArithmeticException) {
            throw QueryParseException("Argument positions must be integers")
        }

        consumeWhitespace(context)
        if (context.read() != ']'.toInt()) {
            throw QueryParseException("Expected ']' at end of argument positional arg position, but found '${runeToStr(c)}'")
        }

        return index
    }

    // numeric-predicate := ('=' | '<' | '>') number
    fun parseNumericPredicate(context: KDLParseContext): Predicate<KDLValue> {
        val c = context.read()
        val number = kdlParser.parseNumber(context).asBigDecimal
        return when (c) {
            '='.toInt() -> Predicate { value: KDLValue -> value.isNumber && number == value.asNumber.get().asBigDecimal }
            '>'.toInt() -> Predicate { value: KDLValue -> value.isNumber && value.asNumber.get().asBigDecimal > number }
            '<'.toInt() -> Predicate { value: KDLValue -> value.isNumber && value.asNumber.get().asBigDecimal < number }
            else -> throw KDLInternalException("Expected '=', '>', or '<' but got '${runeToStr(c)}'")
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
                    inEscape = if (inEscape) {
                        stringBuilder.append('\\')
                        false
                    } else {
                        true
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
    private fun parseRawRegexOrString(context: KDLParseContext): StringPredicate {
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
                val regexStr = parseRawRegexOrString(context, c, hashDepth)
                try {
                    StringPredicate.RegexPredicate(Pattern.compile(regexStr))
                } catch (e: PatternSyntaxException) {
                    throw QueryParseException("Failed to parse regex '$regexStr': ${e.localizedMessage}")
                }
            }
            '"'.toInt() -> StringPredicate.LiteralStringPredicate(parseRawRegexOrString(context, c, hashDepth))
            EOF -> throw QueryParseException("Input exhausted when parsing raw regex or string")
            else -> throw QueryParseException("Malformed raw regex or string, expected '/' or '\"' but found '\\u{$c}'")
        }
    }

    fun parseRawRegexOrString(context: KDLParseContext, terminator: Int, hashDepth: Int): String {
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
        if (!isValidQueryBareIdStart(c)) {
            throw QueryParseException("Illegal character at start of bare identifier or literal")
        } else if (c == EOF) {
            throw KDLInternalException("EOF when a bare identifier or literal expected")
        }

        val stringBuilder = StringBuilder()
        stringBuilder.appendCodePoint(c)
        c = context.peek()
        while (isValidQueryBareIdChar(c) && c != EOF) {
            stringBuilder.appendCodePoint(context.read())
            c = context.peek()
        }
        return stringBuilder.toString()
    }

    fun parsePositionalArgMutation(context: KDLParseContext): Pair<Int, KDLValue> {
        val index = parsePositionalArgPosition(context)

        val c = context.read()
        if (c != '='.toInt()) {
            throw KDLInternalException("Expected '+' at start of add mutation, but got '${runeToStr(c)}'")
        }

        return index to kdlParser.parseValue(context)
    }

    // mutation := add-mutation | sub-mutation | set-mutation
    fun parseMutation(context: KDLParseContext): Mutation? {
        consumeWhitespace(context)
        return when (val c = context.peek()) {
            '+'.toInt() -> parseAddMutation(context)
            '-'.toInt() -> parseSubtractionMutation(context)
            '='.toInt() -> parseSetMutation(context)
            EOF -> return null
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

        val builder = AddMutation.builder()
        var readChild = false

        consumeWhitespace(context)
        c = context.peek()
        while (c != EOF) {
            when (c) {
                '{'.toInt() -> {
                    if (readChild) {
                        throw QueryParseException("Only one child may be specified per add mutation.")
                    }

                    readChild = true
                    builder.setChild(kdlParser.parseChild(context))
                }
                '.'.toInt() -> {
                    val (index, value) = parsePositionalArgMutation(context)
                    builder.addPositionalArg(index, value)
                }
                else -> {
                    when (val argOrProp = kdlParser.parseArgOrProp(context)) {
                        is KDLProperty -> builder.addProp(argOrProp.key, argOrProp.value)
                        is KDLValue -> builder.addArg(argOrProp)
                        else -> throw QueryParseException("Expected argument or property, but found '${argOrProp.toKDL()}'")
                    }
                }
            }

            consumeWhitespace(context)
            c = context.peek()
        }

        return builder.build()
    }

    // sub-mutation := ws* '-' ws* subtraction sub-list
    // subtraction := (value | regex | numeric-predicate | property-predicate | '{' '*'? '}' |  '[*]')
    // sub-list := (ws+ subtraction sub-list)?
    fun parseSubtractionMutation(context: KDLParseContext): Mutation {
        var c = context.read()
        if (c != '-'.toInt()) {
            throw KDLInternalException("Expected '-' at start of subtract mutation, but got '${runeToStr(c)}'")
        }

        val builder = SubtractMutation.builder()
        var foundChildSubtraction = false
        var foundSplatArgPropDeletion = false
        var foundArgOrPropSubtraction = false

        consumeWhitespace(context)
        c = context.peek()
        if (c == '.'.toInt()) {
            context.read()
            if (context.peek() != '['.toInt()) {
                // The 'delete node' case is represented by all fields of the subtraction being empty
                return builder.build()
            } else {
                context.unread('.'.toInt())
            }
        }

        while (c != EOF) {
            when (c) {
                '{'.toInt() -> {
                    if (foundChildSubtraction) {
                        throw QueryParseException("Only one child clause is allowed in subtraction mutation")
                    }

                    parseChildSubtraction(context, builder)
                    foundChildSubtraction = true
                }
                '['.toInt() -> {
                    if (foundArgOrPropSubtraction) {
                        throw QueryParseException("Splat argument/property deletion cannot be mixed with specific deletions")
                    }

                    context.read()
                    if (context.read() != '*'.toInt() || context.read() != ']'.toInt()) {
                        throw QueryParseException("Read '[', but it wasn't followed by '*]'")
                    }

                    builder.addArg { true }
                    builder.addProp { true }

                    foundSplatArgPropDeletion = true
                }
                '.'.toInt() -> {
                    builder.deleteArgAt(parsePositionalArgPosition(context))
                }
                else -> {
                    val pred = parsePropOrArgPredicate(context)
                    if (foundSplatArgPropDeletion) {
                        throw QueryParseException("Splat argument/property deletion cannot be mixed with specific deletions")
                    }

                    foundArgOrPropSubtraction = true
                    when (pred) {
                        is ArgPredicate -> builder.addArg(pred.predicate)
                        is PropPredicate ->
                            builder.addProp { prop -> pred.keyPredicate.test(prop.key) && pred.valuePredicate.test(prop.value) }
                        else -> throw QueryParseException("Expected prop predicate or arg predicate but found ${pred.javaClass.simpleName}")
                    }
                }
            }

            consumeWhitespace(context)
            c = context.peek()
        }

        return builder.build()
    }

    // childSubtraction
    fun parseChildSubtraction(context: KDLParseContext, builder: SubtractMutation.Builder) {
        var c = context.read()
        if (c != '{'.toInt()) {
            throw KDLInternalException("Expected '{', but found '${runeToStr(c)}'")
        }

        c = context.read()
        when (c) {
            '*'.toInt() -> {
                if (context.read() != '}'.toInt()) {
                    throw QueryParseException("Expected '}', but found '${runeToStr(c)}'")
                }
                builder.emptyChild()
            }
            '}'.toInt() -> builder.deleteChild()
        }
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
                    builder.setIdentifier(kdlParser.parseIdentifier(context))
                    foundId = true
                }
                c == '{'.toInt() -> {
                    if (foundChild) {
                        throw QueryParseException("Only one child specification is allowed in a set mutation")
                    }

                    context.read()
                    builder.setChild(Optional.of(kdlParser.parseDocument(context, false)))
                    foundChild = true
                }
                c == '.'.toInt() -> {
                    val (index, value) = parsePositionalArgMutation(context)
                    builder.addPositionalArg(index, value)
                }
                isValidNumericStart(c) -> {
                    builder.addArg(kdlParser.parseNumber(context))
                }
                else -> parseSetMutationItem(context, builder)
            }

            consumeWhitespace(context)
            c = context.peek()
        }

        return builder.build()
    }

    fun parseSetMutationItem(context: KDLParseContext, builder: SetMutation.Builder) {
        var c = context.peek()
        val propOrValue = when {
            c == '"'.toInt() -> {
                kdlParser.parseEscapedString(context)
            }
            isValidBareIdStart(c) -> {
                when (c) {
                    'r'.toInt() -> {
                        context.read()
                        c = context.peek()
                        context.unread('r'.toInt())
                        if (c == '"'.toInt() || c == '#'.toInt()) {
                            parseBareIdentifierOrLiteral(context)
                        }
                        kdlParser.parseRawString(context)
                    }
                    else -> {
                        when (val bareIdentifierOrLiteral = parseBareIdentifierOrLiteral(context)) {
                            "true" -> {
                                builder.addArg(KDLBoolean.TRUE)
                                return
                            }
                            "false" -> {
                                builder.addArg(KDLBoolean.FALSE)
                                return
                            }
                            "null" -> {
                                builder.addArg(KDLNull.INSTANCE)
                                return
                            }
                            else -> {
                                bareIdentifierOrLiteral
                            }
                        }
                    }
                }
            }
            else -> throw QueryParseException("Expected one of: literal, string, or identifier, but found ${runeToStr(c)}")
        }

        c = context.peek()
        when (c) {
            '='.toInt() -> {
                context.read()
                val value = kdlParser.parseValue(context)
                builder.addProp(propOrValue, value)
            }
            else -> builder.addArg(KDLString.from(propOrValue))
        }
    }

    private sealed class StringPredicate : Predicate<String> {
        data class LiteralStringPredicate(val str: String) : StringPredicate() {
            override fun test(t: String): Boolean = str == t
        }

        data class RegexPredicate(val pat: Pattern) : StringPredicate() {
            private val pred: Predicate<String> = pat.asPredicate()

            override fun test(t: String): Boolean = pred.test(t)
        }
    }

    private fun consumeWhitespace(context: KDLParseContext) {
        var c = context.peek()
        while (isUnicodeWhitespace(c)) {
            context.read()
            c = context.peek()
        }
    }

    private fun runeToStr(c: Int): String =
            when {
                c <= Char.MAX_VALUE.toInt() -> "${c.toChar()}"
                c == EOF -> "EOF"
                else -> "\\u{${Integer.toHexString(c)}}"
            }
}