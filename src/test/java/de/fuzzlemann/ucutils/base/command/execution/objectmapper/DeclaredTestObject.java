package de.fuzzlemann.ucutils.base.command.execution.objectmapper;

import de.fuzzlemann.ucutils.base.command.ParameterParser;

/**
 * @author Fuzzlemann
 */
@ParameterParser.At(DeclaredTestObject.DeclaredTestObjectParser.class)
public class DeclaredTestObject {

    private final String string;

    public DeclaredTestObject(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }

    public static class DeclaredTestObjectParser implements ParameterParser<String, DeclaredTestObject> {
        @Override
        public DeclaredTestObject parse(String input) {
            return new DeclaredTestObject(input);
        }
    }
}
