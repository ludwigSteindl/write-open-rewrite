package com.gepardec.wor.lord.util;

public class JAXBElementUtil {
    public static final String JAXB_ELEMENT_CLASS_NAME = "JAXBElement";
    public static String unwrapJaxbElement(String type) {
        return isJaxbElement(type) ? getJaxbElementTypeParameter(type) : type;
    }

    public static boolean isJaxbElement(String type) {
        return type.startsWith(JAXB_ELEMENT_CLASS_NAME);
    }

    public static String getJaxbElementTypeParameter(String jaxbElementType) {
        return jaxbElementType.split("[<>]")[1];
    }
}
