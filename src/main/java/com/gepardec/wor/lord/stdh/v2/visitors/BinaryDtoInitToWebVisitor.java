package com.gepardec.wor.lord.stdh.v2.visitors;

import com.gepardec.wor.lord.stdh.v2.common.Accumulator;
import com.gepardec.wor.lord.util.ParserUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;
import java.util.Optional;

public class BinaryDtoInitToWebVisitor extends JavaIsoVisitor<ExecutionContext> {
    private String variableName;
    private boolean usesStdh;
    private Accumulator accumulator;
    private static final String OBJECT_FACTORY_NAME = "objectFactory";

    private static final String NEW_WEB_DTO = "#{} #{} = new #{}();";

    public static final String SET_NEW_STDH_TEMPLATE = "\n#{}.setOmStandardRequestHeader(%s.createOmStandardRequestHeader());";
    private static final String NEW_WEB_DTO_WITH_STDH_TEMPLATE = NEW_WEB_DTO + SET_NEW_STDH_TEMPLATE;


    public BinaryDtoInitToWebVisitor(String variableName, boolean usesStdh, Accumulator accumulator) {
        this.variableName = variableName;
        this.usesStdh = usesStdh;
        this.accumulator = accumulator;
    }

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
        List<Statement> statements = method.getBody().getStatements();
        Optional<J.VariableDeclarations> dtoDeclarationsOptional = statements.stream()
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .filter(variableDeclarations -> variableDeclarations.getVariables().get(0).getSimpleName().equals(variableName))
                .findFirst();

        if (dtoDeclarationsOptional.isEmpty()) {
            return method;
        }

        J.VariableDeclarations dtoDeclarations = dtoDeclarationsOptional.get();
        String type = dtoDeclarations.getType().toString();

        Optional<String> wsdlTypeOptional = accumulator.getWsdlTypeFromBinary(type);

        if (wsdlTypeOptional.isEmpty()) {
            return method;
        }
        String wsdlType = wsdlTypeOptional.get();
        String shortWsdlType = Accumulator.shortNameOfFullyQualified(wsdlType);

        if (usesStdh) {
            doAfterVisit(new ClassUsingBinaryDtoToWebVisitor(OBJECT_FACTORY_NAME));
        }

        maybeAddImport(wsdlType);
        return getCreateDtoJavaTemplate(wsdlType).apply(
                updateCursor(method),
                dtoDeclarations.getCoordinates().replace(),
                getCreateDtoJavaTemplateParams(shortWsdlType)
                );
    }

    private Object[] getCreateDtoJavaTemplateParams(String shortWsdlType) {
        return usesStdh ?
                new Object[]{shortWsdlType, variableName, shortWsdlType, variableName} :
                new Object[]{shortWsdlType, variableName, shortWsdlType};
    }

    private JavaTemplate getCreateDtoJavaTemplate(String wsdlType) {
        String template = usesStdh ? String.format(NEW_WEB_DTO_WITH_STDH_TEMPLATE, OBJECT_FACTORY_NAME) : NEW_WEB_DTO;
        return javaTemplateOf(template, wsdlType);
    }

    private static JavaTemplate javaTemplateOf(String template, String wsdlType) {
        return JavaTemplate
                .builder(template)
                .javaParser(ParserUtil.createParserWithRuntimeClasspath())
                .imports(wsdlType)
                .contextSensitive()
                .build();
    }

}
