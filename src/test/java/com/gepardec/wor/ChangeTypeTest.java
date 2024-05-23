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
                  """
                  import java.util.ArrayList;
                  import java.util.List;
                  
                  class MyTest {
                    void testFoo() {
                        ArrayList<Integer> values = new java.util.ArrayList<>(List.of(1,2,3));
                        values.forEach(v->System.out.println(v));
                    }
                  }
                  """,
                  """
                  import java.util.LinkedList;
                  import java.util.List;
                  
                  class MyTest {
                    void testFoo() {
                        LinkedList<Integer> values = new java.util.LinkedList<>(List.of(1,2,3));
                        values.forEach(v->System.out.println(v));
                    }
                  }
                  """
          )
        );
    }

    @Test
    void changeTypeDefinition() {
        rewriteRun(
          //language=java
          java(
                  """
                  package java.util;
                  
                  class ArrayList {
                    void doSomething() {}
                  }
                  """,
                  """
                  package java.util;
                  
                  class LinkedList {
                    void doSomething() {}
                  }
                  """
          )
        );
    }
}
