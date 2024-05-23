package com.gepardec.wor.lord.util;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;

public class ClassDeclarations {
    public static boolean isInterface(J.ClassDeclaration classDeclaration) {
        return classDeclaration.getKind().equals(J.ClassDeclaration.Kind.Type.Interface);
    }

    public static @NotNull List<J.MethodDeclaration> getMethodDeclarations(J.ClassDeclaration classDeclaration) {
        return MethodDeclarations.extractMethodDeclarations(classDeclaration.getBody().getStatements());
    }

}
