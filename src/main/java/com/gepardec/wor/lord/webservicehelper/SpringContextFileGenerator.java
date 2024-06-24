package com.gepardec.wor.lord.webservicehelper;

import org.openrewrite.Parser;

import java.nio.file.Path;

// Stuff like this should be added later
//<bean id=\"LAAAUMV4\" class=\"at.sozvers.stp.lgkk.webservice.helper.Laaaumv4ServiceHelper\" />\n" +
//        "\n" +
//        "    <util:map id=\"webserviceHelpers\"\n" +
//        "              map-class=\"java.util.HashMap\"\n" +
//        "              key-type=\"java.lang.String\"\n" +
//        "              value-type=\"at.sozvers.stp.lgkk.webservice.common.WebserviceHelper\">\n" +
//        "        <entry key="LAAAUMV4">
//        "            <ref bean="LAAAUMV"\>
//        "        </entry>
//        "    </util:map>\n" +
public class SpringContextFileGenerator {
    private static final String META_TAG = "\"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\\n\"\n";
    private static final String ROOT_TAG = """
            <beans xmlns="http://www.springframework.org/schema/beans"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:util="http://www.springframework.org/schema/util"
                   xsi:schemaLocation=
                           "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
                            http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util.xsd">
            
            </beans>\
            """;

    private static final String BEAN_TEMPLATE = "<bean id=\"%s\" class=\"%s\" />\n";
    private static final String BEAN_CLASS_TEMPLATE = "at.sozvers.stp.lgkk.webservice.helper.%sServiceHelper";

    public static Parser.Input createFile(String filePath) {
        return Parser.Input
                .fromString(
                        Path.of(filePath),
                        createRoot()
                );
    }

    private static String createRoot() {
        return META_TAG + ROOT_TAG;
    }
}
