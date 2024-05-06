/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gepardec.wor.lord;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import javax.swing.plaf.nimbus.State;
import java.util.Iterator;
import java.util.List;

@Value
@EqualsAndHashCode(callSuper = false)
public class BinaryProxyToWeb extends Recipe {
    public static final String METHOD_NAME = "callSvcProxy";

    @Override
    public String getDisplayName() {
        // language=markdown
        return "Change binary proxy calls to web calls";
    }

    @Override
    public String getDescription() {
        return "Change binary proxy call to web call.";
    }

    private static MethodMatcher MATCHER = new MethodMatcher("org.junit.jupiter.api.Assertions assertEquals(..)");


    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

            private final JavaTemplate NEW_BOOLEAN = JavaTemplate.builder("final boolean useWeb = true;\n")
                    .contextSensitive()
                    .build();

            private final JavaTemplate IF_INITIALIZATION = JavaTemplate.builder(
                    """
                    if(useWeb) {
                        AuMhHostInfoResponseDto #{} = callSvcWeb(req);
                    } else {
                        #{any()};
                    }
                    """)
                    .contextSensitive()
                    .build();
            private final JavaTemplate IF_RETURN = JavaTemplate.builder(
                    """
                    if(useWeb) {
                        return callSvcWeb(req);
                    } else {
                        #{any()};
                    }
                    """)
                    .contextSensitive()
                    .build();
            private final JavaTemplate IF_ASSIGNMENT = JavaTemplate.builder(
                    """
                    if(useWeb) {
                        ret = callSvcWeb(req);
                    } else {
                        #{any()};
                    }
                    """)
                    .contextSensitive()
                    .build();
            private final JavaTemplate IF_INVOCATION = JavaTemplate.builder(
                    """
                    if(useWeb) {
                        callSvcWeb(req);
                    } else {
                        #{any()};
                    }
                    """)
                    .contextSensitive()
                    .build();


                    @Override
                    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
                        final J.MethodDeclaration m = super.visitMethodDeclaration(method, ctx);

                        List<Statement> statements = m.getBody().getStatements();
                        String first = statements.get(0) + ";";

                        if (first.equals(NEW_BOOLEAN.getCode())) {
                            return m;
                        }

                        List<?> invocations = statements
                                .stream()
                                .filter(t -> t.print(getCursor()).contains(METHOD_NAME))
                                .toList();


                        if (invocations.isEmpty()) {
                            return m;
                        }

                        J.MethodDeclaration me = NEW_BOOLEAN.apply(
                                updateCursor(m),
                                m.getBody().getCoordinates().firstStatement());


                        for (Object o : invocations) {
                            Statement invocation = (Statement) o;
                            if (invocation instanceof J.VariableDeclarations decl) {
                                String varName = decl.getVariables().get(0).getName().getSimpleName();
                                me = IF_INITIALIZATION.apply(
                                        updateCursor(me),
                                        decl.getCoordinates().replace(),
                                        varName,
                                        invocation);
                            }

                            if (invocation instanceof J.Return returnStatement) {
                                me = IF_RETURN.apply(
                                        updateCursor(me),
                                        returnStatement.getCoordinates().replace(),
                                        invocation);
                            }

                            if (invocation instanceof J.Assignment assignment) {
                                me = IF_ASSIGNMENT.apply(
                                        updateCursor(me),
                                        assignment.getCoordinates().replace(),
                                        invocation);
                            }

                            if (invocation instanceof J.MethodInvocation methodInvocation) {
                                me = IF_INVOCATION.apply(
                                        updateCursor(me),
                                        methodInvocation.getCoordinates().replace(),
                                        invocation);
                            }
                        }

                        return me;
                    }

                };
    }
}
