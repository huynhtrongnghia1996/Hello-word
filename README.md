# EFMS Automation Framework

Automation framework for EFMS UAT using Java 17, Maven, TestNG, Playwright Java, Rest Assured, PostgreSQL, Allure, Log4j2, Git and Jenkins.

## Default environment

- Environment: `UAT`
- UI URL: `https://uat-efms.logtechub.com/en/#/home`
- Supported browsers: `chrome`, `edge`

## Project structure

```text
src/main/java/com/logtechub/automation
├── api
├── config
├── database
├── logging
├── pages
├── playwright
└── reporting

src/test/java/com/logtechub/automation
├── base
├── listeners
└── tests/ui

src/test/resources
├── config/uat.properties
├── testng/testng-uat-ui.xml
├── allure.properties
└── log4j2.xml
```

## Local setup

Required tools:

- Java 17+
- Maven 3.9+
- Google Chrome and Microsoft Edge installed for Playwright channel mode

Install Playwright browser channels if needed:

```bash
mvn -B -DskipTests exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chrome msedge"
```

Run UAT UI suite on both Chrome and Edge:

```bash
mvn clean test -Denv=UAT -DsuiteXmlFile=src/test/resources/testng/testng-uat-ui.xml -Dheadless=true
```

Run only Chrome:

```bash
mvn clean test -Denv=UAT -DsuiteXmlFile=src/test/resources/testng/testng-uat-single-browser.xml -Dbrowser=chrome -Dheadless=true
```

Run only Edge:

```bash
mvn clean test -Denv=UAT -DsuiteXmlFile=src/test/resources/testng/testng-uat-single-browser.xml -Dbrowser=edge -Dheadless=true
```

Generate Allure report:

```bash
mvn allure:serve
```

## Configuration

UAT configuration is stored in `src/test/resources/config/uat.properties`.

Sensitive values can be overridden by system properties or environment variables. Examples:

```bash
mvn clean test -Ddb.url=jdbc:postgresql://host:5432/db -Ddb.username=user -Ddb.password=secret
```

or

```bash
export DB_URL=jdbc:postgresql://host:5432/db
export DB_USERNAME=user
export DB_PASSWORD=secret
```

## Jenkins

`Jenkinsfile` provides a parameterized pipeline for UAT execution. Configure Jenkins tools named `java-17` and `maven-3`, plus the Allure plugin.
