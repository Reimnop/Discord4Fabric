package me.reimnop.d4f.customevents;

import eu.pb4.placeholders.api.PlaceholderHandler;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.constraints.ConstraintProcessor;
import me.reimnop.d4f.customevents.constraints.ConstraintProcessorFactory;
import me.reimnop.d4f.exceptions.SyntaxException;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class Constraint {
    public final boolean negated;
    private final ConstraintProcessor processor;
    public final List<String> arguments;

    public Constraint(Map<String, ConstraintProcessorFactory> supportedProcessors, String constraintString) throws SyntaxException {
        ConstraintParser parser = new ConstraintParser(constraintString);
        ConstraintParser.ParseResult parseResult = parser.parse();

        negated = parseResult.negated;
        processor = supportedProcessors.get(parseResult.name).getProcessor();
        arguments = parseResult.arguments;

        if (arguments != null) {
            processor.loadArguments(arguments);
        }
    }

    public boolean satisfied() {
        return negated != processor.satisfied();
    }

    public Map<Identifier, PlaceholderHandler> getHandlers() {
        return processor.getHandlers();
    }
}
