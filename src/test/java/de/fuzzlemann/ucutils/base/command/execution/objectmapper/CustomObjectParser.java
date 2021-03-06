package de.fuzzlemann.ucutils.base.command.execution.objectmapper;

import de.fuzzlemann.ucutils.base.command.ParameterParser;

/**
 * @author Fuzzlemann
 */
public class CustomObjectParser implements ParameterParser<DeclaredTestObject, GeneralTestObject> {
    @Override
    public GeneralTestObject parse(DeclaredTestObject input) {
        if (input.getString() == null) return null;

        return new GeneralTestObject(input.getString());
    }
}
