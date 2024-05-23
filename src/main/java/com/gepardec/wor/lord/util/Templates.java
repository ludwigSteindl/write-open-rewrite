package com.gepardec.wor.lord.util;

import org.openrewrite.java.JavaTemplate;

public class Templates {
    public static JavaTemplate javaTemplateOf(String template, String... importType) {
        return JavaTemplate
                .builder(template)
                .javaParser(Parsers.createParserWithRuntimeClasspath())
                .imports(importType)
                .contextSensitive()
                .build();
    }
}
