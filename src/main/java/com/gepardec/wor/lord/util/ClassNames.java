package com.gepardec.wor.lord.util;

public class ClassNames {
    public static String packageOf(String type) {
        return type.substring(0, type.lastIndexOf('.'));
    }

    public static String shortNameOfFullyQualified(String fullyQualifiedName) {
        return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
    }
}
