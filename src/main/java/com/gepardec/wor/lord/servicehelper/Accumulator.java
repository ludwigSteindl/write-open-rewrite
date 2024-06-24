package com.gepardec.wor.lord.servicehelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Accumulator {
    private List<String> servicesPackages = new ArrayList<>();

    public List<String> getServicePackages() {
        return List.copyOf(servicesPackages);
    }

    public void addServicePackage(String servicePackage) {
        servicesPackages.add(servicePackage);
    }
}
