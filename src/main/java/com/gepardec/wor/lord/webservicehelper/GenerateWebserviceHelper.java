package com.gepardec.wor.lord.webservicehelper;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Parser;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.TreeVisitor;
import org.openrewrite.text.PlainTextParser;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateWebserviceHelper extends ScanningRecipe<Accumulator> {
    private static final String FILE_PATH = "elgkk-util/src/main/resources/webserviceHelperContext.xml";

    @Override
    public String getDisplayName() {
        return "Append to release notes";
    }

    @Override
    public String getDescription() {
        return "Adds the specified line to RELEASE.md.";
    }

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        return new Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator acc) {
        return new ServiceSearch(acc);
    }


    @Override
    public Collection<? extends SourceFile> generate(Accumulator acc, ExecutionContext ctx) {
        List<Parser.Input> inputs = acc.getServicePackages().stream()
                .map(CodeGenerator::createInput)
                .collect(Collectors.toList());

        inputs.add(SpringContextFileGenerator.createFile(FILE_PATH));

        return PlainTextParser
                .builder()
                .build()
                .parseInputs(inputs, Path.of(""), ctx)
                .collect(Collectors.toList());
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Accumulator acc) {
            return super.getVisitor(acc);
    }
}
