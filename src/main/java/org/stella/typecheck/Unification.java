package org.stella.typecheck;

import org.syntax.stella.Absyn.*;

public class Unification {
    boolean recon = false;

    public Type unify(Type S, Type T) {

        if (S.equals(T)) {
            return S;
        } else if (T instanceof TypeVar type) {
            return 
        }
        throw new IllegalArgumentException("[ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION]");
    }
}
