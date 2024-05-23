/*
 * Copyright 2023 the original author or authors.
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
package com.gepardec.wor.xml;

import com.gepardec.wor.lord.util.Parsers;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.config.Environment;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.xml.Assertions.xml;

class JavaxOrmXmlToJakartaOrmXmlTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .parser(Parsers.createParserWithRuntimeClasspath())
          .recipe(Environment.builder()
            .scanRuntimeClasspath("com.gepardec.wor")
            .build()
            .activateRecipes("org.openrewrite.java.migrate.jakarta.JavaxOrmXmlToJakartaOrmXml"));
    }

    @DocumentExample
    @Test
    void testOrmXml() {
        rewriteRun(
          spec -> spec.expectedCyclesThatMakeChanges(2),
          //language=xml
          xml(
                  "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                  "<entity-mappings xmlns=\"http://java.sun.com/xml/ns/persistence/orm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                  "    xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence/orm http://java.sun.com/xml/ns/persistence/orm_2_0.xsd\"\n" +
                  "    version=\"2.0\">\n" +
                  "    <description>SV Batch Framework JPA Entities</description>\n" +
                  "\n" +
                  "    <!-- SV-Batch Entity Mapping -->\n" +
                  "    <entity class=\"com.gepardec.JobDefinition\" />\n" +
                  "    <entity class=\"com.gepardec.JobType\" />\n" +
                  "\n" +
                  "</entity-mappings>\n",
                  "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                  "<entity-mappings xmlns=\"https://jakarta.ee/xml/ns/persistence/orm\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                  "    xsi:schemaLocation=\"https://jakarta.ee/xml/ns/persistence/orm https://jakarta.ee/xml/ns/persistence/orm/orm_3_0.xsd\"\n" +
                  "    version=\"3.0\">\n" +
                  "    <description>SV Batch Framework JPA Entities</description>\n" +
                  "\n" +
                  "    <!-- SV-Batch Entity Mapping -->\n" +
                  "    <entity class=\"com.gepardec.JobDefinition\" />\n" +
                  "    <entity class=\"com.gepardec.JobType\" />\n" +
                  "\n" +
                  "</entity-mappings>\n",
            sourceSpecs -> sourceSpecs.path("myapp-orm.xml")
          )
        );
    }
}
