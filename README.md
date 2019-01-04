# openfasttrace-maven

Maven Plugin for [OpenFastTrace](https://github.com/itsallcode/openfasttrace)

## Usage

## Development

### Installation of Initial Build Dependencies on Linux

#### Ubuntu or Debian

If you want to build OFT:

    apt-get install openjdk-8-jdk maven

### Essential Build Steps

* `git clone https://github.com/itsallcode/openfasttrace-maven-plugin.git`
* Run `mvn test` to run unit tests.

### Using Eclipse

Import as a Maven project using *"File" &rarr; "Import..." &rarr; "Maven" &rarr; "Existing Maven Projects"*

### License File Header

* We use [license-maven-plugin](http://www.mojohaus.org/license-maven-plugin) to check in `verify` phase that all files have the correct license header. The build will fail if there are any files with missing/outdated headers.
* To update files with correct license headers and generate file `LICENSE.txt`, run command

```bash
mvn license:update-project-license license:update-file-header
```

## Run local sonar analysis

```bash
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package sonar:sonar \
    -Dsonar.host.url=https://sonarcloud.io \
    -Dsonar.organization=itsallcode \
    -Dsonar.login=[token]
```

See analysis results at https://sonarcloud.io/dashboard?id=org.itsallcode%3Aopenfasttrace-maven-plugin

## Publishing to JCenter and Maven Central

1. Add the following to your `~/.m2/settings.xml`:

    ```xml
    <servers>
        <server>
            <id>itsallcode-maven-repo</id>
            <username>[bintray-username]</username>
            <password>[bintray-api-key]</password>
        </server>
    </servers>
    ```

1. Checkout the `develop` branch.
1. Update version in `pom.xml` and `README.md`, commit and push.
1. Run command

    ```bash
    mvn deploy
    ```
1. Create a [release](https://github.com/itsallcode/openfasttrace/releases) of the `develop` branch on GitHub.
1. Sign in at [bintray.com](https://bintray.com)
1. Go to the [Bintray project page](https://bintray.com/itsallcode/itsallcode/openfasttrace-maven-plugin)
1. There should be a notice saying "You have 6 unpublished item(s) for this package". Click the "Publish" link. Binaries will be available for download at [JCenter](https://jcenter.bintray.com/org/itsallcode/openfasttrace-maven-plugin/)
1. Publish to Maven Central by clicking the "Sync" button at https://bintray.com/itsallcode/itsallcode/openfasttrace#central. After some time the new version will appear at https://repo1.maven.org/maven2/org/itsallcode/openfasttrace-maven-plugin/
