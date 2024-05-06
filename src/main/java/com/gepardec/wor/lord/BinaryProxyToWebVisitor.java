package com.gepardec.wor.lord;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;

public class BinaryProxyToWebVisitor extends JavaIsoVisitor<ExecutionContext> {
    public static final String METHOD_NAME = "callSvcProxy";


    private final JavaTemplate NEW_BOOLEAN = JavaTemplate.builder("final boolean useWeb = true;\n")
            .contextSensitive()
            .build();
    private final JavaTemplate IF_INITIALIZATION = JavaTemplate.builder(
                    """
                            if(useWeb) {
                                AuMhHostInfoResponseDto #{} = callSvcWeb(#{});
                            } else {
                                #{any()};
                            }
                            """)
            .contextSensitive()
            .build();
    private final JavaTemplate IF_RETURN = JavaTemplate.builder(
                    """
                            if(useWeb) {
                                return callSvcWeb(#{});
                            } else {
                                #{any()};
                            }
                            """)
            .contextSensitive()
            .build();
    private final JavaTemplate IF_ASSIGNMENT = JavaTemplate.builder(
                    """
                            if(useWeb) {
                                #{} = callSvcWeb(#{});
                            } else {
                                #{any()};
                            }
                            """)
            .contextSensitive()
            .build();
    private final JavaTemplate IF_INVOCATION = JavaTemplate.builder(
                    """
                            if(useWeb) {
                                callSvcWeb(#{});
                            } else {
                                #{any()};
                            }
                            """)
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
        String argName = null;
        if (methodInvocation.getArguments().get(0) instanceof J.Identifier identifier) {
            argName = identifier.getSimpleName();
        }
        methodDeclaration = IF_INVOCATION.apply(
                updateCursor(methodDeclaration),
                methodInvocation.getCoordinates().replace(),
                argName,
                statement);
        return methodDeclaration;
    }

    private J.MethodDeclaration addWebAssignment(J.Assignment assignment, J.MethodDeclaration methodDeclaration) {
        String argName = null;
        if (assignment.getAssignment() instanceof J.MethodInvocation methodInvocation) {
            if (methodInvocation.getArguments().get(0) instanceof J.Identifier identifier) {
                argName = identifier.getSimpleName();
            }

        }
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
        String argName = null;
        if (returnStatement.getExpression() instanceof J.MethodInvocation methodInvocation) {
            if (methodInvocation.getArguments().get(0) instanceof J.Identifier identifier) {
                argName = identifier.getSimpleName();
            }
        }
        methodDeclaration = IF_RETURN.apply(
                updateCursor(methodDeclaration),
                returnStatement.getCoordinates().replace(),
                argName,
                returnStatement);
        return methodDeclaration;
    }

    private J.MethodDeclaration addWebDeclaration(J.VariableDeclarations decl, J.MethodDeclaration methodDeclaration) {
        String argName = decl.print().split(METHOD_NAME + "\\(")[1];
        argName = argName.split("\\)")[0];

        String varName = decl.getVariables().get(0).getName().getSimpleName();
        methodDeclaration = IF_INITIALIZATION.apply(
                updateCursor(methodDeclaration),
                decl.getCoordinates().replace(),
                varName,
                argName,
                decl);
        return methodDeclaration;
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
