package com.gepardec.wor.lord.stdh.v1;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;

public class BinaryStdhToWebVisitor extends JavaIsoVisitor<ExecutionContext> {
    private static final JavaTemplate NEW_OBJECT_FACTORY = JavaTemplate.builder("ObjectFactory objectFactory = new ObjectFactory();\n")
            .javaParser(JavaParser.fromJavaVersion().dependsOn(
                    "package javax.xml.bind;\n" +
                            "public class ObjectFactory {\n" +
                            "    public OmStandardRequestHeader createOmStandardRequestHeader() {\n" +
                            "        return new OmStandardRequestHeader();\n" +
                            "    }\n" +
                            "}\n" +
                            "public class OmStandardRequestHeader {\n" +
                            "    public void setZvst(String zvst) {\n" +
                            "    }\n" +
                            "}\n"))
            .contextSensitive()
            .build();

    private static final JavaTemplate NEW_STDH = JavaTemplate.builder("OmStandardRequestHeader stdh = objectFactory.createOmStandardRequestHeader();\n")
            .javaParser(JavaParser.fromJavaVersion().dependsOn(
                    "package javax.xml.bind;\n" +
                            "public class ObjectFactory {\n" +
                            "    public OmStandardRequestHeader createOmStandardRequestHeader() {\n" +
                            "        return new OmStandardRequestHeader();\n" +
                            "    }\n" +
                            "}\n" +
                            "public class OmStandardRequestHeader {\n" +
                            "    public void setZvst(String zvst) {\n" +
                            "    }\n" +
                            "}\n"))
            .contextSensitive()
            .build();

    private static final JavaTemplate NEW_STDH_SET = JavaTemplate.builder("stdh.setZvst(#{});\n")
            .javaParser(JavaParser.fromJavaVersion().dependsOn(
                    "package javax.xml.bind;\n" +
                            "public class ObjectFactory {\n" +
                            "    public OmStandardRequestHeader createOmStandardRequestHeader() {\n" +
                            "        return new OmStandardRequestHeader();\n" +
                            "    }\n" +
                            "}\n" +
                            "public class OmStandardRequestHeader {\n" +
                            "    public void setZvst(String zvst) {\n" +
                            "    }\n" +
                            "}\n"))
            .contextSensitive()
            .build();

    private static final JavaTemplate NEW_LAQAMHSU = JavaTemplate.builder("Laqamhsu #{} = new Laqamhsu();\n")
            .javaParser(JavaParser.fromJavaVersion().dependsOn(
                    "package javax.xml.bind;\n" +
                            "public class Laqamhsu {\n" +
                            "}\n"))
            .contextSensitive()
            .build();

    private static final JavaTemplate SET_OM_STANDARD_REQUEST_HEADER = JavaTemplate.builder("#{}.setOmStandardRequestHeader(stdh);\n")
            .javaParser(JavaParser.fromJavaVersion().dependsOn(
                    "package javax.xml.bind;\n" +
                            "public class Laqamhsu {\n" +
                            "    public void setOmStandardRequestHeader(OmStandardRequestHeader stdh) {\n" +
                            "    }\n" +
                            "}\n" +
                            "public class OmStandardRequestHeader {\n" +
                            "    public void setZvst(String zvst) {\n" +
                            "    }\n" +
                            "}\n"))
            .contextSensitive()
            .build();

    @Override
    @NotNull
    public J.MethodDeclaration visitMethodDeclaration(@NotNull J.MethodDeclaration method, @NotNull ExecutionContext ctx) {
        J.MethodDeclaration methodDeclaration = super.visitMethodDeclaration(method, ctx);

        boolean usesStd = methodDeclaration.getBody().getStatements().stream()
                .anyMatch(statement -> statement.print(getCursor()).contains("setZvst"));

        if (!usesStd) {
            return methodDeclaration;
        }

        boolean hasObjectFactory = methodDeclaration.getBody().getStatements().stream()
                .anyMatch(statement -> statement.print(getCursor()).contains("ObjectFactory objectFactory = new ObjectFactory()"));


        List<J.MethodInvocation> setZvstCalls = methodDeclaration.getBody().getStatements().stream()
                .filter(J.MethodInvocation.class::isInstance)
                .map(J.MethodInvocation.class::cast)
                .filter(methodInvocation -> methodInvocation.getSimpleName().contains("setZvst"))
                .filter(methodInvocation -> !methodInvocation.print(getCursor()).contains("stdh.setZvst"))
                .toList();


        if (setZvstCalls.isEmpty()) {
            return methodDeclaration;
        }

        boolean hasStdh = methodDeclaration.getBody().getStatements().stream()
                .anyMatch(statement -> statement.print(getCursor()).contains("OmStandardRequestHeader stdh = objectFactory.createOmStandardRequestHeader()"));

        if (!hasStdh) {
            methodDeclaration = NEW_STDH.apply(updateCursor(methodDeclaration), methodDeclaration.getBody().getCoordinates().firstStatement());
        }

        if (!hasObjectFactory) {
            methodDeclaration = NEW_OBJECT_FACTORY.apply(updateCursor(methodDeclaration), methodDeclaration.getBody().getCoordinates().firstStatement());
        }

        for (J.MethodInvocation methodInvocation : setZvstCalls) {
            Expression arg = methodInvocation.getArguments().get(0);
            methodDeclaration = NEW_STDH_SET.apply(updateCursor(methodDeclaration), methodInvocation.getCoordinates().replace(), arg);
        }

        boolean hasSetStdh = methodDeclaration.getBody().getStatements().stream()
                .anyMatch(statement -> statement.print(getCursor()).contains(".setOmStandardRequestHeader"));
        if (hasSetStdh) {
            return methodDeclaration;
        }

        String varName = methodDeclaration.getBody().getStatements().stream()
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .filter(declaration -> declaration.getTypeExpression().print(getCursor()).contains("Laqamhsu"))
                .findFirst()
                .get()
                .getVariables()
                .get(0)
                .getSimpleName()
                .toString();

        Statement lastInvocation = methodDeclaration.getBody().getStatements().stream()
                .filter(statement -> statement.print(getCursor()).contains("stdh"))
                .reduce((first, second) -> second)
                .get();

        return SET_OM_STANDARD_REQUEST_HEADER.apply(updateCursor(methodDeclaration), lastInvocation.getCoordinates().after(), varName);
    }

    @Override
    public J.VariableDeclarations visitVariableDeclarations(@NotNull J.VariableDeclarations declarations, @NotNull ExecutionContext ctx) {
        J.VariableDeclarations variableDeclarations = super.visitVariableDeclarations(declarations, ctx);

        String type = variableDeclarations.getTypeExpression().toString();

        if (type.equals("LaqamhsuDto")) {
            String varName = variableDeclarations.getVariables().get(0).getName().getSimpleName().toString();
            return NEW_LAQAMHSU.apply(updateCursor(variableDeclarations), variableDeclarations.getCoordinates().replace(), varName);
        }
        return variableDeclarations;
    }
}
