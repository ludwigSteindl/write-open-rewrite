package com.gepardec.wor.helpers;

import org.openrewrite.SourceFile;
import org.openrewrite.java.Assertions;
import org.openrewrite.test.SourceSpec;
import org.openrewrite.test.SourceSpecs;
import org.openrewrite.text.PlainText;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SourceFileContents {
    private static final String PATH = "target/generated-sources/cxf/";

    private String serviceAlias;

    public SourceSpecs[] forWsdl2JavaService(SourceSpecs... additionalSources) {
        this.serviceAlias = "";
        List<SourceSpecs> wsdlSources = new ArrayList<>(readTreeUnderFile(new File(PATH), Objects::nonNull))
          .stream()
          .map(Assertions::java)
          .collect(Collectors.toList());
        wsdlSources.addAll(List.of(additionalSources));
        return wsdlSources.toArray(new SourceSpecs[0]);
    }


    public SourceSpecs[] forWsdl2JavaService(String serviceAlias, SourceSpecs... additionalSources) {
        this.serviceAlias = serviceAlias;
        List<SourceSpecs> wsdlSources = new ArrayList<>(readTreeUnderFile(new File(PATH), this::isNotWsdl2JavaService))
          .stream()
          .map(Assertions::java)
          .collect(Collectors.toList());
        wsdlSources.addAll(List.of(additionalSources));
        return wsdlSources.toArray(new SourceSpecs[0]);
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
        String fileMatcher = file
          .getName()
          .toLowerCase();

        return !fileMatcher.contains("a02/") || fileMatcher.contains(serviceAlias);
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
