package com.gepardec.wor.lord.call.ternaries;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

public class AddUseWebFlagToConfigClassVisitor extends JavaIsoVisitor<ExecutionContext> {
    private static final String CLASS_NAME = "ElgkkPropertiesUtil";
    private static final String USE_WEB_GETTER_NAME = "isUseWeb";
    private static final JavaTemplate USE_WEB_GETTER = JavaTemplate
            .builder("""
                    public static boolean #{}() {
                        return true;
                    }
                    """)
            .build();
    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDeclaration, ExecutionContext ctx) {
        classDeclaration = super.visitClassDeclaration(classDeclaration, ctx);
        
        if (hasName(classDeclaration, CLASS_NAME)) {
            return classDeclaration;
        }

        if (hasMethodWithName(classDeclaration, USE_WEB_GETTER_NAME)) {
            return classDeclaration;
        }

        return addUseWebGetter(classDeclaration, USE_WEB_GETTER_NAME);
    }

    /**
     * Example: For useWebGetterName = "isUseWeb" add following snippet to class:
     *     public static boolean isUseWeb() {
     *         return true;
     *     }
     *
     * @param classDeclaration Class Definition which the getter should be added to
     * @param useWebGetterName Name of the getter
     * @return Class Definition with added getter
     */
    @NotNull
    private J.ClassDeclaration addUseWebGetter(J.ClassDeclaration classDeclaration, String useWebGetterName) {
        return USE_WEB_GETTER.apply(
                updateCursor(classDeclaration),
                classDeclaration.getBody().getCoordinates().lastStatement(),
                useWebGetterName
        );
    }

    private static boolean hasName(J.ClassDeclaration classDeclaration, String className) {
        boolean isNotConfigClass = !classDeclaration.getName().getSimpleName().equals(className);
        return isNotConfigClass;
    }

    private static boolean hasMethodWithName(J.ClassDeclaration classDeclaration, String useWebGetterName) {
        boolean hasIsUseWebAlready = classDeclaration.getBody().getStatements().stream()
                .filter(J.MethodDeclaration.class::isInstance)
                .map(J.MethodDeclaration.class::cast)
                .anyMatch(methodDeclaration -> methodDeclaration.getName().getSimpleName().equals(useWebGetterName));
        return hasIsUseWebAlready;
    }
}
