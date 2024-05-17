package com.gepardec.wor.lord.dto;

import com.gepardec.wor.lord.dto.common.Accumulator;
import com.gepardec.wor.lord.dto.visitors.transform.BinarySetterToWeb;
import com.gepardec.wor.lord.dto.visitors.search.WSDLTypesSearch;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;

public class BinaryDtoToWeb extends ScanningRecipe<Accumulator> {

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
        return new WSDLTypesSearch(accumulator);
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor(Accumulator accumulator) {
        return new BinarySetterToWeb(accumulator);
    }


}
