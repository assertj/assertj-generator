/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2012-2025 the original author or authors.
 */
package org.assertj.assertions.generator.maven;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.assertj.assertions.generator.Template;
import org.junit.Before;
import org.junit.Test;

public class TemplatesTest {

  private AssertionsGeneratorReport report;
  private Templates templates;

  @Before
  public void setup() {
    templates = new Templates();
    report = new AssertionsGeneratorReport();
  }

  @Test
  public void should_load_user_template() {
    // GIVEN
    List<Template> list = new ArrayList<>();
    String templateFilename = "my_has_assertion_template.txt";
    templates.templatesDirectory = "target/test-classes/templates/";
    // WHEN
    templates.loadUserTemplate(templateFilename, Template.Type.HAS, "my has template", list, report);
    // THEN
    assertThat(list).hasSize(1);
    assertThat(list.get(0).getContent()).isNotEmpty();
    assertThat(report.getUserTemplates()).containsOnly("Using custom template for my has template loaded from target/test-classes/templates/my_has_assertion_template.txt");
  }

  @Test
  public void should_log_loading_failure_and_move_one() {
    // GIVEN
    List<Template> list = new ArrayList<>();
    templates.templatesDirectory = "target/test-classes/templates/";
    // WHEN
    templates.loadUserTemplate("unknown", Template.Type.HAS, "my has template", list, report);
    // THEN
    assertThat(list).isEmpty();
    assertThat(report.getUserTemplates()).containsOnly("Use default my has template assertion template as we failed to read user template from target/test-classes/templates/unknown");
  }
}
