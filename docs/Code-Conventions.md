Over the course of its life, every project develops it's own code conventions, and Terasology is no different.
Note that we try and stick to the standard Java conventions as much as possible.

- [Indentation](#indentation)
- [Naming Conventions](#naming-conventions)
- [Starred Imports](#starred-imports)
- [Others](#others)
- [Checkstyle](#checkstyle)
- [PMD](#pmd)
- [Testing](#testing)
- [JavaDoc](#javadoc)

## Indentation

We follow the [1TBS](https://en.wikipedia.org/wiki/Indentation_style#Variant:_1TBS_.28OTBS.29) (One true brace style) exclusively. Also, every text file (be it code, or text-asset like json) should end with an empty line.

For all code files, we follow a **4-space** indentation style, and firmly believe in the saying "death to all tabs".

```java
void someFunction() {
    doSomething();
    doSomethingElse();
}
```

However, for asset files (like JSON, that is used for all prefab and config files) we follow a **2-space** indentation style.

```json
container: {
  property1: "someValue",
  property2: "someOtherValue"
}
```

Note that even for single line `if` and `while` statements, we use brackets.

```java
// Bad
if (something)
    // Do something

// Good
if (something) {
    // Do something
}
```

## Naming Conventions

Almost everything uses either camelCase, or PascalCase (same as camelCase, but with the first letter capitalized).

| Type                     | Style to follow |
| ------------------------ | --------------- |
| Package name             | camelCase       |
| Class or interface name  | PascalCase      |
| Functions and attributes | camelCase       |
| Constants                | ALL_CAPITAL     |

For more information, refer to the [official Oracle docs](http://www.oracle.com/technetwork/java/codeconventions-135099.html).

```java
package test;

class TestClass {
    private static final int SOME_CONSTANT = 0;

    int someInt;

    void doSomething() {
    }
}
```

### Descriptive Variable Names

> ℹ️  _**Almost all variables should have descriptive names**_  

A single letter variable is almost always not a descriptive name. This means that a developer should be able to isolate a proportion of the code and have a rough understanding of what each variable does. This does not require long and complicated names, but primarily means that a variable should be a complete word.

There are reasonable exceptions to this such as using `i` as a variable in a `for` loop.

## Starred Imports

> ⚠️ **_Strictly avoid star imports in all cases_**  

A number of IDE's by default collapse imports into the star format. For instance, a collection of imports such as

```java
import java.util.List;
import java.util.Set;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Optional;
import java.util.Arrays;
```

would be condensed down to

```java
import java.util.*;
```

Whilst this is shorter code, it has a few drawbacks. For these reasons we strictly avoid star imports.

Of note is that IntelliJ is known to not behave nicely when it comes to disabling this. [There are details on how to disable it in this stack overflow QA](https://stackoverflow.com/questions/3587071/disable-intellij-starred-package-imports).

## Others

- Use `// TODO` comments to note where something needs to be done
- Don't use Hungarian notation to denote types
- Use interfaces over concrete classes for variables (e.g. `Map` instead of `HashMap`).

## Checkstyle

We use the [Checkstyle](http://checkstyle.sourceforge.net/) project to help keep the codebase consistent with some code conventions. 
The Terasology engine and all modules use the same configuration as defined in [MovingBlocks/TeraConfig](https://github.com/MovingBlocks/TeraConfig).
You can find the local copy of the configuration file at [`config/metrics/checkstyle/checkstyle.xml`](https://github.com/MovingBlocks/TeraConfig/blob/master/checkstyle/checkstyle.xml).

When working an area please keep an eye out for existing warnings to fix in files you're impacting anyway.
Make sure to run Checkstyle on any new files you add since that's the best time to fix warnings.

### Gradle

You can easily run Checkstyle via the Gradle wrapper `gradlew`. 
See [Checkstyle Gradle Plugin](https://docs.gradle.org/current/userguide/checkstyle_plugin.html) for more information.

- `gradlew check` to run all checks and tests (incl. Checkstyle)
- `gradlew engine:checkstyleMain` to run Checkstyle for just the engine's main source directory

### IntelliJ Integration

We recommend to use the [Checkstyle IDEA Plugin](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea).
IntelliJ IDEA allows to easily run Checkstyle checks for the whole project, specific folders, or just single files.

In case the IDE does not pick put the correct Checkstyle rules automatically you may have to configure it manually.
The Checkstyle configuration file is located at `config/metrics/checkstyle/checkstyle.xml`.

### Jenkins Integration

Checkstyle statistics are automatically calculated per build and turned into nifty metrics per build.
For instance, have a look at the [Checkstyle Report for the engine](http://jenkins.terasology.io/teraorg/job/Terasology/job/engine/job/develop/checkstyle).

## PMD

We use the [PMD](https://pmd.github.io/) project to help keep the codebase free from common programming flaws like unused variables, empty catch blocks, unnecessary object creation, etc.
The Terasology engine's configuration is defined in [MovingBlocks/TeraConfig](https://github.com/MovingBlocks/TeraConfig).
You can find the local copy of the configuration file at [`config/metrics/pmd/pmd.xml`](https://github.com/MovingBlocks/TeraConfig/blob/master/pmd/pmd.xml).

When working an area please keep an eye out for existing warnings to fix in files you're impacting anyway.
Make sure to run PMD on any new files you add since that's the best time to fix warnings.

### Check Execution

#### Gradle

You can easily run PMD via the Gradle wrapper `gradlew`. 
See [PMD Gradle Plugin](https://docs.gradle.org/current/userguide/pmd_plugin.html) for more information.

- `gradlew check` to run all checks and tests (incl. PMD)
- `gradlew engine:pmdMain` to run PMD for just the engine's main source directory

#### IntelliJ Integration

We recommend to use the [PMD IDEA Plugin](https://plugins.jetbrains.com/plugin/15412-pmd-idea).
IntelliJ IDEA allows to easily run PMD checks for the whole project, specific folders, or just single files.

In case the IDE does not pick put the correct PMD rules automatically you may have to configure it manually.
The PMD configuration file is located at `config/metrics/checkstyle/pmd.xml`.

### Guard Log Statement Warnings

The [PMD GuardLogStatement rule](https://pmd.github.io/pmd/pmd_rules_java_bestpractices.html#guardlogstatement) identifies logs that are not guarded resulting in the String creation and manipulation as well as any associated calculations be performed even if the respective log level or the entire logging support are disabled.

See the following example for a non-guarded log statement:
```java
logger.debug("log something" + method() + " and " + param.toString() + "concat strings");
```

#### Parameter Substitution

In general, parameter substitution is a sufficient log guard.
It also removes the need for explicit `toString()` calls.
Parameter substitution can be applied to the above-mentioned non-guarded log statement as follows:
```java
logger.debug("log something {} and {}", method(), param);
```

Unfortunately, PMD is currently subject to a [bug](https://github.com/pmd/pmd/issues/4703) that can lead to warnings still being reported despite the parametrized logging approach.
If you utilized parameter substitution but are still facing the `GuardLogStatement` PMD warning, alternative approaches need to be found until the bug is resolved.
These include local variable creation, suppression, and the fluent log API.
For a decision about the appropriate approach, the context, the log level, and the individual performance impact of any relevant case should be taken into consideration.

#### Local Variable Creation

If the calculation performed was already performed before or will be performed after the log line (including in further log lines), introducing a local variable and referencing it in the log is a suitable approach.
In some cases a respective local variable may already exist and simply be referenced in your log statement.
```java
String localVar = method();
logger.debug("log something {} and {}", localVar, param);
[...]
logger.debug("log something else {}", localVar);
```

#### Suppression

For Terasology complete disablement of the logging support is very unlikely, as is the disablement of the `error` log level.
While `warn` and `info` log levels may be disabled in rare cases, they're usually not.
Accordingly, any reported cases on `error`, `warn`, and `debug` log levels should be considered for suppression.

Especially, if the performance impact is neglectable, e.g. for variable references (no method call) or simple getter or setter methods, or if the log frequency is very limited, e.g. only during initialization or cleanup, the PMD warning can be suppressed using an inline comment as follows:
```java
logger.warn("log something {} and {}", method(), param); //NOPMD
```

If the logs are part of a logging-specific method, that is intentionally called (only) for logging specific aspects like machine specs or config values, the warning can be suppressed for the entire method like so:
```java
@SuppressWarnings("PMD.GuardLogStatement")
public logSpecs() {
logger.info("log something {} and {}", method(), param)
}
```

Suppressing warnings allows for easier identification and reversion once the PMD bug is resolved.

#### Fluent Logging API

If parameter substitution is insufficient, local variable creation is not suitable, and suppression is not desired, the fluent logging API can be utilized as follows:
```java
logger.atDebug().log("log something {} and {}", method(), param);
```

Please do not use the more verbose variant(s) utilizing the `setMessage()`, `addArgument()`, `addKeyValue()` and similar methods of the fluent logging API's [LoggingEventBuilder](https://www.slf4j.org/apidocs/org/slf4j/spi/LoggingEventBuilder.html) to keep complexity low and your log statements readable.

## Testing

Terasology's test suite can be found in the [engine-tests/src/test/java/org/terasology](https://github.com/MovingBlocks/Terasology/tree/develop/engine-tests/src/test/java/org/terasology) folder.

### Why Test?

Most developers spend the majority of their time not *writing* code, but debugging and maintaining it. Unit tests are one of the best ways to minimize unnecessary time spent on both. Testing also helps document your code. Finally, we use a passing unit test suite as one of the criteria for accepting pull requests.

### What software is used to test?

Terasology uses [JUnit 5](http://www.junit.org/) for its automated test suite. It also uses [Mockito](http://site.mockito.org) for mocking/stubbing in special situations for which a dependency is too expensive or unreliable to bring into a test suite - for example, network activity or OpenGL.

An IDE is highly encouraged for running tests, as most support JUnit. In Eclipse, for example, you can quickly run a single test by right-clicking on the test method declaration and selecting *Run As → JUnit Test*. You can give this command a shortcut key of your choice to make it even faster.

### How often should I use Mocks or Stubs?

Rarely. If you find yourself using Mockito a lot, you may want to consider refactoring your code to be more modular.

### What should I test?

Ideally, every line of code that you want to merge should be backed by a unit test. However, there are exceptions, such as  straightforward getters/setters. The general rule is that the more likely your code is to fail or change, the more important it is to have test coverage.

### When should I run the full test suite?

On pulling any changes and before making any pull requests. To save yourself any unpleasant surprises, you should also run the complete test suite after completing any unit of work on the code.

### How should I test?

Ideally, you should [write your tests first](http://en.wikipedia.org/wiki/Test-driven_development). Begin by thinking about what success looks like for the problem you're trying to solve. Then attempt to write a test in code that captures the solution. Then write the code to make the test pass.

## JavaDoc

Our Javadoc guidelines are loosely based on Stephen Colebourne's blog article on [Javadoc coding standards](http://blog.joda.org/2012/11/javadoc-coding-standards.html) and [Oracle's guide on writing Javadoc](http://www.oracle.com/technetwork/java/javase/documentation/index-137868.html). Note that these guidelines merely specify the layout and formatting of the documentation but not its content. 

A Javadoc comment is written in HTML and can therefore use common HTML tags. A doc comment is made up of two parts, the description followed by block tags. Keep in mind that often as not Javadoc is read in its source form, so it should be easy to read and understand without the generated web frontend.

### First Sentence

Write the first sentence as a short summary of the method, as Javadoc automatically places it in the method summary table (and index). This first sentence, typically ended by a dot, is used in the next-level higher Javadoc. As such, it has the responsibility of summing up the method or class to readers scanning the class or package. To achieve this, the first sentence should be clear and punchy, and generally short.

While not required, it is recommended that the first sentence is a paragraph to itself. This helps retain the punchiness for readers of the source code.

It is recommended to use the third person form at the start. For example, "Gets the foo", "Sets the "bar" or "Consumes the baz". Avoid the second person form, such as "Get the foo". 

### HTML Tags

As a rule-of-thumb use the [standard format for doc comments](http://www.oracle.com/technetwork/java/javase/documentation/index-137868.html#format) with plain HTML tags (no XHTML). For longer doc comments use `<p>` to separate different paragraphs. Note that you are not allowed to use self-closing tags, e.g., `</p>`. Place a single `<p>` tag on the blank line between paragraphs:

```java
  /**
   * First paragraph.
   * <p>
   * Second paragraph.
   * May be on multiple lines.
   * <p>
   * Third paragraph.
   */
  public ...
```

Lists are useful in Javadoc when explaining a set of options, choices or issues. These standards place a single `<li>` tag at the start of the line and no closing tag. In order to get correct paragraph formatting, extra paragraph tags are required: 

```java
  /**
   * First paragraph.
   * <p><ul>
   * <li>the first item
   * <li>the second item
   * <li>the third item
   * <ul><p>
   * Second paragraph.
   */
  public ...
```

### @code and @link
Many Javadoc descriptions reference other methods and classes. This can be achieved most effectively using the @link and @code features.

The @link feature creates a visible hyperlink in generated Javadoc to the target. The @link target is one of the following forms:

```java
  /**
   * First paragraph.
   * <p>
   * Link to a class named 'Foo': {@link Foo}.
   * Link to a method 'bar' on a class named 'Foo': {@link Foo#bar}.
   * Link to a method 'baz' on this class: {@link #baz}.
   * Link specifying text of the hyperlink after a space: {@link Foo the Foo class}.
   * Link to a method handling method overload {@link Foo#bar(String,int)}.
   */
  public ...
```

The @code feature provides a section of fixed-width font, ideal for references to methods and class names. While @link references are checked by the Javadoc compiler, @code references are not.

Only use @link on the first reference to a specific class or method. Use @code for subsequent references. This avoids excessive hyperlinks cluttering up the Javadoc.

Do not use @link in the punch line. The first sentence is used in the higher level Javadoc. Adding a hyperlink in that first sentence makes the higher level documentation more confusing. Always use @code in the first sentence if necessary. @link can be used from the second sentence/paragraph onwards.

Do not use @code for null, true or false. The concepts of null, true and false are very common in Javadoc. Adding @code for every occurrence is a burden to both the reader and writer of the Javadoc and adds no real value. 

### @param, @return, and @throws

Almost all methods take in a parameter, return a result or both. The @param and @return features specify those inputs and outputs. The @throws feature specifies the thrown exceptions. The @param entries should be specified in the same order as the parameters. The @return should be after the @param entries, followed by @throws.

Use @param for generics. If a class or method has generic type parameters, then these should be documented. The correct approach is an @param tag with the parameter name of &lt;T&gt; where T is the type parameter name.

Use one blank line before @param. There should be one blank line between the Javadoc text and the first @param or @return. This aids readability in source code.

The @param and @return should be treated as phrases rather than complete sentences. They should start with a lower case letter, typically using the word "the". They should not end with a dot. This aids readability in source code and when generated.

Treat @throws as an if clause. The @throws feature should normally be followed by "if" and the rest of the phrase describing the condition. For example, "@throws if the file could not be found". This aids readability in source code and when generated.

**To discuss:** use a single space after @param or indent to common column?

### @author

By convention we avoid the `@author` tag. It will generally go out of date, and can promote code ownership by an individual. The source control system is in a much better position to record authors.

### Document All Public and Protected 

All public and protected methods should be fully defined with Javadoc. Especially complex methods, procedures with side-effects, and non-trivial operations should be documented well. Package and private methods do not have to be, but may benefit from it.

### null-tolerance, Pre- and Postconditions

Whether a method accepts null on input, or can return null is critical information for building large systems. All non-primitive methods should define their null-tolerance in the @param or @return. Some standard forms expressing this should be used wherever possible:

* "not null" means that null is not accepted and passing in null will probably throw an exception , typically NullPointerException
* "may be null" means that null may be passed in. In general the behaviour of the passed in null should be defined
* "null treated as xxx" means that a null input is equivalent to the specified value
* "null returns xxx" means that a null input always returns the specified value

When defined in this way, there should not be an @throws for NullPointerException.

```java
  /**
   * Javadoc text.
   * 
   * @param foo  the foo parameter, not null
   * @param bar  the bar parameter, null returns null
   * @return the baz content, null if not processed
   */
  public String process(String foo, String bar) {...}
```

While it may be tempting to define null-handling behavior in a single central location, such as the class or package Javadoc, this is far less useful for developers. The Javadoc at the method level appears in IDEs during normal coding, whereas class or package level Javadoc requires a separate "search and learn" step.

Other simple constraints may be added as well if applicable, for example "not empty, not null". Primitive values might specify their bounds, for example "from 1 to 5", or "not negative". 

### Generating it

To generate Javadoc for the project simply execute `gradle javadoc` at the root of the project. The actual `build.gradle` doesn't need to have any related scripting in it, the Javadoc Gradle plugin does it all!
