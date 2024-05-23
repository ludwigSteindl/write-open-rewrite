package com.gepardec.wor.lord.dto.visitors.transform;

import com.gepardec.wor.lord.dto.common.Accessor;
import com.gepardec.wor.lord.dto.common.Accumulator;
import com.gepardec.wor.lord.util.LSTUtil;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.java.AddImport;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.RemoveImport;
import org.openrewrite.java.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class BinaryInitToWeb extends JavaIsoVisitor<ExecutionContext> {
    private String variableName;
    private Accessor accessor;
    private Accumulator accumulator;

    private static final Logger LOG = LoggerFactory.getLogger(BinaryInitToWeb.class);

    // Template for creating a new instance of a type
    // pos1 & pos3: type name
    // pos2: variable name
    private static final String NEW_WEB_DTO = "%s %s = new %s();";

    // Template for creating a setter statement
    // pos1: variable name
    // pos2: setter name
    // pos3: value to set
    public static final String SETTER_TEMPLATE = "%s.%s(%s);";

    private static final String OBJECT_FACTORY_NAME = "objectFactory";


    public BinaryInitToWeb(String variableName, Accessor accessor, Accumulator accumulator) {
        this.variableName = variableName;
        this.accessor = accessor;
        this.accumulator = accumulator;
    }

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext ctx) {
        method = super.visitMethodDeclaration(method, ctx);

        boolean createsWebDto = false;
        Optional<J.VariableDeclarations> dtoDeclarations = getDtoDeclaration(LSTUtil.getStatements(method));

        if (dtoDeclarations.isEmpty()) {
            return method;
        }

        String dtoShortType = getShortTypeOfDeclarations(dtoDeclarations.get());
        String wsdlType = null;
        if (dtoShortType.contains("Dto")) {
            createsWebDto = true;
            String binaryType = LSTUtil.getType(dtoDeclarations.get());
            Optional<String> wsdlTypeFromBinary = accumulator.lookupWsdlTypeFromBinary(binaryType);

            if (wsdlTypeFromBinary.isEmpty()) {
                return method;
            }

            replaceBinaryImportWithWeb(binaryType, wsdlTypeFromBinary.get());
            addObjectFactoryCreationToClass(wsdlTypeFromBinary);

            wsdlType = LSTUtil.shortNameOfFullyQualified(wsdlTypeFromBinary.get());

        }
        if (accumulator.getIOTypesShort().contains(dtoShortType)) {
            wsdlType = dtoShortType;
        }

        if (wsdlType == null) {
            return method;
        }

        return updateMethodToInitializeWebDto(method, dtoDeclarations.get(), createsWebDto, wsdlType);
    }

    public Optional<String> createSetterStatement(Accessor accessor) {
        if (accessor.getParent().isEmpty()) {
            return Optional.empty();
        }
        String getter = accessor.getParent().get().getName();
        String setter = "set" + removeGetterPrefix(getter);

        String accessorClassName = LSTUtil.shortNameOfFullyQualified(accessor.getClazz());
        String objectFactoryCreate = String.format("%s.create%s()", OBJECT_FACTORY_NAME, accessorClassName);

        return Optional.of(String.format(SETTER_TEMPLATE, variableName, setter, objectFactoryCreate));
    }

    private void addObjectFactoryCreationToClass(Optional<String> wsdlTypeFromBinary) {
        doAfterVisit(new ObjectFactoryCreator(
                OBJECT_FACTORY_NAME,
                LSTUtil.packageOf(wsdlTypeFromBinary.get())));
    }

    private J.MethodDeclaration updateMethodToInitializeWebDto(J.MethodDeclaration method, J.VariableDeclarations dtoDeclarations, boolean createsWebDto, String newType) {
        JavaCoordinates coordinates = getTemplateCoordinates(dtoDeclarations, createsWebDto);

        Optional<String> lineToBeCreated = createSetterStatement(accessor);
        String newSetter = lineToBeCreated
                .filter(setter -> containsMethodInvocationOf(method, setter))
                .orElse("");


        Optional<JavaTemplate> initStatements = getCreateDtoJavaTemplate(newType, newSetter, createsWebDto);
        initStatements
                .ifPresent(statements ->  LOG.info("Changing Dto init " +
                    method.printTrimmed(getCursor()) +
                    "to: " + statements.getCode()));
        return initStatements
                .map(javaTemplate -> javaTemplate.apply(updateCursor(method), coordinates))
                .map(J.MethodDeclaration.class::cast)
                .orElse(method);
    }

    private boolean containsMethodInvocationOf(J.MethodDeclaration method, String newSetter) {
        return LSTUtil.getStatements(method)
                .stream()
                .map(statement -> statement.printTrimmed(getCursor()))
                .map(s -> s + ";")
                .anyMatch(line -> line.equals(newSetter));
    }

    private static @NotNull JavaCoordinates getTemplateCoordinates(J.VariableDeclarations dtoDeclarations, boolean createsWebDto) {
        CoordinateBuilder.VariableDeclarations coordinateBuilder = dtoDeclarations.getCoordinates();
        return createsWebDto ? coordinateBuilder.replace() : coordinateBuilder.after();
    }


    private String getShortTypeOfDeclarations(J.VariableDeclarations dtoDeclarations) {
        String dtoType = dtoDeclarations.getTypeExpression().toString();
        return LSTUtil.shortNameOfFullyQualified(dtoType);
    }

    private @NotNull Optional<J.VariableDeclarations> getDtoDeclaration(List<Statement> statements) {
        return LSTUtil.getDeclarationOfVariable(statements, variableName);
    }

    private void replaceBinaryImportWithWeb(String binaryType, String wsdlType) {
        doAfterVisit(new AddImport<>(wsdlType, null, false));
        doAfterVisit(new RemoveImport<>(binaryType, true));
    }

    private static @NotNull String removeGetterPrefix(String getter) {
        return getter.substring(getter.startsWith("is") ? 2 : 3);
    }

    private Optional<JavaTemplate> getCreateDtoJavaTemplate(String wsdlType, String newSetter, boolean createsWebDto) {
        StringBuilder template = new StringBuilder();

        if (createsWebDto) {
            template.append(String.format(NEW_WEB_DTO, wsdlType, variableName, wsdlType));
        }
        if (accessor != null)
            template
                    .append(createsWebDto ? "\n" : "")
                    .append(newSetter);

        if (template.length() == 0) {
            return Optional.empty();
        }

        JavaTemplate resultingTemplate = LSTUtil.javaTemplateOf(template.toString(), wsdlType);
        return Optional.of(resultingTemplate);
    }

}
