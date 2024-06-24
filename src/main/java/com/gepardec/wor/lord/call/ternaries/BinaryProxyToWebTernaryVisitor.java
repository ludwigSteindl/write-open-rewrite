package com.gepardec.wor.lord.call.ternaries;

import com.gepardec.wor.lord.common.Accumulator;
import com.gepardec.wor.lord.common.search.WSDLTypesSearch;
import com.gepardec.wor.lord.util.LSTUtil;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.Cursor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class BinaryProxyToWebTernaryVisitor extends JavaVisitor<ExecutionContext> {
    private static final Logger LOG = LoggerFactory.getLogger(BinaryProxyToWebTernaryVisitor.class);

    public static final String BINARY_METHOD_NAME = "send";
    public static final String WEB_METHOD_NAME = "callSvcWeb";

    private static final String GENSVC_PACKAGE = "at.sozvers.stp.lgkk.gensvc";

    private static final String QUEUE_SENDER_FQN = "at.sozvers.stp.lgkk.aumeldung.impl.QueueSender";

    private static final String WEB_SEND_TEMPLATE = "new QueueSender<>(\"%s\")%s.send(%s);\n";

    private final Accumulator accumulator;

    public BinaryProxyToWebTernaryVisitor(Accumulator accumulator) {
        this.accumulator = accumulator;
    }

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

        Optional<String> serviceAlias = getServiceAlias(method);
        if (serviceAlias.isEmpty()) {
            return method;
        }
        doAfterVisit(new BinaryProxyToWebConfigClassVisitor());

        J.Block surrounding = getCursor().getParent().firstEnclosingOrThrow(J.Block.class);
        Optional<J.MethodInvocation> requestSetter = surrounding.getStatements()
                .stream()
                .filter(J.MethodInvocation.class::isInstance)
                .map(J.MethodInvocation.class::cast)
                .filter(methodInvocation -> methodInvocation.getSimpleName().equals("setRequest"))
                .findFirst();

        String queueHelperSetter = surrounding.getStatements()
                .stream()
                .filter(J.MethodInvocation.class::isInstance)
                .map(J.MethodInvocation.class::cast)
                .filter(methodInvocation -> methodInvocation.getSimpleName().equals("setQueueHelper"))
                .findFirst()
                .map(setter -> setter.getArguments().get(0))
                .map(argument -> argument.printTrimmed(getCursor()).replace("\n", ""))
                .map(argString -> ".withQueueHelper(" + argString + ")")
                .orElse("");

        if (requestSetter.isEmpty()) {
            LOG.error("Found no request setter for {}", method.printTrimmed(getCursor()));
            return method;
        }

        if (requestSetter.get().getArguments().isEmpty()) {
            return method;
        }

        Expression request = requestSetter.get().getArguments().get(0);
        if (request.getType() != null && accumulator.lookupWsdlTypeFromBinary(request.getType().toString()).isEmpty()) {
            return method;
        }

        return replaceBinaryCallWithTernary(method, serviceAlias.get(), request, queueHelperSetter);
    }

    /**
     * Example: For webMethodName = "callSvcWeb" replace following snippet:
     * callSvcProxy(argument);
     * with:
     * ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(argument) : callSvcProxy(argument);
     *
     * @param method  Method to be replaced
     * @param argument
     * @return
     */
    private J.MethodInvocation replaceBinaryCallWithTernary(J.MethodInvocation method, String serviceAlias, Expression argument, String additionalExpressions) {
        String webCall = String.format(WEB_SEND_TEMPLATE,
                serviceAlias,
                additionalExpressions,
                argument.printTrimmed(getCursor()).replace("\n", ""));
        maybeAddImport(QUEUE_SENDER_FQN, false);
        return LSTUtil.javaTemplateOf(webCall)
                .apply(new Cursor(getCursor(), method), method.getCoordinates().replace());
    }

    @NotNull
    private static Optional<String> getServiceAlias(J.MethodInvocation method) {
        String methodType = LSTUtil.getType(method);
        if (methodType == null || !methodType.startsWith(GENSVC_PACKAGE)) {
            return Optional.empty();
        }
        String methodTypeShort = LSTUtil.shortNameOfFullyQualified(methodType);
        String serviceAlias = methodTypeShort.toUpperCase();
        return Optional.of(serviceAlias.replace("SVCPROXY", ""));
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
