package com.gepardec.wor.lord.webservicehelper;

import com.gepardec.wor.lord.util.LSTUtil;

import java.util.ArrayList;
import java.util.List;

public class Accumulator {
    private List<String> servicesPackages = new ArrayList<>();

    public List<String> getServicePackages() {
        return List.copyOf(servicesPackages);
    }

    public void addServicePackage(String servicePackage) {
        servicesPackages.add(servicePackage);
    }

    public List<String> getServiceClasses() {
        return getServicePackages().stream()
                .map(Accumulator::getServiceClass)
                .toList();
    }

    public static String getServiceClass(String servicePackage) {
        String lastSubPackage = LSTUtil.shortNameOfFullyQualified(servicePackage);
        return lastSubPackage.substring(0, 1).toUpperCase() + lastSubPackage.substring(1);
    }
}

