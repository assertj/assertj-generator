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

import static org.assertj.assertions.generator.Template.Type.ABSTRACT_ASSERT_CLASS;
import static org.assertj.assertions.generator.Template.Type.ASSERTIONS_ENTRY_POINT_CLASS;
import static org.assertj.assertions.generator.Template.Type.ASSERTION_ENTRY_POINT;
import static org.assertj.assertions.generator.Template.Type.ASSERT_CLASS;
import static org.assertj.assertions.generator.Template.Type.BDD_ASSERTIONS_ENTRY_POINT_CLASS;
import static org.assertj.assertions.generator.Template.Type.BDD_ENTRY_POINT_METHOD_ASSERTION;
import static org.assertj.assertions.generator.Template.Type.HAS;
import static org.assertj.assertions.generator.Template.Type.HAS_FOR_ARRAY;
import static org.assertj.assertions.generator.Template.Type.HAS_FOR_CHAR;
import static org.assertj.assertions.generator.Template.Type.HAS_FOR_CHARACTER;
import static org.assertj.assertions.generator.Template.Type.HAS_FOR_ITERABLE;
import static org.assertj.assertions.generator.Template.Type.HAS_FOR_REAL_NUMBER;
import static org.assertj.assertions.generator.Template.Type.HAS_FOR_REAL_NUMBER_WRAPPER;
import static org.assertj.assertions.generator.Template.Type.HAS_FOR_WHOLE_NUMBER;
import static org.assertj.assertions.generator.Template.Type.HAS_FOR_WHOLE_NUMBER_WRAPPER;
import static org.assertj.assertions.generator.Template.Type.HIERARCHICAL_ASSERT_CLASS;
import static org.assertj.assertions.generator.Template.Type.IS;
import static org.assertj.assertions.generator.Template.Type.IS_WRAPPER;
import static org.assertj.assertions.generator.Template.Type.JUNIT_SOFT_ASSERTIONS_ENTRY_POINT_CLASS;
import static org.assertj.assertions.generator.Template.Type.SOFT_ASSERTIONS_ENTRY_POINT_CLASS;
import static org.assertj.assertions.generator.Template.Type.SOFT_ENTRY_POINT_METHOD_ASSERTION;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.CharEncoding;
import org.assertj.assertions.generator.Template;
import org.assertj.core.util.Files;
import org.assertj.core.util.VisibleForTesting;

public class Templates {

  public String templatesDirectory;
  // assertion class templates
  public String assertionClass;
  public String hierarchicalAssertionConcreteClass;
  public String hierarchicalAssertionAbstractClass;
  // assertion method templates
  public String objectAssertion;
  public String booleanAssertion;
  public String booleanWrapperAssertion;
  public String arrayAssertion;
  public String iterableAssertion;
  public String charAssertion;
  public String characterAssertion;
  public String realNumberAssertion;
  public String realNumberWrapperAssertion;
  public String wholeNumberAssertion;
  public String wholeNumberWrapperAssertion;
  // entry point templates
  public String assertionsEntryPointClass;
  public String assertionEntryPointMethod;
  public String softEntryPointAssertionClass;
  public String junitSoftEntryPointAssertionClass;
  public String softEntryPointAssertionMethod;
  public String bddEntryPointAssertionClass;
  public String bddEntryPointAssertionMethod;

  public List<Template> getTemplates(AssertionsGeneratorReport report) {
    // resolve user templates directory
    if (templatesDirectory == null) templatesDirectory = "./";
    if (!templatesDirectory.endsWith("/")) templatesDirectory += "/";
    // load any templates overridden by the user
    List<Template> userTemplates = new ArrayList<>();
    // @format:off
    // assertion class templates
    loadUserTemplate(assertionClass, ASSERT_CLASS, "'class assertions'", userTemplates, report);
    loadUserTemplate(hierarchicalAssertionConcreteClass, HIERARCHICAL_ASSERT_CLASS, "'hierarchical concrete class assertions'", userTemplates, report);
    loadUserTemplate(hierarchicalAssertionAbstractClass, ABSTRACT_ASSERT_CLASS, "'hierarchical abstract class assertions'", userTemplates, report);
    // assertion method templates
    loadUserTemplate(objectAssertion, HAS, "'object assertions'", userTemplates, report);
    loadUserTemplate(booleanAssertion, IS, "'boolean assertions'", userTemplates, report);
    loadUserTemplate(booleanWrapperAssertion, IS_WRAPPER, "'boolean wrapper assertions'", userTemplates, report);
    loadUserTemplate(arrayAssertion, HAS_FOR_ARRAY, "'array assertions'", userTemplates, report);
    loadUserTemplate(iterableAssertion, HAS_FOR_ITERABLE, "'iterable assertions'", userTemplates, report);
    loadUserTemplate(realNumberAssertion, HAS_FOR_REAL_NUMBER, "'real number assertions (float, double)'", userTemplates, report);
    loadUserTemplate(realNumberWrapperAssertion, HAS_FOR_REAL_NUMBER_WRAPPER, "'real number wrapper assertions (Float, Double)'", userTemplates, report);
    loadUserTemplate(wholeNumberAssertion, HAS_FOR_WHOLE_NUMBER, "'whole number assertions (int, long, short, byte)'", userTemplates, report);
    loadUserTemplate(wholeNumberWrapperAssertion, HAS_FOR_WHOLE_NUMBER_WRAPPER, "'whole number has assertions (Integer, Long, Short, Byte)'", userTemplates, report);
    loadUserTemplate(charAssertion, HAS_FOR_CHAR, "'char assertions'", userTemplates, report);
    loadUserTemplate(characterAssertion, HAS_FOR_CHARACTER, "'Character assertions'", userTemplates, report);
    // entry point templates
    loadUserTemplate(assertionsEntryPointClass,ASSERTIONS_ENTRY_POINT_CLASS, "'assertions entry point class'", userTemplates, report);
    loadUserTemplate(assertionEntryPointMethod,ASSERTION_ENTRY_POINT,  "'assertions entry point method'", userTemplates, report);
    loadUserTemplate(softEntryPointAssertionClass, SOFT_ASSERTIONS_ENTRY_POINT_CLASS, "'soft assertions entry point class'", userTemplates, report);
    loadUserTemplate(junitSoftEntryPointAssertionClass, JUNIT_SOFT_ASSERTIONS_ENTRY_POINT_CLASS, "'junit soft assertions entry point class'", userTemplates, report);
    loadUserTemplate(softEntryPointAssertionMethod, SOFT_ENTRY_POINT_METHOD_ASSERTION, "'soft assertions entry point method'", userTemplates, report);
    loadUserTemplate(bddEntryPointAssertionClass, BDD_ASSERTIONS_ENTRY_POINT_CLASS, "'BDD assertions entry point class'", userTemplates, report);
    loadUserTemplate(bddEntryPointAssertionMethod, BDD_ENTRY_POINT_METHOD_ASSERTION, "'BDD assertions entry point method'", userTemplates, report);
    // @format:on
    return userTemplates;
  }

  @VisibleForTesting
  void loadUserTemplate(String userTemplate, Template.Type type, String templateDescription,
                        List<Template> userTemplates, AssertionsGeneratorReport report) {
    if (userTemplate != null) {
      try {
        File templateFile = new File(templatesDirectory, userTemplate);
        String templateContent = Files.contentOf(templateFile, CharEncoding.UTF_8);
        userTemplates.add(new Template(type, templateContent));
        report.registerUserTemplate("Using custom template for " + templateDescription + " loaded from "
                                    + templatesDirectory + userTemplate);
      } catch (@SuppressWarnings("unused") Exception e) {
        // best effort : if we can't read user template, use the default one.
        report.registerUserTemplate("Use default " + templateDescription
                                    + " assertion template as we failed to read user template from "
                                    + templatesDirectory + userTemplate);
      }
    }
  }
}
