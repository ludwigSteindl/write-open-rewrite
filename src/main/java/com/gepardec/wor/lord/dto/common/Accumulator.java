package com.gepardec.wor.lord.dto.common;

import com.gepardec.wor.lord.util.LSTUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class Accumulator {
    Map<String, Wsdl2JavaService> wsdl2JavaServices = new HashMap<>();

    public void addService(String packageName, Wsdl2JavaService wsdl2JavaService) {
        this.wsdl2JavaServices.put(packageName, wsdl2JavaService);
    }


    public Optional<Wsdl2JavaService> getService(String packageName) {
        if (wsdl2JavaServices.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(wsdl2JavaServices.get(packageName));
    }

    public Collection<Wsdl2JavaService> getServices() {
        return wsdl2JavaServices.values();
    }


    public List<String> getIOTypesShort() {
        return wsdl2JavaServices.values().stream()
                .flatMap(Wsdl2JavaService::getIOTypesStream)
                .map(LSTUtil::shortNameOfFullyQualified)
                .collect(Collectors.toList());
    }

    public List<String> getIOTypes() {
        return wsdl2JavaServices.values().stream()
                .flatMap(Wsdl2JavaService::getIOTypesStream)
                .collect(Collectors.toList());
    }

    public static Accumulator of(String serviceAlias, List<String> types, String packageName) {
        Accumulator accumulator = new Accumulator();
        accumulator.addService(packageName, new Wsdl2JavaService(serviceAlias, types, List.of()));
        return accumulator;
    }

    public @NotNull Optional<String> lookupWsdlTypeFromBinary(String type) {
        if(aIOTypeContains(type).isPresent()) {
            return Optional.empty();
        }

        String binaryTypeWithoutDtoQualifiers = type.toString()
                .replaceAll("Dto", "");
        String shortWsdlType = LSTUtil.shortNameOfFullyQualified(binaryTypeWithoutDtoQualifiers);

        return aIOTypeContains(shortWsdlType);
    }

    private @NotNull Optional<String> aIOTypeContains(String shortWsdlType) {
        return getIOTypes().stream()
                .filter(wsdlType -> wsdlType.contains(shortWsdlType))
                .findFirst();
    }

}
