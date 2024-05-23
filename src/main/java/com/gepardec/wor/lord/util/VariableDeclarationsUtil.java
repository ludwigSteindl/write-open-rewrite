package com.gepardec.wor.lord.util;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;
import java.util.Optional;

public class VariableDeclarationsUtil {
    public static String getType(J.VariableDeclarations dtoDeclarations) {
        return dtoDeclarations.getType().toString();
    }

    public static @NotNull Optional<J.VariableDeclarations> getDeclarationOfVariable(List<Statement> statements, String variable) {
        return Statements.extractStatementsOfType(statements, J.VariableDeclarations.class)
                .stream()
                .filter(variableDeclarations -> variableDeclarations.getVariables().get(0).getSimpleName().equals(variable))
                .findFirst();
    }

    public static @NotNull String getVariableName(J.VariableDeclarations variableDeclarations) {
        return variableDeclarations
                .getVariables()
                .get(0)
                .getSimpleName();
    }

    public static String getVariableDeclarationsType(J.VariableDeclarations variableDeclarations) {
        return variableDeclarations.getType().toString();
    }
}
