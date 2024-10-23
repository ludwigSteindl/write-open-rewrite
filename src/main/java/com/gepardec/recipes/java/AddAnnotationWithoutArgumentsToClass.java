package com.gepardec.recipes.java;

import org.jetbrains.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;

import java.util.Comparator;

public class AddAnnotationWithoutArgumentsToClass extends Recipe {
    @Override
    public String getDisplayName() {
        return "Add annotation to class";
    }

    @Override
    public String getDescription() {
        return "Adds an annotation to a class.";
    }

    @Option
    private final String classFqn;

    @Option
    private final String annotationFqn;

    @Option
    private final Boolean repeatable;

    private transient final String annotationSimpleName;

    private transient final JavaTemplate newAnnotation;

    private transient final AnnotationMatcher annotationMatcher;



    public AddAnnotationWithoutArgumentsToClass(String classFqn, String annotationFqn, @Nullable Boolean repeatable) {
        this.classFqn = classFqn;
        this.annotationFqn = annotationFqn;
        this.annotationSimpleName = annotationFqn == null ? null : annotationFqn.substring(annotationFqn.lastIndexOf('.') + 1);
        this.repeatable = repeatable != null && repeatable;

        newAnnotation = annotationFqn == null ? null : JavaTemplate
                .builder("@#{}")
                .javaParser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()))
                .imports(annotationFqn)
                .build();

        annotationMatcher = annotationFqn == null ? null :new AnnotationMatcher(annotationFqn);
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
                new FindClassByPackageAndName(classFqn),
                new JavaIsoVisitor<>() {
                    @Override
                    public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                        classDecl =  super.visitClassDeclaration(classDecl, executionContext);

                        if (!repeatable && classHasAnnotation(classDecl)) {
                            return classDecl;
                        }

                        maybeAddImport(annotationFqn, false);
                        return newAnnotation
                                .apply(
                                    updateCursor(classDecl),
                                    classDecl.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)),
                                    annotationSimpleName
                                );

                    }
                    private boolean classHasAnnotation(J.ClassDeclaration classDecl) {
                        return classDecl.getLeadingAnnotations().stream()
                                .anyMatch(annotationMatcher::matches);

                    }
                }
        );
    }
}