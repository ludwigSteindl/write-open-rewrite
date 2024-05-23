package com.gepardec.wor.lord.util;

import org.openrewrite.java.tree.JavaType;

public class JavaTypes {
    public static boolean methodNameStartsWith(JavaType.Method method, String get) {
        return method.getName().startsWith(get);
    }

    public static boolean declaringTypeIsNotType(JavaType.Method method, JavaType type) {
        if (type instanceof JavaType.Class clazz) {
            String className = clazz.getFullyQualifiedName();
            return !className.equals(method.getDeclaringType().toString());
        }
        return false;
    }
}
