package me.reimnop.d4f.customevents;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import me.reimnop.d4f.Discord4Fabric;
import me.reimnop.d4f.customevents.constraints.ConstraintProcessor;
import me.reimnop.d4f.customevents.constraints.ConstraintProcessorFactory;
import me.reimnop.d4f.exceptions.SyntaxException;
import me.reimnop.d4f.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstraintList {
    public final List<Constraint> constraints;

    public ConstraintList(Map<String, ConstraintProcessorFactory> supportedProcessors, JsonArray constraintArray) {
        constraints = new ArrayList<>();

        for (JsonElement element : constraintArray) {
            try {
                constraints.add(new Constraint(supportedProcessors, element.getAsString()));
            } catch (SyntaxException e) {
                Discord4Fabric.LOGGER.warn("Failed to parse constraint '" + element.getAsString() + "'!");
                Utils.logException(e);
            }
        }
    }
}
