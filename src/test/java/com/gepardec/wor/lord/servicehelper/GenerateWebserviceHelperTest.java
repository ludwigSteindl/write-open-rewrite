package com.gepardec.wor.lord.servicehelper;

import com.gepardec.wor.lord.call.ternaries.BinaryProxyToWebTernaryAndClassTest;
import com.gepardec.wor.lord.dto.BinaryDtoToWeb;
import com.gepardec.wor.lord.util.ParserUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openrewrite.java.Assertions.java;

public class GenerateWebserviceHelperTest implements RewriteTest {

    private static final Logger LOG = LoggerFactory.getLogger(BinaryProxyToWebTernaryAndClassTest.class);

    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .recipe(new BinaryDtoToWeb())
          .parser(ParserUtil.createParserWithRuntimeClasspath())
          .typeValidationOptions(TypeValidation.none());
    }

    @Disabled
    @DocumentExample
    @Test
    public void whenBinaryStdhSet_thenCreateWebStdh() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java("""
              package com.gepardec.wor.lord;
              
              public class Test {
                  public void test() {
                      int i = 0;
                  }
              }
              """,
            """
            package com.gepardec.wor.lord;
            
            import at.sozvers.stp.lgkk.a02.laaamhsu.Laqamhsu;
            import at.sozvers.stp.lgkk.a02.laaamhsu.ObjectFactory;
            
            public class Test {
                private static final ObjectFactory objectFactory = new ObjectFactory();
                public void test() {
                    Laqamhsu reqDto = new Laqamhsu();
                    reqDto.setStdh(objectFactory.createOmStandardRequestHeader());
                    reqDto.getStdh().setZvst("11");
                }
            }
            """)
        );
    }
}
