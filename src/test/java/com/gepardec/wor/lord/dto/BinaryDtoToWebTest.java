package com.gepardec.wor.lord.dto;

import com.gepardec.wor.helpers.SourceFileContents;
import com.gepardec.wor.lord.call.ternaries.BinaryProxyToWebTernaryAndClassTest;
import com.gepardec.wor.lord.util.ParserUtil;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpecs;
import org.openrewrite.test.TypeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        rewriteRunWithWsdlClasses(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.LaqamhsuDto;
          
          public class Test {
              public void test() {
                  LaqamhsuDto reqDto = new LaqamhsuDto();
                  reqDto.setZvst("11");
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

    @DocumentExample
    @Test
    public void whenBinaryStdhSetWithOtherNamesAndTyoe_thenCreateWebStdh() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.Laqaumv4Dto;
          
          public class Test {
              public void test() {
                  Laqaumv4Dto request = new Laqaumv4Dto();
                  request.setZvst("11");
              }
          }
          """,
            """
            package com.gepardec.wor.lord;
            
            import at.sozvers.stp.lgkk.a02.laaaumv4.Laqaumv4;
            import at.sozvers.stp.lgkk.a02.laaaumv4.ObjectFactory;
            
            public class Test {
                private static final ObjectFactory objectFactory = new ObjectFactory();
                public void test() {
                    Laqaumv4 request = new Laqaumv4();
                    request.setStdh(objectFactory.createOmStandardRequestHeader());
                    request.getStdh().setZvst("11");
                }
            }
            """)
        );
    }
    @DocumentExample
    @Test
    public void whenBinaryStdhSetWith2TimesSameType_thenCreateWebStdh() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.Laqaumv4Dto;
          
          public class Test {
              public void test() {
                  Laqaumv4Dto request = new Laqaumv4Dto();
                  request.setZvst("11");
                  request.setTransactionid(1L);
              }
          }
          """,
            """
            package com.gepardec.wor.lord;
            
            import at.sozvers.stp.lgkk.a02.laaaumv4.Laqaumv4;
            import at.sozvers.stp.lgkk.a02.laaaumv4.ObjectFactory;
            
            public class Test {
                private static final ObjectFactory objectFactory = new ObjectFactory();
                public void test() {
                    Laqaumv4 request = new Laqaumv4();
                    request.setStdh(objectFactory.createOmStandardRequestHeader());
                    request.getStdh().setZvst("11");
                    request.getStdh().setTransactionid(1L);
                }
            }
            """)
        );
    }
    @DocumentExample
    @Test
    public void whenBinarySetterWithNestedWebObjectUsed_thenCreateNestedWebDto() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.Laqaumv4Dto;
          
          public class Test {
              public void test() {
                  Laqaumv4Dto request = new Laqaumv4Dto();
                  request.setPostleitzahl("1220");
              }
          }
          """,
            """
            package com.gepardec.wor.lord;
            
            import at.sozvers.stp.lgkk.a02.laaaumv4.Laqaumv4;
            import at.sozvers.stp.lgkk.a02.laaaumv4.ObjectFactory;
            
            public class Test {
                private static final ObjectFactory objectFactory = new ObjectFactory();
                public void test() {
                    Laqaumv4 request = new Laqaumv4();
                    request.setDatenv3(objectFactory.createLaqaumv4Datenv3());
                    request.getDatenv3().setPostleitzahl(objectFactory.createLaqaumv4Datenv3Postleitzahl("1220"));
                }
            }
            """)
        );
    }
    @DocumentExample
    @Test
    public void whenNestedSetters_thenTransformNestedSetters() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.Laqaumv4Dto;
          
          public class Test {
              public void test() {
                  Laqaumv4Dto request = new Laqaumv4Dto();
                  request.setPostleitzahl("1220");
                  request.setCReserved("test");
                  request.setZvst("11");
              }
          }
          """,
            """
            package com.gepardec.wor.lord;
            
            import at.sozvers.stp.lgkk.a02.laaaumv4.Laqaumv4;
            import at.sozvers.stp.lgkk.a02.laaaumv4.ObjectFactory;
            
            public class Test {
                private static final ObjectFactory objectFactory = new ObjectFactory();
                public void test() {
                    Laqaumv4 request = new Laqaumv4();
                    request.setStdh(objectFactory.createOmStandardRequestHeader());
                    request.setMxcb(objectFactory.createMxcb());
                    request.setDatenv3(objectFactory.createLaqaumv4Datenv3());
                    request.getDatenv3().setPostleitzahl(objectFactory.createLaqaumv4Datenv3Postleitzahl("1220"));
                    request.getMxcb().setCReserved(objectFactory.createMxcbCReserved("test"));
                    request.getStdh().setZvst("11");
                }
            }
            """)
        );
    }
    @DocumentExample
    @Test
    public void whenBinaryStdhSetWithObjectFactoryAlreadyThere_thenCreateWebStdh() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.LaqamhsuDto;
          import at.sozvers.stp.lgkk.a02.laaamhsu.ObjectFactory;
          
          public class Test {
              private static final ObjectFactory objectFactory = new ObjectFactory();
              public void test() {
                  LaqamhsuDto reqDto = new LaqamhsuDto();
                  reqDto.setZvst("11");
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
    @DocumentExample
    @Test
    public void whenNoBinaryStdhSetButDto_thenCreateWebDtoWithoutStdh() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.LaqamhsuDto;

          public class Test {
              public void test() {
                  LaqamhsuDto reqDto = new LaqamhsuDto();
                  reqDto.toString();
              }
          }
          """)
        );
    }
    @DocumentExample
    @Test
    public void whenNoWsdlService_thenDoNothing() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          import com.gepardec.wor.lord.stubs.LaqaumwtDto;

          public class Test {
              public void test() {
                  LaqaumwtDto reqDto = new LaqaumwtDto();
                  reqDto.toString();
              }
          }
          """)
        );
    }

    @DocumentExample
    @Test
    public void whenTypeUsedByWsdlServiceButNoDto_thenDoNothing() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          public class Test {
              public void test() {
                  String reqDto = new String();
                  reqDto.getClass();
              }
          }
          """)
        );
    }
    @DocumentExample
    @Test
    public void whenNoDtoUsed_thenDoNothing() {
        LOG.info("Start Test");
        rewriteRunWithWsdlClasses(
          //language=java
          java("""
          package com.gepardec.wor.lord;
          
          public class Test {
              public void test() {
                    int i = 0;
              }
          }
          """)
        );
    }

    private void rewriteRunWithWsdlClasses(SourceSpecs... sourceSpecs) {
        rewriteRun(
            new SourceFileContents().forWsdl2JavaService(sourceSpecs)
        );
    }
}

