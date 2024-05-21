/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gepardec.wor;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.ChangeType;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ChangeTypeTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ChangeType("java.util.ArrayList", "java.util.LinkedList", false))
          .parser(JavaParser.fromJavaVersion());
    }

    @Test
    void changeType() {
        rewriteRun(
          //language=java
          java(
                  "import java.util.ArrayList;\n" +
                  "import java.util.List;\n" +
                  "\n" +
                  "class MyTest {\n" +
                  "  void testFoo() {\n" +
                  "      ArrayList<Integer> values = new java.util.ArrayList<>(List.of(1,2,3));\n" +
                  "      values.forEach(v->System.out.println(v));\n" +
                  "  }\n" +
                  "}\n",
                  "import java.util.LinkedList;\n" +
                  "import java.util.List;\n" +
                  "\n" +
                  "class MyTest {\n" +
                  "  void testFoo() {\n" +
                  "      LinkedList<Integer> values = new java.util.LinkedList<>(List.of(1,2,3));\n" +
                  "      values.forEach(v->System.out.println(v));\n" +
                  "  }\n" +
                  "}\n"
          )
        );
    }

    @Test
    void changeTypeDefinition() {
        rewriteRun(
          //language=java
          java(
                  "package java.util;\n" +
                  "\n" +
                  "class ArrayList {\n" +
                  "  void doSomething() {}\n" +
                  "}\n",
                  "package java.util;\n" +
                  "\n" +
                  "class LinkedList {\n" +
                  "  void doSomething() {}\n" +
                  "}\n"
          )
        );
    }
}
