package com.gepardec.wor.lord.common.search;

import com.gepardec.wor.lord.common.Accessor;
import com.gepardec.wor.lord.common.Accumulator;
import com.gepardec.wor.lord.common.Wsdl2JavaService;
import com.gepardec.wor.lord.util.LSTUtil;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;
import org.openrewrite.java.tree.TypeTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WSDLTypesSearch extends JavaIsoVisitor<ExecutionContext> {
    private final static String PACKAGE_PREFIX = "at.sozvers.stp.lgkk.a02";

    private Accumulator accumulator;

    private static final Logger LOG = LoggerFactory.getLogger(WSDLTypesSearch.class);

    public WSDLTypesSearch(Accumulator accumulator) {
        this.accumulator = accumulator;
    }


    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl,
                                                              ExecutionContext ctx) {
        J.ClassDeclaration classDeclaration = super.visitClassDeclaration(classDecl, ctx);

        if (classDeclaration.getType() == null) {
            return classDeclaration;
        }

        if (!isWsdlService(classDeclaration)) {
            return classDeclaration;
        }

        if (isRoot(classDeclaration)) {
            addRootToAccumulator(classDeclaration);
            return classDeclaration;
        }

        addAccesssorsToAccumulator(classDeclaration);

        return classDeclaration;
    }

    private static boolean isRoot(J.ClassDeclaration classDeclaration) {
        return LSTUtil.isInterface(classDeclaration);
    }

    private static boolean isWsdlService(J.ClassDeclaration classDeclaration) {
        return classDeclaration.getType().getPackageName().startsWith(PACKAGE_PREFIX);
    }

    private void addAccesssorsToAccumulator(J.ClassDeclaration classDeclaration) {
        List<Accessor> accessors = LSTUtil.extractStatementsOfType(classDeclaration.getBody().getStatements(), J.MethodDeclaration.class)
                .stream()
                .map(methodDeclaration -> Accessor
                        .builder(
                                methodDeclaration.getSimpleName(),
                                classDeclaration.getSimpleName())
                        .type(methodDeclaration.getReturnTypeExpression() == null ?
                                getParameterType(methodDeclaration, 0) :
                                methodDeclaration.getReturnTypeExpression().toString())
                        .build())
                .collect(Collectors.toList());


        Wsdl2JavaService wsdl2JavaService = getWsdl2JavaService(classDeclaration);
        wsdl2JavaService.addAccessors(accessors);
        LOG.info("Found {} accessors for {}", accessors.size(), classDeclaration.getSimpleName());
    }

    private String getParameterType(J.MethodDeclaration methodDeclaration, int parameterIndex) {
        Statement statement = methodDeclaration.getParameters().get(parameterIndex);

        if (statement == null) {
            return null;
        }

        if (!(statement instanceof J.VariableDeclarations)) {
            return null;
        }

        J.VariableDeclarations declarations = (J.VariableDeclarations) statement;
        TypeTree typeExpression = declarations.getTypeExpression();

        return typeExpression == null ? null : typeExpression.toString();
    }

    @NotNull
    private Wsdl2JavaService getWsdl2JavaService(J.ClassDeclaration classDeclaration) {
        if (classDeclaration.getType() == null) {
            return new Wsdl2JavaService("", List.of(), List.of());
        }
        Optional<Wsdl2JavaService> service = accumulator.getService(classDeclaration.getType().getPackageName());
        Wsdl2JavaService wsdl2JavaService;
        if (service.isEmpty()) {
            List<J.MethodDeclaration> methods =  LSTUtil.extractStatementsOfType(classDeclaration.getBody().getStatements(), J.MethodDeclaration.class);
            wsdl2JavaService = createService(methods, getServiceAlias(classDeclaration));
            accumulator.addService(classDeclaration.getType().getPackageName(), wsdl2JavaService);
        } else {
            wsdl2JavaService = service.get();
        }
        return wsdl2JavaService;
    }

    private static String getServiceAlias(J.ClassDeclaration classDeclaration) {
        String packageName = classDeclaration.getType().getPackageName();

        return LSTUtil.shortNameOfFullyQualified(packageName);
    }

    private void addRootToAccumulator(J.ClassDeclaration classDeclaration) {
        Wsdl2JavaService service = getWsdl2JavaService(classDeclaration);
        List<Accessor> accessors = LSTUtil.extractStatementsOfType(classDeclaration.getBody().getStatements(), J.MethodDeclaration.class)
                .stream()
                .map(methodDeclaration -> createAccessors(classDeclaration, methodDeclaration))
                .flatMap(List::stream)
                .collect(Collectors.toList());
        service.setRoot(accessors);

        List<J.MethodDeclaration> methods = LSTUtil.extractStatementsOfType(classDeclaration.getBody().getStatements(), J.MethodDeclaration.class);
        List<String> requestTypes = LSTUtil.getParameterTypes(methods);
        List<String> responseTypes = LSTUtil.getReturnTypes(methods);
        service.setRequestTypes(requestTypes);
        service.setResponseTypes(responseTypes);
        accumulator.addService(classDeclaration.getType().getPackageName(), service);
        LOG.info("Found WSDL Class root {} with {} accessors", classDeclaration.getSimpleName(), accessors.size());
    }

    @NotNull
    private static List<Accessor> createAccessors(J.ClassDeclaration classDeclaration, J.MethodDeclaration methodDeclaration) {
        List<Accessor> accessors = new ArrayList<>();
        String returnType = methodDeclaration.getReturnTypeExpression().toString();
        if (!returnType.equals("void")) {
            accessors.add(Accessor
                    .builder(
                            methodDeclaration.getSimpleName(),
                            classDeclaration.getSimpleName())
                    .type(methodDeclaration.getReturnTypeExpression().toString())
                    .build());
        }
        Statement param1 = methodDeclaration.getParameters().get(0);
        if (param1 instanceof J.Empty) {
            return accessors;
        }
        accessors.add(Accessor
            .builder(
                    methodDeclaration.getSimpleName(),
                    classDeclaration.getSimpleName())
            .type(((J.VariableDeclarations) param1).getTypeExpression().toString())
            .build());
        return accessors;
    }

    private static @NotNull Wsdl2JavaService createService(List<J.MethodDeclaration> methods, String className) {
        List<String> requestTypes = LSTUtil.getParameterTypes(methods);
        List<String> responseTypes = LSTUtil.getReturnTypes(methods);
        Wsdl2JavaService service = new Wsdl2JavaService(className, requestTypes, responseTypes);
        return service;
    }
}
