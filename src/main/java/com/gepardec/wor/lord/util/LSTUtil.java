package com.gepardec.wor.lord.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openrewrite.Cursor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.plaf.nimbus.State;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LSTUtil {
    private static final Logger LOG = LoggerFactory.getLogger(LSTUtil.class);
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

    public static String getPackageName(Cursor cursor) {
        return cursor.firstEnclosingOrThrow(J.CompilationUnit.class).getPackageDeclaration().getPackageName();
    }

    public static <T extends J> List<T> extractStatementsOfTypeRecursively(List<Statement> statements, Class<T> type) {
        List<T> statementsOfType = extractStatementsOfType(statements, type);

        List<T> subStatements = statements.stream()
                .map(s -> getSubstatements(type, s))
                .filter(Objects::nonNull)
                .flatMap(nextLayer -> extractStatementsOfTypeRecursively(nextLayer, type).stream())
                .collect(Collectors.toList());

        statementsOfType.addAll(subStatements);
        return statementsOfType;
    }

    @Nullable
    private static List<Statement> getSubstatements(Class<? extends J> type, Statement s) {
        if (!(s instanceof J.If)) {
            return null;
        }

        Statement then = ((J.If) s).getThenPart();
        if (!(then instanceof J.Block)) {
            if (!(then instanceof J.VariableDeclarations)) {
                return null;
            }
            return List.of(then);
        }

        J.Block block = (J.Block) then;
        return block.getStatements();
    }

    public static String getType(J.VariableDeclarations dtoDeclarations) {
        return dtoDeclarations.getType().toString();
    }

    public static String getType(J.MethodInvocation method) {
        JavaType.Method methodType = method.getMethodType();
        if (methodType == null)
            return null;

        return methodType.getDeclaringType().toString();
    }


    public static @NotNull List<Statement> getStatements(J.ClassDeclaration classDeclaration) {
        return classDeclaration.getBody().getStatements();
    }

    public static @NotNull List<Statement> getStatements(J.MethodDeclaration methodDeclaration) {
        return methodDeclaration.getBody().getStatements();
    }

    public static @NotNull List<String> getReturnTypes(List<J.MethodDeclaration> methods) {
        return methods.stream()
                .map(m -> m.getType().toString())
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
        if (type instanceof JavaType.Class clazz) {
            String className = clazz.getFullyQualifiedName();
            return !className.equals(method.getDeclaringType().toString());
        }
        return false;
    }

    public static JavaTemplate javaTemplateOf(String template, String... importType) {
        return JavaTemplate
                .builder(template)
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

    public static boolean hasVariableWithNameAlready(J.ClassDeclaration classDecl, String name) {
        return extractStatementsOfType(classDecl.getBody().getStatements(), J.VariableDeclarations.class)
                .stream()
                .anyMatch(declaration -> isVariableWithName(declaration, name));
    }

    public static boolean isVariableWithName(J.VariableDeclarations variableDeclarations, String name) {
        return variableDeclarations
                .getVariables()
                .stream()
                .anyMatch(variable -> variable.getName().toString().equals(name));
    }
}
