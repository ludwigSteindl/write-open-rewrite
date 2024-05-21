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
package com.gepardec.wor.lord.call.ifs;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openrewrite.java.Assertions.java;

public class BinaryProxyToWebTest implements RewriteTest {

    private static final Logger LOG = LoggerFactory.getLogger(BinaryProxyToWebTest.class);

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new BinaryProxyToWeb())
            .parser(JavaParser.fromJavaVersion().classpath(JavaParser.runtimeClasspath()));
    }

    @DocumentExample
    @Test
    public void whenCallInInitialization_thenAddWebCall() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoResponseDto ret = callSvcProxy(req);\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        final boolean useWeb = true;\n" +
                  "        if (useWeb) {\n" +
                  "            AuMhHostInfoResponseDto ret = callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            AuMhHostInfoResponseDto ret = callSvcProxy(req);\n" +
                  "        }\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n"
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
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoResponseDto ret;\n" +
                  "\n" +
                  "        ret = callSvcProxy(req);\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        final boolean useWeb = true;\n" +
                  "        AuMhHostInfoResponseDto ret;\n" +
                  "\n" +
                  "        if (useWeb) {\n" +
                  "            ret = callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            ret = callSvcProxy(req);\n" +
                  "        }\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n"
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
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoResponseDto ret;\n" +
                  "        ret = callSvcProxy(req);\n" +
                  "        AuMhHostInfoResponseDto ret2;\n" +
                  "        ret2 = callSvcProxy(req);\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        final boolean useWeb = true;\n" +
                  "        AuMhHostInfoResponseDto ret;\n" +
                  "        if (useWeb) {\n" +
                  "            ret = callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            ret = callSvcProxy(req);\n" +
                  "        }\n" +
                  "        AuMhHostInfoResponseDto ret2;\n" +
                  "        if (useWeb) {\n" +
                  "            ret2 = callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            ret2 = callSvcProxy(req);\n" +
                  "        }\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n"
          )
        );
    }
    @DocumentExample
    @Test
    public void whenCallsWithDifferentParameterNames_thenAddWebForDifferentParameterNames() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoRequestDto req2 = null;\n" +
                  "        AuMhHostInfoResponseDto ret;\n" +
                  "        ret = callSvcProxy(req);\n" +
                  "        AuMhHostInfoResponseDto ret2;\n" +
                  "        ret2 = callSvcProxy(req2);\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        final boolean useWeb = true;\n" +
                  "        AuMhHostInfoRequestDto req2 = null;\n" +
                  "        AuMhHostInfoResponseDto ret;\n" +
                  "        if (useWeb) {\n" +
                  "            ret = callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            ret = callSvcProxy(req);\n" +
                  "        }\n" +
                  "        AuMhHostInfoResponseDto ret2;\n" +
                  "        if (useWeb) {\n" +
                  "            ret2 = callSvcWeb(req2);\n" +
                  "        } else {\n" +
                  "            ret2 = callSvcProxy(req2);\n" +
                  "        }\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n"
          )
        );
    }
    @DocumentExample
    @Test
    public void whenDeclarationsWithDifferentParameterNames_thenAddWebForDifferentParameterNames() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoRequestDto req2 = null;\n" +
                  "        AuMhHostInfoResponseDto ret = callSvcProxy(req);\n" +
                  "        AuMhHostInfoResponseDto ret2 = callSvcProxy(req2);\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        final boolean useWeb = true;\n" +
                  "        AuMhHostInfoRequestDto req2 = null;\n" +
                  "        if (useWeb) {\n" +
                  "            AuMhHostInfoResponseDto ret = callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            AuMhHostInfoResponseDto ret = callSvcProxy(req);\n" +
                  "        }\n" +
                  "        if (useWeb) {\n" +
                  "            AuMhHostInfoResponseDto ret2 = callSvcWeb(req2);\n" +
                  "        } else {\n" +
                  "            AuMhHostInfoResponseDto ret2 = callSvcProxy(req2);\n" +
                  "        }\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n"
          )
        );
    }
    @DocumentExample
    @Test
    public void whenInvocationsWithDifferentParameterNames_thenAddWebForDifferentParameterNames() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoRequestDto req2 = null;\n" +
                  "        callSvcProxy(req);\n" +
                  "        callSvcProxy(req2);\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        final boolean useWeb = true;\n" +
                  "        AuMhHostInfoRequestDto req2 = null;\n" +
                  "        if (useWeb) {\n" +
                  "            callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            callSvcProxy(req);\n" +
                  "        }\n" +
                  "        if (useWeb) {\n" +
                  "            callSvcWeb(req2);\n" +
                  "        } else {\n" +
                  "            callSvcProxy(req2);\n" +
                  "        }\n" +
                  "        return null;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n"
          )
        );
    }
    @DocumentExample
    @Test
    public void whenReturnWithDifferentParameterNames_thenAddWebForDifferentParameterNames() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto asd) {\n" +
                  "        return callSvcProxy(asd);\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto asd) {\n" +
                  "        final boolean useWeb = true;\n" +
                  "        if (useWeb) {\n" +
                  "            return callSvcWeb(asd);\n" +
                  "        } else {\n" +
                  "            return callSvcProxy(asd);\n" +
                  "        }\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n"
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
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoResponseDto ret = callSvcProxy(req);\n" +
                  "        AuMhHostInfoResponseDto ret2 = callSvcProxy(req);\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        final boolean useWeb = true;\n" +
                  "        if (useWeb) {\n" +
                  "            AuMhHostInfoResponseDto ret = callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            AuMhHostInfoResponseDto ret = callSvcProxy(req);\n" +
                  "        }\n" +
                  "        if (useWeb) {\n" +
                  "            AuMhHostInfoResponseDto ret2 = callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            AuMhHostInfoResponseDto ret2 = callSvcProxy(req);\n" +
                  "        }\n" +
                  "        return ret;\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n"
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
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
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
                  "public class AuMhHostInfoRequestDto {}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        final boolean useWeb = true;\n" +
                  "        if (useWeb) {\n" +
                  "            return callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            return callSvcProxy(req);\n" +
                  "        }\n" +
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
                  "public class AuMhHostInfoRequestDto {}\n"
          )
        );
    }
    @Test
    public void whenCallMultipleTimes_thenOnlyOneBooleanManyIfs() {
        LOG.info("Start Test");
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        AuMhHostInfoResponseDto ret = callSvcProxy(req);\n" +
                  "        callSvcProxy(req);\n" +
                  "        return callSvcProxy(req);\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n",
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "    public AuMhHostInfoResponseDto getAuMhHostInfo(AuMhHostInfoRequestDto req) {\n" +
                  "        final boolean useWeb = true;\n" +
                  "        if (useWeb) {\n" +
                  "            AuMhHostInfoResponseDto ret = callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            AuMhHostInfoResponseDto ret = callSvcProxy(req);\n" +
                  "        }\n" +
                  "        if (useWeb) {\n" +
                  "            callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            callSvcProxy(req);\n" +
                  "        }\n" +
                  "        if (useWeb) {\n" +
                  "            return callSvcWeb(req);\n" +
                  "        } else {\n" +
                  "            return callSvcProxy(req);\n" +
                  "        }\n" +
                  "    }\n" +
                  "\n" +
                  "    AuMhHostInfoResponseDto callSvcProxy(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "    AuMhHostInfoResponseDto callSvcWeb(AuMhHostInfoRequestDto req) {return null;}\n" +
                  "}\n"
          )
        );
    }

    @Test
    public void whenNoProxyCall_doNothing() {
        rewriteRun(
          //language=java
          java(
                  "package com.gepardec.wor.lord;\n" +
                  "\n" +
                  "import com.gepardec.wor.lord.stubs.*;\n" +
                  "\n" +
                  "public class Test {\n" +
                  "  private AuMhHostInfoResponseDto response;\n" +
                  "}\n")
        );
    }
}
