package com.gepardec.wor.lord.call.ternaries;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class BinaryProxyToWebTernaryVisitor extends JavaVisitor<ExecutionContext> {

    public static final String BINARY_METHOD_NAME = "callSvcProxy";
    public static final String WEB_METHOD_NAME = "callSvcWeb";

    private static final JavaTemplate WEB_OR_PROXY =
            JavaTemplate.builder("ElgkkPropertiesUtil.isUseWeb() ? #{}(#{any()}) : #{}(#{any()});")
            .build();

    @Override
    @NotNull
    public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
        method = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

        if (!hasName(method, BINARY_METHOD_NAME)) {
            return method;
        }

        if (hasSurroundingTernaryWithWebCall(WEB_METHOD_NAME)) {
            return method;
        }

        Expression argument = method.getArguments().get(0);
        doAfterVisit(new BinaryProxyToWebConfigClassVisitor());
        return replaceBinaryCallWithTernary(method, WEB_METHOD_NAME);
    }

    /**
     * Example: For webMethodName = "callSvcWeb" replace following snippet:
     *     callSvcProxy(request);
     * with:
     *     ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(request) : callSvcProxy(request);
     * @param method Method to be replaced
     * @param webMethodName
     * @return
     */
    private J.Ternary replaceBinaryCallWithTernary(J.MethodInvocation method, String webMethodName) {
        Expression argument = method.getArguments().get(0);
        String binaryMethodName = method.getSimpleName();
        return WEB_OR_PROXY.apply(
                updateCursor(method),
                method.getCoordinates().replace(),

                webMethodName,
                argument,
                binaryMethodName,
                argument);
    }

    private boolean hasSurroundingTernaryWithWebCall(String webMethodName) {
        J.Ternary surrounding = getCursor().getParentOrThrow().firstEnclosing(J.Ternary.class);
        if (surrounding == null) {
            return false;
        }
        return surrounding.getTruePart().print(getCursor()).contains(webMethodName);
    }

    private static boolean hasName(J.MethodInvocation method, String binaryMethodName) {
        return method.getSimpleName().equals(binaryMethodName);
    }


}
