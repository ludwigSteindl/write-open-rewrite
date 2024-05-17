package com.gepardec.wor.lord.dto.visitors.transform;

import com.gepardec.wor.lord.dto.common.Accessor;
import com.gepardec.wor.lord.dto.common.Accumulator;
import com.gepardec.wor.lord.util.LSTUtil;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.Optional;

public class BinarySetterToWeb extends JavaIsoVisitor<ExecutionContext> {

    // JavaTemplate for constructing a setter invocation
    // pos1: instance name
    // pos2: setter name
    // pos3: argument
    private static final JavaTemplate SETTER = LSTUtil.javaTemplateOf("#{}.#{}(#{});");

    private final Accumulator accumulator;

    public BinarySetterToWeb(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    @Override
    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
        method = super.visitMethodInvocation(method, ctx);

        if (isNotOfBinaryDto(method)) {
            return method;
        }

        if (hasNoCorrespondingWsdlType(method)) {
            return method;
        }

        return changeToBinarySetter(method);
    }

    private J.MethodInvocation changeToBinarySetter(J.MethodInvocation method) {
        Optional<Accessor> accessor = findWebAccessorForBinary(method);
        String instanceName = method.getSelect().toString();
        String argument = LSTUtil.getFirstArgument(method, getCursor());

        if (accessor.isEmpty()) {
            return method;
        }

        doAfterVisit(new BinaryInitToWeb(instanceName, accessor.get(), accumulator));

        return SETTER.apply(updateCursor(method),
                method.getCoordinates().replace(),
                instanceName,
                generateWebDtoSetter(accessor.get(), method.getSimpleName()),
                argument
        );
    }

    private Optional<Accessor> findWebAccessorForBinary(J.MethodInvocation method) {
        String type = LSTUtil.getType(method);
        String methodName = method.getSimpleName();
        return findWebAccessorForBinary(methodName, type);
    }

    private static boolean isNotOfBinaryDto(J.MethodInvocation method) {
        return method.getSelect() != null && method.getSelect().getType() != null && !method.getSelect().getType().toString().contains("Dto");
    }

    private String generateWebDtoSetter(Accessor accessor, String methodName) {
        Optional<Accessor> nextAccessor = accessor.getParent();
        StringBuilder stringBuilder = new StringBuilder();
        while (nextAccessor.isPresent()) {
            Accessor accessorIteration = nextAccessor.get();
            stringBuilder.insert(0, accessorIteration.getName() + "().");
            nextAccessor = accessorIteration.getParent();
        }
        return stringBuilder.append(methodName).toString();
    }

    private String cutPrefixFromMethodName(String methodName) {
        if (methodName.startsWith("is")) {
            return methodName.substring(2);
        }
        return methodName.substring(3);
    }

    private Optional<Accessor> findWebAccessorForBinary(String methodName, String methodType) {
        String typeWithoutDtoSuffix = methodType.substring(0, methodType.length() - 3);
        String shortType = LSTUtil.shortNameOfFullyQualified(typeWithoutDtoSuffix);
        return accumulator.getServices().stream()
                .flatMap(service -> service.getAccessors().stream())
                .filter(accessor -> hasThatTypeContainsInTree(accessor, shortType))
                .filter(accessor -> !accessor.getClazz().contains("StdReqHeader"))
                .filter(accessor -> methodName.contains(cutPrefixFromMethodName(accessor.getName())))
                .findFirst();
    }

    private boolean hasThatTypeContainsInTree(Accessor accessor, String string) {
        Optional<Accessor> currentAccessor = Optional.of(accessor);
        while(currentAccessor.isPresent()) {
            if (currentAccessor.get().getClazz().contains(string)) {
                return true;
            }
            currentAccessor = currentAccessor.get().getParent();
        }
        return false;
    }

    private boolean hasNoCorrespondingWsdlType(J.MethodInvocation method) {
        return !accumulator.getIOTypesShort().stream()
                .anyMatch(type -> getWsdlTypeFromBinary(method).contains(type));
    }

    private static @NotNull String getWsdlTypeFromBinary(J.MethodInvocation method) {
        Expression select = method.getSelect();
        if (select == null || select.getType() == null) {
            return "";
        }
        return select.getType().toString().replaceAll("Dto", "");
    }
}
