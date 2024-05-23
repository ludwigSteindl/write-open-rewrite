package com.gepardec.wor.lord.util;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.Cursor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

public class MethodInvocations {
    public static J.MethodInvocation updateDeclaringType(J.MethodInvocation method, String fullyQualifiedType) {
        if (!(method.getSelect() instanceof J.Identifier)) {
            return method;
        }
        return method.withSelect(
                method.getSelect().withType(
                        JavaType.buildType(fullyQualifiedType)
                )
        );
    }

    public static @NotNull String getFirstArgument(J.MethodInvocation method, Cursor cursor) {
        return method.getArguments().get(0).printTrimmed(cursor);
    }

    public static String getType(J.MethodInvocation method) {
        return method.getSelect().getType().toString();
    }
}
