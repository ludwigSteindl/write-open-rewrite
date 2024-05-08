package com.gepardec.wor.lord.stdh;

import com.gepardec.wor.lord.call.ternaries.BinaryProxyToWebTernaryAndClassTest;
import com.gepardec.wor.lord.stdh.BinaryStdhToWeb;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openrewrite.java.Assertions.java;

public class BinaryStdhToWebStdhTest implements RewriteTest {

    private static final Logger LOG = LoggerFactory.getLogger(BinaryProxyToWebTernaryAndClassTest.class);

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
    @Disabled("Not yet implemented")
    public void whenBinaryFullStdhSet_thenCreateWebFullStdh() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java("""
          public class Test {
              public void test() {
                  LaqamhsuDto reqDto = new LaqamhsuDto();
                  reqDto.setZvst("11");
                  reqDto.setVers("001");
                  reqDto.setDebuglevel(9.0);
                  reqDto.setTransactionid(Sequencer.next());
                  reqDto.setIdapp("LGKK");
                  reqDto.setBearbgrund("LGKK");
                  reqDto.setTraegerid(String.valueOf(req.getZustaendigertraeger()));
                  reqDto.setOrgeinheitid("0000000000");
                  reqDto.setSystemmodus(ElgkkPropertiesUtil.getSystemmodus());
                  reqDto.setVerarbeitungsmodus("O");
                  reqDto.setBerechtpruefung("0");
                  reqDto.setQbid("");
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
    public void whenBinaryStdhSetWithOtherStatements_thenCreateWebStdhAndAddToDtoAfterLastSetter() {
        rewriteRun(
          //language=java
          java("""
          public class Test {
              public void test() {
                  System.out.println("Hello");
                  LaqamhsuDto reqDto2 = new LaqamhsuDto();
                  int i = 0;
                  reqDto2.setZvst("14");
                  System.out.println("Sending request to you: " + reqDto2);
              }
          }
          """,
          """
          public class Test {
              public void test() {
                  ObjectFactory objectFactory = new ObjectFactory();
                  OmStandardRequestHeader stdh = objectFactory.createOmStandardRequestHeader();
                  System.out.println("Hello");
                  Laqamhsu reqDto2 = new Laqamhsu();
                  int i = 0;
                  stdh.setZvst("14");
                  reqDto2.setOmStandardRequestHeader(stdh);
                  System.out.println("Sending request to you: " + reqDto2);
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
