package com.gepardec.wor.lord.dto.visitors.transform;

import com.gepardec.wor.lord.util.LSTUtil;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ObjectFactoryCreator extends JavaIsoVisitor<ExecutionContext> {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectFactoryCreator.class);

    // Template for creating a new instance of ObjectFactory
    // pos1: object factory name
    private final static String NEW_OBJECT_FACTORY_TEMPLATE = "private static final ObjectFactory %s = new ObjectFactory();";

    public static final String OBJECT_FACTORY_TYPE_SUFFIX = ".ObjectFactory";
    private String objectFactoryName;
    private String objectFactoryType;
    private JavaTemplate newObjectFactory;


    public ObjectFactoryCreator(String objectFactoryName, String objectFactoryPackage) {
        this.objectFactoryName = objectFactoryName;
        this.objectFactoryType = objectFactoryPackage + OBJECT_FACTORY_TYPE_SUFFIX;

        String newObjectFactoryStatement = String.format(NEW_OBJECT_FACTORY_TEMPLATE, objectFactoryName);
        this.newObjectFactory = LSTUtil.javaTemplateOf(newObjectFactoryStatement, this.objectFactoryType);
    }

    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
        classDecl = super.visitClassDeclaration(classDecl, context);

        if (hasObjectFactory(classDecl)) {
            return classDecl;
        }

        LOG.info("Adding " + objectFactoryType + " " + objectFactoryName + " as a static member to class " + classDecl);
        maybeAddImport(objectFactoryType);
        return addObjectFactory(classDecl);
    }

    private J.@NotNull ClassDeclaration addObjectFactory(J.ClassDeclaration classDecl) {
        return newObjectFactory.apply(
                updateCursor(classDecl),
                classDecl.getBody().getCoordinates().firstStatement()
        );
    }

    private boolean hasObjectFactory(J.ClassDeclaration classDecl) {
        return getVariableDeclarations(classDecl)
                .stream()
                .filter(variableDeclarations -> LSTUtil.getType(variableDeclarations).equals(objectFactoryType))
                .anyMatch(variableDeclarations -> LSTUtil.getVariableName(variableDeclarations).equals(objectFactoryName));
    }

    private static List<J.VariableDeclarations> getVariableDeclarations(J.ClassDeclaration classDecl) {
        return LSTUtil
                .extractStatementsOfType(LSTUtil.getStatements(classDecl), J.VariableDeclarations.class);
    }

}
