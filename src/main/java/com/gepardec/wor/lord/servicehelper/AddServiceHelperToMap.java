package com.gepardec.wor.lord.servicehelper;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.UUID;

public class AddServiceHelperToMap extends XmlVisitor<ExecutionContext> {
    @Override
    public Xml visitDocument(Xml.Document document, ExecutionContext executionContext) {
        if (document.getSourcePath().toString().endsWith("webserviceHelperContext.xml")) {
            Xml.Tag root = document.getRoot();
//            root.withContent(root.getContent().add(new Xml.Tag(UUID.randomUUID(), "", null, "bean", List.of())))
        }
        return super.visitDocument(document, executionContext);
    }
}
