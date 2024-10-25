package com.gepardec.wor.lord.call.ternaries;

import com.gepardec.wor.lord.util.LSTUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

public class ReplaceWebserviceCallWithTernary extends Recipe {
    private static final String GENSVC_PACKAGE = "at.sozvers.stp.lgkk.gensvc";

    private static final String SERVICE_HELPER_TEMPLATE = "%sServiceHelper";
    private static final JavaTemplate NEW_CALL = JavaTemplate.builder("new #{}().callWebservice(#{}).getResponse();").build();

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            @Override
            public J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext) {
                method = super.visitMethodInvocation(method, executionContext);

                String classType = getCursor().getParent().firstEnclosingOrThrow(J.ClassDeclaration.class).getType().toString();
                if (classType.startsWith("at.sozvers.stp.lgkk.a02.laaamhsu")
                        || classType.startsWith("at.sozvers.stp.lgkk.gensvc")) {
                    return method;
                }

                if (!method.getSimpleName().equals("call")) {
                    return method;
                }

                String methodType = LSTUtil.getType(method);
                if (methodType == null || !methodType.startsWith(GENSVC_PACKAGE)) {
                    return method;
                }
                String methodTypeShort = LSTUtil.shortNameOfFullyQualified(methodType);
                String serviceClassName = methodTypeShort.replace("SvcProxy", "");

                String argument = method.getArguments().get(0).printTrimmed(getCursor()).replace("\n", "");
                String serviceHelper = String.format(SERVICE_HELPER_TEMPLATE, serviceClassName);
                maybeAddImport("at.sozvers.stp.lgkk.webservice.helper." + serviceHelper, false);
                method = NEW_CALL.apply(
                        updateCursor(method),
                        method.getCoordinates().replace(),
                        serviceHelper,
                        argument
                        );
                return super.visitMethodInvocation(method, executionContext);
            }
        };
    }
}
