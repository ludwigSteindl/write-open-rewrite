package com.gepardec.wor.lord.stdh.v2;

import com.gepardec.wor.helpers.SourceFileContents;
import com.gepardec.wor.lord.call.ternaries.BinaryProxyToWebTernaryAndClassTest;
import com.gepardec.wor.lord.stdh.v2.recipes.BinaryDtoToWeb;
import com.gepardec.wor.lord.stdh.v2.recipes.BinaryDtoToWsdl2JavaServiceDto;
import com.gepardec.wor.lord.util.ParserUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.LargeSourceSet;
import org.openrewrite.RecipeRun;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import static org.openrewrite.java.Assertions.java;

public class BinaryDtoToWebTest implements RewriteTest {

    private static final Logger LOG = LoggerFactory.getLogger(BinaryProxyToWebTernaryAndClassTest.class);

    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .recipe(new BinaryDtoToWeb())
          .parser(ParserUtil.createParserWithRuntimeClasspath())
          .typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    public void whenBinaryStdhSet_thenCreateWebStdh() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.LaqamhsuDto;
          import com.gepardec.wor.lord.stubs.Laqamhsu;
          import com.gepardec.wor.lord.stubs.ObjectFactory;
          import com.gepardec.wor.lord.stubs.OmStandardRequestHeader;
          
          public class Test {
              public void test() {
                  LaqamhsuDto reqDto = new LaqamhsuDto();
                  reqDto.setZvst("11");
              }
          }
          """,
            """
            package com.gepardec.wor.lord;
            
            import com.gepardec.wor.lord.stubs.LaqamhsuDto;
            import com.gepardec.wor.lord.stubs.Laqamhsu;
            import com.gepardec.wor.lord.stubs.ObjectFactory;
            import com.gepardec.wor.lord.stubs.OmStandardRequestHeader;
            
            public class Test {
                private static final ObjectFactory objectFactory = new ObjectFactory();
                public void test() {
                    Laqamhsu reqDto = new Laqamhsu();
                    reqDto.setOmStandardRequestHeader(objectFactory.createOmStandardRequestHeader());
                    reqDto.getOmStandardRequestHeader().setZvst("11");
                }
            }
            """)
        );
    }
    @DocumentExample
    @Test
    public void whenNoBinaryStdhSetButDto_thenCreateWebDtoWithoutStdh() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.LaqamhsuDto;
          import com.gepardec.wor.lord.stubs.Laqamhsu;
          import com.gepardec.wor.lord.stubs.ObjectFactory;
          import com.gepardec.wor.lord.stubs.OmStandardRequestHeader;
          
          public class Test {
              public void test() {
                  LaqamhsuDto reqDto = new LaqamhsuDto();
                  reqDto.setDatenv3("blubb");
              }
          }
          """,
            """
            package com.gepardec.wor.lord;
            
            import com.gepardec.wor.lord.stubs.LaqamhsuDto;
            import com.gepardec.wor.lord.stubs.Laqamhsu;
            import com.gepardec.wor.lord.stubs.ObjectFactory;
            import com.gepardec.wor.lord.stubs.OmStandardRequestHeader;
            
            public class Test {
                public void test() {
                    Laqamhsu reqDto = new Laqamhsu();
                    reqDto.setDatenv3("blubb");
                }
            }
            """)
        );
    }
    @DocumentExample
    @Test
    public void whenNoDtoUsed_thenDoNothing() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.LaqamhsuDto;
          import com.gepardec.wor.lord.stubs.Laqamhsu;
          import com.gepardec.wor.lord.stubs.ObjectFactory;
          import com.gepardec.wor.lord.stubs.OmStandardRequestHeader;
          
          public class Test {
              public void test() {
                    int i = 0;
              }
          }
          """)
        );
    }

    @Test
    public void withWsdl() {
        Stream<SourceFile> sourceFiles = JavaParser.fromJavaVersion().build().parse(new SourceFileContents().forWsdl2JavaService("laaamhsu"));
        LargeSourceSet sourceSet = new InMemoryLargeSourceSet(sourceFiles.toList());
        RecipeRun testResults = new BinaryDtoToWsdl2JavaServiceDto().run(sourceSet, new InMemoryExecutionContext());
    }
}

