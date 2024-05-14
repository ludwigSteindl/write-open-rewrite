package com.gepardec.wor.lord.stdh.v2.recipes;

import com.gepardec.wor.lord.stdh.v2.common.Accumulator;
import com.gepardec.wor.lord.stdh.v2.visitors.BinaryDtoToWebVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

import java.util.List;

public class BinaryDtoToWeb extends Recipe {
    @Override
    public String getDisplayName() {
        // language=markdown
        return "Change binary standard request headers to web stdh";
    }

    @Override
    public String getDescription() {
        return "Change binary proxy call to web call.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new BinaryDtoToWebVisitor(Accumulator.of("laaamhsu", List.of("at.sozvers.stp.lgkk.a02.laaamhsu.Laqamhsu", "at.sozvers.stp.lgkk.a02.laaamhsu.Lasamhsu")));
    }
}
