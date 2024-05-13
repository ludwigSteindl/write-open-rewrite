package com.gepardec.wor.lord.stdh.v2;

import com.gepardec.wor.lord.util.ParserUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;
import java.util.Optional;

public class BinaryDtoToWebVisitor extends JavaIsoVisitor<ExecutionContext> {
    private String variableName;
    private boolean usesStdh;
    private static final String OBJECT_FACTORY_NAME = "objectFactory";

    private static final String NEW_WEB_DTO = "Laqamhsu #{} = new Laqamhsu();";

    public static final String SET_NEW_STDH_TEMPLATE = "\n#{}.setOmStandardRequestHeader(%s.createOmStandardRequestHeader());";
    private static final String NEW_WEB_DTO_WITH_STDH_TEMPLATE = NEW_WEB_DTO + SET_NEW_STDH_TEMPLATE;



    public BinaryDtoToWebVisitor(String variableName, boolean usesStdh) {
        this.variableName = variableName;
        this.usesStdh = usesStdh;
    }

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
        List<Statement> statements = method.getBody().getStatements();
        Optional<J.VariableDeclarations> dtoDeclarations = statements.stream()
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .filter(variableDeclarations -> variableDeclarations.getVariables().get(0).getSimpleName().equals(variableName))
                .filter(variableDeclarations -> !variableDeclarations.getTypeExpression().toString().equals("Laqamhsu"))
                .findFirst();

        if (dtoDeclarations.isEmpty()) {
            return method;
        }

        if (usesStdh) {
            doAfterVisit(new ClassUsingBinaryDtoToWebVisitor(OBJECT_FACTORY_NAME));
        }

        return getCreateDtoJavaTemplate().apply(
                updateCursor(method),
                dtoDeclarations.get().getCoordinates().replace(),
                getCreateDtoJavaTemplateParams()
                );
    }

    private Object[] getCreateDtoJavaTemplateParams() {
        return usesStdh ?  new Object[]{variableName, variableName} : new Object[]{variableName};
    }

    private JavaTemplate getCreateDtoJavaTemplate() {
        String template = usesStdh ? String.format(NEW_WEB_DTO_WITH_STDH_TEMPLATE, OBJECT_FACTORY_NAME) : NEW_WEB_DTO;
        return javaTemplateOf(template);
    }

    private static JavaTemplate javaTemplateOf(String template) {
        return JavaTemplate
                .builder(template)
                .javaParser(ParserUtil.createParserWithRuntimeClasspath())
                .contextSensitive()
                .build();
    }

}
