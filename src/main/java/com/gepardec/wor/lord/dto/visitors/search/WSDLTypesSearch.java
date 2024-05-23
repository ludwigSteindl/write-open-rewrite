package com.gepardec.wor.lord.dto.visitors.search;

import com.gepardec.wor.lord.dto.common.Accessor;
import com.gepardec.wor.lord.dto.common.Accumulator;
import com.gepardec.wor.lord.dto.common.Wsdl2JavaService;
import com.gepardec.wor.lord.util.LSTUtil;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class WSDLTypesSearch extends JavaIsoVisitor<ExecutionContext> {

    private Accumulator accumulator;

    private static final Logger LOG = LoggerFactory.getLogger(WSDLTypesSearch.class);

    public WSDLTypesSearch(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    @Override
    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl,
                                                              ExecutionContext ctx) {
        J.ClassDeclaration classDeclaration = super.visitClassDeclaration(classDecl, ctx);
        String packagePrefix = "at.sozvers.stp.lgkk.a02";
        if (!classDeclaration.getType().getPackageName().startsWith(packagePrefix)) {
            return classDeclaration;
        }

        if (LSTUtil.isInterface(classDeclaration)) {
            addRootToAccumulator(classDeclaration);
            return classDeclaration;
        }

        List<Accessor> accessors = LSTUtil.extractStatementsOfType(classDeclaration.getBody().getStatements(), J.MethodDeclaration.class)
                .stream()
                .map(methodDeclaration -> Accessor
                        .builder(
                                methodDeclaration.getSimpleName(),
                                classDeclaration.getSimpleName())
                        .type(methodDeclaration.getReturnTypeExpression() == null ? "" : methodDeclaration.getReturnTypeExpression().toString())
                        .build())
                .collect(Collectors.toList());


        Wsdl2JavaService wsdl2JavaService = getWsdl2JavaService(classDeclaration);
        wsdl2JavaService.addAccessors(accessors);

        return classDeclaration;
    }

    @NotNull
    private Wsdl2JavaService getWsdl2JavaService(J.ClassDeclaration classDeclaration) {
        Optional<Wsdl2JavaService> service = accumulator.getService(classDeclaration.getType().getPackageName());
        Wsdl2JavaService wsdl2JavaService;
        if (service.isEmpty()) {
            List<J.MethodDeclaration> methods =  LSTUtil.extractStatementsOfType(classDeclaration.getBody().getStatements(), J.MethodDeclaration.class);
            wsdl2JavaService = createService(methods, classDeclaration.getSimpleName());
            accumulator.addService(classDeclaration.getType().getPackageName(), wsdl2JavaService);
        } else {
            wsdl2JavaService = service.get();
        }
        return wsdl2JavaService;
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

    List<Accessor> createAccessors(J.ClassDeclaration classDeclaration) {
        List<JavaType.Method> methods = classDeclaration.getType().getMethods();
        return createAccessors(methods, null, classDeclaration.getSimpleName()).stream()
                .filter(accessor -> !accessor.getClazz().equals(classDeclaration.getType().toString()))
                .collect(Collectors.toList());
    }

    private static List<Accessor> createAccessors(List<JavaType.Method> methods, Accessor parent, String rootName) {
        Optional<Accessor> filteredParent = removeRootParent(parent, rootName);
        JAXBElement<String> elem = new JAXBElement<>(null, String.class, "a");

        List<Accessor> accessors = extractMethodsNotReturningSubDtos(methods, filteredParent);

        List<JavaType.Method> methodsReturningSubDtos = extractMethodsReturningSubDtos(methods, parent);
        for (JavaType.Method method : methodsReturningSubDtos ) {
            JavaType returnType = method.getReturnType();

            List<JavaType.Class> types = new LinkedList<>();
            if (returnType instanceof JavaType.Class clazz) {
                types.add(clazz);
            }
            if (parent == null) {
                types.addAll(getAllDtoParameters(method));
            }

            types.stream()
                    .map(clazz -> createAccessors(
                        clazz.getMethods(),
                        createParentAccessor(method, filteredParent.orElse(null)),
                        rootName))
                    .forEach(accessors::addAll);
        }

        return accessors;
    }

    private static @NotNull List<JavaType.Method> extractMethodsReturningSubDtos(List<JavaType.Method> methods, Accessor parent) {
        return methods.stream()
                .filter(method -> hasSubDtoReturnType(method) || (parent == null &&
                        method.getParameterTypes().stream()
                                .anyMatch(WSDLTypesSearch::isDtoType)))
                .collect(Collectors.toList());
    }

    private static @NotNull List<JavaType.Class> getAllDtoParameters(JavaType.Method method) {
        return method.getParameterTypes().stream()
                .filter(WSDLTypesSearch::isDtoType)
                .map(JavaType.Class.class::cast)
                .collect(Collectors.toList());
    }

    private static List<Accessor> extractMethodsNotReturningSubDtos(List<JavaType.Method> methods, Optional<Accessor> filteredParent) {
        List<JavaType.Method> methodsReturningOtherTypes = methods.stream()
                .filter(method -> !hasSubDtoReturnType(method))
                .collect(Collectors.toList());

        return methodsReturningOtherTypes.stream()
                .filter(method -> {
                    String get = "get";
                    return LSTUtil.methodNameStartsWith(method, get);
                })
                .map(method ->
                        filteredParent
                                .map(accessor -> createParentAccessor(method, accessor))
                                .orElseGet(() -> createAccessor(method))
                ).collect(Collectors.toList());
    }

    private static @NotNull Accessor createParentAccessor(JavaType.Method method, Accessor filteredParent) {
        return initBuilder(method).parent(filteredParent).build();
    }

    private static @NotNull Accessor createAccessor(JavaType.Method method) {
        return initBuilder(method).build();
    }

    private static Accessor.Builder initBuilder(JavaType.Method method) {
        Accessor.Builder builder = Accessor.builder(method.getName(), method.getDeclaringType().getFullyQualifiedName());
        builder.type(method.getReturnType().toString());
        return builder;
    }

    private static Optional<Accessor> removeRootParent(Accessor parent, String rootName) {
        return Optional.ofNullable(parent != null &&
                        accessorHasName(parent, rootName) ?
                        null :
                        parent);
    }

    private static boolean accessorHasName(Accessor parent, String rootName) {
        return parent.getClazz().contains(rootName);
    }

    private static boolean hasSubDtoReturnType(JavaType.Method method) {
        JavaType returnType = method.getReturnType();
        return isDtoType(returnType) && LSTUtil.declaringTypeIsNotType(method, returnType);
     }

    private static boolean isDtoType(JavaType type) {
         if ( type instanceof JavaType.Class clazz) {
             String className = clazz.getFullyQualifiedName();
             return className.contains("at.sozvers.stp.lgkk.a02") || (hasDtoTypeParameter(clazz));
         }
         return false;
     }

     private static boolean hasDtoTypeParameter(JavaType.Class clazz) {
         return classHasTypeParameters(clazz) && anyTypeParameterIsDto(clazz);
     }

    private static boolean classHasTypeParameters(JavaType.Class clazz) {
        return !clazz.getTypeParameters().isEmpty();
    }

    private static boolean anyTypeParameterIsDto(JavaType.Class clazz) {
        return clazz.getTypeParameters()
                .stream()
                .anyMatch(WSDLTypesSearch::typeParameterIsDto);
    }

    private static boolean typeParameterIsDto(JavaType typeParameter) {
        return typeParameter
                    .toString()
                    .contains("at.sozvers.stp.lgkk.a02");
    }


}
