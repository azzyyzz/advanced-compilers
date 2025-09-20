package org.stella.typecheck;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.syntax.stella.Absyn.Type;

public class Context {

    public final Map<String, Type> variables;
    public final Set<String> extensions;
    public Type exceptionType;
    public Type expectedType;
    public Map<String, Type> variantFields;

    public Context(Map<String, Type> variables, Set<String> extensions, Type exceptionType, Type expectedType) {
        this.variables = variables;
        this.extensions = extensions;
        this.exceptionType = exceptionType;
        this.expectedType = expectedType;
    }

    public Context newScope() {
        return new Context(new HashMap<>(variables), extensions, exceptionType, expectedType);
    }

    public Context newScope(Type newExpectedType) {
        return new Context(new HashMap<>(variables), extensions, exceptionType, newExpectedType);
    }
}
