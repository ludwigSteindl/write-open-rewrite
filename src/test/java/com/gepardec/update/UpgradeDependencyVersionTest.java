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
package com.gepardec.update;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.dependencies.UpgradeDependencyVersion;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.maven.Assertions.pomXml;

class UpgradeDependencyVersionTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UpgradeDependencyVersion("log4j", "log4j", "1.2.17", null,
          true, null));
    }

    @Test
    void upgradeLog4JVersion() {
        rewriteRun(
          pomXml(
            """         
              <project>
                <groupId>com.gepardec.update</groupId>
                <artifactId>update-log4j-version</artifactId>
                <version>1</version>
                <dependencies>
                    <dependency>
              			<groupId>log4j</groupId>
              			<artifactId>log4j</artifactId>
              			<version>1.2.14</version>
              			<scope>provided</scope>
              		</dependency>
                </dependencies>
            </project>
            """,
            """
              <project>
                <groupId>com.gepardec.update</groupId>
                <artifactId>update-log4j-version</artifactId>
                <version>1</version>
                <dependencies>
                    <dependency>
              			<groupId>log4j</groupId>
              			<artifactId>log4j</artifactId>
              			<version>1.2.17</version>
              			<scope>provided</scope>
              		</dependency>
                </dependencies>
            </project>
            """
          )
        );
    }

    @Test
    void keepLog4JVersion() {
        rewriteRun(
          pomXml(
            """         
              <project>
                <groupId>com.gepardec.update</groupId>
                <artifactId>update-log4j-version</artifactId>
                <version>1</version>
                <dependencies>
                    <dependency>
              			<groupId>log4j</groupId>
              			<artifactId>log4j</artifactId>
              			<version>1.2.17</version>
              			<scope>provided</scope>
              		</dependency>
                </dependencies>
            </project>
            """
          )
        );
    }
}
