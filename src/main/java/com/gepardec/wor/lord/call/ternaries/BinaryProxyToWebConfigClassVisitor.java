package com.gepardec.wor.lord.call.ternaries;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

public class BinaryProxyToWebConfigClassVisitor extends JavaIsoVisitor<ExecutionContext> {
    private static final String CLASS_NAME = "ElgkkPropertiesUtil";
    private static final JavaTemplate USE_WEB_GETTER = JavaTemplate
            .builder("""
                    public static boolean isUseWeb() {
                        return true;
                    }
                    """)
            .build();
    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDeclaration, ExecutionContext ctx) {
        classDeclaration = super.visitClassDeclaration(classDeclaration, ctx);

        boolean isNotConfigClass = !classDeclaration.getName().getSimpleName().equals(CLASS_NAME);
        if (isNotConfigClass) {
            return classDeclaration;
        }

        boolean hasIsUseWebAlready = classDeclaration.getBody().getStatements().stream()
                .filter(J.MethodDeclaration.class::isInstance)
                .map(J.MethodDeclaration.class::cast)
                .anyMatch(methodDeclaration -> methodDeclaration.getName().getSimpleName().equals("isUseWeb"));
        if (hasIsUseWebAlready) {
            return classDeclaration;
        }

        return USE_WEB_GETTER.apply(
                updateCursor(classDeclaration),
                classDeclaration.getBody().getCoordinates().lastStatement()
        );
    }
}
