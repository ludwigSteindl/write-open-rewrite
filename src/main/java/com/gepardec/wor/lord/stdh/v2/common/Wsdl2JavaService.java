package com.gepardec.wor.lord.stdh.v2.common;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Wsdl2JavaService {
    private final static String PACKAGE_PREFIX = "at.sozvers.stp.lgkk.a02";
    private String serviceAlias;

    private List<String> requestTypes;

    private List<String> responseTypes;

    public Wsdl2JavaService(String serviceAlias, List<String> requestTypes, List<String> responseTypes) {

        this.serviceAlias = serviceAlias.toLowerCase();
        this.requestTypes = requestTypes;
        this.responseTypes = responseTypes;
    }

    public List<String> getIOTypes() {
        return getIOTypesStream().collect(Collectors.toList());
    }

    public Stream<String> getIOTypesStream() {
        return Stream.concat(
                    getIOTypes(requestTypes),
                    getIOTypes(responseTypes)
                );
    }

    private Stream<String> getIOTypes(List<String> classes) {
        return classes.stream()
                .filter(className -> className.toLowerCase().contains(PACKAGE_PREFIX));

    }

    public String getServiceAlias() {
        return serviceAlias;
    }

    public List<String> getRequestTypes() {
        return requestTypes;
    }

    public List<String> getResponseTypes() {
        return responseTypes;
    }
}
