package com.gepardec.wor.lord.call.ternaries;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;

public class BinaryProxyToWebTernaryAndClass extends Recipe {
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
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new BinaryProxyToWebTernaryVisitor();
    }
}
