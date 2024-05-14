package com.gepardec.wor.lord.stdh.v2.visitors;

import com.gepardec.wor.lord.stdh.v2.common.Accumulator;
import com.gepardec.wor.lord.stdh.v2.common.Wsdl2JavaService;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

import java.util.List;
import java.util.stream.Collectors;

public class Wsdl2JavaServiceScanningVisitor extends JavaIsoVisitor<ExecutionContext> {
    Accumulator accumulator;

    public Wsdl2JavaServiceScanningVisitor(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    @Override
    public @Nullable J.ClassDeclaration visitClassDeclaration(@Nullable J.ClassDeclaration classDeclaration,
                                                              ExecutionContext ctx) {

        classDeclaration = super.visitClassDeclaration(classDeclaration, ctx);
        String serviceAlias = classDeclaration.getSimpleName();

        if (!isInterface(classDeclaration)) {
            return classDeclaration;
        }

        List<J.MethodDeclaration> methods = getMethods(classDeclaration);

        List<String> requestTypes = getParameterTypes(methods);
        List<String> responseTypes = getReturnTypes(methods);

        Wsdl2JavaService service = new Wsdl2JavaService(serviceAlias, requestTypes, responseTypes);
        accumulator.addService(service);

        return classDeclaration;
    }

    private static boolean isInterface(J.ClassDeclaration classDeclaration) {
        return classDeclaration.getKind().equals(J.ClassDeclaration.Kind.Type.Interface);
    }

    private static @NotNull List<J.MethodDeclaration> getMethods(J.ClassDeclaration classDeclaration) {
        return classDeclaration.getBody().getStatements().stream()
                .filter(J.MethodDeclaration.class::isInstance)
                .map(J.MethodDeclaration.class::cast)
                .collect(Collectors.toList());
    }

    private static @NotNull List<String> getReturnTypes(List<J.MethodDeclaration> methods) {
        return methods.stream()
                .map(m -> m.getReturnTypeExpression().toString())
                .collect(Collectors.toList());
    }

    private static @NotNull List<String> getParameterTypes(List<J.MethodDeclaration> methods) {
        List<String> requestTypes = methods.stream().flatMap(m -> m.getParameters().stream())
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .map(J.VariableDeclarations::getType)
                .map(type-> type.toString())
                .distinct()
                .collect(Collectors.toList());
        return requestTypes;
    }
}
