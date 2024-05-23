package com.gepardec.wor.lord.util;

import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

import java.util.List;
import java.util.stream.Collectors;

public class Statements {
    public static <T extends J> List<T> extractStatementsOfType(List<Statement> statements, Class<T> type) {
        return statements.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }
}
