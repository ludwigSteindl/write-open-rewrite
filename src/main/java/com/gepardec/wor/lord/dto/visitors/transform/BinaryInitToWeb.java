package com.gepardec.wor.lord.dto.visitors.transform;

import com.gepardec.wor.lord.common.Accessor;
import com.gepardec.wor.lord.common.Accumulator;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BinaryInitToWeb extends JavaIsoVisitor<ExecutionContext> {
    private List<String> variableNames;
    private Map<String, List<Accessor>> accessors;
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

    private static final String OBJECT_FACTORY_NAME = "OBJECT_FACTORY";


    public BinaryInitToWeb(List<String> variableNames, Map<String, List<Accessor>> accessors, Accumulator accumulator) {
        this.variableNames = variableNames;
        this.accessors = accessors;
        this.accumulator = accumulator;
    }

    public static @NotNull Optional<J.VariableDeclarations> getDeclarationOfVariable(List<Statement> statements, String variable) {
        LOG.info("Search for variable <" + variable + "> in declarations: ");
        LOG.info("__________");
        var ret = LSTUtil.extractStatementsOfType(statements, J.VariableDeclarations.class)
                .stream()
                .peek(d -> LOG.info("Declaration | " + d))
                .filter(variableDeclarations -> variableDeclarations.getVariables().get(0).getSimpleName().equals(variable))
                .findFirst();
        LOG.info("__________");
        LOG.info(ret.map(variableDeclarations -> "Found " + variableDeclarations).orElse("Found nothing"));
        LOG.info("\n\n");
        return ret;
    }

    @Override
    public J.Block visitBlock(J.Block block, ExecutionContext ctx) {
        block = super.visitBlock(block, ctx);
        String classType = getCursor().getParent().firstEnclosingOrThrow(J.ClassDeclaration.class).getType().toString();
        if (classType.startsWith("at.sozvers.stp.lgkk.a02.laaamhsu")
            || classType.startsWith("at.sozvers.stp.lgkk.gensvc")) {
            return block;
        }

        List<J.VariableDeclarations> dtoDeclarations = getDtoDeclaration(block.getStatements());

        if (dtoDeclarations.isEmpty()) {
            return block;
        }

        J.Block newBlock = block;
        for (J.VariableDeclarations dtoDeclaration : dtoDeclarations) {
            Optional<String> variableName = variableNames.stream()
                    .filter(variable -> dtoDeclaration.getVariables().get(0).getName().toString().equals(variable))
                    .findFirst();


            if (variableName.isPresent()) {
                newBlock = forVariable(newBlock, dtoDeclaration, variableName.get());
            }
        }
        return newBlock;
    }

    private J.Block forVariable(J.Block block, J.VariableDeclarations dtoDeclarations, String variableName) {
        boolean createsWebDto = false;
        String dtoShortType = getShortTypeOfDeclarations(dtoDeclarations);
        String wsdlType = null;
        if (dtoShortType.contains("Dto")) {
            createsWebDto = true;
            String binaryType = LSTUtil.getType(dtoDeclarations);
            Optional<String> wsdlTypeFromBinary = accumulator.lookupWsdlTypeFromBinary(binaryType);

            if (wsdlTypeFromBinary.isEmpty()) {
                return block;
            }

            replaceBinaryImportWithWeb(binaryType, wsdlTypeFromBinary.get());
            addObjectFactoryCreationToClass(wsdlTypeFromBinary);

            wsdlType = LSTUtil.shortNameOfFullyQualified(wsdlTypeFromBinary.get());

        }
        if (accumulator.getIOTypesShort().contains(dtoShortType)) {
            wsdlType = dtoShortType;
        }

        if (wsdlType == null) {
            return block;
        }

        return updateBlockToInitializeWebDto(block, dtoDeclarations, createsWebDto, wsdlType, variableName);
    }

    public Optional<String> createSetterStatement(Accessor accessor, String variableName) {
        if (accessor.getParent().isEmpty()) {
            return Optional.empty();
        }
        String getter = accessor.getParent().get().getName();
        String setter = "set" + removeGetterPrefix(getter);

        String accessorClassName = LSTUtil.shortNameOfFullyQualified(accessor.getClazz());
        String objectFactoryCreate = String.format("%s.create%s()", OBJECT_FACTORY_NAME, accessorClassName);

        return Optional.of(String.format(SETTER_TEMPLATE, variableName, setter, objectFactoryCreate)
        );
    }

    private void addObjectFactoryCreationToClass(Optional<String> wsdlTypeFromBinary) {
        doAfterVisit(new ObjectFactoryCreator(
                OBJECT_FACTORY_NAME,
                LSTUtil.packageOf(wsdlTypeFromBinary.get())));
    }

    private J.Block updateBlockToInitializeWebDto(J.Block block, J.VariableDeclarations dtoDeclarations, boolean createsWebDto, String newType, String variableName) {
        List<Accessor> accessorsForVariable = accessors.get(variableName);
        List<String> newSetters = accessorsForVariable
                .stream()
                .map(acc -> this.createSetterStatement(acc, variableName))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(Collectors.toList());

        String webDeclaration = getCreateDtoJavaTemplate(newType, variableName);

        JavaCoordinates coordinates = getTemplateCoordinates(dtoDeclarations, true);
        J.Block newBlock =  JavaTemplate
                .builder(webDeclaration)
                .build()
                .apply(updateCursor(block), coordinates);

        J.VariableDeclarations newDeclaration = getDeclarationOfVariable(newBlock.getStatements(), variableName).get();

        JavaCoordinates newCoordinates = getTemplateCoordinates(newDeclaration, false);
        for (String webSetter : newSetters) {
            if (!containsMethodInvocationOf(newBlock, webSetter))
                newBlock = JavaTemplate
                    .builder(webSetter)
                    .build()
                    .apply(updateCursor(newBlock), newCoordinates);
        }

        return newBlock;
    }

    private boolean containsMethodInvocationOf(J.Block block, String newSetter) {
        return block.getStatements()
                .stream()
                .map(statement -> statement.printTrimmed(getCursor()))
                .map(s -> s + ";")
                .anyMatch(line -> line.replace("\n", "").equals(newSetter));
    }

    private static @NotNull JavaCoordinates getTemplateCoordinates(J.VariableDeclarations dtoDeclarations, boolean replace) {
        CoordinateBuilder.VariableDeclarations coordinateBuilder = dtoDeclarations.getCoordinates();
        return replace ?coordinateBuilder.replace() : coordinateBuilder.after();
    }


    private String getShortTypeOfDeclarations(J.VariableDeclarations dtoDeclarations) {
        String dtoType = dtoDeclarations.getTypeExpression().toString();
        return LSTUtil.shortNameOfFullyQualified(dtoType);
    }

    private @NotNull List<J.VariableDeclarations> getDtoDeclaration(List<Statement> statements) {
        return variableNames.stream()
                .map(variable -> getDeclarationOfVariable(statements, variable))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void replaceBinaryImportWithWeb(String binaryType, String wsdlType) {
        doAfterVisit(new AddImport<>(wsdlType, null, false));
        doAfterVisit(new RemoveImport<>(binaryType, true));
    }

    private static @NotNull String removeGetterPrefix(String getter) {
        return getter.substring(getter.startsWith("is") ? 2 : 3);
    }

    private String getCreateDtoJavaTemplate(String wsdlType, String variableName) {
        return String.format(NEW_WEB_DTO, wsdlType, variableName, wsdlType);
    }
}
