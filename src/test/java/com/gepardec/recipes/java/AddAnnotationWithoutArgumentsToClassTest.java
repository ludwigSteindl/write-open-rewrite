package com.gepardec.recipes.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class AddAnnotationWithoutArgumentsToClassTest implements RewriteTest {
    @Test
    void test() {
        rewriteRun(
          recipeSpec -> recipeSpec.recipe(new AddAnnotationWithoutArgumentsToClass("com.gepardec.Class", "org.junit.jupiter.api.Disabled", false)),
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
