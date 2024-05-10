package com.gepardec.wor.lord.stdh.v2;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

public class ClassReferencingBinaryDtoToReferencingWebVisitor extends JavaIsoVisitor<ExecutionContext> {
    private String objectFactoryName;

    public ClassReferencingBinaryDtoToReferencingWebVisitor(String objectFactoryName) {
        this.objectFactoryName = objectFactoryName;
    }

    private static JavaTemplate NEW_OBJECT_FACTORY = JavaTemplate
            .builder("private static final ObjectFactory #{} = new ObjectFactory();")
            .build();

    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
        classDecl = super.visitClassDeclaration(classDecl, context);
        boolean hasObjectFactory = classDecl.getBody().getStatements().stream()
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .anyMatch(variableDeclarations -> variableDeclarations
                        .getVariables()
                        .get(0)
                        .getSimpleName()
                        .equals(objectFactoryName));

        if (hasObjectFactory) {
            return classDecl;
        }


        J.ClassDeclaration classDeclaration = NEW_OBJECT_FACTORY.apply(
                updateCursor(classDecl),
                classDecl.getBody().getCoordinates().firstStatement(),
                objectFactoryName
        );
        return classDeclaration;
    }
}
