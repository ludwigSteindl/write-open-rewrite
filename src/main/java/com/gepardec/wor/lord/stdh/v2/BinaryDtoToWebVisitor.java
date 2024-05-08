package com.gepardec.wor.lord.stdh.v2;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;
import java.util.Optional;

public class BinaryDtoToWebVisitor extends JavaIsoVisitor<ExecutionContext> {
    private String variableName;
    private static final String OBJECT_FACTORY_NAME = "objectFactory";

    private static final String NEW_WEB_DTO_TEMPLATE = """
            Laqamhsu #{} = new Laqamhsu();
            #{}.setOmStandardRequestHeader(%s.createOmStandardRequestHeader());
            """;

    private static final JavaTemplate NEW_WEB_DTO = JavaTemplate
            .builder(NEW_WEB_DTO_TEMPLATE.formatted(OBJECT_FACTORY_NAME))
            .build();

    public BinaryDtoToWebVisitor(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
        List<Statement> statements = method.getBody().getStatements();
        Optional<J.VariableDeclarations> dtoDeclarations = statements.stream()
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .filter(variableDeclarations -> variableDeclarations.getVariables().getFirst().getSimpleName().equals(variableName))
                .filter(variableDeclarations -> !variableDeclarations.getTypeExpression().toString().equals("Laqamhsu"))
                .findFirst();

        if (dtoDeclarations.isEmpty()) {
            return method;
        }

        doAfterVisit(new ClassReferencingBinaryDtoToReferencingWebVisitor(OBJECT_FACTORY_NAME));

        return NEW_WEB_DTO.apply(
                updateCursor(method),
                dtoDeclarations.get().getCoordinates().replace(),
                variableName,
                variableName
                );
    }

}
