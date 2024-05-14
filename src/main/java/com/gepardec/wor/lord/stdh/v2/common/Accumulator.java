package com.gepardec.wor.lord.stdh.v2.common;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Accumulator {
    List<Wsdl2JavaService> wsdl2JavaServices = new LinkedList<>();

    public void addService(Wsdl2JavaService wsdl2JavaService) {
        this.wsdl2JavaServices.add(wsdl2JavaService);
    }

    public List<Wsdl2JavaService> getServices() {
        return wsdl2JavaServices;
    }

    public List<String> getIOTypesShort() {
        return wsdl2JavaServices.stream()
                .flatMap(Wsdl2JavaService::getIOTypesStream)
                .map(type -> type.substring(type.lastIndexOf('.') + 1))
                .collect(Collectors.toList());
    }

    public List<String> getIOTypes() {
        return wsdl2JavaServices.stream()
                .flatMap(Wsdl2JavaService::getIOTypesStream)
                .collect(Collectors.toList());
    }

    public static Accumulator of(String serviceAlias, List<String> types) {
        Accumulator accumulator = new Accumulator();
        accumulator.addService(new Wsdl2JavaService(serviceAlias, types, List.of()));
        return accumulator;
    }


}
