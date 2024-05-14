package com.gepardec.wor.lord.stdh.v2.visitors;

import com.gepardec.wor.lord.stdh.v2.common.Accumulator;
import com.gepardec.wor.lord.util.ParserUtil;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;

import java.util.List;

public class BinaryDtoToWebVisitor extends JavaIsoVisitor<ExecutionContext> {

    private static final List<String> STDH_SETTERS_NAMES = List.of("setZvst");
    private static final String STDH_GETTER_NAME = "getOmStandardRequestHeader";

    private static final String BINARY_DTO_NAME = "LaqamhsuDto";

    private static final JavaTemplate STDH_SETTER = JavaTemplate
            .builder("#{}.getOmStandardRequestHeader().#{}(#{});")
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

        if (!hasCorrespondingWsdlType(method)) {
            return method;
        }

        String instanceName = method.getSelect().toString();
        if (instanceName.contains(STDH_GETTER_NAME)) {
            return method;
        }

        String methodName = method.getSimpleName();
        if (!STDH_SETTERS_NAMES.contains(methodName)) {
            doAfterVisit(new BinaryDtoInitToWebVisitor(instanceName, false));
            return method;
        }

        doAfterVisit(new BinaryDtoInitToWebVisitor(instanceName, true));

        String argument = method.getArguments().get(0).printTrimmed(getCursor());
        return STDH_SETTER.apply(updateCursor(method),
                method.getCoordinates().replace(),
                instanceName,
                methodName,
                argument
        );
    }

    private boolean hasCorrespondingWsdlType(J.MethodInvocation method) {
        return accumulator.getIOTypesShort().stream()
                .anyMatch(type -> getWsdlTypeFromBinary(method).contains(type));
    }

    private static @NotNull String getWsdlTypeFromBinary(J.MethodInvocation method) {
        JavaType type = method.getSelect().getType();
        if (type == null) {
            return "";
        }
        return type.toString().replaceAll("Dto", "");
    }
}
