package com.gepardec.wor.lord.stdh.v2.visitors;

import com.gepardec.wor.lord.stdh.v2.common.Accessor;
import com.gepardec.wor.lord.stdh.v2.common.Accumulator;
import com.gepardec.wor.lord.util.ParserUtil;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.RemoveImport;
import org.openrewrite.java.tree.CoordinateBuilder;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaCoordinates;
import org.openrewrite.java.tree.Statement;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openrewrite.java.JavaTemplate.apply;

public class BinaryDtoInitToWebVisitor extends JavaIsoVisitor<ExecutionContext> {
    private String variableName;
    private Accessor accessor;
    private Accumulator accumulator;

    private List<String> settersAdded = new LinkedList<>();

    private static final String OBJECT_FACTORY_NAME = "objectFactory";

    private static final String NEW_WEB_DTO = "%s %s = new %s();";

    public static final String SETTER_TEMPLATE = "%s.%s(%s);";


    public BinaryDtoInitToWebVisitor(String variableName, Accessor accessor, Accumulator accumulator) {
        this.variableName = variableName;
        this.accessor = accessor;
        this.accumulator = accumulator;
    }

    public BinaryDtoInitToWebVisitor(String variableName, Accumulator accumulator) {
        this.variableName = variableName;
        this.accumulator = accumulator;
    }

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
        method = super.visitMethodDeclaration(method, ctx);
        List<Statement> statements = method.getBody().getStatements();

        boolean createsWebDto = false;
        Optional<J.VariableDeclarations> dtoDeclarationsOptional = statements.stream()
                .filter(J.VariableDeclarations.class::isInstance)
                .map(J.VariableDeclarations.class::cast)
                .filter(variableDeclarations -> variableDeclarations.getVariables().get(0).getSimpleName().equals(variableName))
                .findFirst();

        if (dtoDeclarationsOptional.isEmpty()) {
            return method;
        }

        J.VariableDeclarations dtoDeclarations = dtoDeclarationsOptional.get();
        String dtoType = dtoDeclarationsOptional
                .map(declaration -> declaration.getTypeExpression().toString())
                .get();

        String dtoShortType = Accumulator.shortNameOfFullyQualified(dtoType);

        String wsdlType = null;
        if (dtoShortType.contains("Dto")) {
            createsWebDto = true;
            String binaryType = dtoDeclarations.getType().toString();
            Optional<String> wsdlTypeOptional = accumulator.getWsdlTypeFromBinary(binaryType);

            if (wsdlTypeOptional.isEmpty()) {
                return method;
            }

            wsdlType = wsdlTypeOptional.get();

            doAfterVisit(new AddImport<>(wsdlType, null, false));
            doAfterVisit(new ClassUsingBinaryDtoToWebVisitor(
                    OBJECT_FACTORY_NAME,
                    wsdlType.substring(0, wsdlType.lastIndexOf('.'))));
            doAfterVisit(new RemoveImport<>(binaryType, true));
        }
        if (accumulator.getIOTypesShort().contains(dtoShortType)) {
            wsdlType = dtoType;
        }

        if (wsdlType == null) {
            return method;
        }

        Optional<JavaTemplate> template = getCreateDtoJavaTemplate(Accumulator.shortNameOfFullyQualified(wsdlType), createsWebDto);

        CoordinateBuilder.VariableDeclarations coordinateBuilder = dtoDeclarations.getCoordinates();
        JavaCoordinates coordinates = createsWebDto ? coordinateBuilder.replace() : coordinateBuilder.after();
        J.MethodDeclaration usedMethodDeclaration = method;
        return template
                .map(javaTemplate -> javaTemplate.apply(updateCursor(usedMethodDeclaration), coordinates))
                .map(J.MethodDeclaration.class::cast)
                .orElse(method);
    }


    public String createSetterStatement(Accessor accessor) {
        if (accessor.getParent().isEmpty()) {
            return "";
        }
        String getter = accessor.getParent().get().getName();
        String setter = "set" + getter.substring(getter.startsWith("is") ? 2 : 3);
//        settersAdded.add(setter);

        String[] typeParts = accessor.getClazz().split("\\.");
        String objectFactoryCreate = "%s.create%s()".formatted(OBJECT_FACTORY_NAME, typeParts[typeParts.length - 1]);

        return SETTER_TEMPLATE.formatted(variableName, setter, objectFactoryCreate);
    }

    private Object[] getCreateDtoJavaTemplateParams(String shortWsdlType) {
        return accessor == null ?
                new Object[]{shortWsdlType, variableName, shortWsdlType} :
                new Object[]{shortWsdlType, variableName, shortWsdlType, variableName};

    }

    private Optional<JavaTemplate> getCreateDtoJavaTemplate(String wsdlType, boolean createsWebDto) {
        StringBuilder template = new StringBuilder();

        if (createsWebDto) {
            template.append(NEW_WEB_DTO.formatted(wsdlType, variableName, wsdlType));
        }
        if (accessor != null)
            template
                    .append(createsWebDto ? "\n" : "")
                    .append(createSetterStatement((accessor)));

        if (template.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(JavaTemplate.builder(template.toString())
                .javaParser(ParserUtil.createParserWithRuntimeClasspath())
                .imports(wsdlType)
                .contextSensitive()
                .build());
    }

    private static JavaTemplate javaTemplateOf(String template, String wsdlType) {
        return JavaTemplate
                .builder(template)
                .javaParser(ParserUtil.createParserWithRuntimeClasspath())
                .imports(wsdlType)
                .contextSensitive()
                .build();
    }

    private static String packageOf(String type) {
        return type.substring(0, type.lastIndexOf('.'));
    }

}
