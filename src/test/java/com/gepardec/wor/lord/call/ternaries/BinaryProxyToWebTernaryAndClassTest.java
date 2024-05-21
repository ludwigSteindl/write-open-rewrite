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

import org.junit.jupiter.api.Disabled;
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
    public void whenCall_thenAddWebCall() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoResponseDto ret = callSvcProxy(req);\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoResponseDto ret = ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(req) : callSvcProxy(req);\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    public static boolean isUseWeb() {\n" +
                  "        return true;\n" +
                  "    }\n" +
                  "}\n"
          )
        );
    }
    @DocumentExample
    @Test
    public void whenNoCall_thenDoNothing() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n")
        );
    }
    @DocumentExample
    @Test
    public void whenMultipleCallsDifferentTypes_thenChangeToTernaryAndAddToConfigClassOnce() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoResponseDto ret = callSvcProxy(req);\n" +
                  "        ret = callSvcProxy(req);\n" +
                  "        return callSvcProxy(req);\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoResponseDto ret = ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(req) : callSvcProxy(req);\n" +
                  "        ret = ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(req) : callSvcProxy(req);\n" +
                  "        return ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(req) : callSvcProxy(req);\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    public static boolean isUseWeb() {\n" +
                  "        return true;\n" +
                  "    }\n" +
                  "}\n"
          )
        );
    }
    @DocumentExample
    @Test
    public void whenCallWithDifferentVariableNames_thenChangeToTernaryAndAddToConfigClassOnce() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto requestDto) {\n" +
                  "        AuMhHostInfoResponseDto returnDto = callSvcProxy(requestDto);\n" +
                  "        return returnDto;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto requestDto) {\n" +
                  "        AuMhHostInfoResponseDto returnDto = ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(requestDto) : callSvcProxy(requestDto);\n" +
                  "        return returnDto;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    public static boolean isUseWeb() {\n" +
                  "        return true;\n" +
                  "    }\n" +
                  "}\n"
          )
        );
    }
    @DocumentExample
    @Test
    public void whenCallInOtherCall_thenChangeToTernaryAndAddToConfigClassOnce() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto requestDto) {\n" +
                  "        System.out.print(callSvcProxy(requestDto));\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto requestDto) {\n" +
                  "        System.out.print(ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(requestDto) : callSvcProxy(requestDto));\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    public static boolean isUseWeb() {\n" +
                  "        return true;\n" +
                  "    }\n" +
                  "}\n"
          )
        );
    }

    @DocumentExample
    @Test
    @Disabled("Fails due to a bug: Ternary without assignment is not a valid statement")
    public void whenCallWithoutTargetVariable_thenAddWebCallWithSemicolons() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        req == null ? 1 : 0;\n" +
                  "        callSvcProxy(req);\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoResponseDto unused = ElgkkPropertiesUtil.isUseWeb() ? callSvcWeb(req) : callSvcProxy(req);\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoResponseDto {\n" +
                  "    public Integer getCallStatus() {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "}\n" +
                  "\n" +
                  "public class AuMhHostInfoRequestDto {}\n" +
                  "\n" +
                  "public class ElgkkPropertiesUtil {\n" +
                  "    public static final String getElgkkProperties(String key) {\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    public static boolean isUseWeb() {\n" +
                  "        return true;\n" +
                  "    }\n" +
                  "}\n"
          )
        );
    }

}
