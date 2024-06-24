package com.gepardec.wor.lord.common;

import com.gepardec.wor.lord.util.LSTUtil;

import java.util.ArrayList;
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

    private List<Accessor> root = new ArrayList<>();

    private List<Accessor> accessors;

    public void setRequestTypes(List<String> requestTypes) {
        this.requestTypes = excludeNonDtoTypes(requestTypes);
    }

    public void setResponseTypes(List<String> responseTypes) {
        this.responseTypes = excludeNonDtoTypes(responseTypes);
    }

    public Wsdl2JavaService(String serviceAlias, List<String> requestTypes, List<String> responseTypes) {
        this.serviceAlias = serviceAlias;
        this.setRequestTypes(requestTypes);
        this.setResponseTypes(responseTypes);
        this.accessors = new LinkedList<>();
    }

    public List<String> getRequestTypes() {
        return requestTypes;
    }

    public List<String> getResponseTypes() {
        return responseTypes;
    }

    public void structureAccessors() {
        accessors = accessors.stream().filter(accessor -> root
                .stream()
                .anyMatch(rootAccessor -> LSTUtil.shortNameOfFullyQualified(rootAccessor
                    .getType())
                    .equals(accessor.getClazz())
                )
            )
            .flatMap(acc -> structureAccessors(acc).stream())
            .collect(Collectors.toList());
    }

    public List<Accessor> structureAccessors(Accessor accessor) {
        List<Accessor> childAccessors =  accessors.stream()
                .filter(acc -> acc.getClazz().equals(accessor.getType()))
                .map(acc -> Accessor
                        .builder(acc.getName(), acc.getClazz())
                        .type(acc.getType())
                        .parent(accessor)
                        .build()
                )
                .map(this::structureAccessors)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return childAccessors.isEmpty() ? List.of(accessor) : childAccessors;
    }


    public void setRoot(List<Accessor> root) {
        this.root = root;
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
