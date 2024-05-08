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
package com.gepardec.wor.lord.call.ternaries;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openrewrite.java.Assertions.java;

public class BinaryProxyToWebTernaryAndClassTest implements RewriteTest {

    private static final Logger LOG = LoggerFactory.getLogger(BinaryProxyToWebTernaryAndClassTest.class);

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new BinaryProxyToWebTernaryAndClass()).typeValidationOptions(TypeValidation.none());
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
              
              public class ElgkkPropertiesUtil {
                  public static final String getElgkkProperties(String key) {
                      return null;
                  }
              }
              """,
            """
              package com.gepardec.wor.lord;
              
              public class Test {
                  public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {
                      AuMhHostInfoResponseDto ret = ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(req) : callSvcProxy(req);
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
              
              public class ElgkkPropertiesUtil {
                  public static final String getElgkkProperties(String key) {
                      return null;
                  }
                  
                  public static boolean isUseWeb() {
                      return true;
                  }
              }
              """
          )
        );
    }
}
