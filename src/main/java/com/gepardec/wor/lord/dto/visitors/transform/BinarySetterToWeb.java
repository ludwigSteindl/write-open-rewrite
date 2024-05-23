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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class BinarySetterToWeb extends JavaIsoVisitor<ExecutionContext> {
    private static final Logger LOG = LoggerFactory.getLogger(BinarySetterToWeb.class);

    // JavaTemplate for constructing a setter invocation
    // pos1: instance name
    // pos2: setter name
    // pos3: argument
    private static final JavaTemplate SETTER = LSTUtil.javaTemplateOf("#{}.#{}(#{})");
    private static final JavaTemplate GETTER = LSTUtil.javaTemplateOf("#{}.#{}()");

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
        Optional<String> argumentOpt = getArgumentStatements(method);

        if (accessor.isEmpty()) {
            return method;
        }

        doAfterVisit(new BinaryInitToWeb(instanceName, accessor.get(), accumulator));

        J.MethodInvocation newSetter = (J.MethodInvocation) argumentOpt
                .map(argument -> applySetter(method, accessor, instanceName, argument))
                .orElse(applyGetter(method, accessor, instanceName));

        LOG.info("Replacing " + method.printTrimmed(getCursor()) + " with " + newSetter.printTrimmed(getCursor()));
        return newSetter;
    }

    @NotNull
    private <J2> J2 applySetter(J.MethodInvocation method, Optional<Accessor> accessor, String instanceName, String argument) {
        return SETTER.apply(updateCursor(method),
                method.getCoordinates().replace(),
                instanceName,
                generateWebDtoSetter(accessor.get(), method.getSimpleName()),
                argument
        );
    }

    @NotNull
    private <J2> J2 applyGetter(J.MethodInvocation method, Optional<Accessor> accessor, String instanceName) {
        return GETTER.apply(updateCursor(method),
                method.getCoordinates().replace(),
                instanceName,
                generateWebDtoSetter(accessor.get(), method.getSimpleName()));
    }

    private Optional<String> getArgumentStatements(J.MethodInvocation method) {
        Optional<Accessor> accessor = findWebAccessorForBinary(method);
        List<Expression> arguments = method.getArguments();

        if (arguments.isEmpty()) {
            return Optional.empty();
        }

        return arguments.stream()
                .filter(argument -> !argument.printTrimmed(getCursor()).equals(""))
                .map(argument -> getArgumentStatement(argument, accessor))
                .reduce((arg1, arg2) -> arg1 + ", " + arg2);
    }

    private String getArgumentStatement(Expression argument, Optional<Accessor> accessor) {
        String argumentString = argument.printTrimmed(getCursor());
        if (accessor.isEmpty()) {
            return argumentString;
        }

        if (!accessor.get().getType().contains("JAXBElement")) {
            return argumentString;
        }

        return createObjectFactoryInitializer(argumentString, accessor.get());
    }

    private String capitalizeFirstLetter(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    private String createObjectFactoryInitializer(String argument, Accessor accessor) {
        String fieldName = capitalizeFirstLetter(cutPrefixFromMethodName(accessor.getName()));
        String parentPart =  accessor.getParent()
                .map(Accessor::getType)
                .map(LSTUtil::shortNameOfFullyQualified)
                .orElse("");
        return String.format("objectFactory.create%s%s(%s)", parentPart, fieldName, argument);

    }

    private Optional<Accessor> findWebAccessorForBinary(J.MethodInvocation method) {
        String type = LSTUtil.getType(method);
        String methodName = method.getSimpleName();
        return findWebAccessorForBinary(methodName, type);
    }

    private static boolean isNotOfBinaryDto(J.MethodInvocation method) {
        return !(method.getSelect() != null && method.getSelect().getType() != null && method.getSelect().getType().toString().contains("Dto") && method.getSelect().getType().toString().startsWith("at.sozvers.stp.lgkk.gensvc"));
    }

    private String generateWebDtoSetter(Accessor accessor, String methodName) {
        Optional<Accessor> nextAccessor = accessor.getParent();
        StringBuilder stringBuilder = new StringBuilder();
        while (nextAccessor.isPresent()) {
            Accessor accessorIteration = nextAccessor.get();
            stringBuilder.insert(0, accessorIteration.getName() + "().");
            nextAccessor = accessorIteration.getParent();
        }

        if (stringBuilder.length() == 0) {
            if (getCursor().getParent().firstEnclosing(J.ForLoop.class) != null) {
                methodName += "()";
            }
        }

        return stringBuilder.append(methodName).toString();
    }

    private String cutPrefixFromMethodName(String methodName) {
        if (methodName.startsWith("is")) {
            return methodName.substring(2);
        }
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            return methodName.substring(3);
        }
        return methodName;
    }

    private Optional<Accessor> findWebAccessorForBinary(String methodName, String methodType) {
        String typeWithoutNested = cutNestedClassFromType(methodType);
        String typeWithoutDtoSuffix = typeWithoutNested.substring(0, typeWithoutNested.length() - 3);
        String shortType = LSTUtil.shortNameOfFullyQualified(typeWithoutDtoSuffix);
        return accumulator.getServices().stream()
                .flatMap(service -> service.getAccessors().stream())
                .filter(accessor -> hasThatTypeContainsInTree(accessor, shortType))
                .filter(accessor -> !accessor.getClazz().contains("StdReqHeader"))                      // Doesn't work on web
                .filter(accessor -> methodName.contains(cutPrefixFromMethodName(accessor.getName())))
                .findFirst();
    }

    @NotNull
    private static String cutNestedClassFromType(String type) {
        int nestedClassSeperator = '$';
        int indexNestedClassSeperator = type.indexOf(nestedClassSeperator);
        return indexNestedClassSeperator == -1 ? type : type.substring(0, indexNestedClassSeperator);
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
