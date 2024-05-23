package com.gepardec.wor.lord.util;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.Cursor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LSTUtil {
    public static boolean isInterface(J.ClassDeclaration classDeclaration) {
        return classDeclaration.getKind().equals(J.ClassDeclaration.Kind.Type.Interface);
    }

    public static @NotNull List<J.MethodDeclaration> getMethodDeclarations(J.ClassDeclaration classDeclaration) {
        return extractMethodDeclarations(getStatements(classDeclaration));
    }

    public static List<J.MethodDeclaration> extractMethodDeclarations(List<Statement> statements) {
        return extractStatementsOfType(statements, J.MethodDeclaration.class);
    }

    public static <T extends J> List<T> extractStatementsOfType(List<Statement> statements, Class<T> type) {
        return statements.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    public static String getType(J.VariableDeclarations dtoDeclarations) {
        return dtoDeclarations.getType().toString();
    }

    public static String getType(J.MethodInvocation method) {
        return method.getSelect().getType().toString();
    }


    public static @NotNull List<Statement> getStatements(J.ClassDeclaration classDeclaration) {
        return classDeclaration.getBody().getStatements();
    }

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
                .flatMap(LSTUtil::extractStatementsOfType)
                .map(LSTUtil::getVariableDeclarationsType)
                .distinct()
                .collect(Collectors.toList());
    }

    private static Stream<J.VariableDeclarations> extractStatementsOfType(J.MethodDeclaration m) {
        return extractStatementsOfType(m.getParameters(), J.VariableDeclarations.class).stream();
    }

    public static String getVariableDeclarationsType(J.VariableDeclarations variableDeclarations) {
        return variableDeclarations.getType().toString();
    }

    public static boolean methodNameStartsWith(JavaType.Method method, String get) {
        return method.getName().startsWith(get);
    }

    public static boolean declaringTypeIsNotType(JavaType.Method method, JavaType type) {
        if (type instanceof JavaType.Class) {
            JavaType.Class clazz = (JavaType.Class) type;
            String className = clazz.getFullyQualifiedName();
            return !className.equals(method.getDeclaringType().toString());
        }
        return false;
    }

    public static @NotNull Optional<J.VariableDeclarations> getDeclarationOfVariable(List<Statement> statements, String variable) {
        return extractStatementsOfType(statements, J.VariableDeclarations.class)
                .stream()
                .filter(variableDeclarations -> variableDeclarations.getVariables().get(0).getSimpleName().equals(variable))
                .findFirst();
    }

    public static JavaTemplate javaTemplateOf(String template, String... importType) {
        return JavaTemplate
                .builder(template)
                .javaParser(ParserUtil.createParserWithRuntimeClasspath())
                .imports(importType)
                .contextSensitive()
                .build();
    }

    public static String packageOf(String type) {
        return type.substring(0, type.lastIndexOf('.'));
    }

    public static Optional<String> getFirstArgument(J.MethodInvocation method, Cursor cursor) {
        String argument = method.getArguments().get(0).printTrimmed(cursor);
        return argument.equals("") ? Optional.empty() : Optional.of(argument);
    }

    public static @NotNull String getVariableName(J.VariableDeclarations variableDeclarations) {
        return variableDeclarations
                .getVariables()
                .get(0)
                .getSimpleName();
    }

    public static String shortNameOfFullyQualified(String fullyQualifiedName) {
        return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
    }
}
