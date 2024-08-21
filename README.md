# openfasttrace-maven-plugin

Maven Plugin for [OpenFastTrace](https://github.com/itsallcode/openfasttrace) (OFT).

## Project Information

[![Build](https://github.com/itsallcode/openfasttrace-maven-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/itsallcode/openfasttrace-maven-plugin/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.itsallcode/openfasttrace-maven-plugin.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.itsallcode%22%20a%3A%22openfasttrace-maven-plugin%22)

Sonarcloud status:

[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=bugs)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)
[![Code smells](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=code_smells)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)
[![Duplicated Lines](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=ncloc)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=security_rating)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)
[![Technical Dept](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=sqale_index)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=org.itsallcode%3Aopenfasttrace-maven-plugin&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin)

* [Blog](https://blog.itsallcode.org/)
* [Changelog](CHANGELOG.md)
* [Contributing guide](CONTRIBUTING.md)
* [OpenFastTrace stories](https://github.com/itsallcode/openfasttrace/wiki/OFT-Stories)

## Usage

Add the openfasttrace-maven-plugin to your `pom.xml`:

```xml
<plugin>
    <groupId>org.itsallcode</groupId>
    <artifactId>openfasttrace-maven-plugin</artifactId>
    <version>2.1.0</version>
    <executions>
        <execution>
            <id>trace-requirements</id>
            <goals>
                <goal>trace</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <failBuild>true</failBuild>
        <reportOutputFormat>html</reportOutputFormat>
        <reportVerbosity>ALL</reportVerbosity>
        <reportShowOrigin>true</reportShowOrigin>
        <detailsSectionDisplay>COLLAPSE</detailsSectionDisplay>
        <artifactTypes>feat,req</artifactTypes>
    </configuration>
</plugin>
```

Then you can run tracing by calling the goal directly: `mvn openfasttrace:trace`.

The plugin binds to the `verify` lifecycle, so you can also use `mvn verify`.

See [src/test/resources/empty-project](src/test/resources/simple-project/) for an example project.

### OpenFastTrace Plugins

You can use OpenFastTrace plugins to import and export requirements in additional formats. Include plugins by adding them as a dependency to the `openfasttrace-maven-plugin`, see [project-with-plugins](src/test/resources/project-with-plugins) as an example.

```xml
<plugin>
    <groupId>org.itsallcode</groupId>
    <artifactId>openfasttrace-maven-plugin</artifactId>
    <version>2.1.0</version>
    <configuration>
        <failBuild>true</failBuild>
    </configuration>
    <dependencies>
        <dependency>
            <groupId>org.itsallcode</groupId>
            <artifactId>openfasttrace-asciidoc-plugin</artifactId>
            <version>0.2.0</version>
        </dependency>
    </dependencies>
</plugin>
```

### Configuration

You can configure the plugin using the `<configuration>` element.

#### Traced Directories

By default the OFT plugin imports requirements from the following directories:

* The `doc` sub-directory of the module that includes the plugin if it exists
* For each Maven module in the project if they exist:
  * Compile source roots (default: `src/main/java`)
  * Resources (default: `src/main/resources`)
  * Test compile source roots (default: `src/test/java`)
  * Test resources (default: `src/test/resources`)

##### Adding Custom Source Directories

You can add additional custom source directories using the [Build Helper Maven Plugin](https://www.mojohaus.org/build-helper-maven-plugin/).

Please note that the phases `generate-sources` and `generate-test-sources` have nothing to do with the phase in which OFT does its job, rather it defines in which phase the directory is added to the list of known source directories by the `build-helper-maven-plugin`.

The following snipped adds source directory `src/main/rust` and test source directory `src/test/rust`:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.6.0</version>
    <executions>
        <execution>
            <id>add-source</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>add-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>src/main/rust</source>
                </sources>
            </configuration>
        </execution>
        <execution>
            <id>add-test-source</id>
            <phase>generate-test-sources</phase>
            <goals>
                <goal>add-test-source</goal>
            </goals>
            <configuration>
                <sources>
                    <source>src/test/rust</source>
                </sources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

##### Adding Custom Resource Directories

You can add additional resource directories using the [Maven Resources Plugin](https://maven.apache.org/plugins/maven-resources-plugin/examples/resource-directory.html). The following snipped adds `src/custom-resources` as additional resource directory:

```xml
<build>
    <resources>
        <resource>
            <directory>src/custom-resources</directory>
        </resource>
    </resources>
</build>
```

#### Selecting the Imported ArtifactTypes

Sometimes you don't want to trace the whole requirement chain. Instead, you are interested in the consistency of a subset. For instance, if you need to deliver a system requirement specification to another team, your job is to assure that the document is consistent in itself.

For those cases you can add an include list to the configuration that explicitly lists all artifact types to be imported. Note that this also affects which required coverage is imported — which is exactly what you want in this situation.

The following example configuration limits import to artifact types `feat` and `req`.

```xml
<configuration>
    <artifactTypes>feat,req</artifactTypes>
</configuration>
```

#### Report

##### Report Format

The tracing report is in HTML format by default. You can configure plain text format with `<reportOutputFormat>plain</reportOutputFormat>`.

##### Report Location

The tracing report will be written to `target/tracing-report.html` by default. You can configure the location with `<outputDirectory>${project.build.directory}/reports/</outputDirectory>`.

##### HTML Report Details Section Display

The HTML report will have its details sections collapsed (i.e. hidden) by default. You can render the HTML with expanded details sections with `<detailsSectionDisplay>EXPAND</detailsSectionDisplay>`.

#### Fail Build

By default the build will fail when there are errors found during tracing. To continue with the build when tracing fails, use configuration `<failBuild>false</failBuild>`.

#### Skipping Execution

To skip execution of the plugin, add command line option `-Dopenfasttrace.skip=true` when running Maven.

## Development

### Installation of Initial Build Dependencies on Linux

#### Ubuntu or Debian

If you want to build OFT:

```sh
apt-get install openjdk-17-jdk maven
```

### Configure Maven Toolchain

This project uses Maven Toolchains to configure the correct JDK version (see the [documentation](https://maven.apache.org/guides/mini/guide-using-toolchains.html) for details). To configure the Toolchains plugin create file ` ~/.m2/toolchains.xml` with the following content. Adapt the paths to your JDKs.

```xml
<toolchains xmlns="http://maven.apache.org/TOOLCHAINS/1.1.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://maven.apache.org/TOOLCHAINS/1.1.0 http://maven.apache.org/xsd/toolchains-1.1.0.xsd">
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>17</version>
        </provides>
        <configuration>
            <jdkHome>/usr/lib/jvm/java-17-openjdk-amd64/</jdkHome>
        </configuration>
    </toolchain>
        <toolchain>
        <type>jdk</type>
        <provides>
            <version>21</version>
        </provides>
        <configuration>
            <jdkHome>/usr/lib/jvm/java-21-openjdk-amd64/</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
```

### Essential Build Steps

* `git clone https://github.com/itsallcode/openfasttrace-maven-plugin.git`
* Run `mvn test` to run unit tests.
* Run `mvn integration-test` to run integration tests.

### Using Eclipse

Import as a Maven project using *"File" &rarr; "Import..." &rarr; "Maven" &rarr; "Existing Maven Projects"*

### Run local sonar analysis

```sh
sonar_token="[token]"
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package sonar:sonar \
    -Dsonar.login=$sonar_token
```

See analysis results at https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin

### Check for updated dependencies / plugins

Display dependencies and plugins with newer versions:

```sh
mvn --update-snapshots versions:display-dependency-updates versions:display-plugin-updates
```

Automatically upgrade dependencies:

```sh
mvn --update-snapshots versions:use-latest-releases versions:update-properties
```

### Creating a Release on Maven Central and GitHub

#### Prepare the Release

1. Checkout the `main` branch.
2. Update version in `pom.xml`, `CHANGELOG.md` and `README.md`.
3. Commit and push changes.
4. Create a new pull request, have it reviewed and merged to `main`.

### Perform the Release

1. Start the release workflow
  * Run command `gh workflow run release.yml --repo itsallcode/openfasttrace-maven-plugin --ref main`
  * or go to [GitHub Actions](https://github.com/itsallcode/openfasttrace-maven-plugin/actions/workflows/release.yml) and start the `release.yml` workflow on branch `main`.
2. Update title and description of the newly created [GitHub release](https://github.com/itsallcode/openfasttrace-maven-plugin/releases).
3. After some time the release will be available at [Maven Central](https://repo1.maven.org/maven2/org/itsallcode/openfasttrace-maven-plugin/).
