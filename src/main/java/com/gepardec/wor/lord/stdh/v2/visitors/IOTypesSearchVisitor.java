package com.gepardec.wor.lord.stdh.v2.visitors;

import com.gepardec.wor.lord.stdh.v2.common.Accessor;
import com.gepardec.wor.lord.stdh.v2.common.Accumulator;
import com.gepardec.wor.lord.stdh.v2.common.Wsdl2JavaService;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class IOTypesSearchVisitor extends JavaIsoVisitor<ExecutionContext> {
    private Accumulator accumulator;

    public IOTypesSearchVisitor(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    @Override
    public @Nullable J.ClassDeclaration visitClassDeclaration(@Nullable J.ClassDeclaration classDeclaration,
                                                              ExecutionContext ctx) {


        classDeclaration = super.visitClassDeclaration(classDeclaration, ctx);
        String className = classDeclaration.getSimpleName();

        if (!isInterface(classDeclaration)) {
            return classDeclaration;
        }

        List<J.MethodDeclaration> methods = getMethods(classDeclaration);

        List<String> requestTypes = getParameterTypes(methods);
        List<String> responseTypes = getReturnTypes(methods);


        Wsdl2JavaService service = new Wsdl2JavaService(className, requestTypes, responseTypes);
        accumulator.addService(service);

        service.addAccessors(getAccessors(classDeclaration));
        return classDeclaration;
    }

    static boolean isInterface(J.ClassDeclaration classDeclaration) {
        return classDeclaration.getKind().equals(J.ClassDeclaration.Kind.Type.Interface);
    }

    static @NotNull List<J.MethodDeclaration> getMethods(J.ClassDeclaration classDeclaration) {
        return classDeclaration.getBody().getStatements().stream()
                .filter(J.MethodDeclaration.class::isInstance)
                .map(J.MethodDeclaration.class::cast)
                .collect(Collectors.toList());
    }

    static @NotNull List<String> getReturnTypes(List<J.MethodDeclaration> methods) {
        return methods.stream()
                .map(m -> m.getReturnTypeExpression().toString())
                .collect(Collectors.toList());
    }

    static @NotNull List<String> getParameterTypes(List<J.MethodDeclaration> methods) {
        List<String> requestTypes = methods.stream().flatMap(m -> m.getParameters().stream())
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .map(J.VariableDeclarations::getType)
                .map(type -> type.toString())
                .distinct()
                .collect(Collectors.toList());
        return requestTypes;
    }

    List<Accessor> getAccessors(J.ClassDeclaration classDeclaration) {
        List<JavaType.Method> methods = classDeclaration.getType().getMethods();
        return getAccessors(methods, null, classDeclaration.getSimpleName()).stream()
                .filter(accessor -> !accessor.getClazz().equals(classDeclaration.getType().toString()))
                .collect(Collectors.toList());
    }

    private static List<Accessor> getAccessors(List<JavaType.Method> methods, Accessor parent, String rootName) {
        List<Accessor> accessors = new LinkedList<>();

        Accessor filteredParent = parent != null && parent.getClazz().contains(rootName) ? null : parent;

        List<JavaType.Method> methodsReturningOtherTypes = methods.stream()
                .filter(method -> !hasSubDtoReturnType(method))
                .collect(Collectors.toList());

        methodsReturningOtherTypes.stream()
                .filter(method -> method.getName().startsWith("get"))

                .map(method -> new Accessor(method.getName(), method.getDeclaringType().getFullyQualifiedName(), filteredParent))
                .forEach(accessors::add);

        List<JavaType.Method> methodsReturningSubDtos = methods.stream()
                .filter(method -> hasSubDtoReturnType(method))
                .collect(Collectors.toList());
        for (JavaType.Method method : methodsReturningSubDtos ) {
            JavaType returnType = method.getReturnType();
            if (returnType instanceof JavaType.Class clazz) {
                List<Accessor> subAccessors = getAccessors(
                        clazz.getMethods(),
                        new Accessor(method.getName(), method.getDeclaringType().getFullyQualifiedName(), filteredParent),
                        rootName
                );
                accessors.addAll(subAccessors);
            }
        }

        return accessors;
    }

     private static boolean hasSubDtoReturnType(JavaType.Method method) {

        if (method.getReturnType() instanceof JavaType.Class clazz) {
                String className = clazz.getFullyQualifiedName();
                return (className.contains("at.sozvers.stp.lgkk.a02") && (!className.equals(method.getDeclaringType().toString())))
                        || (hasDtoTypeParameter(clazz));
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
                .anyMatch(IOTypesSearchVisitor::typeParameterIsDto);
    }

    private static boolean typeParameterIsDto(JavaType typeParameter) {
        return typeParameter
                    .toString()
                    .contains("at.sozvers.stp.lgkk.a02");
    }


}
