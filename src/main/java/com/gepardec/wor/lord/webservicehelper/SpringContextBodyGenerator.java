package com.gepardec.wor.lord.webservicehelper;

import com.gepardec.wor.lord.dto.visitors.transform.BinarySetterToWeb;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Content;
import org.openrewrite.xml.tree.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class SpringContextBodyGenerator extends XmlVisitor<ExecutionContext> {
    private static final Logger LOG = LoggerFactory.getLogger(BinarySetterToWeb.class);

    private static final String BEAN_TEMPLATE = "<bean id=\"%s\" class=\"%s\" />\n";
    private static final String BEAN_CLASS_TEMPLATE = "at.sozvers.stp.lgkk.webservice.helper.%sServiceHelper";
    private static final String MAP =
            """
            <util:map id="webserviceHelpers"
               map-class="java.util.HashMap"
               key-type="java.lang.String"
               value-type="at.sozvers.stp.lgkk.webservice.common.WebserviceHelper" \\>\
            """;
    private static final String MAP_ENTRY_TEMPLATE =
            """
            <entry key="%s">
               <ref bean="%s" \\>
            </entry>\
            """;

    private final String filePath;
    private final String beansTagName;
    private final Accumulator accumulator;

    public SpringContextBodyGenerator(String filePath, String beansTagName, Accumulator accumulator) {
        this.filePath = filePath;
        this.accumulator = accumulator;
        this.beansTagName = beansTagName;
    }

    @Override
    public Xml visitDocument(Xml.Document document, ExecutionContext context) {
        if (!hasFilePath(document)) {
            return document;
        }
        return super.visitDocument(document, context);
    }

    @Override
    public Xml visitTag(Xml.Tag tag, ExecutionContext context) {
        LOG.info("Visited {}", tag);
        Xml xml = super.visitTag(tag, context);
        if (!(xml instanceof Xml.Tag)) {
            return xml;
        }
        tag = (Xml.Tag) xml;

        if (!tag.getName().equals("beans")) {
            return tag;
        }

        List<Xml.Tag> newSubTags = createBeans();
        newSubTags.add(createMap());

        return tag.withContent(newSubTags);
    }

    private @NotNull List<Xml.Tag> createBeans() {
        return accumulator.getServiceClasses().stream()
                .map(serviceClass -> String.format(BEAN_TEMPLATE, serviceClass.toUpperCase(), toBeanClass(serviceClass)))
                .peek(xml -> LOG.info("Bean created: {}", xml))
                .map(Xml.Tag::build)
                .collect(Collectors.toUnmodifiableList());
    }

    private Xml.Tag createMap() {
        Xml.Tag map = Xml.Tag.build(MAP);
        LOG.info("Map created: {}", MAP);

        return map.withContent(createMapEntries());
    }

    private List<Xml.Tag> createMapEntries() {
        return accumulator.getServiceClasses().stream()
                .map(serviceClass -> String.format(MAP_ENTRY_TEMPLATE, serviceClass.toUpperCase(), serviceClass.toUpperCase()))
                .peek(xml -> LOG.info("Entry created: {}", xml))
                .map(Xml.Tag::build)
                .collect(Collectors.toUnmodifiableList());
    }

    private static String toBeanClass(String serviceClass) {
        return String.format(BEAN_CLASS_TEMPLATE, serviceClass);
    }

    private boolean hasFilePath(Xml.Document document) {
        return document
                .getSourcePath()
                .toString()
                .equals(filePath);
    }
}
