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
package com.gepardec.wor.lord;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openrewrite.java.Assertions.java;

public class BinaryProxyToWebTest implements RewriteTest {

    private static final Logger LOG = LoggerFactory.getLogger(BinaryProxyToWebTest.class);

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new BinaryProxyToWeb()).typeValidationOptions(TypeValidation.none());
    }

    @DocumentExample
    @Test
    public void whenCallInInitialization_thenAddWebCall() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      AuMhHostInfoResponseDto ret = callSvcProxy(req);
                      return ret;
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """,
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      final boolean useWeb = true;
                      if (useWeb) {
                          AuMhHostInfoResponseDto ret = callSvcWeb(req);
                      } else {
                          AuMhHostInfoResponseDto ret = callSvcProxy(req);
                      }
                      return ret;
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """
          )
        );
    }
    @DocumentExample
    @Test
    public void whenCallInAssignment_thenAddWebCall() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      AuMhHostInfoResponseDto ret;
                      
                      ret = callSvcProxy(req);
                      return ret;
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """,
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      final boolean useWeb = true;
                      AuMhHostInfoResponseDto ret;
                      
                      if (useWeb) {
                          ret = callSvcWeb(req);
                      } else {
                          ret = callSvcProxy(req);
                      }
                      return ret;
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """
          )
        );
    }

    @DocumentExample
    @Test
    public void whenCallsInAssignmentsWithDifferentVariableNames_thenAddWebForDifferentVariables() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      AuMhHostInfoResponseDto ret;
                      ret = callSvcProxy(req);
                      AuMhHostInfoResponseDto ret2;
                      ret2 = callSvcProxy(req);
                      return ret;
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """,
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      final boolean useWeb = true;
                      AuMhHostInfoResponseDto ret;
                      if (useWeb) {
                          ret = callSvcWeb(req);
                      } else {
                          ret = callSvcProxy(req);
                      }
                      AuMhHostInfoResponseDto ret2;
                      if (useWeb) {
                          ret2 = callSvcWeb(req);
                      } else {
                          ret2 = callSvcProxy(req);
                      }
                      return ret;
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """
          )
        );
    }


    @DocumentExample
    @Test
    public void whenCallsWithDifferentVariableNames_thenAddWebForDifferentVariables() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      AuMhHostInfoResponseDto ret = callSvcProxy(req);
                      AuMhHostInfoResponseDto ret2 = callSvcProxy(req);
                      return ret;
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """,
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      final boolean useWeb = true;
                      if (useWeb) {
                          AuMhHostInfoResponseDto ret = callSvcWeb(req);
                      } else {
                          AuMhHostInfoResponseDto ret = callSvcProxy(req);
                      }
                      if (useWeb) {
                          AuMhHostInfoResponseDto ret2 = callSvcWeb(req);
                      } else {
                          AuMhHostInfoResponseDto ret2 = callSvcProxy(req);
                      }
                      return ret;
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """
          )
        );
    }
    @DocumentExample
    @Test
    public void whenCallInReturnStatement_thenAddWebCall() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      return callSvcProxy(req);
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """,
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      final boolean useWeb = true;
                      if (useWeb) {
                          return callSvcWeb(req);
                      } else {
                          return callSvcProxy(req);
                      }
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """
          )
        );
    }
    @Test
    public void whenCallMultipleTimes_thenOnlyOneBooleanManyIfs() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      AuMhHostInfoResponseDto ret = callSvcProxy(req);
                      callSvcProxy(req);
                      return callSvcProxy(req);
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """,
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      final boolean useWeb = true;
                      if (useWeb) {
                          AuMhHostInfoResponseDto ret = callSvcWeb(req);
                      } else {
                          AuMhHostInfoResponseDto ret = callSvcProxy(req);
                      }
                      if (useWeb) {
                          callSvcWeb(req);
                      } else {
                          callSvcProxy(req);
                      }
                      if (useWeb) {
                          return callSvcWeb(req);
                      } else {
                          return callSvcProxy(req);
                      }
                  }
                  
                  AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """
          )
        );
    }

    @Test
    public void whenNoProxyCall_doNothing() {
        rewriteRun(
          //language=java
          java(
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      AuMhHostInfoResponseDto ret = callSvcWeb(req);
                      return ret;
                  }
                  
                  AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}
              }
              
              public class AuMhHostInfoResponseDto {
                  public Integer getCallStatus() {
                      return null;
                  }
              }
              
              public class AuMhHostInfoRequestDto {}
              """
          )
        );
    }
}
