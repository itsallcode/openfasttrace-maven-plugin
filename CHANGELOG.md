# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.3.0] - 2024-11-13

- [#70](https://github.com/itsallcode/openfasttrace-maven-plugin/issues/70) Add support for OFT's command line option `--wanted-tags`

## [2.2.0] - 2024-08-21

- [PR #66](https://github.com/itsallcode/openfasttrace-maven-plugin/issues/66) Add filter for artifact types.

## [2.1.0] - 2024-08-11

- [PR #65](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/65) Add integration test using an OFT plugin and upgrade to [OpenFastTrace 4.1.0](https://github.com/itsallcode/openfasttrace/releases/tag/4.1.0)

## [2.0.0] - 2024-06-09

- [PR #64](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/64) Upgrade to [OpenFastTrace 4.0.0](https://github.com/itsallcode/openfasttrace/releases/tag/4.0.0)
  - This adds support for reading RST files
  - **Breaking change**: starting with this version the plugin requires Java 17 or later
- [PR #63](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/63) Verify that the build produces reproducible artifacts

## [1.8.0] - 2024-02-28

- [Issue #60](https://github.com/itsallcode/openfasttrace-maven-plugin/issues/60) Import coverage from resource directories
- [Issue #61](https://github.com/itsallcode/openfasttrace-maven-plugin/issues/61) Suppress warning about missing input directories

## [1.7.0] - 2024-02-26

- [PR #57](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/57) Mark plugin as thread safe
  - This suppresses a warning when running the Maven build in parallel with `-T 1C`
  - This suppresses warnings about vulnerabilities in `provided` dependency `com.google.guava:guava:jar:25.1-android` via `org.apache.maven:maven-core`:
    - CVE-2023-2976 CWE-552: Files or Directories Accessible to External Parties (7.1)
    - CVE-2020-8908 CWE-379: Creation of Temporary File in Directory with Incorrect Permissions (3.3)
  - This also updates test dependencies and fixes the following vulnerabilities:
    - `commons-io:commons-io:jar:2.2`
      - CVE-2021-29425 CWE-22: Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal') (5.3)
    - `org.codehaus.plexus:plexus-archiver:jar:2.2`
      - CVE-2012-2098 CWE-310 (5.0)
      - CVE-2023-37460 CWE-22: Improper Limitation of a Pathname to a Restricted Directory ('Path Traversal') (9.8)
- [Issue #55](https://github.com/itsallcode/openfasttrace-maven-plugin/issues/55) Allow skipping the execution of this plugin by adding option `-Dopenfasttrace.skip=true`
- [Issue #56](https://github.com/itsallcode/openfasttrace-maven-plugin/issues/56) Upgrade to [OpenFastTrace 3.8.0](https://github.com/itsallcode/openfasttrace/releases/tag/3.8.0)
  - This adds support for the new option `detailsSectionDisplay`

## [1.6.2] - 2023-03-12

- [PR #49](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/49) Upgrade OpenFastTrace
  - This upgrades to [OpenFastTrace 3.7.0](https://github.com/itsallcode/openfasttrace/releases/tag/3.7.0)

## [1.6.1] - 2022-08-21

### Fixed

- [PR #48](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/48) Use correct OpenFastTrace version 3.6.0
  - This upgrades to [OpenFastTrace 3.6.0](https://github.com/itsallcode/openfasttrace/releases/tag/3.6.0)

## [1.6.0] - 2022-08-21

### Refactoring

- [PR #45](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/45) Upgrade dependencies
- [PR #46](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/46) Remove license header from source files
- [PR #47](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/47) Prepare release
  - This upgrades to [OpenFastTrace 3.6.0](https://github.com/itsallcode/openfasttrace/releases/tag/3.6.0)

## [1.5.0] - 2022-03-17

- [#40](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/40) Upgrade to [OpenFastTrace 3.5.0](https://github.com/itsallcode/openfasttrace/releases/tag/3.5.0)

## [1.4.0] - 2022-02-01

- [#39](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/39) Upgrade to [OpenFastTrace 3.4.0](https://github.com/itsallcode/openfasttrace/releases/tag/3.4.0)

## [1.3.0] - 2021-12-20

- [#35](https://github.com/itsallcode/openfasttrace-maven-plugin/issues/35) Support tracing projects with multiple modules.

## [1.2.1] - 2021-11-26

### Changed

- [#29](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/29) Upgrade dependencies, using OpenFastTrace [OpenFastTrace 3.2.1](https://github.com/itsallcode/openfasttrace/releases/tag/3.2.1) and test with Java 17.
- [#33](https://github.com/itsallcode/openfasttrace-maven-plugin/issues/33) Print tracing summary report

## [1.2.0] - 2021-05-30

### Changed

- [#20](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/20) Upgrade to Maven 3.8.1
- [#20](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/20) Upgrade to [OpenFastTrace 3.2.0](https://github.com/itsallcode/openfasttrace/releases/tag/3.2.0) including
  - [#271](https://github.com/itsallcode/openfasttrace/issues/271)
  - [#258](https://github.com/itsallcode/openfasttrace/pull/258)

## [1.1.0] - 2021-05-21

### Changed

- [#22](https://github.com/itsallcode/openfasttrace-maven-plugin/pull/22) Upgrade to [OpenFastTrace 3.1.0](https://github.com/itsallcode/openfasttrace/releases/tag/3.1.0), adding support for JVM languages Clojure, Kotlin and Scala.

## [1.0.0] - 2020-07-08

### Changed

- [#4](https://github.com/itsallcode/openfasttrace-maven-plugin/issues/4) Allow configuration of report format, using HTML by default

## [0.1.0] - 2020-05-23

### Changed

- Add configuration option `failBuild`

## [0.0.3] - 2020-05-12

### Changed

- Upgrade to OpenFastTrace 3.0.2
- Requires Java 11

## [0.0.2] - Initial release

- Initial release
