package com.gepardec.wor.lord.stdh.v2.visitors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gepardec.wor.lord.stdh.v2.common.Accessor;
import com.gepardec.wor.lord.stdh.v2.common.Accumulator;
import com.gepardec.wor.lord.stdh.v2.common.Wsdl2JavaService;
import com.gepardec.wor.lord.util.ParserUtil;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

public class BinaryDtoToWebVisitor extends JavaIsoVisitor<ExecutionContext> {

    private static final String STDH_GETTER_NAME = "getOmStandardRequestHeader";

    private static final JavaTemplate STDH_SETTER = JavaTemplate
            .builder("#{}.#{}(#{});")
            .javaParser(ParserUtil.createParserWithRuntimeClasspath())
            .contextSensitive()
            .build();

    private Accumulator accumulator;

    public BinaryDtoToWebVisitor(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

    @Override
    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
        method = super.visitMethodInvocation(method, ctx);

        if (method.getSelect() != null && method.getSelect().getType() != null && !method.getSelect().getType().toString().contains("Dto")) {
            return method;
        }

        if (!hasCorrespondingWsdlType(method)) {
            return method;
        }

        String instanceName = method.getSelect().toString();
        if (instanceName.contains(STDH_GETTER_NAME)) {
            return method;
        }

        String type = method.getSelect().getType().toString();
        String methodName = method.getSimpleName();
        Optional<Accessor> accessor = findAccessor(methodName, type);
        if (accessor.isEmpty()) {
            return method;
        }
        String newSetter = generateWebDtoSetter(accessor.get(), methodName);
        if (isNotNested(methodName)) {
            doAfterVisit(new BinaryDtoInitToWebVisitor(instanceName, accumulator));
            return method;
        }

        doAfterVisit(new BinaryDtoInitToWebVisitor(instanceName, accessor.get(), accumulator));

        String argument = method.getArguments().get(0).printTrimmed(getCursor());
        return STDH_SETTER.apply(updateCursor(method),
                method.getCoordinates().replace(),
                instanceName,
                newSetter,
                argument
        );
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

    private Optional<Accessor> findAccessor(String methodName, String methodType) {
        String[] methodTypeParts = methodType.substring(0, methodType.length() - 3).split("\\.");
        return accumulator.getServices().stream()
                .flatMap(service -> service.getAccessors().stream())
                .filter(accessor -> hasThatTypeContainsInTree(accessor, methodTypeParts[methodTypeParts.length - 1]))
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

    private boolean isNotNested(String methodName) {
        return accumulator
                .getServices()
                .stream()
                .map(Wsdl2JavaService::getAccessors)
                .noneMatch(getters -> getters.stream()
                        .filter(accessor -> accessor.getParent().isPresent())
                        .map(Accessor::getName)
                        .map(name -> "set" + name.substring(3))
                        .anyMatch(name -> name.equals(methodName))
                );
    }

    private boolean hasCorrespondingWsdlType(J.MethodInvocation method) {
        return accumulator.getIOTypesShort().stream()
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
