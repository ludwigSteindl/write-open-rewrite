package com.gepardec.wor.lord.call.ternaries;

import com.gepardec.wor.lord.common.Accumulator;
import com.gepardec.wor.lord.common.search.WSDLTypesSearch;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;

public class BinaryProxyToWebTernaryAndClass extends ScanningRecipe<Accumulator> {
    @Override
    public String getDisplayName() {
        // language=markdown
        return "Change binary proxy calls to web calls";
    }

    @Override
    public String getDescription() {
        return "Change binary proxy call to web call.";
    }

    @Override
    public Accumulator getInitialValue(ExecutionContext ctx) {
        return new Accumulator();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Accumulator acc) {
        return new WSDLTypesSearch(acc);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Accumulator accumulator) {
        return new BinaryProxyToWebTernaryVisitor(accumulator);
    }
}
