package com.gepardec.wor.lord.webservicehelper;

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
}

