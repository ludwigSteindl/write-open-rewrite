package com.gepardec.wor.lord.util;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodDeclarations {
    public static @NotNull List<Statement> getStatements(J.MethodDeclaration methodDeclaration) {
        return methodDeclaration.getBody().getStatements();
    }

    public static @NotNull List<String> getReturnTypes(List<J.MethodDeclaration> methods) {
        return methods.stream()
                .map(m -> m.getReturnTypeExpression().toString())
                .collect(Collectors.toList());
    }

    public static @NotNull List<String> getParameterTypes(List<J.MethodDeclaration> methods) {
        return methods.stream()
                .flatMap(MethodDeclarations::extractStatementsOfType)
                .map(VariableDeclarationsUtil::getVariableDeclarationsType)
                .distinct()
                .collect(Collectors.toList());
    }

    public static Stream<J.VariableDeclarations> extractStatementsOfType(J.MethodDeclaration m) {
        return Statements.extractStatementsOfType(m.getParameters(), J.VariableDeclarations.class).stream();
    }

    public static List<J.MethodDeclaration> extractMethodDeclarations(List<Statement> statements) {
        return Statements.extractStatementsOfType(statements, J.MethodDeclaration.class);
    }
}
