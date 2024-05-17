package com.gepardec.wor.lord.dto.common;

import java.util.Optional;

public class Accessor {
    private String name;
    private String clazz;
    private Accessor parent;

    public Accessor(String name, String clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public Accessor(String name, String clazz, Accessor parent) {
        this(name, clazz);
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public String getClazz() {
        return clazz;
    }

    public Optional<Accessor> getParent() {
        return Optional.ofNullable(parent);
    }

    public void setParent(Accessor parent) {
        this.parent = parent;
    }
}
