package com.gepardec.wor.lord.stdh.v2.visitors;

import com.gepardec.wor.lord.util.ParserUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.List;

public class ClassUsingBinaryDtoToWebVisitor extends JavaIsoVisitor<ExecutionContext> {
    private String objectFactoryName;
    private String objectFactoryType;
    private JavaTemplate newObjectFactory;

    private final static String NEW_OBJECT_FACTORY_TEMPLATE = "private static final ObjectFactory %s = new ObjectFactory();";

    public ClassUsingBinaryDtoToWebVisitor(String objectFactoryName, String objectFactoryPackage) {
        this.objectFactoryName = objectFactoryName;
        this.objectFactoryType = objectFactoryPackage + ".ObjectFactory";

        String newObjectFactoryStatement = String.format(NEW_OBJECT_FACTORY_TEMPLATE, objectFactoryName);
        this.newObjectFactory = JavaTemplate
                .builder(newObjectFactoryStatement)
                .javaParser(ParserUtil.createParserWithRuntimeClasspath())
                .imports(this.objectFactoryType)
                .build();
    }

    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
        classDecl = super.visitClassDeclaration(classDecl, context);
        boolean hasObjectFactory = classDecl.getBody().getStatements().stream()
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .filter(variableDeclarations -> variableDeclarations.getType().toString().equals(objectFactoryType))
                .anyMatch(variableDeclarations -> variableDeclarations
                        .getVariables()
                        .get(0)
                        .getSimpleName()
                        .equals(objectFactoryName));

        if (hasObjectFactory) {
            return classDecl;
        }

        maybeAddImport(objectFactoryType);
        return newObjectFactory.apply(
                updateCursor(classDecl),
                classDecl.getBody().getCoordinates().firstStatement()
        );
    }
}
