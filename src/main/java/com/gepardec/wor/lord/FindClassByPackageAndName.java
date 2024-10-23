package com.gepardec.wor.lord;

import com.gepardec.wor.lord.util.LSTUtil;
import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.SearchResult;

public class FindClassByPackageAndName extends Recipe {

    @Option
    private String className;

    @Option
    private String packageName;

    public FindClassByPackageAndName(String className, String packageName) {
        this.className = className;
        this.packageName = packageName;
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
                String declaredPackage = LSTUtil.getPackageName(getCursor());

                return isSearchedClass(declaredPackage, classDecl.getSimpleName()) ?
                        SearchResult.found(classDecl) : classDecl;
            }
        };
    }

    private boolean isSearchedClass(String packageName, String className) {
        return packageName.equals(this.packageName) && className.equals(this.className);
    }
}
