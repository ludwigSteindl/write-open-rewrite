package com.gepardec.wor.lord.call.ternaries;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class BinaryProxyToWebTernaryVisitor extends JavaVisitor<ExecutionContext> {

    private static final JavaTemplate WEB_OR_PROXY =
            JavaTemplate.builder("ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(#{any()}) : callSvcProxy(#{any()});")
            .build();

    @Override
    public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx) {
        method = (J.MethodInvocation) super.visitMethodInvocation(method, ctx);

        if (!method.getSimpleName().equals("callSvcProxy")) {
            return method;
        }

        J.Ternary surrounding = getCursor().getParentOrThrow().firstEnclosing(J.Ternary.class);
        if (surrounding != null) {
            if (surrounding.getTruePart().print(getCursor()).contains("callSvcWeb")) {
                return method;
            }
        }

        Expression argument = method.getArguments().getFirst();
        doAfterVisit(new BinaryProxyToWebConfigClassVisitor());
        return WEB_OR_PROXY.apply(
                updateCursor(method),
                method.getCoordinates().replace(),
                argument,
                argument
        );
    }

}
