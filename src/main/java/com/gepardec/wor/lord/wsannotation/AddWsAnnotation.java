package com.gepardec.wor.lord.wsannotation;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.AnnotationMatcher;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.service.AnnotationService;
import org.openrewrite.java.service.ImportService;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.TypeUtils;

import java.util.Comparator;
import java.util.List;

public class AddWsAnnotation extends Recipe {
    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {
            @Override
            public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext executionContext) {
                if (!classes.contains(classDecl.getName().toString())) {
                    return classDecl;
                }

                boolean hasWebserviceAnnotation = service(AnnotationService.class).getAllAnnotations(getCursor())
                        .stream()
                        .anyMatch(annotation -> annotation.getSimpleName().equals("WebService"));
                if (hasWebserviceAnnotation) {
                    return classDecl;
                }

                maybeAddImport("javax.jws.WebService", false);
                classDecl = JavaTemplate.apply("@WebService", updateCursor(classDecl),
                        classDecl.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));
                return super.visitClassDeclaration(classDecl, executionContext);
            }

            List<String> classes = List.of(
                    "Laaaebv2",
                    "Laaaebv3",
                    "Laaaebv4",
                    "Laaaebv5",
                    "Laaaebvw",
                    "Laaamhl2",
                    "Laaamhl3",
                    "Laaamhl4",
                    "Laaamhl7",
                    "Laaamhl9",
                    "Laaamhle",
                    "Laaamhsu",
                    "Laaanwa1",
                    "Laaanwa2",
                    "Laaanwal",
                    "Laaauml3",
                    "Laaaumv2",
                    "Laaaumv3",
                    "Laaaumv4",
                    "Laaaumvw",
                    "Laadgil2",
                    "Laadgil3",
                    "Laadgile",
                    "Laaeauue",
                    "Laaefesu",
                    "Laawgavw",
                    "Ladaebvw",
                    "Ladamhl5",
                    "Ladauml2",
                    "Ladausv2",
                    "Ladausvw",
                    "Ladbesvw",
                    "Ladktyan",
                    "Lfafrtan",
                    "Lhxtarif",
                    "Lixbehsu",
                    "Lkalanko",
                    "Lmaaebv2",
                    "Lmaaebvw",
                    "Lmaemhue",
                    "Lmaemhvw",
                    "Lmdaebvw",
                    "Lraeinl1",
                    "Lrargfl1",
                    "Lrdanwle",
                    "Ltapk2le",
                    "Ltapkyle",
                    "Lwaaikvw",
                    "Lwaarv2",
                    "Lwaarv3",
                    "Lwaarvo",
                    "Lwawa2vw",
                    "Lwawahan",
                    "Lwawahh2",
                    "Lwawahh3",
                    "Lwawahho",
                    "Lwawazan",
                    "Lwawdivw",
                    "Lwawherf",
                    "Lwawzerf",
                    "Lwxgesch",
                    "Lwxgproz",
                    "Lwxtarif");
        };
    }
}
