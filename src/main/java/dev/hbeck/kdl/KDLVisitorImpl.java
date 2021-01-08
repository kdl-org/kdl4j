package dev.hbeck.kdl;

import dev.hbeck.kdl.antlr.kdlBaseVisitor;
import dev.hbeck.kdl.antlr.kdlParser;
import dev.hbeck.kdl.objects.KDLBoolean;
import dev.hbeck.kdl.objects.KDLDocument;
import dev.hbeck.kdl.objects.KDLIdentifier;
import dev.hbeck.kdl.objects.KDLNode;
import dev.hbeck.kdl.objects.KDLNull;
import dev.hbeck.kdl.objects.KDLNumber;
import dev.hbeck.kdl.objects.KDLObject;
import dev.hbeck.kdl.objects.KDLProperty;
import dev.hbeck.kdl.objects.KDLString;
import dev.hbeck.kdl.objects.KDLValue;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.text.StringEscapeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class KDLVisitorImpl extends kdlBaseVisitor<KDLObject> {
    @Override
    public KDLObject visitParse(kdlParser.ParseContext ctx) {
        final kdlParser.NodesContext nodes = ctx.nodes();
        if (nodes == null) {
            return new KDLDocument(Collections.emptyList());
        }

        return visitNodes(nodes);
    }

    @Override
    public KDLObject visitNodes(kdlParser.NodesContext ctx) {
        final List<kdlParser.NodeContext> nodeCtxs = ctx.node();
        if (nodeCtxs == null || nodeCtxs.isEmpty()) {
            return new KDLDocument(Collections.emptyList());
        }

        final List<KDLNode> parsedNodes = new ArrayList<>();
        nodeCtxs.forEach(nodeCtx -> parsedNodes.add((KDLNode) visitNode(nodeCtx)));

        return new KDLDocument(parsedNodes);
    }

    @Override
    public KDLObject visitNode(kdlParser.NodeContext ctx) {
        if (ctx.COMMENTED_CHUNK() != null) {
            return null;
        }

        final KDLIdentifier identifier = (KDLIdentifier) visitIdentifier(ctx.identifier());

        final Map<KDLIdentifier, KDLValue> props = new HashMap<>();
        final List<KDLValue> args = new ArrayList<>(ctx.node_props_and_args().size());
        ctx.node_props_and_args().forEach(argCtx -> {
            final KDLObject object = visitNode_props_and_args(argCtx);
            if (object instanceof KDLProperty) {
                final KDLProperty property = (KDLProperty) object;
                props.put(property.getKey(), property.getValue());
            } else if (object instanceof KDLValue) {
                args.add((KDLValue) object);
            }
        });

        final Optional<KDLDocument> child = Optional.ofNullable(ctx.node_children()).map(childCtx ->
                (KDLDocument) visitNodes(childCtx.nodes())
        );

        return new KDLNode(identifier, props, args, child);
    }

    @Override
    public KDLObject visitNode_props_and_args(kdlParser.Node_props_and_argsContext ctx) {
        if (ctx.COMMENTED_CHUNK() != null) {
            return null;
        }

        final kdlParser.PropContext propCtx = ctx.prop();
        if (propCtx != null) {
            return visitProp(propCtx);
        }

        final kdlParser.ValueContext valueCtx = ctx.value();
        if (valueCtx != null) {
            return visitValue(valueCtx);
        }

        throw new RuntimeException("wat");
    }

    @Override
    public KDLObject visitNode_children(kdlParser.Node_childrenContext ctx) {
        if (ctx.COMMENTED_CHUNK() != null) {
            return null;
        }

        return visitNodes(ctx.nodes());
    }

    @Override
    public KDLObject visitIdentifier(kdlParser.IdentifierContext ctx) {
        final kdlParser.Bare_identifierContext bareIdCtx = ctx.bare_identifier();
        if (bareIdCtx != null) {
            return visitBare_identifier(bareIdCtx);
        }

        final kdlParser.StringContext stringCtx = ctx.string();
        if (stringCtx != null) {
            final KDLString asStr = (KDLString) visitString(stringCtx);
            return new KDLIdentifier(asStr.getValue());
        }

        throw new RuntimeException("wat");
    }

    @Override
    public KDLObject visitBare_identifier(kdlParser.Bare_identifierContext ctx) {
        return new KDLIdentifier(ctx.getText());
    }

    @Override
    public KDLObject visitProp(kdlParser.PropContext ctx) {
        final kdlParser.IdentifierContext idCtx = ctx.identifier();
        if (idCtx == null) {
                throw new RuntimeException("Missing property key");
        }

        final kdlParser.ValueContext valueCtx = ctx.value();
        if (valueCtx == null) {
            throw new RuntimeException("Missing property value");
        }

        final KDLIdentifier key = (KDLIdentifier) visitIdentifier(idCtx);
        final KDLValue value = (KDLValue) visitValue(valueCtx);

        return new KDLProperty(key, value);
    }

    @Override
    public KDLObject visitValue(kdlParser.ValueContext ctx) {
        final kdlParser.StringContext stringCtx = ctx.string();
        if (stringCtx != null) {
            return visitString(stringCtx);
        }

        final kdlParser.NumberContext numberCtx = ctx.number();
        if (numberCtx != null) {
            return visitNumber(numberCtx);
        }

        final TerminalNode bool = ctx.BOOLEAN();
        if (bool != null) {
            switch (bool.getText()) {
                case "true":
                    return KDLBoolean.TRUE;
                case "false":
                    return KDLBoolean.FALSE;
            }
        }

        final TerminalNode isNull = ctx.NULL();
        if (isNull != null) {
            return KDLNull.INSTANCE;
        }

        throw new RuntimeException("wat");
    }

    @Override
    public KDLObject visitString(kdlParser.StringContext ctx) {
        final kdlParser.Escaped_stringContext escapedCtx = ctx.escaped_string();
        if (escapedCtx != null) {
            return visitEscaped_string(escapedCtx);
        }

        final kdlParser.Raw_stringContext rawCtx = ctx.raw_string();
        if (rawCtx != null) {
            return visitRaw_string(rawCtx);
        }

        throw new RuntimeException("wat");
    }

    @Override
    public KDLObject visitEscaped_string(kdlParser.Escaped_stringContext ctx) {
        final String rawText = ctx.getText();
        final String stripped = rawText.substring(1, rawText.length() - 1);
        final String unescaped = StringEscapeUtils.unescapeJava(stripped);

        return new KDLString(unescaped);
    }

    @Override
    public KDLObject visitRaw_string(kdlParser.Raw_stringContext ctx) {
        final kdlParser.Raw_string_hashContext hashContext = ctx.raw_string_hash();
        if (hashContext != null) {
            return visitRaw_string_hash(hashContext);
        }

        throw new RuntimeException("wat");
    }

    @Override
    public KDLObject visitRaw_string_hash(kdlParser.Raw_string_hashContext ctx) {
        final kdlParser.Raw_string_hashContext hashContext = ctx.raw_string_hash();
        if (hashContext != null) {
            return visitRaw_string_hash(hashContext);
        }

        final kdlParser.Raw_string_quotesContext quotesCtx = ctx.raw_string_quotes();
        if (quotesCtx != null) {
            return visitRaw_string_quotes(quotesCtx);
        }

        throw new RuntimeException("wat");
    }

    @Override
    public KDLObject visitRaw_string_quotes(kdlParser.Raw_string_quotesContext ctx) {
        final String rawText = ctx.getText();
        final String stripped = rawText.substring(1, rawText.length() - 1);

        return new KDLString(stripped);
    }

    @Override
    public KDLObject visitNumber(kdlParser.NumberContext ctx) {
        final TerminalNode decimal = ctx.DECIMAL();
        if (decimal != null) {
            final String text = decimal.getText();
            return new KDLNumber(new BigDecimal(text));
        }

        final TerminalNode hex = ctx.HEX();
        if (hex != null) {
            final String text = hex.getText();
            return new KDLNumber(new BigDecimal(new BigInteger(text, 16)));
        }

        final TerminalNode binary = ctx.BINARY();
        if (binary != null) {
            final String text = binary.getText();
            return new KDLNumber(new BigDecimal(new BigInteger(text, 2)));
        }

        final TerminalNode octal = ctx.OCTAL();
        if (octal != null) {
            final String text = octal.getText();
            return new KDLNumber(new BigDecimal(new BigInteger(text, 8)));
        }

        throw new RuntimeException("wat");
    }
}
