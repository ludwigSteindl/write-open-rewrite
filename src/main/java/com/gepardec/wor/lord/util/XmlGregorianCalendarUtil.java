package com.gepardec.wor.lord.util;

import java.util.Calendar;

public class XmlGregorianCalendarUtil {
    public static final String DEFAULT_DATA_TYPE_FACTORY_NAME = "DATA_TYPE_FACTORY";
    private static final String XML_GREGORIAN_CONVERSION =
            "DatumsUtil.toXMLGregorianCalendar(%s)";

    private static final String CALENDAR_CLASS_NAME = Calendar.class.getSimpleName();



    public static boolean isCalendar(String type) {
        type = getSimpleName(type);

        return type.equals(CALENDAR_CLASS_NAME);
    }

    public static String convertToXmlGregorian(String argumentString) {
        return String.format(XML_GREGORIAN_CONVERSION, argumentString);
    }

    private static String getSimpleName(String type) {
        return LSTUtil.shortNameOfFullyQualified(type);
    }
}
