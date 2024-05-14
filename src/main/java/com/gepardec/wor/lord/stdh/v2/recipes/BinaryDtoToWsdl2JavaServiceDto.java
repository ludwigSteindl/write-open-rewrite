package com.gepardec.wor.lord.stdh.v2.recipes;

import com.gepardec.wor.lord.stdh.v2.common.Accumulator;
import com.gepardec.wor.lord.stdh.v2.visitors.BinaryDtoToWebVisitor;
import com.gepardec.wor.lord.stdh.v2.visitors.Wsdl2JavaServiceScanningVisitor;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;

public class BinaryDtoToWsdl2JavaServiceDto extends ScanningRecipe<Accumulator> {

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
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator accumulator) {
        return new Wsdl2JavaServiceScanningVisitor(accumulator);
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor(Accumulator accumulator) {
        return new BinaryDtoToWebVisitor(accumulator);
    }


}
