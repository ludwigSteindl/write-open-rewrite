package com.gepardec.wor.lord.dto.visitors.search;

import com.gepardec.wor.lord.dto.common.Accessor;
import com.gepardec.wor.lord.dto.common.Accumulator;
import com.gepardec.wor.lord.dto.common.Wsdl2JavaService;
import com.gepardec.wor.lord.util.LSTUtil;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class WSDLTypesSearch extends JavaIsoVisitor<ExecutionContext> {
    private Accumulator accumulator;

    public WSDLTypesSearch(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    @Override
    public @Nullable J.ClassDeclaration visitClassDeclaration(@Nullable J.ClassDeclaration classDeclaration,
                                                              ExecutionContext ctx) {
        classDeclaration = super.visitClassDeclaration(classDeclaration, ctx);
        if (!LSTUtil.isInterface(classDeclaration)) {
            return classDeclaration;
        }

        Wsdl2JavaService service = createServiceWithAccessors(classDeclaration);
        accumulator.addService(service);

        return classDeclaration;
    }

    private @NotNull Wsdl2JavaService createServiceWithAccessors(J.ClassDeclaration classDeclaration) {
        String className = classDeclaration.getSimpleName();
        Wsdl2JavaService service = createService(LSTUtil.getMethodDeclarations(classDeclaration), className);
        service.addAccessors(createAccessors(classDeclaration));
        return service;
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
