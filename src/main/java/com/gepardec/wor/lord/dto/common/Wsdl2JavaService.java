package com.gepardec.wor.lord.dto.common;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Wsdl2JavaService {
    private final static String PACKAGE_PREFIX = "at.sozvers.stp.lgkk.a02";
    private String serviceAlias;

    private List<String> requestTypes;

    private List<String> responseTypes;

    private List<Accessor> accessors;

    public Wsdl2JavaService(String serviceAlias, List<String> requestTypes, List<String> responseTypes) {

        this.serviceAlias = serviceAlias;
        this.requestTypes = excludeNonDtoTypes(requestTypes);
        this.responseTypes = excludeNonDtoTypes(responseTypes);
        this.accessors = new LinkedList<>();
    }

    public void addAccessors(List<Accessor> accessors) {
        this.accessors.addAll(accessors);
    }

    public List<Accessor> getAccessors() {
        return accessors;
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

    private static List<String> excludeNonDtoTypes(List<String> types) {
        Pattern packagePrefix = Pattern.compile("^" + PACKAGE_PREFIX);
        return types.stream()
                .filter(packagePrefix.asPredicate())
                .collect(Collectors.toList());
    }

    public String getServiceAlias() {
        return serviceAlias;
    }
}
