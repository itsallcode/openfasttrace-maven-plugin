# openfasttrace-maven

Maven Plugin for [OpenFastTrace](https://github.com/itsallcode/openfasttrace)

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
    <version>1.6.2</version>
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
    </configuration>
</plugin>
```

Then you can run tracing by calling the goal directly: `mvn openfasttrace:trace`.

The plugin binds to the `verify` lifecycle, so you can also use `mvn verify`.

See [src/test/resources/empty-project](src/test/resources/simple-project) for an example project.

### Configuration

You can configure the plugin using the `<configuration>` element.

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
apt-get install openjdk-11-jdk maven
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
    -Dsonar.host.url=https://sonarcloud.io \
    -Dsonar.organization=itsallcode \
    -Dsonar.login=$sonar_token
```

See analysis results at https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin

### Check for updated dependencies / plugins

```sh
mvn --update-snapshots versions:display-dependency-updates versions:display-plugin-updates
```

### Publishing to Maven Central

1. Add the following to your `~/.m2/settings.xml`:

    ```xml
    <settings>
        <servers>
            <server>
                <id>ossrh</id>
                <username>your-jira-id</username>
                <password>your-jira-pwd</password>
            </server>
        </servers>
        <profiles>
            <profile>
                <id>ossrh</id>
                <activation>
                    <activeByDefault>true</activeByDefault>
                </activation>
                <properties>
                    <gpg.executable>gpg</gpg.executable>
                    <gpg.keyname>ABCDEF1234567890</gpg.keyname>
                    <gpg.passphrase>the_pass_phrase</gpg.passphrase>
                </properties>
            </profile>
        </profiles>
    </settings>
    ```

1. Checkout the `main` branch.
1. Update version in `pom.xml`, `CHANGELOG.md` and `README.md`, commit and push.
1. Run command

    ```sh
    mvn -DskipSigningArtifacts=false clean deploy
    ```

1. Create a [release](https://github.com/itsallcode/openfasttrace-maven-plugin/releases) of the `main` branch on GitHub.
1. After some time the new version will appear at [Maven Central](https://repo1.maven.org/maven2/org/itsallcode/openfasttrace-maven-plugin/)
