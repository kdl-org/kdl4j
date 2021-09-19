package dev.hbeck.kdl.parse;

import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLNumber;
import dev.hbeck.kdl.objects.KDLObject;
import dev.hbeck.kdl.objects.KDLValue;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Internal class allowing access to the internal methods of KDLParser without confusing KDLParser's interface
 */
public class KDLParserFacade extends KDLParser {
    public KDLDocument parseDocument(KDLParseContext context, boolean root) throws IOException {
        return super.parseDocument(context, root);
    }

    @Override
    public Optional<KDLNode> parseNode(KDLParseContext context) throws IOException {
        return super.parseNode(context);
    }

    @Override
    public String parseIdentifier(KDLParseContext context) throws IOException {
        return super.parseIdentifier(context);
    }

    @Override
    public KDLObject parseArgOrProp(KDLParseContext context) throws IOException {
        return super.parseArgOrProp(context);
    }

    @Override
    public KDLDocument parseChild(KDLParseContext context) throws IOException {
        return super.parseChild(context);
    }

    @Override
    public KDLValue<?> parseValue(KDLParseContext context) throws IOException {
        return super.parseValue(context);
    }

    @Override
    public KDLNumber parseNumber(KDLParseContext context, Optional<String> type) throws IOException {
        return super.parseNumber(context, type);
    }

    @Override
    public KDLNumber parseNonDecimalNumber(KDLParseContext context, Predicate<Integer> legalChars, int radix, Optional<String> type) throws IOException {
        return super.parseNonDecimalNumber(context, legalChars, radix, type);
    }

    @Override
    public KDLNumber parseDecimalNumber(KDLParseContext context, Optional<String> type) throws IOException {
        return super.parseDecimalNumber(context, type);
    }

    @Override
    public String parseBareIdentifier(KDLParseContext context) throws IOException {
        return super.parseBareIdentifier(context);
    }

    @Override
    public String parseEscapedString(KDLParseContext context) throws IOException {
        return super.parseEscapedString(context);
    }

    @Override
    public int getEscaped(int c, KDLParseContext context) throws IOException {
        return super.getEscaped(c, context);
    }

    @Override
    public String parseRawString(KDLParseContext context) throws IOException {
        return super.parseRawString(context);
    }

    @Override
    public SlashAction getSlashAction(KDLParseContext context, boolean escaped) throws IOException {
        return super.getSlashAction(context, escaped);
    }

    @Override
    public void consumeAfterNode(KDLParseContext context) throws IOException {
        super.consumeAfterNode(context);
    }

    @Override
    public WhitespaceResult consumeWhitespaceAndBlockComments(KDLParseContext context) throws IOException {
        return super.consumeWhitespaceAndBlockComments(context);
    }

    @Override
    public void consumeLineComment(KDLParseContext context) throws IOException {
        super.consumeLineComment(context);
    }

    @Override
    public void consumeBlockComment(KDLParseContext context) throws IOException {
        super.consumeBlockComment(context);
    }

    @Override
    public WhitespaceResult consumeWhitespaceAndLinespace(KDLParseContext context) throws IOException {
        return super.consumeWhitespaceAndLinespace(context);
    }
}
