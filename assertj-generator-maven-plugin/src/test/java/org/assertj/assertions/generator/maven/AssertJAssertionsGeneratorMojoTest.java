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
import static org.assertj.core.api.Assertions.contentOf;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.util.Arrays.array;
import static org.assertj.core.util.Files.newFile;
import static org.assertj.core.util.Lists.newArrayList;
import static org.assertj.assertions.generator.maven.AssertJAssertionsGeneratorMojo.shouldHaveNonEmptyPackagesOrClasses;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.assertj.assertions.generator.BaseAssertionGenerator;
import org.assertj.assertions.generator.description.ClassDescription;
import org.assertj.assertions.generator.maven.test.All;
import org.assertj.assertions.generator.maven.test.Employee;
import org.assertj.assertions.generator.maven.test.Player;
import org.assertj.assertions.generator.maven.test.name.Name;
import org.assertj.assertions.generator.maven.test.name.NameService;
import org.assertj.assertions.generator.maven.test2.adress.Address;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AssertJAssertionsGeneratorMojoTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private AssertJAssertionsGeneratorMojo assertjAssertionsGeneratorMojo;
  private MavenProject mavenProject;

  @Before
  public void setUp() throws Exception {
    mavenProject = mock(MavenProject.class);
    assertjAssertionsGeneratorMojo = new AssertJAssertionsGeneratorMojo();
    assertjAssertionsGeneratorMojo.project = mavenProject;
    assertjAssertionsGeneratorMojo.targetDir = temporaryFolder.getRoot().getAbsolutePath();
  }

  @Test
  public void executing_plugin_with_classes_and_packages_parameter_only_should_pass() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test", "org.assertj.assertions.generator.maven.test2");
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee");
    List<String> classes = newArrayList(Employee.class.getName(), Address.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsFileFor(Address.class)).exists();
    assertThat(assertionsEntryPointFile("Assertions.java")).exists();
    assertThat(assertionsEntryPointFile("BddAssertions.java")).exists();
    assertThat(assertionsEntryPointFile("SoftAssertions.java")).exists();
  }

  @Test
  public void executing_plugin_with_hierarchical_option_should_generate_hierarchical_assertions() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test", "org.assertj.assertions.generator.maven.test2");
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee");
    assertjAssertionsGeneratorMojo.hierarchical = true;
    List<String> classes = newArrayList(Employee.class.getName(), Address.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(abstractAssertionsFileFor(Employee.class)).exists();
    assertThat(assertionsFileFor(Address.class)).exists();
    assertThat(abstractAssertionsFileFor(Address.class)).exists();
    assertThat(assertionsEntryPointFile("Assertions.java")).exists();
    assertThat(assertionsEntryPointFile("BddAssertions.java")).exists();
    assertThat(assertionsEntryPointFile("SoftAssertions.java")).exists();
  }

  @Test
  public void should_generate_assertions_with_user_templates() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.All");
    assertjAssertionsGeneratorMojo.templates = new Templates();
    assertjAssertionsGeneratorMojo.templates.templatesDirectory = "src/test/resources/templates/";
    assertjAssertionsGeneratorMojo.templates.objectAssertion = "my_has_assertion_template.txt";
    assertjAssertionsGeneratorMojo.templates.bddEntryPointAssertionMethod = "my_bdd_assertion_entry_point_method_template.txt";
    assertjAssertionsGeneratorMojo.templates.bddEntryPointAssertionClass = "my_bdd_assertions_entry_point_class_template.txt";
    assertjAssertionsGeneratorMojo.templates.hierarchicalAssertionAbstractClass = "my_custom_abstract_assertion_class_template.txt";
    assertjAssertionsGeneratorMojo.templates.assertionClass = "my_custom_assertion_class_template.txt";
    assertjAssertionsGeneratorMojo.templates.hierarchicalAssertionConcreteClass = "my_custom_hierarchical_assertion_class_template.txt";
    assertjAssertionsGeneratorMojo.templates.characterAssertion = "my_has_assertion_template_for_character.txt";
    assertjAssertionsGeneratorMojo.templates.charAssertion = "my_has_assertion_template_for_char.txt";
    assertjAssertionsGeneratorMojo.templates.realNumberAssertion = "my_has_assertion_template_for_real_number.txt";
    assertjAssertionsGeneratorMojo.templates.realNumberWrapperAssertion = "my_has_assertion_template_for_real_number_wrapper.txt";
    assertjAssertionsGeneratorMojo.templates.wholeNumberAssertion = "my_has_assertion_template_for_whole_number.txt";
    assertjAssertionsGeneratorMojo.templates.wholeNumberWrapperAssertion = "my_has_assertion_template_for_whole_number_wrapper.txt";
    assertjAssertionsGeneratorMojo.templates.arrayAssertion = "my_has_elements_assertion_template_for_array.txt";
    assertjAssertionsGeneratorMojo.templates.iterableAssertion = "my_has_elements_assertion_template_for_iterable.txt";
    assertjAssertionsGeneratorMojo.templates.booleanAssertion = "my_is_assertion_template.txt";
    assertjAssertionsGeneratorMojo.templates.booleanWrapperAssertion = "my_is_wrapper_assertion_template.txt";
    assertjAssertionsGeneratorMojo.templates.junitSoftEntryPointAssertionClass = "my_junit_soft_assertions_entry_point_class_template.txt";
    assertjAssertionsGeneratorMojo.templates.softEntryPointAssertionMethod = "my_soft_assertion_entry_point_method_template.txt";
    assertjAssertionsGeneratorMojo.templates.softEntryPointAssertionClass = "my_soft_assertions_entry_point_class_template.txt";
    assertjAssertionsGeneratorMojo.templates.assertionEntryPointMethod = "my_standard_assertion_entry_point_method_template.txt";
    assertjAssertionsGeneratorMojo.templates.assertionsEntryPointClass = "my_standard_assertions_entry_point_class_template.txt";
    List<String> classes = newArrayList(All.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    // WHEN
    assertjAssertionsGeneratorMojo.execute();

    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    File allAssertFile = assertionsFileFor(All.class);
    assertThat(allAssertFile).exists();
    // check that its content was done using custom templates
    assertThat(contentOf(allAssertFile)).contains("my_custom_assertion_class_template",
                                                  "my_has_assertion_template_for_character",
                                                  "my_has_assertion_template_for_char",
                                                  "my_has_assertion_template_for_real_number",
                                                  "my_has_assertion_template_for_real_number_wrapper",
                                                  "my_has_assertion_template_for_whole_number",
                                                  "my_has_assertion_template_for_whole_number_wrapper",
                                                  "my_has_assertion_template",
                                                  "my_has_elements_assertion_template_for_array",
                                                  "my_has_elements_assertion_template_for_iterable",
                                                  "my_is_assertion_template",
                                                  "my_is_wrapper_assertion_template");

    assertThat(contentOf(assertionsEntryPointFile("Assertions.java"))).contains("my_standard_assertion_entry_point_method_template",
                                                                                "my_standard_assertions_entry_point_class_template");
    assertThat(contentOf(assertionsEntryPointFile("BddAssertions.java"))).contains("my_bdd_assertion_entry_point_method_template",
                                                                                   "my_bdd_assertions_entry_point_class_template");
    assertThat(contentOf(assertionsEntryPointFile("SoftAssertions.java"))).contains("my_soft_assertion_entry_point_method_template",
                                                                                    "my_soft_assertions_entry_point_class_template");
    assertThat(contentOf(assertionsEntryPointFile("JUnitSoftAssertions.java"))).contains("my_junit_soft_assertions_entry_point_class_template");
  }

  @Test
  public void should_not_generate_assertions_for_assert_classes() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.MyAssert");
    assertjAssertionsGeneratorMojo.packages = array("some.package");
    assertjAssertionsGeneratorMojo.hierarchical = true;
    assertjAssertionsGeneratorMojo.execute();
    assertThat(assertionsFileFor("org.assertj.assertions.generator.maven.test.MyAssertAssert")).doesNotExist();
  }

  @Test
  public void should_not_generate_assertions_for_assertions_classes() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.MyAssertions");
    assertjAssertionsGeneratorMojo.packages = array("some.package");
    assertjAssertionsGeneratorMojo.execute();
    assertThat(assertionsFileFor("org.assertj.assertions.generator.maven.test.MyAssertionsAssert")).doesNotExist();
  }

  @Test
  public void executing_plugin_with_classes_parameter_only_should_pass() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee",
                                                   "org.assertj.assertions.generator.maven.test2.adress.Address");
    List<String> classes = newArrayList(Address.class.getName());
    assertjAssertionsGeneratorMojo.hierarchical = true;
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsFileFor(Address.class)).exists();
    assertThat(assertionsEntryPointFile("Assertions.java")).exists();
    assertThat(assertionsEntryPointFile("BddAssertions.java")).exists();
    assertThat(assertionsEntryPointFile("SoftAssertions.java")).exists();
  }

  @Test
  public void executing_plugin_with_custom_entry_point_class_package_should_pass() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee");
    assertjAssertionsGeneratorMojo.entryPointClassPackage = "my.custom.pkg";
    List<String> classes = newArrayList(Employee.class.getName(), Address.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsEntryPointFileWithCustomPackage()).exists();
  }

  @Test
  public void should_not_generate_entry_point_classes_if_disabled() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee");
    List<String> classes = newArrayList(Employee.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);
    assertjAssertionsGeneratorMojo.generateAssertions = false;
    assertjAssertionsGeneratorMojo.generateBddAssertions = false;
    assertjAssertionsGeneratorMojo.generateSoftAssertions = false;
    assertjAssertionsGeneratorMojo.generateJUnitSoftAssertions = false;

    assertjAssertionsGeneratorMojo.execute();

    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsEntryPointFile("Assertions.java")).doesNotExist();
    assertThat(assertionsEntryPointFile("BddAssertions.java")).doesNotExist();
    assertThat(assertionsEntryPointFile("SoftAssertions.java")).doesNotExist();
    assertThat(assertionsEntryPointFile("JUniSoftAssertions.java")).doesNotExist();
  }

  @Test
  public void executing_plugin_with_fake_package_should_not_generate_anything() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("fakepackage");
    List<String> classes = newArrayList();
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(temporaryFolder.getRoot().list()).isEmpty();
  }

  @Test
  public void executing_plugin_with_skip_set_to_true_should_not_generate_anything() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test.Employee");
    assertjAssertionsGeneratorMojo.skip = true;
    List<String> classes = newArrayList();
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(temporaryFolder.getRoot().list()).isEmpty();
  }

  @Test
  public void plugin_should_only_generate_assertions_for_included_classes() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test");
    assertjAssertionsGeneratorMojo.includes = array(".*Name");
    List<String> classes = newArrayList(Employee.class.getName(), Name.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(assertionsFileFor(Name.class)).exists();
    assertThat(assertionsFileFor(NameService.class)).doesNotExist();
    assertThat(assertionsFileFor(Employee.class)).doesNotExist();
  }

  @Test
  public void plugin_should_generate_assertions_for_all_fields() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Player");
    assertjAssertionsGeneratorMojo.generateAssertionsForAllFields = true;
    when(mavenProject.getCompileClasspathElements()).thenReturn(newArrayList(Player.class.getName()));

    // WHEN
    assertjAssertionsGeneratorMojo.execute();

    // THEN
    File playerAssertFile = assertionsFileFor(Player.class);
    assertThat(playerAssertFile).exists();
    assertThat(contentOf(playerAssertFile)).contains("hasSalary", "hasTeam", "hasName", "isRookie");
  }

  @Test
  public void plugin_should_generate_assertions_in_given_package() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Player");
    assertjAssertionsGeneratorMojo.generateAssertionsInPackage = "my.assertions";
    when(mavenProject.getCompileClasspathElements()).thenReturn(newArrayList(Player.class.getName()));

    // WHEN
    assertjAssertionsGeneratorMojo.execute();

    // THEN
    File playerAssertFile = new File(temporaryFolder.getRoot(), "my/assertions/PlayerAssert.java");
    assertThat(playerAssertFile).exists();
    assertThat(contentOf(playerAssertFile)).contains("package my.assertions");
  }

  @Test
  public void plugin_should_not_generate_assertions_for_excluded_classes() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test");
    assertjAssertionsGeneratorMojo.excludes = array(".*Employee", ".*Service");
    List<String> classes = newArrayList(Employee.class.getName(), Name.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(assertionsFileFor(Name.class)).exists();
    assertThat(assertionsFileFor(NameService.class)).doesNotExist();
    assertThat(assertionsFileFor(Employee.class)).doesNotExist();
  }

  @Test
  public void plugin_should_not_generate_any_assertions_as_all_package_classes_are_excluded() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test");
    assertjAssertionsGeneratorMojo.excludes = array(".*Employ..", ".*Name.*");
    List<String> classes = newArrayList(Employee.class.getName(), Name.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(assertionsFileFor(Name.class)).doesNotExist();
    assertThat(assertionsFileFor(NameService.class)).doesNotExist();
    assertThat(assertionsFileFor(Employee.class)).doesNotExist();
  }

  @Test
  public void plugin_should_not_generate_assertions_for_classes_matching_both_include_and_exclude_pattern() throws Exception {
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test");
    assertjAssertionsGeneratorMojo.includes = array(".*Employee", ".*Name.*");
    assertjAssertionsGeneratorMojo.excludes = array(".*Employee", ".*Service");
    List<String> classes = newArrayList(Employee.class.getName(), Name.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);

    assertjAssertionsGeneratorMojo.execute();

    assertThat(assertionsFileFor(Name.class)).exists();
    assertThat(assertionsFileFor(NameService.class)).doesNotExist();
    assertThat(assertionsFileFor(Employee.class)).doesNotExist();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void executing_plugin_with_error_should_be_reported_in_generator_report() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee");
    when(mavenProject.getCompileClasspathElements()).thenReturn(newArrayList(Employee.class.getName()));
    // let's throws an IOException when generating custom assertions
    AssertionsGenerator generator = new AssertionsGenerator(Thread.currentThread().getContextClassLoader());
    BaseAssertionGenerator baseGenerator = mock(BaseAssertionGenerator.class);
    generator.setBaseGenerator(baseGenerator);
    when(baseGenerator.generateCustomAssertionFor(any(ClassDescription.class))).thenThrow(IOException.class);
    AssertionsGeneratorReport report = assertjAssertionsGeneratorMojo.executeWithAssertionGenerator(generator);

    assertThat(report.getReportedException()).isInstanceOf(IOException.class);
    assertThat(temporaryFolder.getRoot().list()).isEmpty();
  }

  @Test
  public void input_classes_not_found_should_be_listed_in_generator_report() throws Exception {
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee", "org.Foo", "org.Bar");
    when(mavenProject.getCompileClasspathElements()).thenReturn(newArrayList(Employee.class.getName()));
    AssertionsGenerator generator = new AssertionsGenerator(Thread.currentThread().getContextClassLoader());

    AssertionsGeneratorReport report = assertjAssertionsGeneratorMojo.executeWithAssertionGenerator(generator);

    // check that expected assertions file exist (we don't check the content we suppose the generator works).
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(report.getInputClassesNotFound()).as("check report").containsExactly("org.Bar", "org.Foo");
  }

  @Test
  public void should_fail_if_packages_and_classes_parameters_are_null() throws Exception {
    try {
      assertjAssertionsGeneratorMojo.execute();
      failBecauseExceptionWasNotThrown(MojoFailureException.class);
    } catch (MojoFailureException e) {
      assertThat(e).hasMessage(shouldHaveNonEmptyPackagesOrClasses());
    }
  }

  @Test
  public void should_clean_previously_generated_assertions_before_generating_new_ones() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test.Employee");
    assertjAssertionsGeneratorMojo.cleanTargetDir = true;
    File shouldBeDeleted = newFile(assertjAssertionsGeneratorMojo.targetDir + "/should-be-deleted");
    assertThat(shouldBeDeleted).exists();
    // WHEN
    assertjAssertionsGeneratorMojo.execute();
    // THEN
    assertThat(shouldBeDeleted).doesNotExist();
  }

  @Test
  public void should_not_clean_previously_generated_assertions_by_default() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test.Employee");
    File shouldStillExist = newFile(assertjAssertionsGeneratorMojo.targetDir + "/should-still-exist");
    assertThat(shouldStillExist).exists();
    // WHEN
    assertjAssertionsGeneratorMojo.execute();
    // THEN
    assertThat(shouldStillExist).exists();
  }

  @Test
  public void should_not_log_anything() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test.Employee");
    // WHEN
    assertjAssertionsGeneratorMojo.quiet = true;
    assertjAssertionsGeneratorMojo.execute();
    // THEN
    // no logs should be produced
  }

  @Test
  public void should_write_report_to_file() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.packages = array("org.assertj.assertions.generator.maven.test.Employee");
    String reportFilename = "target/reportFilename";
    assertjAssertionsGeneratorMojo.writeReportInFile = reportFilename;
    // WHEN
    assertjAssertionsGeneratorMojo.execute();
    // THEN
    File reportFile = new File(reportFilename);
    assertThat(reportFile).exists();
    assertThat(contentOf(reportFile)).contains("EmployeeAssert");
  }

  @Test
  public void executing_plugin_with_default_target_scope_should_pass() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee");
    assertjAssertionsGeneratorMojo.entryPointClassPackage = "my.custom.pkg";
    List<String> classes = newArrayList(Employee.class.getName(), Address.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);
    // WHEN
    assertjAssertionsGeneratorMojo.execute();
    // THEN
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsEntryPointFileWithCustomPackage()).exists();
    verify(mavenProject).addTestCompileSourceRoot(assertjAssertionsGeneratorMojo.targetDir);
  }

  @Test
  public void executing_plugin_with_test_target_scope_should_pass() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee");
    assertjAssertionsGeneratorMojo.entryPointClassPackage = "my.custom.pkg";
    assertjAssertionsGeneratorMojo.generatedSourcesScope = "test";
    List<String> classes = newArrayList(Employee.class.getName(), Address.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);
    // WHEN
    assertjAssertionsGeneratorMojo.execute();
    // THEN
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsEntryPointFileWithCustomPackage()).exists();
    verify(mavenProject).addTestCompileSourceRoot(assertjAssertionsGeneratorMojo.targetDir);
  }

  @Test
  public void executing_plugin_with_compile_target_scope_should_pass() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee");
    assertjAssertionsGeneratorMojo.entryPointClassPackage = "my.custom.pkg";
    assertjAssertionsGeneratorMojo.generatedSourcesScope = "compile";
    List<String> classes = newArrayList(Employee.class.getName(), Address.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);
    // WHEN
    assertjAssertionsGeneratorMojo.execute();
    // THEN
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsEntryPointFileWithCustomPackage()).exists();
    verify(mavenProject).addCompileSourceRoot(assertjAssertionsGeneratorMojo.targetDir);
  }

  @Test
  public void executing_plugin_with_invalid_target_scope_should_pass() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.test.Employee");
    assertjAssertionsGeneratorMojo.entryPointClassPackage = "my.custom.pkg";
    assertjAssertionsGeneratorMojo.generatedSourcesScope = "invalid";
    List<String> classes = newArrayList(Employee.class.getName(), Address.class.getName());
    when(mavenProject.getCompileClasspathElements()).thenReturn(classes);
    // WHEN
    assertjAssertionsGeneratorMojo.execute();
    // THEN
    assertThat(assertionsFileFor(Employee.class)).exists();
    assertThat(assertionsEntryPointFileWithCustomPackage()).exists();
    verify(mavenProject, never()).addCompileSourceRoot(assertjAssertionsGeneratorMojo.targetDir);
    verify(mavenProject, never()).addTestCompileSourceRoot(assertjAssertionsGeneratorMojo.targetDir);
  }

  @Test
  public void plugin_should_generate_assertions_for_included_package_private_classes_when_option_enabled() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.PackagePrivate");
    assertjAssertionsGeneratorMojo.includePackagePrivateClasses = true;
    when(mavenProject.getCompileClasspathElements()).thenReturn(newArrayList(PackagePrivate.class.getName()));
    AssertionsGenerator generator = new AssertionsGenerator(Thread.currentThread().getContextClassLoader());
    // WHEN
    assertjAssertionsGeneratorMojo.executeWithAssertionGenerator(generator);
    // THEN
    assertThat(assertionsFileFor(PackagePrivate.class)).exists();
  }

  @Test
  public void plugin_should_not_generate_assertions_for_included_package_private_classes_by_default() throws Exception {
    // GIVEN
    assertjAssertionsGeneratorMojo.classes = array("org.assertj.assertions.generator.maven.PackagePrivate");
    when(mavenProject.getCompileClasspathElements()).thenReturn(newArrayList(PackagePrivate.class.getName()));
    AssertionsGenerator generator = new AssertionsGenerator(Thread.currentThread().getContextClassLoader());
    // WHEN
    assertjAssertionsGeneratorMojo.executeWithAssertionGenerator(generator);
    // THEN
    assertThat(assertionsFileFor(PackagePrivate.class)).doesNotExist();
  }

  private File assertionsFileFor(Class<?> clazz) {
    return new File(temporaryFolder.getRoot(), basePathName(clazz) + "Assert.java");
  }

  private File assertionsFileFor(String className) {
    return new File(temporaryFolder.getRoot(), className.replace('.', File.separatorChar) + ".java");
  }

  private File abstractAssertionsFileFor(Class<?> clazz) {
    return new File(temporaryFolder.getRoot(), basePathName("Abstract", clazz) + "Assert.java");
  }

  private static String basePathName(Class<?> clazz) {
    return basePathName("", clazz);
  }

  private static String basePathName(String prefix, Class<?> clazz) {
    return clazz.getPackage().getName().replace('.', File.separatorChar) + File.separator + prefix
           + clazz.getSimpleName();
  }

  private File assertionsEntryPointFile(String simpleName) {
    return new File(temporaryFolder.getRoot(), "org.assertj.assertions.generator.maven.test".replace('.', File.separatorChar)
                                               + File.separator + simpleName);
  }

  private File assertionsEntryPointFileWithCustomPackage() {
    return new File(temporaryFolder.getRoot(), "my.custom.pkg".replace('.', File.separatorChar) + File.separator
                                               + "Assertions.java");
  }
}
