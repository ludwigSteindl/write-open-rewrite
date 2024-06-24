package com.gepardec.wor.lord.servicehelper;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpringContextFileGenerator {
    private static final String META_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String ROOT_START = """
            <beans xmlns="http://www.springframework.org/schema/beans"
            	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            	xmlns:util="http://www.springframework.org/schema/util"
            	xsi:schemaLocation=
            		"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
            		 http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd">\
            """;

    private static final String ROOT_END = "</beans>";

    private static final String BEAN_TEMPLATE =
            "<bean id=\"%s\" class=\"at.sozvers.stp.lgkk.webservice.helper.%sServiceHelper\" />";

    private static final String MAP_START = """
            <util:map id="webserviceHelpers"
            	map-class="java.util.HashMap"
            	key-type="java.lang.String"
            	value-type="at.sozvers.stp.lgkk.webservice.common.WebserviceHelper">\
            """;

    private static final String MAP_ENTRY_TEMPLATE = """
            <entry key="%s">
            	<ref bean="%s" />
            </entry>\
            """;

    private static final String MAP_END = "</util:map>";

    public static String generate(List<String> serviceAliases) {
        StringBuilder xml = new StringBuilder();
        appendMeta(xml);
        appendRootStart(xml);

        appendBeans(serviceAliases, xml);
        appendMap(serviceAliases, xml);

        appendRootEnd(xml);
        return xml.toString();
    }

    private static void appendRootEnd(StringBuilder xml) {
        xml.append(ROOT_END);
    }

    private static void appendMap(List<String> serviceAliases, StringBuilder xml) {
        appendMapStart(xml);
        appendMapEntries(serviceAliases, xml);
        appendMapEnd(xml);
    }

    private static void appendMapEnd(StringBuilder xml) {
        xml.append(withIndent(MAP_END,1) + "\n");
    }

    @NotNull
    private static StringBuilder appendMapStart(StringBuilder xml) {
        return xml
                .append(withIndent(MAP_START, 1))
                .append("\n");
    }

    @NotNull
    private static StringBuilder appendRootStart(StringBuilder xml) {
        return xml.append(ROOT_START + "\n");
    }

    private static void appendMeta(StringBuilder xml) {
        xml.append(META_TAG + "\n");
    }

    private static void appendMapEntries(List<String> serviceAliases, StringBuilder xml) {
        serviceAliases.stream()
                .map(alias -> String.format(MAP_ENTRY_TEMPLATE, alias.toUpperCase(), alias.toUpperCase()))
                .map(entry -> withIndent(entry, 2))
                .map(entry -> entry + "\n")
                .forEach(xml::append);
    }

    private static void appendBeans(List<String> serviceAliases, StringBuilder xml) {
        serviceAliases.stream()
                .map(alias -> String.format(BEAN_TEMPLATE, alias.toUpperCase(), getClassName(alias)))
                .map(bean -> withIndent(bean, 1))
                .map(entry -> entry + "\n")
                .forEach(xml::append);
    }


    private static String withIndent(String string, int indent) {
        return string.lines()
                .map(line -> "\t".repeat(indent) + line)
                .reduce((line1, line2) -> line1 + "\n" + line2)
                .get();
    }

    private static String getClassName(String serviceAlias) {
        serviceAlias = serviceAlias.toLowerCase();
        return serviceAlias.substring(0, 1).toUpperCase() + serviceAlias.substring(1);
    }
}
