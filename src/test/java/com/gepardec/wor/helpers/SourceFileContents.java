package com.gepardec.wor.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SourceFileContents {
    private static final String PATH = "target/generated-sources/cxf/";

    private String serviceAlias;

    public String[] forWsdl2JavaService(String serviceAlias) {
        this.serviceAlias = serviceAlias;
        return readTreeUnderFile(new File(PATH), this::isNotWsdl2JavaService).toArray(new String[0]);
    }

    public List<String> readTreeUnderFile(File file, Predicate<File> filter) {
        if(!filter.test(file)) {
            return List.of();
        }
        if (file.isDirectory()) {
            return Arrays.stream(file.listFiles())
              .map(fileUnderDir -> this.readTreeUnderFile(fileUnderDir, filter))
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
        }
        return List.of(readFile(file));
    }

    private boolean isNotWsdl2JavaService(File file) {
        return !file
          .getName()
          .toLowerCase()
          .contains(serviceAlias);
    }

    private  String readFile(File file) {
        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            return reader.lines()
              .reduce("", (a, b) -> a + b + System.lineSeparator());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
