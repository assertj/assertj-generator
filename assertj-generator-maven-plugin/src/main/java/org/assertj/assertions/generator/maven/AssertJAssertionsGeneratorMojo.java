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

import static com.google.common.base.Charsets.UTF_8;
import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.write;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_TEST_SOURCES;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;
import static org.assertj.assertions.generator.AssertionsEntryPointType.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.assertj.core.util.VisibleForTesting;
import org.codehaus.plexus.util.FileUtils;

/**
 * Generates custom AssertJ assertions (*Assert) for all given classes and classes of given packages.
 */
@Mojo(name = "generate-assertions", defaultPhase = GENERATE_TEST_SOURCES, requiresDependencyResolution = TEST, requiresProject = true)
public class AssertJAssertionsGeneratorMojo extends AbstractMojo {

  private static final String[] INCLUDE_ALL_CLASSES = { ".*" };

  private static final NoLog NO_LOG = new NoLog();
  /**
   * Current maven project
   */
  @Parameter(property = "project", required = true, readonly = true)
  public MavenProject project;

  /**
   * Destination dir to store generated assertion source files. <br>
   * Defaults to 'target/generated-test-sources/assertj-assertions'.<br>
   * Your IDE should be able to pick up files from this location as sources automatically when generated.
   */
  @Parameter(defaultValue = "${project.build.directory}/generated-test-sources/assertj-assertions", property = "assertj.targetDir")
  public String targetDir;

  /**
   * Package where generated assertion classes will reside.
   * <p/>
   * If not set (or set to empty), each assertion class is generated in the package of the corresponding class to assert.
   * For example the generated assertion class for com.nba.Player will be com.nba.PlayerAssert (in the same package as Player).
   * Defaults to ''.<br>
   * <p/>
   * Note that the Assertions entry point classes package is controlled by the entryPointClassPackage property.
   */
  @Parameter(defaultValue = "", property = "assertj.generateAssertionsInPackage")
  public String generateAssertionsInPackage;

  /**
   * Flag specifying whether to clean the directory where assertions are generated. The default is false.
   */
  @Parameter(defaultValue = "false", property = "assertj.cleanTargetDir")
  public boolean cleanTargetDir;

  /**
   * The scope of generates sources ('test' or 'compile') to be added to the maven build. <br>
   * Expected to be used in conjunction with {@link #targetDir}, for example:
   * <pre>{@code
   *  <targetDir>${project.build.directory}/generated-sources/assertj-assertions</targetDir>
   *  <generatedSourcesScope>compile</generatedSourcesScope>
   * }</pre>
   * Defaults to 'test'.<br>
   */
  @Parameter(defaultValue = "test", property = "assertj.generatedSourcesScope")
  public String generatedSourcesScope;

  /**
   * List of packages to generate assertions for.
   */
  @Parameter(property = "assertj.packages")
  public String[] packages;

  /**
   * List of classes to generate assertions for.
   */
  @Parameter(property = "assertj.classes")
  public String[] classes;

  /**
   * Generated assertions are limited to classes matching one of the given regular expressions, default is to include
   * all classes.
   */
  @Parameter(property = "assertj.includes")
  public String[] includes = INCLUDE_ALL_CLASSES;

  /**
   * If class matches one of the given regex, no assertions will be generated for it, default is not to exclude
   * anything.
   */
  @Parameter(property = "assertj.excludes")
  public String[] excludes = new String[0];

  /**
   * Flag specifying whether to generate hierarchical assertions. The default is false.
   */
  @Parameter(defaultValue = "true", property = "assertj.hierarchical")
  public boolean hierarchical;

  /**
   * Flag specifying whether to generate assertions for all fields (including non public ones). The default is false.
   */
  @Parameter(defaultValue = "false", property = "assertj.generateAssertionsForAllFields")
  public boolean generateAssertionsForAllFields;

  /**
   * An optional package name for the Assertions entry point class. If omitted, the package will be determined
   * heuristically from the generated assertions.
   */
  @Parameter(property = "assertj.entryPointClassPackage")
  public String entryPointClassPackage;

  /**
   * Skip generating classes, handy way to disable the plugin.
   */
  @Parameter(property = "assertj.skip")
  public boolean skip = false;

  /**
   * Generate Assertions entry point class.
   */
  @Parameter(property = "assertj.generate.Assertions")
  public boolean generateAssertions = true;

  /**
   * Generate generating BDD Assertions entry point class.
   */
  @Parameter(property = "assertj.generate.BddAssertions")
  public boolean generateBddAssertions = true;

  /**
   * Generate generating JUnit Soft Assertions entry point class.
   */
  @Parameter(property = "assertj.generate.JUnitSoftAssertions")
  public boolean generateJUnitSoftAssertions = true;

  /**
   * Generate generating Soft Assertions entry point class.
   */
  @Parameter(property = "assertj.generate.SoftAssertions")
  public boolean generateSoftAssertions = true;

  /**
   * Do not log anything if true, false by default.
   */
  @Parameter(property = "assertj.quiet")
  public boolean quiet = false;

  /**
   * The generated assertions report is written to the given file, if given a relative path the root path is where the plugin is executed.
   */
  @Parameter(property = "assertj.writeReportInFile")
  public String writeReportInFile;

  /**
   * Generate generating Soft Assertions entry point class.
   */
  @Parameter(property = "assertj.templates")
  public Templates templates;

  /**
   * Generate assertions for package private classes if true
   */
  @Parameter(property = "assertj.includePackagePrivateClasses")
  public boolean includePackagePrivateClasses = false;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (skip) {
      getLog().info("Assertions generator is disabled as 'skip' option is true.");
      return;
    }
    failIfMojoParametersAreMissing();
    try {
      ClassLoader projectClassLoader = getProjectClassLoader();
      AssertionsGenerator assertionGenerator = new AssertionsGenerator(projectClassLoader);
      assertionGenerator.generateAssertionsForAllFields(this.generateAssertionsForAllFields);
      assertionGenerator.setIncludePatterns(includes);
      assertionGenerator.setExcludePatterns(excludes);
      if (generateAssertions) assertionGenerator.enableEntryPointClassesGenerationFor(STANDARD);
      if (generateBddAssertions) assertionGenerator.enableEntryPointClassesGenerationFor(BDD);
      if (generateSoftAssertions) assertionGenerator.enableEntryPointClassesGenerationFor(SOFT);
      if (generateJUnitSoftAssertions) {
        if (junitFoundBy(projectClassLoader)) assertionGenerator.enableEntryPointClassesGenerationFor(JUNIT_SOFT);
        else
          getLog().info("JUnit not found in project classpath => JUnitSoftAssertions entry point class won't be generated.");
      }
      assertionGenerator.setLog(getLog());
      if (generateAssertionsInPackage != null) {
        // user has set generateAssertionsInPackage  (not that maven converts empty string param to null)
        assertionGenerator.setGeneratedAssertionsPackage(generateAssertionsInPackage);
      }
      if (cleanTargetDir) cleanPreviouslyGeneratedSources();
      executeWithAssertionGenerator(assertionGenerator);
    } catch (Exception e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }

  @Override
  public Log getLog() {
    return quiet ? NO_LOG : super.getLog();
  }

  private void cleanPreviouslyGeneratedSources() {
    try {
      Path targetDirPath = Paths.get(targetDir);
      if (Files.exists(targetDirPath) && targetDirPath.toFile().list().length > 0) {
        getLog().info("Removing previously generated sources in " + targetDir);
        FileUtils.cleanDirectory(targetDirPath.toFile());
      }
    } catch (IOException e) {
      getLog().warn("Fail to remove previously generated sources in " + targetDir, e);
    }
  }

  @VisibleForTesting
  AssertionsGeneratorReport executeWithAssertionGenerator(AssertionsGenerator assertionGenerator) {
    if (classes == null) classes = new String[0];
    AssertionsGeneratorReport generatorReport = assertionGenerator.generateAssertionsFor(packages, classes, targetDir,
                                                                                         entryPointClassPackage, hierarchical,
                                                                                         templates, includePackagePrivateClasses);
    printReport(generatorReport);
    if (isEmpty(generatedSourcesScope) || equalsIgnoreCase("test", generatedSourcesScope)) project.addTestCompileSourceRoot(targetDir);
    else if (equalsIgnoreCase("compile", generatedSourcesScope)) project.addCompileSourceRoot(targetDir);
    else getLog().warn(format("Unknown generated sources scope '%s' - no sources added to project", generatedSourcesScope));
    return generatorReport;
  }

  private void printReport(AssertionsGeneratorReport assertionsGeneratorReport) {
    String reportContent = assertionsGeneratorReport.getReportContent();
    if (shouldWriteReportInFile()) {
      getLog().info("Writing the assertions generator report in file: " + writeReportInFile);
      writeReportInFile(reportContent);
    } else {
      getLog().info(reportContent);
    }
  }

  private void writeReportInFile(String reportContent) {
    try {
      write(new File(writeReportInFile), reportContent, UTF_8);
    } catch (IOException e) {
      getLog().warn("Failed to write the assertions generation assertionsGeneratorReport in file "
                    + writeReportInFile, e);
    }
  }

  private boolean shouldWriteReportInFile() {
    return writeReportInFile != null;
  }

  private void failIfMojoParametersAreMissing() throws MojoFailureException {
    if (isEmpty(packages) && isEmpty(classes)) {
      throw new MojoFailureException(shouldHaveNonEmptyPackagesOrClasses());
    }
  }

  @SuppressWarnings("unchecked")
  private ClassLoader getProjectClassLoader() throws DependencyResolutionRequiredException, MalformedURLException {
    List<String> classpathElements = new ArrayList<String>(project.getCompileClasspathElements());
    classpathElements.addAll(project.getTestClasspathElements());
    List<URL> classpathElementUrls = new ArrayList<>(classpathElements.size());
    for (String classpathElement : classpathElements) {
      classpathElementUrls.add(new File(classpathElement).toURI().toURL());
    }
    return new URLClassLoader(classpathElementUrls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader());
  }

  @VisibleForTesting
  static String shouldHaveNonEmptyPackagesOrClasses() {
    return format(
        "Parameter 'packages' or 'classes' must be set to generate assertions.%n[Help] https://github.com/joel-costigliola/assertj-assertions-generator-maven-plugin");
  }

  private boolean junitFoundBy(ClassLoader projectClassLoader) {
    try {
      Class.forName("org.junit.Rule", false, projectClassLoader);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

}
