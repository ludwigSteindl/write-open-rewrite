package com.gepardec.wor.lord.dto.visitors.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectFactoryCreations {

    private static ObjectFactoryCreations instance;

    private final Map<String, List<String>> createdObjectFactories = new HashMap<>();

    public static ObjectFactoryCreations getInstance() {
        if (instance == null) {
            instance = new ObjectFactoryCreations();
        }
        return instance;
    }

    private ObjectFactoryCreations() {

    }

    public void addObjectFactoryCreation(String className, String objectFactoryType) {
        if (!createdObjectFactories.containsKey(className)) {
            createdObjectFactories.put(className, new ArrayList<>());
        }
        createdObjectFactories.get(className).add(objectFactoryType);
    }

    public List<String> getObjectFactoryCreations(String className) {
        if (!createdObjectFactories.containsKey(className)) {
            return List.of();
        }
        return createdObjectFactories.get(className);
    }

}
