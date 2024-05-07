package com.gepardec.wor.lord;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openrewrite.java.Assertions.java;

public class BinaryStdhToWebStdhTest implements RewriteTest {

    private static final Logger LOG = LoggerFactory.getLogger(BinaryProxyToWebTest.class);

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new BinaryStdhToWeb()).typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    public void whenBinaryStdhSet_thenCreateWebStdh() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java("""
          public class Test {
              public void test() {
                  LaqamhsuDto reqDto = new LaqamhsuDto();
                  reqDto.setZvst("11");
              }
          }
          """,
          """
          public class Test {
              public void test() {
                  ObjectFactory objectFactory = new ObjectFactory();
                  OmStandardRequestHeader stdh = objectFactory.createOmStandardRequestHeader();
                  Laqamhsu reqDto = new Laqamhsu();
                  stdh.setZvst("11");
                  reqDto.setOmStandardRequestHeader(stdh);
              }
          }
          """));
    }    //language=java
    @DocumentExample
    @Test
    public void whenBinaryStdhSetOtherNameAndValue_thenCreateWebStdh() {
        rewriteRun(
          //language=java
          java("""
          public class Test {
              public void test() {
                  LaqamhsuDto reqDto2 = new LaqamhsuDto();
                  reqDto2.setZvst("14");
              }
          }
          """,
          """
          public class Test {
              public void test() {
                  ObjectFactory objectFactory = new ObjectFactory();
                  OmStandardRequestHeader stdh = objectFactory.createOmStandardRequestHeader();
                  Laqamhsu reqDto2 = new Laqamhsu();
                  stdh.setZvst("14");
                  reqDto2.setOmStandardRequestHeader(stdh);
              }
          }
          """));
    }    //language=java
    @DocumentExample
    @Test
    public void whenNoDtoUsed_thenDoNothing() {
        rewriteRun(
          //language=java
          java("""
          public class Test {
              public void test() {
                  int i = 0;
                  i++;
              }
          }
          """));
    }    //language=java
}
