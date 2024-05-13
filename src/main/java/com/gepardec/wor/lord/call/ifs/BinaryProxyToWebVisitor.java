package com.gepardec.wor.lord.call.ifs;

import com.gepardec.wor.lord.util.ParserUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;

public class BinaryProxyToWebVisitor extends JavaIsoVisitor<ExecutionContext> {
    public static final String METHOD_NAME = "callSvcProxy";

    private final JavaTemplate NEW_BOOLEAN = JavaTemplate.builder("final boolean useWeb = true;\n")
            .contextSensitive()
            .build();

    private final static String IF_TEMPLATE = """
                            if(useWeb) {
                                %s
                            } else {
                                #{any()};
                            }
                            """;
    private final static JavaTemplate IF_INITIALIZATION = JavaTemplate
            .builder(IF_TEMPLATE.formatted("AuMhHostInfoResponseDto #{} = callSvcWeb(#{});"))
            .javaParser(ParserUtil.createParserWithRuntimeClasspath())
            .contextSensitive()
            .build();
    private final static JavaTemplate IF_RETURN = JavaTemplate
            .builder(IF_TEMPLATE.formatted("return callSvcWeb(#{});"))
            .javaParser(ParserUtil.createParserWithRuntimeClasspath())
            .contextSensitive()
            .build();
    private final static JavaTemplate IF_ASSIGNMENT = JavaTemplate
            .builder(IF_TEMPLATE.formatted("#{} = callSvcWeb(#{});"))
            .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
            .contextSensitive()
            .build();
    private final JavaTemplate IF_INVOCATION = JavaTemplate
            .builder(IF_TEMPLATE.formatted("callSvcWeb(#{});"))
            .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
            .contextSensitive()
            .build();


    @Override
    @NotNull
    public J.MethodDeclaration visitMethodDeclaration(@NotNull J.MethodDeclaration method, @NotNull ExecutionContext ctx) {
        J.MethodDeclaration methodDeclaration = super.visitMethodDeclaration(method, ctx);

        List<Statement> statements = methodDeclaration.getBody().getStatements();
        List<Statement> statementsWithInvocation = getStatementsWithInvocation(statements);
        if (statementsWithInvocation.isEmpty()) {
            return methodDeclaration;
        }

        if (!hasUseWeb(statements)) {
            methodDeclaration = addUseWebBoolean(methodDeclaration);
        }

        for (Statement statement : statementsWithInvocation) {
            if (statement instanceof J.VariableDeclarations declaration) {
                methodDeclaration = addWebDeclaration(declaration, methodDeclaration);
            }
            if (statement instanceof J.Return returnStatement) {
                methodDeclaration = addWebReturn(returnStatement, methodDeclaration);
            }
            if (statement instanceof J.Assignment assignment) {
                methodDeclaration = addWebAssignment(assignment, methodDeclaration);
            }
            if (statement instanceof J.MethodInvocation methodInvocation) {
                methodDeclaration = addWebInvocation(statement, methodInvocation, methodDeclaration);
            }
        }

        return methodDeclaration;
    }

    private J.MethodDeclaration addWebInvocation(Statement statement, J.MethodInvocation methodInvocation, J.MethodDeclaration methodDeclaration) {
        String argName = getArgumentName(methodInvocation);
        methodDeclaration = IF_INVOCATION.apply(
                updateCursor(methodDeclaration),
                methodInvocation.getCoordinates().replace(),
                argName,
                statement);
        return methodDeclaration;
    }

    @Nullable
    private static String getArgumentName(J.MethodInvocation methodInvocation) {
        String argName = null;
        if (methodInvocation.getArguments().get(0) instanceof J.Identifier identifier) {
            argName = identifier.getSimpleName();
        }
        return argName;
    }

    private J.MethodDeclaration addWebAssignment(J.Assignment assignment, J.MethodDeclaration methodDeclaration) {
        String argName = getArgumentName(assignment.getAssignment());
        String varName = assignment.getVariable().toString();
        methodDeclaration = IF_ASSIGNMENT.apply(
                updateCursor(methodDeclaration),
                assignment.getCoordinates().replace(),
                varName,
                argName,
                assignment);
        return methodDeclaration;
    }

    private J.MethodDeclaration addWebReturn(J.Return returnStatement, J.MethodDeclaration methodDeclaration) {
        String argName = getArgumentName(returnStatement.getExpression());
        methodDeclaration = IF_RETURN.apply(
                updateCursor(methodDeclaration),
                returnStatement.getCoordinates().replace(),
                argName,
                returnStatement);
        return methodDeclaration;
    }

    @Nullable
    private static String getArgumentName(Expression expression) {
        String argName = null;
        if (expression instanceof J.MethodInvocation methodInvocation) {
            if (methodInvocation.getArguments().get(0) instanceof J.Identifier identifier) {
                argName = identifier.getSimpleName();
            }
        }
        return argName;
    }

    private J.MethodDeclaration addWebDeclaration(J.VariableDeclarations variableDeclarations, J.MethodDeclaration methodDeclaration) {
        String argName = variableDeclarations.print(getCursor()).split(METHOD_NAME + "\\(")[1];
        argName = argName.split("\\)")[0];

        String varName = variableDeclarations.getVariables().get(0).getName().getSimpleName();
        return IF_INITIALIZATION.apply(
                updateCursor(methodDeclaration),
                variableDeclarations.getCoordinates().replace(),
                varName,
                argName,
                variableDeclarations);
    }

    @NotNull
    private J.MethodDeclaration addUseWebBoolean(J.MethodDeclaration methodDeclaration) {
        return NEW_BOOLEAN.apply(
                updateCursor(methodDeclaration),
                methodDeclaration.getBody().getCoordinates().firstStatement());
    }

    @NotNull
    private List<Statement> getStatementsWithInvocation(List<Statement> statements) {
        List<Statement> invocations = statements
                .stream()
                .filter(t -> t.print(getCursor()).contains(METHOD_NAME))
                .toList();
        return invocations;
    }

    private boolean hasUseWeb(List<Statement> statements) {
        String first = statements.get(0) + ";";
        return first.equals(NEW_BOOLEAN.getCode());
    }

}
