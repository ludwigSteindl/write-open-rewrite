package com.gepardec.wor.lord.common;

import java.util.Optional;

public class Accessor {
    private final String name;
    private final String clazz;
    private final Accessor parent;
    private final String type;

    public Accessor(Builder builder) {
        this.name = builder.name;
        this.clazz = builder.clazz;
        this.parent = builder.parent;
        this.type = builder.type;
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

    public String getType() {
        return type;
    }

    public static Builder builder(String name, String clazz) {
        return new Builder()
                .name(name)
                .clazz(clazz);
    }

    public static class Builder {
        private String name;
        private String clazz;
        private Accessor parent;
        private String type;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder clazz(String clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder parent(Accessor parent) {
            this.parent = parent;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Accessor build() {
            return new Accessor(this);
        }
    }
}
