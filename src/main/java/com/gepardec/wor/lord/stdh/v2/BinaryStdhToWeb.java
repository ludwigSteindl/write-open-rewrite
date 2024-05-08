package com.gepardec.wor.lord.stdh.v2;

import com.gepardec.wor.lord.stdh.v2.BinaryStdhToWebVisitor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

public class BinaryStdhToWeb extends Recipe {
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
        return new BinaryStdhToWebVisitor();
    }
}
