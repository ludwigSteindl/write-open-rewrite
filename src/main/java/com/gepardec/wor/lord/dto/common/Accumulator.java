package com.gepardec.wor.lord.dto.common;

import com.gepardec.wor.lord.util.LSTUtil;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
                .map(LSTUtil::shortNameOfFullyQualified)
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
