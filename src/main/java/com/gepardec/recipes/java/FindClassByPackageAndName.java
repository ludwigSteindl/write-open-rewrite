package com.gepardec.recipes.java;

import com.gepardec.wor.lord.util.LSTUtil;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.SearchResult;

public class FindClassByPackageAndName extends Recipe {

    @Option
    private String classFqn;

    public FindClassByPackageAndName(String classFqn) {
        this.classFqn = classFqn;
    }

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                return classDecl.getType().getFullyQualifiedName().equals(classFqn) ?
                        SearchResult.found(classDecl) : classDecl;
            }
        };
    }
}
