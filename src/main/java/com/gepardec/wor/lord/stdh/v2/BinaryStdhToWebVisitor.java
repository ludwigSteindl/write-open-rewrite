package com.gepardec.wor.lord.stdh.v2;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.List;

public class BinaryStdhToWebVisitor extends JavaIsoVisitor<ExecutionContext> {

    private static final List<String> STDH_SETTERS_NAMES = List.of("setZvst");
    private static final String STDH_GETTER_NAME = "getOmStandardRequestHeader";

    private static final JavaTemplate STDH_SETTER = JavaTemplate.builder(
            STR."#{}.\{STDH_GETTER_NAME}().#{}(#{});"
    ).build();

    @Override
    public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
        method = super.visitMethodInvocation(method, ctx);
        String methodName = method.getSimpleName().toString();
        if (!STDH_SETTERS_NAMES.contains(methodName)) {
            return method;
        }


        String instanceName = method.getSelect().toString();
        if (instanceName.contains(STDH_GETTER_NAME)) {
            return method;
        }

        String argument = method.getArguments().getFirst().printTrimmed(getCursor());

        doAfterVisit(new BinaryDtoToWebVisitor(instanceName));
        return STDH_SETTER.apply(updateCursor(method),
                method.getCoordinates().replace(),
                instanceName,
                methodName,
                argument
                );
    }
}
