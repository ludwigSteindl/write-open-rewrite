package com.gepardec.wor;

import com.gepardec.wor.lord.wsannotation.AddAnnotationWithoutArgumentsToClass;
import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.ListRuntimeClasspath;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class AddAnnotationWithoutArgumentsToClassTest implements RewriteTest {
    @Test
    void test() {
        rewriteRun(
          recipeSpec -> recipeSpec.recipe(new AddAnnotationWithoutArgumentsToClass("Class", "com.gepardec", "org.junit.jupiter.api.Disabled", false)),
          java(
            """
              package com.gepardec;
              
              public class Class {}
              """, """
              package com.gepardec;
              
              import org.junit.jupiter.api.Disabled;
              
              @Disabled
              public class Class {}
              """)
        );
    }
}
