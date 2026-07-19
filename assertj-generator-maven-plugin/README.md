Maven plugin to generate AssertJ assertions 
==

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.assertj/assertj-assertions-generator-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.assertj/assertj-assertions-generator-maven-plugin)

## Overview 

This plugin can generate [AssertJ assertions](https://github.com/joel-costigliola/assertj-core) for your own classes via maven (it is based on [assertj-assertions-generator](https://github.com/joel-costigliola/assertj-assertions-generator)).

Let's say that you have a `Player` class with `name` and `team` attributes, the plugin is able to create a `PlayerAssert` assertions class with `hasName` and `hasTeam` assertions, to write code like :

```java
assertThat(mvp).hasName("Lebron James").hasTeam("Miami Heat");
```

The plugin can be launched with command `mvn generate-test-sources` (or simply `mvn test`) or with any IDE that supports maven.
By default, it generates the assertions source files in `target/generated-test-sources/assertions` as per maven convention (but this can be changed - see below).


**Example of plugin execution:**

```
====================================
AssertJ assertions generation report
====================================

--- Generator input parameters ---

Generating AssertJ assertions for classes in following packages and subpackages:
- org.assertj.examples.data

--- Generator results ---

Directory where custom assertions files have been generated :
- /home/joe/assertj/assertj-examples/target/generated-test-sources/assertj-assertions

Custom assertions files generated :
- TeamAssert.java
- BasketBallPlayerAssert.java
- EmployeeAssert.java
- NameAssert.java
- MagicalAssert.java
- PersonAssert.java
- RaceAssert.java
- GameServiceAssert.java
- MansionAssert.java
- TitleAssert.java
- AlignmentAssert.java
- TolkienCharacterAssert.java
- RingAssert.java
- MovieAssert.java
- TeamManagerAssert.java

Assertions entry point class has been generated in file:
- /home/joe/assertj/assertj-examples/target/generated-test-sources/assertj-assertions/org/assertj/examples/data/Assertions.java
```

## Documentation

Please have a look at the complete documentation in [**assertj.org assertions generator section**](http://joel-costigliola.github.io/assertj/assertj-assertions-generator-maven-plugin.html), including a [**quickstart guide**](http://joel-costigliola.github.io/assertj/assertj-assertions-generator-maven-plugin.html#quickstart).
