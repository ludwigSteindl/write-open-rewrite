package com.gepardec.wor.lord.stdh.v2;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WsdlScanner extends ScanningRecipe<WsdlScanner.Accumulator> {

    @Override
    public String getDisplayName() {
        return "Append to release notes";
    }

    @Override
    public String getDescription() {
        return "Adds the specified line to RELEASE.md.";
    }

    public static class Accumulator {
        List<Wsdl2JavaService> wsdl2JavaServices = new LinkedList<>();
    }

    @Override
    public WsdlScanner.Accumulator getInitialValue(ExecutionContext ctx) {
        return new WsdlScanner.Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(WsdlScanner.Accumulator acc) {
        return new JavaIsoVisitor<>() {
            @Override
            public @Nullable J.ClassDeclaration visitClassDeclaration(@Nullable J.ClassDeclaration classDeclaration, ExecutionContext ctx) {
                classDeclaration = super.visitClassDeclaration(classDeclaration, ctx);
                if(!classDeclaration.getKind().equals(J.ClassDeclaration.Kind.Type.Interface)) {
                    return classDeclaration;
                }

                String serviceAlias = classDeclaration.getSimpleName();

                List<J.MethodDeclaration> methods = classDeclaration.getBody().getStatements().stream()
                        .filter(J.MethodDeclaration.class::isInstance)
                        .map(J.MethodDeclaration.class::cast)
                        .collect(Collectors.toList());

                List<String> requestTypes = methods.stream().flatMap(m -> m.getParameters().stream())
                        .filter(J.VariableDeclarations.class::isInstance)
                        .map(J.VariableDeclarations.class::cast)
                        .map(J.VariableDeclarations::getType)
                        .map(type-> type.toString())
                        .distinct()
                        .collect(Collectors.toList());

                List<String> responseTypes = methods.stream()
                        .map(m -> m.getReturnTypeExpression().toString())
                                .collect(Collectors.toList());

                acc.wsdl2JavaServices.add(new Wsdl2JavaService(serviceAlias, requestTypes, responseTypes));

                return classDeclaration;
            }

        };
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor(WsdlScanner.Accumulator acc) {
        return new BinaryDtoToWebVisitor();
    }
}
